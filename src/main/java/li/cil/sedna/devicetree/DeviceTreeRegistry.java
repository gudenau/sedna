package li.cil.sedna.devicetree;

import li.cil.sedna.api.device.Device;
import li.cil.sedna.api.devicetree.DeviceTree;
import li.cil.sedna.api.devicetree.DeviceTreeProvider;
import li.cil.sedna.api.memory.MemoryMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Consumer;

public final class DeviceTreeRegistry {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Map<Class<? extends Device>, DeviceTreeProvider> PROVIDERS = new HashMap<>();
    private static final Map<Class<? extends Device>, DeviceTreeProvider> PROVIDER_CACHE = new HashMap<>();

    public static void putProvider(final Class<? extends Device> type, final DeviceTreeProvider provider) {
        PROVIDERS.put(type, provider);
        PROVIDER_CACHE.clear();
    }

    private static void visitBaseTypes(@Nullable final Class<?> type, final Consumer<Class<?>> visitor) {
        if (type == null) {
            return;
        }

        visitor.accept(type);
        visitBaseTypes(type.getSuperclass(), visitor);

        final Class<?>[] interfaces = type.getInterfaces();
        for (final Class<?> iface : interfaces) {
            visitor.accept(iface);
            visitBaseTypes(iface, visitor);
        }
    }

    @Nullable
    public static DeviceTreeProvider getProvider(final Device device) {
        final Class<? extends Device> deviceClass = device.getClass();
        if (PROVIDER_CACHE.containsKey(deviceClass)) {
            return PROVIDER_CACHE.get(deviceClass);
        }

        final List<DeviceTreeProvider> relevant = new ArrayList<>();
        final Set<Class<?>> seen = new HashSet<>();
        visitBaseTypes(deviceClass, c -> {
            if (seen.add(c) && PROVIDERS.containsKey(c)) {
                relevant.add(PROVIDERS.get(c));
            }
        });

        if (relevant.size() == 0) {
            return null;
        }

        if (relevant.size() == 1) {
            return relevant.get(0);
        }

        // Flip things around so when iterating in visit() we go from least to most specific provider.
        Collections.reverse(relevant);

        return new DeviceTreeProvider() {
            @Override
            public Optional<String> getName(final Device device) {
                for (int i = relevant.size() - 1; i >= 0; i--) {
                    final Optional<String> name = relevant.get(i).getName(device);
                    if (name.isPresent()) {
                        return name;
                    }
                }

                return Optional.empty();
            }

            @Override
            public Optional<DeviceTree> createNode(final DeviceTree root, final MemoryMap memoryMap, final Device device, final String deviceName) {
                for (int i = relevant.size() - 1; i >= 0; i--) {
                    final Optional<DeviceTree> node = relevant.get(i).createNode(root, memoryMap, device, deviceName);
                    if (node.isPresent()) {
                        return node;
                    }
                }

                return Optional.empty();
            }

            @Override
            public void visit(final DeviceTree node, final MemoryMap memoryMap, final Device device) {
                for (final DeviceTreeProvider provider : relevant) {
                    provider.visit(node, memoryMap, device);
                }
            }
        };
    }

    @Nullable
    public static DeviceTree visit(final DeviceTree root, final MemoryMap memoryMap, final Device device) {
        final DeviceTreeProvider provider = getProvider(device);
        if (provider == null) {
            LOGGER.warn("No provider for device [{}].", device);
            return null;
        }

        final Optional<String> name = provider.getName(device);
        if (name.isEmpty()) {
            LOGGER.warn("Failed obtaining name for device [{}].", device);
            return null;
        }

        final Optional<DeviceTree> node = provider.createNode(root, memoryMap, device, name.get());
        if (node.isEmpty()) {
            LOGGER.warn("Failed obtaining node for device [{}].", device);
            return null;
        }

        provider.visit(node.get(), memoryMap, device);

        return node.get();
    }

    public static DeviceTree create(final MemoryMap mmu) {
        return new DeviceTreeImpl(mmu);
    }
}
