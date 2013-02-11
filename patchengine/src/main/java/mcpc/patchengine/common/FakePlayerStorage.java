package mcpc.patchengine.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.bukkit.craftbukkit.entity.CraftFakePlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

public class FakePlayerStorage {
    public static EntityPlayerMP get(World world, String username) {
        return CraftFakePlayer.get(world, new FakePlayerStorage.ModFakePlayer(world, username));
    }

    public static CraftPlayer getBukkitEntity(World world, String username) {
        return CraftFakePlayer.getBukkitEntity(world, new FakePlayerStorage.ModFakePlayer(world, username));
    }

    public static class ModFakePlayer extends EntityPlayer {
        public ModFakePlayer(World world, String username) {
            super(world);

            this.username = username;
        }

        @Override
        public boolean canCommandSenderUseCommand(int arg0, String arg1) {
            return false;
        }

        @Override
        public ChunkCoordinates getPlayerCoordinates() {
            return null;
        }

        @Override
        public void sendChatToPlayer(String arg0) {
        }
    }
}
