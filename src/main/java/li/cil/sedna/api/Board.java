package li.cil.sedna.api;

import li.cil.sedna.api.device.InterruptController;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.api.device.Resettable;
import li.cil.sedna.api.device.Steppable;
import li.cil.sedna.api.memory.MemoryMap;
import li.cil.sedna.api.memory.MemoryRangeAllocationStrategy;

import org.jetbrains.annotations.Nullable;
import java.util.OptionalLong;

public interface Board extends Steppable, Resettable {
    MemoryMap getMemoryMap();

    InterruptController getInterruptController();

    MemoryRangeAllocationStrategy getAllocationStrategy();

    boolean addDevice(long address, MemoryMappedDevice device);

    OptionalLong addDevice(MemoryMappedDevice device);

    void removeDevice(MemoryMappedDevice device);

    int getInterruptCount();

    long getDefaultProgramStart();

    void setBootArguments(String value);

    void setStandardOutputDevice(@Nullable MemoryMappedDevice device);
}
