package mcpc.patchengine.asm.util;

import static org.objectweb.asm.Opcodes.IFEQ;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodUtil {
    public static LocalVariableNode getLocalVariable(MethodNode node, String name, String desc) {
        return getLocalVariable(node, name, desc, "");
    }

    public static LocalVariableNode getLocalVariable(MethodNode node, String name, String desc, String signature) {
        for (Object entry : node.localVariables) {
            LocalVariableNode variable = (LocalVariableNode) entry;

            if (variable.name.equals(name) && (variable.desc.equals("*") || variable.desc.equals(desc)) && (variable.signature == null || variable.signature.equals(signature))) {
                return variable;
            }
        }

        return null;
    }
    
    public static LabelNode getLabelWithInsn(MethodNode node, AbstractInsnNode insn)
    {
        LabelNode label = getFirstLabel(node);
        
        while(label != null && !SeekUtil.labelHasInsn(label, insn))
        {
            label = NavigationUtil.getNextLabel(label);
        }
        
        return label;
    }

    public static LabelNode getLabelWithInsnList(MethodNode node, AbstractInsnNode... instructions)
    {
        InsnList insnList = new InsnList();
        
        for(AbstractInsnNode insn : instructions)
        {
            insnList.add(insn);
        }
        
        return getLabelWithInsnList(node, insnList);
    }
    
    public static LabelNode getLabelWithInsnList(MethodNode node, InsnList instructions)
    {
        LabelNode label = getFirstLabel(node);
        
        while(label != null && !SeekUtil.labelHasInsnList(label, instructions))
        {
            label = NavigationUtil.getNextLabel(label);
        }
        
        return label;
    }
    
    public static LabelNode getFirstLabel(MethodNode node)
    {
        AbstractInsnNode first = node.instructions.getFirst();
        
        while(first != null && !(first instanceof LabelNode))
        {
            first = first.getNext();
        }
        
        return (LabelNode)first;
    }
    
    public static LabelNode getLastLabel(MethodNode node)
    {
        AbstractInsnNode last = node.instructions.getLast();
        
        while(last != null && !(last instanceof LabelNode))
        {
            last = last.getPrevious();
        }
        
        return (LabelNode)last;
    }
    
    public static void insertConditionTrue(MethodNode node, LabelNode start, LabelNode end, AbstractInsnNode condition)
    {
        LabelNode label = new LabelNode();
        
        InsnList instructions = node.instructions;

        instructions.insert(start, new JumpInsnNode(IFEQ, label));
        instructions.insert(start, condition);       
        
        instructions.insertBefore(end, label);
    }
}
