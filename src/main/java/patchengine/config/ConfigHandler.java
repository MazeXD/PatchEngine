package patchengine.config;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.lang3.StringUtils;
import patchengine.util.ConfigUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class ConfigHandler {

    private static final String FILENAME = "settings.conf";
    private static final Function<String, String> TRIMMER = new Function<String, String>() {

        @Override
        public String apply(String input) {
            return input != null ? input.trim() : null;
        }
    };

    private File file;
    private Config config;
    private boolean isDirty = false;

    public ConfigHandler(File file) {
        this.file = new File(file, FILENAME);
    }

    public void load() {
        config = ConfigFactory.parseFile(file);
    }

    public void save() {
        if (!isDirty) {
            return;
        }

        System.out.println("SAVE PERFORMED");

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            String content = config.root().render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false));
            writer.write(content);
            isDirty = false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean getBoolean(String path, boolean defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, defaultValue, "Boolean", comments);

        return config.getBoolean(path);
    }

    public byte getByte(String path, byte defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, (int) defaultValue, "Byte", comments);

        return (byte) (config.getInt(path) & 0xff);
    }

    public short getShort(String path, short defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, (int) defaultValue, "Short", comments);

        return (short) (config.getInt(path) & 0xffff);
    }

    public int getInt(String path, int defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, defaultValue, "Integer", comments);

        return config.getInt(path);
    }

    public long getLong(String path, long defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, defaultValue, "Long", comments);

        return config.getLong(path);
    }

    public float getFloat(String path, float defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, (double) defaultValue, "Float", comments);

        return (float) config.getDouble(path);
    }

    public double getDouble(String path, double defaultValue, String... comments) {
        checkPath(path);

        setValueIfAbsent(path, defaultValue, "Double", comments);

        return config.getDouble(path);
    }

    public String getString(String path, String defaultValue, String... comments) {
        checkPath(path);
        Preconditions.checkNotNull(defaultValue, "defaultValue mustn't be null");

        setValueIfAbsent(path, defaultValue, "String", comments);

        return config.getString(path);
    }

    public void setComments(String path, String... comments) {
        checkPath(path);
        Preconditions.checkNotNull(comments, "comments mustn't be null");

        if (comments.length <= 0 || !config.hasPath(path)) {
            return;
        }

        ConfigValue configValue = config.getValue(path);

        List<String> oldComments = configValue.origin().comments();
        if (oldComments != null) {
            oldComments = Lists.transform(oldComments, TRIMMER);

            if (oldComments.equals(Arrays.asList(comments))) {
                return;
            }
        }

        ConfigUtil.setComments(config.getValue(path), comments);
        isDirty = true;
    }

    private void setValueIfAbsent(String path, Object value, String type, String[] comments) {
        if (!config.hasPath(path)) {
            ConfigValue configValue = ConfigValueFactory.fromAnyRef(value, comments.length == 0 ? null : "");

            config = config.withValue(path, configValue);
            isDirty = true;
        }

        List<String> typesAndComments = Lists.newArrayList(comments);
        if (comments.length > 0) {
            int length = 0;
            for (String comment : comments) {
                if (length < comment.length()) {
                    length = comment.length();
                }
            }
            typesAndComments.add(StringUtils.repeat('-', length));
        }
        typesAndComments.add("Type: " + type);
        setComments(path, typesAndComments.toArray(new String[typesAndComments.size()]));
    }

    private void checkPath(String path) {
        Preconditions.checkNotNull(path, "path mustn't be null");
        Preconditions.checkArgument(!path.isEmpty(), "path musn't be an empty string");
    }
}
