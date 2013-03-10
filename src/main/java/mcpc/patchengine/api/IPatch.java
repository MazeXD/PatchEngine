package mcpc.patchengine.api;

import mcpc.patchengine.common.Configuration;

import org.objectweb.asm.tree.ClassNode;

public interface IPatch {
    public String[] getClassNames();

    public void loadConfigurations(Configuration configuration);

    public void transform(String name, ClassNode node);
}
