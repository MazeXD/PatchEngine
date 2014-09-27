package patchengine.scripting;

import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import patchengine.util.RhinoUtil;

import java.lang.reflect.Method;


public class ScriptFunctions {

    private ScriptFunctions() {}

    public static void registerTransformer(String name, NativeFunction function) {
        if (name.equals("undefined")) {
            System.err.print("Unable to register transformer as no 'name' has been supplied");
            return;
        }
        
        if (function == null) {
            System.err.print("Unable to register transformer for '" + name + "'  as no 'function' has been supplied");
            return;
        }

        int argCount = RhinoUtil.getArgumentCount(function);
        if (argCount < 2) {
            System.err.print("Unable to register transformer for '" + name + "' as it doesn't have at least 2 arguments");
            return;
        }

        // TODO: Add transformer to list
    }

    public static void register(Scriptable scope) {
        RegisterTransformer.register(scope);
    }

    private static final class RegisterTransformer {

        private static final String FUNCTION_NAME = "registerTransformer";
        private static Method METHOD;

        static {
            try {
                METHOD = ScriptFunctions.class.getMethod("registerTransformer", String.class, NativeFunction.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        private static void register(Scriptable scope) {
            FunctionObject function = new FunctionObject(FUNCTION_NAME, METHOD, scope);
            ScriptableObject.putProperty(scope, FUNCTION_NAME, function);
        }
    }
}
