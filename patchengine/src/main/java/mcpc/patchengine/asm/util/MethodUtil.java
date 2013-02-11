package mcpc.patchengine.asm.util;

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
}
