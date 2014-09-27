package patchengine.config;


public class Configuration {

    private static final String DEBUG_CATEGORY = "debug";

    private final ConfigHandler config;

    private Debug debug;

    public Configuration(ConfigHandler config) {
        this.config = config;
        load();
    }

    public Debug debug() {
        return debug;
    }

    private void load() {
        debug = new Debug(config);
    }

    public class Debug {

        private boolean enabled;

        private Debug(ConfigHandler config) {
            enabled = config.getBoolean(DEBUG_CATEGORY + ".enabled", false, "Set true to enable debug features");

            config.setComments(DEBUG_CATEGORY, "Debugging category");
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
