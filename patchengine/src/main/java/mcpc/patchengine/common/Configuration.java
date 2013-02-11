/*
 * Configuration class by MinecraftForge Configuration class
 * Slightly modified for own purpose
 * https://github.com/MinecraftForge/MinecraftForge/blob/master/common/net/minecraftforge/common/Configuration.java
 */
package mcpc.patchengine.common;

import java.io.*;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLInjectionData;

import mcpc.patchengine.common.ConfigCategory;
import mcpc.patchengine.common.Property;
import static mcpc.patchengine.common.Property.Type.*;

public class Configuration {
    public static final String CATEGORY_CORE = "core";
    public static final String ALLOWED_CHARS = "._-";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String CATEGORY_SPLITTER = ".";
    public static final String NEW_LINE;
    private static final Pattern CONFIG_START = Pattern.compile("START: \"([^\\\"]+)\"");
    private static final Pattern CONFIG_END = Pattern.compile("END: \"([^\\\"]+)\"");
    public static final CharMatcher allowedProperties = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf(ALLOWED_CHARS));
    private static Configuration PARENT = null;

    File file;

    public Map<String, ConfigCategory> categories = new TreeMap<String, ConfigCategory>();
    private Map<String, Configuration> children = new TreeMap<String, Configuration>();

    private boolean caseSensitiveCustomCategories;
    public String defaultEncoding = DEFAULT_ENCODING;
    private String fileName = null;
    public boolean isChild = false;

    static {
        NEW_LINE = System.getProperty("line.separator");
    }

    public Configuration() {
    }

    /**
     * Create a configuration file for the file given in parameter.
     */
    public Configuration(File file) {
        this.file = file;
        String basePath = ((File) (FMLInjectionData.data()[6])).getAbsolutePath().replace(File.separatorChar, '/').replace("/.", "");
        String path = file.getAbsolutePath().replace(File.separatorChar, '/').replace("/./", "/").replace(basePath, "");

        if (PARENT != null) {
            PARENT.setChild(path, this);
            isChild = true;
        } else {
            fileName = path;
            load();
        }
    }

    public Configuration(File file, boolean caseSensitiveCustomCategories) {
        this(file);
        this.caseSensitiveCustomCategories = caseSensitiveCustomCategories;
    }

    public Property get(String category, String key, int defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, int defaultValue, String comment) {
        Property prop = get(category, key, Integer.toString(defaultValue), comment, INTEGER);
        if (!prop.isIntValue()) {
            prop.value = Integer.toString(defaultValue);
        }
        return prop;
    }

    public Property get(String category, String key, boolean defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, boolean defaultValue, String comment) {
        Property prop = get(category, key, Boolean.toString(defaultValue), comment, BOOLEAN);
        if (!prop.isBooleanValue()) {
            prop.value = Boolean.toString(defaultValue);
        }
        return prop;
    }

    public Property get(String category, String key, double defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, double defaultValue, String comment) {
        Property prop = get(category, key, Double.toString(defaultValue), comment, DOUBLE);
        if (!prop.isDoubleValue()) {
            prop.value = Double.toString(defaultValue);
        }
        return prop;
    }

    public Property get(String category, String key, String defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, String defaultValue, String comment) {
        return get(category, key, defaultValue, comment, STRING);
    }

    public Property get(String category, String key, String[] defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, String[] defaultValue, String comment) {
        return get(category, key, defaultValue, comment, STRING);
    }

    public Property get(String category, String key, int[] defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, int[] defaultValue, String comment) {
        String[] values = new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++) {
            values[i] = Integer.toString(defaultValue[i]);
        }

        Property prop = get(category, key, values, comment, INTEGER);
        if (!prop.isIntList()) {
            prop.valueList = values;
        }

        return prop;
    }

    public Property get(String category, String key, double[] defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, double[] defaultValue, String comment) {
        String[] values = new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++) {
            values[i] = Double.toString(defaultValue[i]);
        }

        Property prop = get(category, key, values, comment, DOUBLE);

        if (!prop.isDoubleList()) {
            prop.valueList = values;
        }

        return prop;
    }

    public Property get(String category, String key, boolean[] defaultValue) {
        return get(category, key, defaultValue, null);
    }

    public Property get(String category, String key, boolean[] defaultValue, String comment) {
        String[] values = new String[defaultValue.length];
        for (int i = 0; i < defaultValue.length; i++) {
            values[i] = Boolean.toString(defaultValue[i]);
        }

        Property prop = get(category, key, values, comment, BOOLEAN);

        if (!prop.isBooleanList()) {
            prop.valueList = values;
        }

        return prop;
    }

    public Property get(String category, String key, String defaultValue, String comment, Property.Type type) {
        if (!caseSensitiveCustomCategories) {
            category = category.toLowerCase(Locale.ENGLISH);
        }

        ConfigCategory cat = getCategory(category);

        if (cat.containsKey(key)) {
            Property prop = cat.get(key);

            if (prop.getType() == null) {
                prop = new Property(prop.getName(), prop.value, type, true);
                cat.set(key, prop);
            }

            prop.comment = comment;
            return prop;
        } else if (defaultValue != null) {
            Property prop = new Property(key, defaultValue, type);
            cat.set(key, prop);
            prop.comment = comment;
            return prop;
        } else {
            return null;
        }
    }

    public Property get(String category, String key, String[] defaultValue, String comment, Property.Type type) {
        if (!caseSensitiveCustomCategories) {
            category = category.toLowerCase(Locale.ENGLISH);
        }

        ConfigCategory cat = getCategory(category);

        if (cat.containsKey(key)) {
            Property prop = cat.get(key);

            if (prop.getType() == null) {
                prop = new Property(prop.getName(), prop.value, type, true);
                cat.set(key, prop);
            }

            prop.comment = comment;

            return prop;
        } else if (defaultValue != null) {
            Property prop = new Property(key, defaultValue, type);
            prop.comment = comment;
            cat.set(key, prop);
            return prop;
        } else {
            return null;
        }
    }

    public boolean hasCategory(String category) {
        return categories.get(category) != null;
    }

    public boolean hasKey(String category, String key) {
        ConfigCategory cat = categories.get(category);
        return cat != null && cat.containsKey(key);
    }

    public void load() {
        if (PARENT != null && PARENT != this) {
            return;
        }

        BufferedReader buffer = null;
        UnicodeInputStreamReader input = null;
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists() && !file.createNewFile()) {
                return;
            }

            if (file.canRead()) {
                input = new UnicodeInputStreamReader(new FileInputStream(file), defaultEncoding);
                defaultEncoding = input.getEncoding();
                buffer = new BufferedReader(input);

                String line;
                ConfigCategory currentCat = null;
                Property.Type type = null;
                ArrayList<String> tmpList = null;
                int lineNum = 0;
                String name = null;

                while (true) {
                    lineNum++;
                    line = buffer.readLine();

                    if (line == null) {
                        break;
                    }

                    Matcher start = CONFIG_START.matcher(line);
                    Matcher end = CONFIG_END.matcher(line);

                    if (start.matches()) {
                        fileName = start.group(1);
                        categories = new TreeMap<String, ConfigCategory>();
                        continue;
                    } else if (end.matches()) {
                        fileName = end.group(1);
                        Configuration child = new Configuration();
                        child.categories = categories;
                        this.children.put(fileName, child);
                        continue;
                    }

                    int nameStart = -1, nameEnd = -1;
                    boolean skip = false;
                    boolean quoted = false;

                    for (int i = 0; i < line.length() && !skip; ++i) {
                        if (Character.isLetterOrDigit(line.charAt(i)) || ALLOWED_CHARS.indexOf(line.charAt(i)) != -1 || (quoted && line.charAt(i) != '"')) {
                            if (nameStart == -1) {
                                nameStart = i;
                            }

                            nameEnd = i;
                        } else if (Character.isWhitespace(line.charAt(i))) {
                            // ignore space charaters
                        } else {
                            switch (line.charAt(i)) {
                                case '#':
                                    skip = true;
                                    continue;

                                case '"':
                                    if (quoted) {
                                        quoted = false;
                                    }
                                    if (!quoted && nameStart == -1) {
                                        quoted = true;
                                    }
                                    break;

                                case '{':
                                    name = line.substring(nameStart, nameEnd + 1);
                                    String qualifiedName = ConfigCategory.getQualifiedName(name, currentCat);

                                    ConfigCategory cat = categories.get(qualifiedName);
                                    if (cat == null) {
                                        currentCat = new ConfigCategory(name, currentCat);
                                        categories.put(qualifiedName, currentCat);
                                    } else {
                                        currentCat = cat;
                                    }
                                    name = null;

                                    break;

                                case '}':
                                    if (currentCat == null) {
                                        throw new RuntimeException(String.format("Config file corrupt, attepted to close to many categories '%s:%d'", fileName, lineNum));
                                    }
                                    currentCat = currentCat.parent;
                                    break;

                                case '=':
                                    name = line.substring(nameStart, nameEnd + 1);

                                    if (currentCat == null) {
                                        throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, fileName, lineNum));
                                    }

                                    Property prop = new Property(name, line.substring(i + 1), type);
                                    i = line.length();

                                    currentCat.set(name, prop);

                                    break;

                                case ':':
                                    type = Property.Type.tryParse(line.substring(nameStart, nameEnd + 1).charAt(0));
                                    nameStart = nameEnd = -1;
                                    break;

                                case '<':
                                    if (tmpList != null) {
                                        throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                                    }

                                    name = line.substring(nameStart, nameEnd + 1);

                                    if (currentCat == null) {
                                        throw new RuntimeException(String.format("'%s' has no scope in '%s:%d'", name, fileName, lineNum));
                                    }

                                    tmpList = new ArrayList<String>();

                                    skip = true;

                                    break;

                                case '>':
                                    if (tmpList == null) {
                                        throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                                    }

                                    currentCat.set(name, new Property(name, tmpList.toArray(new String[tmpList.size()]), type));
                                    name = null;
                                    tmpList = null;
                                    type = null;
                                    break;

                                default:
                                    throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", line.charAt(i), fileName, lineNum));
                            }
                        }
                    }

                    if (quoted) {
                        throw new RuntimeException(String.format("Unmatched quote in '%s:%d'", fileName, lineNum));
                    } else if (tmpList != null && !skip) {
                        tmpList.add(line.trim());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void save() {
        if (PARENT != null && PARENT != this) {
            PARENT.save();
            return;
        }

        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists() && !file.createNewFile()) {
                return;
            }

            if (file.canWrite()) {
                FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter buffer = new BufferedWriter(new OutputStreamWriter(fos, defaultEncoding));

                buffer.write("# Configuration file" + NEW_LINE);
                buffer.write("# Generated on " + DateFormat.getInstance().format(new Date()) + NEW_LINE + NEW_LINE);

                if (children.isEmpty()) {
                    save(buffer);
                } else {
                    for (Map.Entry<String, Configuration> entry : children.entrySet()) {
                        buffer.write("START: \"" + entry.getKey() + "\"" + NEW_LINE);
                        entry.getValue().save(buffer);
                        buffer.write("END: \"" + entry.getKey() + "\"" + NEW_LINE + NEW_LINE);
                    }
                }

                buffer.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(BufferedWriter out) throws IOException {
        for (ConfigCategory cat : categories.values()) {
            if (!cat.isChild()) {
                cat.write(out, 0);
                out.newLine();
            }
        }
    }

    public ConfigCategory getCategory(String category) {
        ConfigCategory ret = categories.get(category);

        if (ret == null) {
            if (category.contains(CATEGORY_SPLITTER)) {
                String[] hierarchy = category.split("\\" + CATEGORY_SPLITTER);
                ConfigCategory parent = categories.get(hierarchy[0]);

                if (parent == null) {
                    parent = new ConfigCategory(hierarchy[0]);
                    categories.put(parent.getQualifiedName(), parent);
                }

                for (int i = 1; i < hierarchy.length; i++) {
                    String name = ConfigCategory.getQualifiedName(hierarchy[i], parent);
                    ConfigCategory child = categories.get(name);

                    if (child == null) {
                        child = new ConfigCategory(hierarchy[i], parent);
                        categories.put(name, child);
                    }

                    ret = child;
                    parent = child;
                }
            } else {
                ret = new ConfigCategory(category);
                categories.put(category, ret);
            }
        }

        return ret;
    }

    public void addCustomCategoryComment(String category, String comment) {
        if (!caseSensitiveCustomCategories) category = category.toLowerCase(Locale.ENGLISH);
        getCategory(category).setComment(comment);
    }

    private void setChild(String name, Configuration child) {
        if (!children.containsKey(name)) {
            children.put(name, child);
        } else {
            Configuration old = children.get(name);
            child.categories = old.categories;
            child.fileName = old.fileName;
        }
    }

    public static void enableGlobalConfig() {
        PARENT = new Configuration(new File(Loader.instance().getConfigDir(), "global.cfg"));
        PARENT.load();
    }

    public static class UnicodeInputStreamReader extends Reader {
        private final InputStreamReader input;

        public UnicodeInputStreamReader(InputStream source, String encoding) throws IOException {
            String enc = encoding;
            byte[] data = new byte[4];

            PushbackInputStream pbStream = new PushbackInputStream(source, data.length);
            int read = pbStream.read(data, 0, data.length);
            int size = 0;

            int bom16 = (data[0] & 0xFF) << 8 | (data[1] & 0xFF);
            int bom24 = bom16 << 8 | (data[2] & 0xFF);
            int bom32 = bom24 << 8 | (data[3] & 0xFF);

            if (bom24 == 0xEFBBBF) {
                enc = "UTF-8";
                size = 3;
            } else if (bom16 == 0xFEFF) {
                enc = "UTF-16BE";
                size = 2;
            } else if (bom16 == 0xFFFE) {
                enc = "UTF-16LE";
                size = 2;
            } else if (bom32 == 0x0000FEFF) {
                enc = "UTF-32BE";
                size = 4;
            } else if (bom32 == 0xFFFE0000) {
                enc = "UTF-32LE";
                size = 4;
            }

            if (size < read) {
                pbStream.unread(data, size, read - size);
            }

            this.input = new InputStreamReader(pbStream, enc);
        }

        public String getEncoding() {
            return input.getEncoding();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return input.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            input.close();
        }
    }
}