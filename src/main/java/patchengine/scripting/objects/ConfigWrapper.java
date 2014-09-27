package patchengine.scripting.objects;

import com.google.common.base.Preconditions;

import patchengine.config.ConfigHandler;


public class ConfigWrapper {

    private final String category;
    private final ConfigHandler config;

    public ConfigWrapper(String category, ConfigHandler config) {
        Preconditions.checkNotNull(category, "category mustn't be null");
        Preconditions.checkNotNull(config, "config mustn't be null");

        this.category = "patches." + category;
        this.config = config;
    }

    public boolean getBoolean(String path, boolean defaultValue, String... comments) {
        return config.getBoolean(category + "." + path, defaultValue, comments);
    }

    public byte getByte(String path, byte defaultValue, String... comments) {
        return config.getByte(category + "." + path, defaultValue, comments);
    }

    public short getShort(String path, short defaultValue, String... comments) {
        return config.getShort(category + "." + path, defaultValue, comments);
    }

    public int getInt(String path, int defaultValue, String... comments) {
        return config.getInt(category + "." + path, defaultValue, comments);
    }

    public long getLong(String path, long defaultValue, String... comments) {
        return config.getLong(category + "." + path, defaultValue, comments);
    }

    public float getFloat(String path, float defaultValue, String... comments) {
        return config.getFloat(category + "." + path, defaultValue, comments);
    }

    public double getDouble(String path, double defaultValue, String... comments) {
        return config.getDouble(category + "." + path, defaultValue, comments);
    }

    public String getString(String path, String defaultValue, String... comments) {
        return config.getString(category + "." + path, defaultValue, comments);
    }
}
