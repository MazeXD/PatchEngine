package patchengine.internal;

import net.minecraft.launchwrapper.IClassTransformer;
import patchengine.scripting.ScriptManager;


public class PEClassTransformer implements IClassTransformer {

    private static PEClassTransformer instance;

    private ScriptManager scriptManager = null;

    public PEClassTransformer() {
        instance = this;
    }

    public static PEClassTransformer instance() {
        return instance;
    }

    public void setScriptManager(ScriptManager scriptManager) {
        this.scriptManager = scriptManager;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (!name.equals(transformedName)) {
            System.out.println(name + "  -  " + transformedName);
        }

        if (scriptManager == null) {
            return bytes;
        }

        return bytes;
    }
}
