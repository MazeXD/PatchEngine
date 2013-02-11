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
        MethodNode method = ClassUtil.getMethod(node, "doBaoZha", "(Lyc;Luniversalelectricity/core/vector/Vector3;Llq;II)Z");

        if (method == null) {
            return;
        }

        int index = SeekUtil.searchInsn(method.instructions, new TypeInsnNode(CHECKCAST, "abb"));
        if (index == -1) {
            return;
        }

        ((TypeInsnNode) method.instructions.get(index)).desc = "org/bukkit/craftbukkit/" + Constants.CraftBukkitNMS + "/generator/NormalChunkGenerator";
        method.instructions.insert(method.instructions.get(index), new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/craftbukkit/" + Constants.CraftBukkitNMS + "/generator/NormalChunkGenerator", "getForgeChunkProvider", "()Lzw;"));

        MethodUtil.getLocalVariable(method, "chunkProviderGenerate", "Labb;").desc = "Lzw;";

        for (int i = index + 2; i < method.instructions.size(); i++) {
            AbstractInsnNode insn = method.instructions.get(i);

            if (insn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) insn;

                if (methodInsn.owner.equals("abb")) {
                    methodInsn.setOpcode(INVOKEINTERFACE);
                    methodInsn.owner = "zw";
                }
            }
        }

        FMLLog.info("[PatchEngine] Patched ExHuanYuan class");
    }
}
