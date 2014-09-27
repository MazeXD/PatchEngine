package patchengine.scripting;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import patchengine.config.ConfigHandler;
import patchengine.scripting.objects.ConfigWrapper;


public class ScriptContext extends ScriptableObject {

    private static final long serialVersionUID = 1L;

    public ScriptContext(Scriptable scope, String category, ConfigHandler configHandler) {
        ConfigWrapper config = new ConfigWrapper(category, configHandler);

        setPrototype(getObjectPrototype(scope));
        setParentScope(scope);

        ScriptableObject.defineProperty(scope, "context", this, ScriptableObject.DONTENUM);
        ScriptableObject.defineProperty(this, "config", config, ScriptableObject.READONLY | ScriptableObject.PERMANENT);
    }

    @Override
    public String getClassName() {
        return "ScriptContext";
    }
}
