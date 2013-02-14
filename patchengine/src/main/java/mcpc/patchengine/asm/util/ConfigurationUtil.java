package mcpc.patchengine.asm.util;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ConfigurationUtil {
    public static boolean addStringProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        return addStringProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addStringProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {
        return addProperty(instructions, configNode, category, key, comment, fieldName, "java/lang/String", null);
    }
    
    public static boolean addBooleanProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        return addBooleanProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addBooleanProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {

        AbstractInsnNode converterNode =new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z");

        return addProperty(instructions, configNode, category, key, comment, fieldName, "Z", converterNode);
    }

    public static boolean addIntegerProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        return addIntegerProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addIntegerProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {

        AbstractInsnNode converterNode =new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I");

        return addProperty(instructions, configNode, category, key, comment, fieldName, "I", converterNode);
    }

    public static boolean addLongProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        return addLongProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addLongProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {

        AbstractInsnNode converterNode =new MethodInsnNode(INVOKESTATIC, "java/lang/Long", "parseLong", "(Ljava/lang/String;)J");

        return addProperty(instructions, configNode, category, key, comment, fieldName, "J", converterNode);
    }

    public static boolean addFloatProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        return addFloatProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addFloatProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {

        AbstractInsnNode converterNode =new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F");

        return addProperty(instructions, configNode, category, key, comment, fieldName, "F", converterNode);
    }
    
    public static boolean addDoubleProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        return addDoubleProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addDoubleProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {

        AbstractInsnNode converterNode =new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "parseDouble", "(Ljava/lang/String;)D");

        return addProperty(instructions, configNode, category, key, comment, fieldName, "D", converterNode);
    }
    
    private static boolean addProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName, String type, AbstractInsnNode converterNode)
    {
        int index = fieldName.indexOf('.');
        
        if (index == -1) {
            return false;
        }

        boolean hasComment = comment != null;
        
        String className = fieldName.substring(0, index);
        String field = fieldName.substring(index + 1, fieldName.length());

        instructions.add(new LabelNode());
        instructions.add(configNode.clone(null));
        instructions.add(new LdcInsnNode(category));
        instructions.add(new LdcInsnNode(key));
        instructions.add(new FieldInsnNode(GETSTATIC, className, field, type));

        if (hasComment) {
            instructions.add(new LdcInsnNode(comment));
        }

        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/common/Configuration", "get", "(Ljava/lang/String;Ljava/lang/String;" + type + (hasComment ? "Ljava/lang/String;" : "") + ")Lnet/minecraftforge/common/Property;"));
        instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/common/Property", "value", "Ljava/lang/String;"));
        
        if(converterNode != null)
        {
            instructions.add(converterNode.clone(null));
        }
        
        instructions.add(new FieldInsnNode(PUTSTATIC, className, field, type));

        return true;
    }
}
