package mcpc.patchengine.asm.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;

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
    
    public static boolean labelHasInsnList(LabelNode node, AbstractInsnNode... instructions)
    {
        InsnList insnList = new InsnList();
        
        for(AbstractInsnNode insn : instructions)
        {
            insnList.add(insn);
        }
        
        return labelHasInsnList(node, insnList);
    }
    
    public static boolean labelHasInsnList(LabelNode node, InsnList instructions)
    {
        for(int i = 0; i < instructions.size(); i++)
        {
            if(!labelHasInsn(node, instructions.get(i)))
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean labelHasInsn(LabelNode node, AbstractInsnNode insn)
    {
        AbstractInsnNode next = node.getNext();
        
        while(next != null && !(next instanceof LabelNode))
        {
            if(ComparisionUtil.isEquals(next, insn))
            {
                return true;
            }

            next = next.getNext();
        }
        
        return false;
    }
}
