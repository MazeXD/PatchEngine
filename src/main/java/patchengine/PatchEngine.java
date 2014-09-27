package patchengine;

import patchengine.config.Configuration;


public class PatchEngine {

    public static final String VERSION = "%version%";

    private static Configuration config;
    private static PatchEngineCore core;

    static void setConfig(Configuration config) {
        PatchEngine.config = config;
    }

    static void setCore(PatchEngineCore core) {
        PatchEngine.core = core;
    }

    public static Configuration config() {
        return config;
    }

    public static PatchEngineCore core() {
        return core;
    }
}
