package mcpc.patchengine.asm.util;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ComparisionUtil {
    public static boolean isEquals(AbstractInsnNode source, AbstractInsnNode node) {
        if (source instanceof MethodInsnNode) {
            return isEqualsMethodInsn((MethodInsnNode) source, node);
        }

        if (source instanceof FieldInsnNode) {
            return isEqualsFieldInsn((FieldInsnNode) source, node);
        }

        if (source instanceof VarInsnNode) {
            return isEqualsVarInsn((VarInsnNode) source, node);
        }

        if (source instanceof LdcInsnNode) {
            return isEqualsLdcInsn((LdcInsnNode) source, node);
        }

        if (source instanceof TypeInsnNode) {
            return isEqualsTypeInsn((TypeInsnNode) source, node);
        }

        return source.getType() == node.getType() && source.getOpcode() == node.getOpcode();
    }

    public static boolean isEquals(InsnList source, InsnList instructions) {
        if (source.size() != instructions.size()) {
            return false;
        }

        for (int i = 0; i < source.size(); i++) {
            if (!isEquals(source.get(i), instructions.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static boolean isEqualsMethodInsn(MethodInsnNode source, AbstractInsnNode node) {
        if (!(node instanceof MethodInsnNode) || source.getOpcode() != node.getOpcode()) {
            return false;
        }

        MethodInsnNode temp = (MethodInsnNode) node;

        if (source.desc.equals(temp.desc) && source.name.equals(temp.name) && source.owner.equals(temp.owner)) {
            return true;
        }

        return false;
    }

    public static boolean isEqualsFieldInsn(FieldInsnNode source, AbstractInsnNode node) {
        if (!(node instanceof FieldInsnNode) || source.getOpcode() != node.getOpcode()) {
            return false;
        }

        FieldInsnNode temp = (FieldInsnNode) node;

        if (source.desc.equals(temp.desc) && source.name.equals(temp.name) && source.owner.equals(temp.owner)) {
            return true;
        }

        return false;
    }

    public static boolean isEqualsVarInsn(VarInsnNode source, AbstractInsnNode node) {
        if (!(node instanceof VarInsnNode) || source.getOpcode() != node.getOpcode()) {
            return false;
        }

        VarInsnNode temp = (VarInsnNode) node;

        if (source.var == temp.var) {
            return true;
        }

        return false;
    }

    public static boolean isEqualsLdcInsn(LdcInsnNode source, AbstractInsnNode node) {
        if (!(node instanceof LdcInsnNode)) {
            return false;
        }

        LdcInsnNode temp = (LdcInsnNode) node;

        if (source.cst.equals(temp.cst)) {
            return true;
        }

        return false;
    }

    public static boolean isEqualsTypeInsn(TypeInsnNode source, AbstractInsnNode node) {
        if (!(node instanceof TypeInsnNode) || source.getOpcode() != node.getOpcode()) {
            return false;
        }

        TypeInsnNode temp = (TypeInsnNode) node;

        if (source.desc.equals(temp.desc)) {
            return true;
        }

        return false;
    }
}
