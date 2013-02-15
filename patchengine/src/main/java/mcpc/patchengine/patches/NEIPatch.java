package mcpc.patchengine.patches;

import static org.objectweb.asm.Opcodes.*;

import net.minecraft.server.MinecraftServer;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.FMLLog;
import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.asm.util.ClassUtil;
import mcpc.patchengine.asm.util.MethodUtil;
import mcpc.patchengine.asm.util.SeekUtil;
import mcpc.patchengine.common.Configuration;
import mcpc.patchengine.common.Constants;

public class NEIPatch implements IPatch{
    private boolean _enabled = true;
    public static MinecraftServer server = null;
    
    @Override
    public String[] getClassNames() {
        return new String[] { "codechicken.nei.ServerPacketHandler", "codechicken.nei.NEIServerConfig" };
    }

    @Override
    public void loadConfigurations(Configuration configuration) {
        _enabled = configuration.get("NEI", "enabled", _enabled).getBoolean(_enabled);
    }

    @Override
    public void transform(String name, ClassNode node) {
        if (!_enabled) {
            return;
        }

        if(name.equals("codechicken.nei.ServerPacketHandler"))
        {
            patchServerPacketHandler(node);
        }
        else if(name.equals("codechicken.nei.NEIServerConfig")){
            patchNEIServerConfig(node);
        }
    }

    private void patchServerPacketHandler(ClassNode node) {
        MethodNode method = ClassUtil.getMethod(node, "sendPermissableActionsTo", String.format("(L%s;)V", Constants.EntityPlayerMPClass));
        
        InsnList instructions = new InsnList();
        
        LabelNode label = new LabelNode();
        LabelNode label2 = new LabelNode();
        
        LabelNode permLabel = MethodUtil.getLabelWithInsnList(method, new MethodInsnNode(INVOKEVIRTUAL, "codechicken/nei/InterActionMap", "ordinal", "()I"), new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
                
        instructions.add(new LabelNode());
        instructions.add(new VarInsnNode(ALOAD, MethodUtil.getLocalVariable(method, "player", "*").index));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Constants.EntityPlayerMPClass, "getBukkitEntity", String.format("()Lorg/bukkit/craftbukkit/%s/entity/CraftHumanEntity;", Constants.CraftBukkitNMS)));
        instructions.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new LdcInsnNode("nei."));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V"));
        instructions.add(new VarInsnNode(ALOAD, MethodUtil.getLocalVariable(method, "action", "*").index)); // TODO
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "codechicken/nei/InterActionMap", "getName", "()Ljava/lang/String;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, String.format("org/bukkit/craftbukkit/%s/entity/CraftHumanEntity", Constants.CraftBukkitNMS), "hasPermission", "(Ljava/lang/String;)Z"));
        instructions.add(new JumpInsnNode(IFEQ, label));
        instructions.add(new JumpInsnNode(GOTO, label2));
        instructions.add(label);

        method.instructions.insertBefore(permLabel, label2);
        
        LabelNode insertLabel = MethodUtil.getLabelWithInsnList(method, new MethodInsnNode(INVOKEVIRTUAL, "codechicken/nei/InterActionMap", "getName", "()Ljava/lang/String;"), new MethodInsnNode(INVOKESTATIC, "codechicken/nei/NEIServerConfig", "canPlayerUseFeature", "(Ljava/lang/String;Ljava/lang/String;)Z"));

        method.instructions.insertBefore(insertLabel, instructions);
        
        FMLLog.info("[PatchEngine - NEI] Patched permissions in ServerPacketHandler");
    }
    
    private void patchNEIServerConfig(ClassNode node) {
        
        MethodNode method = ClassUtil.getMethod(node, "canPlayerUseFeature", "(Ljava/lang/String;Ljava/lang/String;)Z");
        
        InsnList instructions = new InsnList();
        
        LabelNode label = new LabelNode();
        LabelNode label2 = new LabelNode();
             
        instructions.add(new FieldInsnNode(GETSTATIC, node.name, "server", "Lnet/minecraft/server/MinecraftServer;"));
        instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/server/MinecraftServer", "server", String.format("Lorg/bukkit/craftbukkit/%s/CraftServer;", Constants.CraftBukkitNMS)));
        
        instructions.add(new VarInsnNode(ALOAD, MethodUtil.getLocalVariable(method, "playername", "*").index)); // TODO
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, String.format("org/bukkit/craftbukkit/%s/CraftServer", Constants.CraftBukkitNMS), "getPlayerExact", "(Ljava/lang/String;)Lorg/bukkit/entity/Player;"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new JumpInsnNode(IFNULL,label));

        instructions.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new LdcInsnNode("nei."));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V"));
        instructions.add(new VarInsnNode(ALOAD, MethodUtil.getLocalVariable(method, "featurename", "*").index)); // TODO
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/permissions/Permissible", "hasPermission", "(Ljava/lang/String;)Z"));
        instructions.add(new JumpInsnNode(IFEQ, label2));
        instructions.add(new InsnNode(ICONST_1));
        instructions.add(new InsnNode(IRETURN));

        instructions.add(label);
        instructions.add(new InsnNode(POP));
        
        instructions.add(label2);
        
        AbstractInsnNode insertLabel = MethodUtil.getFirstLabel(method).getNext();
        
        method.instructions.insert(insertLabel, instructions);
        
        FMLLog.info("[PatchEngine - NEI] Patched permissions in NEIServerConfig");
    }
}
