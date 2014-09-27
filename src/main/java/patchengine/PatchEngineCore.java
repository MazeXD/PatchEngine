package patchengine;

import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import patchengine.config.ConfigHandler;
import patchengine.config.Configuration;
import patchengine.internal.PEAccessTransformer;
import patchengine.internal.PEClassTransformer;
import patchengine.scripting.ScriptManager;

import java.io.File;
import java.util.Map;


@IFMLLoadingPlugin.TransformerExclusions({"patchengine."})
public class PatchEngineCore implements IFMLLoadingPlugin, IFMLCallHook {

    private File configFolder;
    private ScriptManager scriptManager;

    public PatchEngineCore() {
        PatchEngine.setCore(this);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {PEClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return PatchEngineModContainer.class.getName();
    }

    @Override
    public String getSetupClass() {
        return PatchEngineCore.class.getName();
    }

    @Override
    public void injectData(Map<String, Object> data) {
        configFolder = new File(new File((File) data.get("mcLocation"), "config"), "PatchEngine");
    }

    @Override
    public String getAccessTransformerClass() {
        return PEAccessTransformer.class.getName();
    }

    @Override
    public Void call() throws Exception {
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }

        ConfigHandler configHandler = new ConfigHandler(configFolder);
        configHandler.load();

        Configuration config = new Configuration(configHandler);
        PatchEngine.setConfig(config);

        scriptManager = new ScriptManager(configFolder, configHandler);

        configHandler.save();

        return null;
    }
}
