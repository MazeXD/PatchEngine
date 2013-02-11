package mcpc.patchengine.asm.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

public class NavigationUtil {
    public static LabelNode getPreviousLabel(InsnList instructions, int index) {
        if (index < 0 || index >= instructions.size()) {
            return null;
        }

        return getPreviousLabel(instructions.get(index));
    }

    public static LabelNode getPreviousLabel(AbstractInsnNode node) {
        AbstractInsnNode instruction = node.getPrevious();

        while (!(instruction instanceof LabelNode)) {
            if (instruction == null) {
                return null;
            }

            instruction = instruction.getPrevious();
        }

        return (LabelNode) instruction;
    }

    public static LabelNode getNextLabel(InsnList instructions, int index) {
        if (index < 0 || index >= instructions.size()) {
            return null;
        }

        return getNextLabel(instructions.get(index));
    }

    public static LabelNode getNextLabel(AbstractInsnNode node) {
        AbstractInsnNode instruction = node.getNext();

        while (!(instruction instanceof LabelNode)) {
            if (instruction == null) {
                return null;
            }

            instruction = instruction.getNext();
        }

        return (LabelNode) instruction;
    }
}
