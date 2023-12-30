package li.cil.sedna.instruction.decoder.tree;

import li.cil.sedna.instruction.InstructionDeclaration;
import li.cil.sedna.instruction.decoder.DecoderTreeNodeArguments;
import li.cil.sedna.instruction.decoder.DecoderTreeVisitor;

import org.jetbrains.annotations.Nullable;
import java.util.stream.Stream;

public abstract class AbstractDecoderTreeNode {
    AbstractDecoderTreeNode() {
    }

    public abstract int getMaxDepth();

    public abstract int getMask();

    public abstract int getPattern();

    public abstract DecoderTreeNodeArguments getArguments();

    public abstract Stream<InstructionDeclaration> getInstructions();

    @Nullable
    public abstract InstructionDeclaration query(final int instruction);

    public abstract void accept(final DecoderTreeVisitor visitor);
}
