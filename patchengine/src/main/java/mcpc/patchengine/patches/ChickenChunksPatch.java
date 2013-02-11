package mcpc.patchengine.patches;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLLog;

import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.asm.util.ClassUtil;
import mcpc.patchengine.asm.util.NavigationUtil;
import mcpc.patchengine.common.Configuration;

public class ChickenChunksPatch implements IPatch {
    private boolean _enabled = true;

    @Override
    public String[] getClassNames() {
        return new String[] { "codechicken.chunkloader.ChunkLoaderManager" };
    }

    @Override
    public void loadConfigurations(Configuration configuration) {
        _enabled = configuration.get("ChickenChunks", "enabled", _enabled).getBoolean(_enabled);
    }

    @Override
    public void transform(String name, ClassNode node) {
        if (!_enabled) {
            return;
        }

        if (name.equals("codechicken.chunkloader.ChunkLoaderManager")) {
            patchChunkLoaderManager(node);
        }
    }

    private void patchChunkLoaderManager(ClassNode node) {
        MethodNode method = ClassUtil.getMethod(node, "cleanChunks", "(Lin;)V");
        if (method == null) {
            return;
        }

        LabelNode label = NavigationUtil.getPreviousLabel(method.instructions, method.instructions.size() -1);
        InsnList instructions = new InsnList();
        
        instructions.add(new JumpInsnNode(GOTO, label));
        
        method.instructions.insert(instructions);

        FMLLog.info("[PatchEngine] Disabled ChickenChunks GC");
    }
}
