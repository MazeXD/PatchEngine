package mcpc.patchengine.patches;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import cpw.mods.fml.common.FMLLog;

import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.asm.util.ClassUtil;
import mcpc.patchengine.asm.util.MethodUtil;
import mcpc.patchengine.asm.util.SeekUtil;
import mcpc.patchengine.common.Configuration;
import mcpc.patchengine.common.Constants;

public class ICBMPatch implements IPatch {
    private boolean _enabled = true;
    
    @Override
    public String[] getClassNames() {
        return new String[] { "icbm.common.zhapin.ex.ExHuanYuan" };
    }

    @Override
    public void loadConfigurations(Configuration configuration) {
        _enabled = configuration.get("ICBM", "enabled", _enabled).getBoolean(_enabled);
    }

    @Override
    public void transform(String name, ClassNode node) {
        if (!_enabled) {
            return;
        }

        patchExHuanYuan(node);
    }

    private void patchExHuanYuan(ClassNode node) {
        MethodNode method = ClassUtil.getMethod(node, "doBaoZha", String.format("(L%s;Luniversalelectricity/core/vector/Vector3;L%s;II)Z", Constants.WorldClass, Constants.EntityClass));

        if (method == null) {
            return;
        }

        int index = SeekUtil.searchInsn(method.instructions, new TypeInsnNode(CHECKCAST, Constants.ChunkProviderGenerateClass));
        if (index == -1) {
            return;
        }

        ((TypeInsnNode) method.instructions.get(index)).desc = String.format("org/bukkit/craftbukkit/%s/generator/NormalChunkGenerator", Constants.CraftBukkitNMS);
        method.instructions.insert(method.instructions.get(index), new MethodInsnNode(INVOKEVIRTUAL, String.format("org/bukkit/craftbukkit/%s/generator/NormalChunkGenerator", Constants.CraftBukkitNMS), "getForgeChunkProvider", String.format("()L%s;", Constants.ChunkProviderInterface)));

        MethodUtil.getLocalVariable(method, "chunkProviderGenerate", String.format("L%s;", Constants.ChunkProviderGenerateClass)).desc = String.format("L%s;", Constants.ChunkProviderInterface);

        for (int i = index + 2; i < method.instructions.size(); i++) {
            AbstractInsnNode insn = method.instructions.get(i);

            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;

                if (methodInsn.owner.equals(Constants.ChunkProviderGenerateClass)) {
                    methodInsn.setOpcode(INVOKEINTERFACE);
                    methodInsn.owner = Constants.ChunkProviderInterface;
                }
            }
        }

        FMLLog.info("[PatchEngine - ICBM] Patched ExHuanYuan class");
    }
}
