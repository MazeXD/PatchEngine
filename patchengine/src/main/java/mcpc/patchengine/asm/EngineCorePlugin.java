package mcpc.patchengine.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.common.Configuration;
import mcpc.patchengine.patches.ChickenChunksPatch;
import mcpc.patchengine.patches.IC2Patch;
import mcpc.patchengine.patches.ICBMPatch;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions({ "mcpc.patchengine" })
public class EngineCorePlugin implements IFMLLoadingPlugin, IFMLCallHook {
    private static boolean _enabled = true;
    private static boolean _debug = false;

    private static File _configurationFolder = null;

    private List<IPatch> _patches = new ArrayList<IPatch>();

    public String[] getLibraryRequestClass() {
        return null;
    }

    public String[] getASMTransformerClass() {
        return new String[] { "mcpc.patchengine.asm.ClassPatchApplier" };
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return "mcpc.patchengine.asm.EngineCorePlugin";
    }

    @SuppressWarnings("rawtypes")
    public void injectData(Map data) {
        if (data.containsKey("mcLocation")) {
            _configurationFolder = new File((File) data.get("mcLocation"), "config").getAbsoluteFile();
        }
    }

    private void collectPatches() {
        // TODO: Search for patches in jar in mods/coremods

        _patches.add(new IC2Patch());
        _patches.add(new ICBMPatch());
        _patches.add(new ChickenChunksPatch());
    }

    private void loadConfig() {
        _configurationFolder.mkdirs();

        Configuration configuration = new Configuration(new File(_configurationFolder, "PatchEngine.cfg"));
        configuration.load();

        _enabled = configuration.get(Configuration.CATEGORY_CORE, "enabled", _enabled, "Should PatchEngine apply patches in general?").getBoolean(_enabled);
        _debug = configuration.get(Configuration.CATEGORY_CORE, "debug", _debug).getBoolean(_debug);

        for (IPatch patch : _patches) {
            patch.loadConfigurations(configuration);
        }

        configuration.save();
    }

    public Void call() {
        collectPatches();

        loadConfig();

        if (isEnabled()) {
            FMLLog.info("[PatchEngine] Active | Loaded %s patches", _patches.size());
        }

        if (isEnabled()) {
            for (IPatch patch : _patches) {
                ClassPatchApplier.instance().addPatch(patch);
            }
        }

        return null;
    }

    public static boolean isEnabled() {
        return _enabled;
    }

    public static boolean isDebug() {
        return _debug;
    }
}
