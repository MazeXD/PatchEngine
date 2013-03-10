package mcpc.patchengine.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.google.common.io.Files;

import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.common.Configuration;
import mcpc.patchengine.common.Constants;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.TransformerExclusions({ "mcpc.patchengine" })
public class EngineCorePlugin implements IFMLLoadingPlugin, IFMLCallHook {
    private static boolean _enabled = true;
    private static boolean _debug = false;

    private static PatchClassLoader _classLoader = null;

    private static File _configurationFolder = null;
    private static File _patchFolder = null;

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
            _configurationFolder = new File(new File((File) data.get("mcLocation"), "config"),"PatchEngine").getAbsoluteFile();
            _patchFolder = new File(_configurationFolder, "patches").getAbsoluteFile();
        }
    }

    private void collectPatches(File patchFolder) {
        for (File file : patchFolder.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }

            String extension = file.getName().substring(file.getName().lastIndexOf('.') + 1, file.getName().length());

            if ("class".equals(extension) || "patch".equals(extension) && !file.getName().contains("$")) {
                loadPatch(file);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadPatch(File patchFile) {
        if (!patchFile.exists()) {
            return;
        }

        Class<?> patchClass;
        try {
            patchClass = _classLoader.loadClass(patchFile.getName());
        } catch (Exception e) {
            FMLLog.warning("[PatchEngine] Failed to load %s", patchFile.getName());
            e.printStackTrace();
            return;
        }

        if (IPatch.class.isAssignableFrom(patchClass)) {
            Class<? extends IPatch> patch = (Class<? extends IPatch>) patchClass;

            IPatch instance;
            try {
                instance = patch.newInstance();
            } catch (Exception e) {
                FMLLog.severe("[PatchEngine] %s doesn't have a constructor without any parameters", patch.getName());
                return;
            }

            _patches.add(instance);
        }
    }

    private void loadConfig() {
        _configurationFolder.mkdirs();

        Configuration configuration = new Configuration(new File(_configurationFolder, "config.cfg"));
        configuration.load();

        _enabled = configuration.get(Configuration.CATEGORY_CORE, "enabled", _enabled, "Should PatchEngine apply patches in general?").getBoolean(_enabled);
        _debug = configuration.get(Configuration.CATEGORY_CORE, "debug", _debug).getBoolean(_debug);

        for (IPatch patch : _patches) {
            patch.loadConfigurations(configuration);
        }

        configuration.save();
    }

    public Void call() {
        if (!_patchFolder.exists()) {
            _patchFolder.mkdirs();
        }

        Constants.load(_configurationFolder);
        
        _classLoader = new PatchClassLoader(_patchFolder);

        collectPatches(_patchFolder);

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

    private class PatchClassLoader extends ClassLoader {
        private File _patchFolder;
        private Map<String, Class<?>> _cachedClasses;

        public PatchClassLoader(File patchFolder) {
            super(IPatch.class.getClassLoader());

            _patchFolder = patchFolder;
            _cachedClasses = new HashMap<String, Class<?>>(10);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                if (_cachedClasses.containsKey(name)) {
                    return _cachedClasses.get(name);
                }

                File file = new File(_patchFolder, name);
                
                Class<?> cl;
                if (!file.exists()) {
                    cl = super.loadClass(name);
                } else {
                    byte[] classData = Files.toByteArray(file);

                    ClassReader reader = new ClassReader(classData);
                    ClassNode node = new ClassNode();
                    reader.accept(node, 0);

                    cl = defineClass(node.name.replace("/", "."), classData, 0, classData.length);
                }

                _cachedClasses.put(name, cl);

                return cl;
            } catch (Exception e) {
                throw new ClassNotFoundException(e.getMessage(), e);
            }
        }
    }
}
