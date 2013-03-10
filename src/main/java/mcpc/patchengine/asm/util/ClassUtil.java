package mcpc.patchengine.asm.util;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassUtil {
    public static MethodNode getMethod(ClassNode node, String methodName) {
        return getMethod(node, methodName, "()V", "");
    }

    public static MethodNode getMethod(ClassNode node, String methodName, String desc) {
        return getMethod(node, methodName, desc, "");
    }

    public static MethodNode getMethod(ClassNode node, String methodName, String desc, String signature) {
        for (Object entry : node.methods) {
            MethodNode method = (MethodNode) entry;

            if (method == null) {
                continue;
            }

            if (method.name.equals(methodName) && method.desc.equals(desc) && (method.signature == null || method.signature.equals(signature))) {
                return method;
            }
        }

        return null;
    }

    public static FieldNode getField(ClassNode node, String fieldName, String desc) {
        return getField(node, fieldName, desc, "");
    }

    public static FieldNode getField(ClassNode node, String fieldName, String desc, String signature) {
        for (Object entry : node.fields) {
            FieldNode field = (FieldNode) entry;

            if (field.name.equals(fieldName) && (desc.equals("*") || field.desc.equals(desc)) && (signature.equals("*") || field.signature == null || field.signature.equals(signature))) {
                return field;
            }
        }

        return null;
    }

    public static AnnotationNode getAnnotation(ClassNode node, String desc) {
        for (Object entry : node.visibleAnnotations) {
            AnnotationNode annotation = (AnnotationNode) entry;

            if (annotation.desc.equals(desc)) {
                return annotation;
            }
        }

        for (Object entry : node.invisibleAnnotations) {
            AnnotationNode annotation = (AnnotationNode) entry;

            if (annotation.desc.equals(desc)) {
                return annotation;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static boolean addField(ClassNode node, int access, String fieldName, Integer defaultValue) {
        if (getField(node, fieldName, "*") != null) {
            return false;
        }

        node.fields.add(new FieldNode(access, fieldName, Type.INT_TYPE.getDescriptor(), null, defaultValue));

        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean addField(ClassNode node, int access, String fieldName, Long defaultValue) {
        if (getField(node, fieldName, "*") != null) {
            return false;
        }

        node.fields.add(new FieldNode(access, fieldName, Type.LONG_TYPE.getDescriptor(), null, defaultValue));

        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean addField(ClassNode node, int access, String fieldName, Float defaultValue) {
        if (getField(node, fieldName, "*") != null) {
            return false;
        }

        node.fields.add(new FieldNode(access, fieldName, Type.FLOAT_TYPE.getDescriptor(), null, defaultValue));

        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean addField(ClassNode node, int access, String fieldName, Double defaultValue) {
        if (getField(node, fieldName, "*") != null) {
            return false;
        }

        node.fields.add(new FieldNode(access, fieldName, Type.DOUBLE_TYPE.getDescriptor(), null, defaultValue));

        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean addField(ClassNode node, int access, String fieldName, String defaultValue) {
        if (getField(node, fieldName, "*") != null) {
            return false;
        }

        node.fields.add(new FieldNode(access, fieldName, "Ljava/lang/String;", null, defaultValue));

        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean addField(ClassNode node, int access, String fieldName, Boolean defaultValue) {
        if (getField(node, fieldName, "*") != null) {
            return false;
        }

        node.fields.add(new FieldNode(access, fieldName, Type.BOOLEAN_TYPE.getDescriptor(), null, null));

        if (defaultValue != null) {
            boolean value = defaultValue;

            MethodNode method = getMethod(node, "<clinit>");

            if (method == null) {
                return false;
            }

            AbstractInsnNode temp = method.instructions.getLast();

            InsnList instructions = new InsnList();

            instructions.add(new LabelNode());
            instructions.add(new InsnNode(value ? ICONST_1 : ICONST_0));
            instructions.add(new FieldInsnNode(PUTSTATIC, node.name.replace('.', '/'), fieldName, "Z"));

            if (ComparisionUtil.isEquals(temp, new InsnNode(RETURN))) {
                method.instructions.insertBefore(temp, instructions);
            } else {
                method.instructions.add(instructions);
            }
        }

        return true;
    }
}
