package patchengine;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;


public class PatchEngineModContainer extends DummyModContainer {

    public PatchEngineModContainer() {
        super(new ModMetadata());
        ModMetadata metadata = getMetadata();
        metadata.modId = "PatchEngine";
        metadata.name = "PatchEngine";
        metadata.description = "A coremod for manipulating class files using ASM using javascript.";
        metadata.version = PatchEngine.VERSION;
        metadata.url = "https://github.com/MazeXD/PatchEngine";
        metadata.authorList = ImmutableList.of("MazeXD");
        metadata.credits = "MazeXD";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
