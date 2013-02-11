package mcpc.patchengine.asm.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

public class SeekUtil {
    public static int searchInsn(InsnList instructions, AbstractInsnNode node) {
        return searchInsn(instructions, node, 0, instructions.size());
    }

    public static int searchInsn(InsnList instructions, AbstractInsnNode node, int startIndex) {
        return searchInsn(instructions, node, startIndex, instructions.size());
    }

    public static int searchInsn(InsnList instructions, AbstractInsnNode node, int startIndex, int endIndex) {
        if (endIndex > instructions.size()) {
            endIndex = instructions.size();
        }

        if (startIndex < 0) {
            startIndex = 0;
        } else if (startIndex > endIndex) {
            startIndex = endIndex;
        }

        for (int i = startIndex; i < endIndex; i++) {
            if (ComparisionUtil.isEquals(instructions.get(i), node)) {
                return i;
            }
        }

        return -1;
    }

    public static int searchInsnPattern(InsnList instructions, InsnList pattern) {
        return searchInsnPattern(instructions, pattern, 0, instructions.size());
    }

    public static int searchInsnPattern(InsnList instructions, InsnList pattern, int startIndex) {
        return searchInsnPattern(instructions, pattern, startIndex, instructions.size());
    }

    public static int searchInsnPattern(InsnList instructions, InsnList pattern, int startIndex, int endIndex) {
        if (endIndex > instructions.size()) {
            endIndex = instructions.size();
        }

        if (startIndex < 0) {
            startIndex = 0;
        } else if (startIndex > endIndex) {
            startIndex = endIndex;
        }

        for (int i = startIndex; i < endIndex - pattern.size(); i++) {
            for (int j = 0; j < pattern.size(); j++) {
                if (!ComparisionUtil.isEquals(instructions.get(i + j), pattern.get(j))) {
                    break;
                }

                if (j == pattern.size() - 1) {
                    return i;
                }
            }
        }

        return -1;
    }
}
