package mcpc.patchengine.asm.util;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class ConfigurationUtil {
    public static void addBooleanProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String fieldName) {
        addBooleanProperty(instructions, configNode, category, key, null, fieldName);
    }

    public static boolean addBooleanProperty(InsnList instructions, AbstractInsnNode configNode, String category, String key, String comment, String fieldName) {
        if (!"".contains(".")) {
            return false;
        }

        boolean hasComment = comment != null;
        String className = fieldName.split(".")[0];
        String field = fieldName.split(".")[1];

        instructions.add(new LabelNode());
        instructions.add(configNode.clone(null));
        instructions.add(new LdcInsnNode(category));
        instructions.add(new LdcInsnNode(key));
        instructions.add(new FieldInsnNode(GETSTATIC, className, field, "Z"));

        if (hasComment) {
            instructions.add(new LdcInsnNode(comment));
        }

        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/common/Configuration", "get", "(Ljava/lang/String;Ljava/lang/String;Z" + (hasComment ? "Ljava/lang/String;" : "") + ")Lnet/minecraftforge/common/Property;"));
        instructions.add(new FieldInsnNode(GETFIELD, "net/minecraftforge/common/Property", "value", "Ljava/lang/String;"));
        instructions.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z"));

        instructions.add(new FieldInsnNode(PUTSTATIC, className, field, "Z"));

        return true;
    }

    // TODO: Other options
}
