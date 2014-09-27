package patchengine.util;

import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValue;
import cpw.mods.fml.relauncher.ReflectionHelper;
import scala.actors.threadpool.Arrays;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class ConfigUtil {

    private static Field commentsField;
    private static Field originField;
    private static Method newSimpleOriginMethod;

    private ConfigUtil() {}

    @SuppressWarnings("unchecked")
    public static void setComments(ConfigValue configValue, String[] comments) {
        ConfigOrigin origin = configValue.origin();

        origin = exchangeOrigin(configValue);

        // Bad to rely on description
        if (origin.description().equals("hardcoded value")) {
            origin = exchangeOrigin(configValue);
        }

        try {
            List<String> newList = new ArrayList<String>(comments.length);
            newList.addAll(Arrays.asList(comments));
            commentsField.set(origin, newList);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static ConfigOrigin exchangeOrigin(ConfigValue configValue) {
        try {
            ConfigOrigin origin = (ConfigOrigin) newSimpleOriginMethod.invoke(null, "");

            originField.set(configValue, origin);

            return origin;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    static { // Bad reflection hackery
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            Class<?> clazz = Class.forName("com.typesafe.config.impl.SimpleConfigOrigin");
            commentsField = ReflectionHelper.findField(clazz, "commentsOrNull");
            modifiersField.setInt(commentsField, commentsField.getModifiers() & ~Modifier.FINAL);

            newSimpleOriginMethod = clazz.getDeclaredMethod("newSimple", String.class);
            newSimpleOriginMethod.setAccessible(true);

            clazz = Class.forName("com.typesafe.config.impl.AbstractConfigValue");
            originField = ReflectionHelper.findField(clazz, "origin");
            modifiersField.setInt(originField, originField.getModifiers() & ~Modifier.FINAL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
