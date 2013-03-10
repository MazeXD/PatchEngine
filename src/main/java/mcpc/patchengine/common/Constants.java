package mcpc.patchengine.common;

import java.io.File;

public class Constants {
    // TODO: Dynamic NMS detection
    public static String CraftBukkitNMS = "v1_4_R1";
    
    public static String WorldClass = "yc";
    public static String EntityClass = "lq";
    public static String ChunkProviderGenerateClass = "abb";
    public static String WorldServerClass = "in";
    public static String ChunkProviderInterface = "zw";
    public static String ItemStackClass = "ur";
    public static String DamageSourceClass = "lh";
    public static String EntityTNTPrimedClass = "pz";
    public static String EntityPlayerMPClass = "iq";
    
    public static void load(File configFolder)
    {
         Configuration config = new Configuration(new File(configFolder, "constants.cfg"));
         config.load();
         
         CraftBukkitNMS = config.get("craftbukkit", "namespace", CraftBukkitNMS).value;

         WorldClass = config.get("minecraft", "worldClass", WorldClass).value;
         EntityClass = config.get("minecraft", "entityClass", EntityClass).value;
         ChunkProviderGenerateClass = config.get("minecraft", "chunkProviderGenerateClass", ChunkProviderGenerateClass).value;
         WorldServerClass = config.get("minecraft", "worldServerClass", WorldServerClass).value;
         ChunkProviderInterface = config.get("minecraft", "chunkProviderInterface", ChunkProviderInterface).value;
         ItemStackClass = config.get("minecraft", "itemStackClass", ItemStackClass).value;
         DamageSourceClass = config.get("minecraft", "damageSourceClass", DamageSourceClass).value;
         EntityTNTPrimedClass = config.get("minecraft", "entityTNTPrimedClass", EntityTNTPrimedClass).value;
         EntityPlayerMPClass = config.get("minecraft", "entityPlayerMPClass", EntityPlayerMPClass).value;
         
         config.addCustomCategoryComment("craftbukkit", "DO NOT CHANGE EXCEPT YOU KNOW WHAT YOU ARE DOING");
         config.addCustomCategoryComment("minecraft", "DO NOT CHANGE EXCEPT YOU KNOW WHAT YOU ARE DOING");
         
         config.save();
    }
}
