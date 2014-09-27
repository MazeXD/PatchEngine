package patchengine.scripting;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.commonjs.module.ModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.Require;
import org.mozilla.javascript.commonjs.module.provider.ModuleSourceProvider;
import org.mozilla.javascript.commonjs.module.provider.SoftCachingModuleScriptProvider;
import org.mozilla.javascript.commonjs.module.provider.UrlModuleSourceProvider;
import patchengine.config.ConfigHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;


public class ScriptManager {

    private static final String PATCH_FOLDERNAME = "patches";
    private static final String LIBS_FOLDERNAME = "libs";

    private final File scriptFolder;
    private final File libsFolder;
    private final ConfigHandler config;

    public ScriptManager(File baseFolder, ConfigHandler config) {
        this.scriptFolder = new File(baseFolder, PATCH_FOLDERNAME);
        this.libsFolder = new File(baseFolder, LIBS_FOLDERNAME);
        this.config = config;
        load();
    }

    private void load() {
        if (!libsFolder.exists()) {
            libsFolder.mkdirs();
        }

        if (!scriptFolder.exists()) {
            scriptFolder.mkdirs();
            return;
        }

        Context context = Context.enter();

        IOFileFilter fileFilter = FileFilterUtils.suffixFileFilter(".js");
        for (File file : FileUtils.listFiles(scriptFolder, fileFilter, DirectoryFileFilter.DIRECTORY)) {
            Scriptable scope = context.initStandardObjects();

            setupScope(scope, file);
            setupRequire(context, scope);

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                context.evaluateReader(scope, reader, file.getName(), 0, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        Context.exit();
        config.save();
    }

    private void setupScope(Scriptable scope, File file) {
        ScriptFunctions.register(scope);

        String category = file.getAbsolutePath().replace(scriptFolder.getAbsolutePath(), "");
        category = category.replace("/", ".").replace("\\", ".");
        category = category.substring(1, category.length() - 3);

        new ScriptContext(scope, category, config);
    }

    private void setupRequire(Context context, Scriptable scope) {
        Set<URI> priviledgedPaths = Collections.singleton(libsFolder.toURI());

        ModuleSourceProvider sourceProvider = new UrlModuleSourceProvider(priviledgedPaths, null);
        ModuleScriptProvider scriptProvider = new SoftCachingModuleScriptProvider(sourceProvider);

        new Require(context, scope, scriptProvider, null, null, true).install(scope);
    }
}
