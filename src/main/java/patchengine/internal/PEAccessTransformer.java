package patchengine.internal;

import com.google.common.io.CharSource;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.lang.reflect.Method;


public class PEAccessTransformer extends AccessTransformer {

    public PEAccessTransformer() throws IOException {
        super("patchengine_at.cfg");

        loadAccessTransformers();
    }

    private void loadAccessTransformers() {
        Method method = ReflectionHelper.findMethod(AccessTransformer.class, this, new String[] {"processATFile"}, CharSource.class);
        System.out.println(method);

        // TODO: Applying of script define access transformers
    }
}
