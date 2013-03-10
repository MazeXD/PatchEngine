package mcpc.patchengine.patches;

import static org.objectweb.asm.Opcodes.*;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.FMLLog;

import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.asm.util.ClassUtil;
import mcpc.patchengine.asm.util.ConfigurationUtil;
import mcpc.patchengine.asm.util.MethodUtil;
import mcpc.patchengine.asm.util.NavigationUtil;
import mcpc.patchengine.asm.util.SeekUtil;
import mcpc.patchengine.common.Configuration;
import mcpc.patchengine.common.Constants;

public class IC2Patch implements IPatch {
    private boolean _enabled = true;

    @Override
    public String[] getClassNames() {
        return new String[] { "ic2.core.IC2", "ic2.core.ExplosionIC2" };
    }

    @Override
    public void loadConfigurations(Configuration configuration) {
        _enabled = configuration.get("ic2", "enabled", _enabled).getBoolean(_enabled);
    }

    @Override
    public void transform(String name, ClassNode node) {
        if (!_enabled) {
            return;
        }

        if (name.equals("ic2.core.IC2")) {
            patchIC2(node);
        }
        
        if (name.equals("ic2.core.ExplosionIC2"))
        {
            patchExplosionIC2(node);
        }
    }

    private void patchIC2(ClassNode node) {
        Map<String, Boolean> booleanFields = new HashMap<String, Boolean>();

        booleanFields.put("enableCraftingQuantum", true);
        booleanFields.put("enableCraftingNano", true);
        booleanFields.put("enableCraftingNanoSaber", true);
        booleanFields.put("enableCraftingJetpacks", true);
        booleanFields.put("enableCraftingLappack", true);
        booleanFields.put("enableCraftingMachines", true);
        booleanFields.put("enableCraftingBatBox", true);
        booleanFields.put("enableCraftingMFE", true);
        booleanFields.put("enableCraftingMFSU", true);
        booleanFields.put("enableCraftingDrill", true);
        booleanFields.put("enableCraftingDiamondDrill", true);
        booleanFields.put("enableCraftingEnergyCrystal", true);
        booleanFields.put("enableCraftingLapotronCrystal", true);
        booleanFields.put("enableCraftingREBattery", true);
        booleanFields.put("enableCraftingElCircuit", true);
        booleanFields.put("enableCraftingAdCircuit", true);
        booleanFields.put("enableCraftingIridiumPlate", true);
        booleanFields.put("enableCraftingOVScanner", true);
        booleanFields.put("enableCraftingDynamite", true);
        booleanFields.put("enableCraftingLaser", true);
        booleanFields.put("enableCraftingMiner", true);
        booleanFields.put("losslessWrench", false);
        
        // Add custom fields
        for (Map.Entry<String, Boolean> entry : booleanFields.entrySet()) {
            if (!ClassUtil.addField(node, ACC_PUBLIC + ACC_STATIC, "patch_" + entry.getKey(), entry.getValue())) {
                FMLLog.warning("[PatchEngine - IC2] Failed to add field patch_%s", entry.getKey());
                return;
            }
        }

        ClassUtil.addField(node, ACC_PUBLIC + ACC_STATIC, "patch_energyPerDamage", 900);
        ClassUtil.addField(node, ACC_PUBLIC + ACC_STATIC, "patch_quantumRatio", 1.0D);
        
        // Insert configuration calls
        MethodNode method = ClassUtil.getMethod(node, "load", "(Lcpw/mods/fml/common/event/FMLPreInitializationEvent;)V");
        if(method == null)
        {
            return;
        }
        
        InsnList instructions = new InsnList();
        
        AbstractInsnNode configInstruction = new VarInsnNode(ALOAD, MethodUtil.getLocalVariable(method, "config", "Lnet/minecraftforge/common/Configuration;").index);
        for (String field : booleanFields.keySet()) {
            ConfigurationUtil.addBooleanProperty(instructions, configInstruction, "patch", field, node.name + ".patch_" + field);
        }
        
        ConfigurationUtil.addIntegerProperty(instructions, configInstruction, "patch", "energyPerDamage", "Default 900", node.name + ".patch_energyPerDamage");
        ConfigurationUtil.addDoubleProperty(instructions, configInstruction, "patch", "quantumRatio", "0 = 0% - 0.9 = 90% - 1.0 = 100% - Default: 1.0", node.name + ".patch_quantumRatio");
        
        int index = SeekUtil.searchInsn(method.instructions, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/common/Configuration", "save", "()V"));
        if(index == -1)
        {
            return;
        }
        
        LabelNode label = NavigationUtil.getPreviousLabel(method.instructions, index);
        
        method.instructions.insertBefore(label, instructions);
        
        // Insert custom conditions
        method = ClassUtil.getMethod(node, "registerCraftingRecipes");
        if(method == null)
        {
            FMLLog.warning("[PatchEngine - IC2] Failed to get method 'registerCraftingRecipes'");
            return;
        }
        
        AbstractInsnNode addRecipeNode = new MethodInsnNode(INVOKESTATIC, "ic2/api/Ic2Recipes", "addCraftingRecipe", String.format("(L%s;[Ljava/lang/Object;)V", Constants.ItemStackClass));
        
        // enableCraftingQuantum
        LabelNode startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "quantumHelmet", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        LabelNode endLabel = NavigationUtil.getNextLabel(MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "quantumBoots", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode));
        
        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingQuantum", "Z"));
        }
        
        // enableCraftingNano
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "nanoHelmet", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "nanoBoots", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode));
        
        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingNano", "Z"));
        }
        
        // enableCraftingNanoSaber
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "nanoSaber", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingNanoSaber", "Z"));
        }

        // enableCraftingJetpacks
        LabelNode temp = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "electricJetpack", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        startLabel = NavigationUtil.getPreviousLabel(temp);
        endLabel = NavigationUtil.getNextLabel(temp);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingJetpacks", "Z"));
        }

        // enableCraftingLappack
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "lapPack", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingLappack", "Z"));
        }
        
        // enableCraftingMachines
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "ironFurnace", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "electrolyzer", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingMachines", "Z"));
        }

        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "personalSafe", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "reactorChamber", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingMachines", "Z"));
        }

        // enableCraftingBatBox
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "batBox", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingBatBox", "Z"));
        }
        
        // enableCraftingMFE
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "mfeUnit", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingMFE", "Z"));
        }

        // enableCraftingMFSU
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "mfsUnit", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingMFSU", "Z"));
        }

        // enableCraftingDrill
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "miningDrill", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingDrill", "Z"));
        }

        // enableCraftingDiamondDrill
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "diamondDrill", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingDiamondDrill", "Z"));
        }

        // enableCraftingEnergyCrystal
        temp = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "energyCrystal", String.format("L%s;", Constants.ItemStackClass)), new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "industrialDiamond", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        startLabel = NavigationUtil.getPreviousLabel(temp);
        endLabel = NavigationUtil.getNextLabel(temp);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingEnergyCrystal", "Z"));
        }

        // enableCraftingLapotronCrystal
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "lapotronCrystal", String.format("L%s;", Constants.ItemStackClass)), new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "energyCrystal", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingLapotronCrystal", "Z"));
        }

        // enableCraftingREBattery
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "reBattery", String.format("L%s;", Constants.ItemStackClass)), new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "insulatedCopperCableItem", String.format("L%s;", Constants.ItemStackClass)), new LdcInsnNode("ingotTin"), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingREBattery", "Z"));
        }

        // enableCraftingElCircuit
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "electronicCircuit", String.format("L%s;", Constants.ItemStackClass)), new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "insulatedCopperCableItem", String.format("L%s;", Constants.ItemStackClass)), new LdcInsnNode("ingotRefinedIron"), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingElCircuit", "Z"));
        }

        // enableCraftingAdCircuit
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "advancedCircuit", String.format("L%s;", Constants.ItemStackClass)), new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "electronicCircuit", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingAdCircuit", "Z"));
        }

        // enableCraftingIridiumPlate
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "iridiumPlate", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingIridiumPlate", "Z"));
        }

        // enableCraftingOVScanner
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "ovScanner", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingOVScanner", "Z"));
        }

        // enableCraftingDynamite
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "stickyDynamite", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(NavigationUtil.getNextLabel(startLabel));

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingDynamite", "Z"));
        }

        // enableCraftingLaser
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "miningLaser", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingLaser", "Z"));
        }

        // enableMiner
        startLabel = MethodUtil.getLabelWithInsnList(method, new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "miner", String.format("L%s;", Constants.ItemStackClass)), addRecipeNode);
        endLabel = NavigationUtil.getNextLabel(startLabel);

        if(startLabel != null && endLabel != null)
        {
            MethodUtil.insertConditionTrue(method, startLabel, endLabel, new FieldInsnNode(GETSTATIC, node.name, "patch_enableCraftingMiner", "Z"));
        }

        FMLLog.fine("[PatchEngine - IC2] Patched IC2 class");
    }
    
    @SuppressWarnings("unchecked")
    private void patchExplosionIC2(ClassNode node)
    {
        // Add player field
        node.fields.add(new FieldNode(ACC_PRIVATE, "player", "Lorg/bukkit/entity/Player;", null, null));
        
        // Modify constructor
        MethodNode method = ClassUtil.getMethod(node, "<init>", String.format("(L%s;L%s;DDDFFFL%s;)V", Constants.WorldClass, Constants.EntityClass, Constants.DamageSourceClass));
        if(method == null)
        {
            FMLLog.warning("[PatchEngine - IC2] Failed to get constructor of ExplosionIC2");
            return;
        }
        
        InsnList instructions = new InsnList();
        
        LabelNode label = new LabelNode();
        LabelNode label2 = new LabelNode();
        
        int thisIndex = MethodUtil.getLocalVariable(method, "this", "*").index;
        int worldIndex = MethodUtil.getLocalVariable(method, "world", "*").index;
        int entityIndex = MethodUtil.getLocalVariable(method, "entity", "*").index;
        
        instructions.add(new VarInsnNode(ALOAD, entityIndex));
        instructions.add(new JumpInsnNode(IFNULL, label));

        instructions.add(new VarInsnNode(ALOAD, entityIndex));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Constants.EntityClass, "getBukkitEntity", String.format("()Lorg/bukkit/craftbukkit/%s/entity/CraftEntity;", Constants.CraftBukkitNMS)));
        instructions.add(new TypeInsnNode(INSTANCEOF, "org/bukkit/entity/Player"));
        instructions.add(new JumpInsnNode(IFEQ, label2));

        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new VarInsnNode(ALOAD, entityIndex));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Constants.EntityClass, "getBukkitEntity", String.format("()Lorg/bukkit/craftbukkit/%s/entity/CraftEntity;", Constants.CraftBukkitNMS)));
        instructions.add(new TypeInsnNode(CHECKCAST, "org/bukkit/entity/Player"));
        instructions.add(new FieldInsnNode(PUTFIELD, node.name, "player", "Lorg/bukkit/entity/Player;"));
        instructions.add(new JumpInsnNode(GOTO, label));
        
        instructions.add(label2);
        instructions.add(new VarInsnNode(ALOAD, worldIndex));
        instructions.add(new MethodInsnNode(INVOKESTATIC, String.format("org/bukkit/craftbukkit/%s/entity/CraftFakePlayer", Constants.CraftBukkitNMS), "getBukkitEntity", String.format("(L%s;)Lorg/bukkit/craftbukkit/%s/entity/CraftPlayer", Constants.WorldClass, Constants.CraftBukkitNMS)));
        instructions.add(new InsnNode(POP));
        
        instructions.add(label);
        
        method.instructions.insert(NavigationUtil.getPreviousLabel(MethodUtil.getLastLabel(method)), instructions);

        // Modify doExplosion
        method = ClassUtil.getMethod(node, "doExplosion");
        if(method == null)
        {
            FMLLog.warning("[PatchEngine - IC2] Can't find doExplosion() in ExplosionIC2");
            return;
        }

        // Add local variables
        
        
        instructions = new InsnList();
        
        LabelNode label0 = new LabelNode(); // L16
        LabelNode label1 = new LabelNode(); // L17
        label2 = new LabelNode(); // L18
        LabelNode label3 = new LabelNode(); // L19
        LabelNode label4 = new LabelNode(); // L20
        LabelNode label5 = new LabelNode(); // L21
        LabelNode label6 = new LabelNode(); // L23
        LabelNode label7 = new LabelNode(); // L24
        LabelNode label8 = new LabelNode(); // L25
        LabelNode label9 = new LabelNode(); // L26
        LabelNode label10 = new LabelNode(); // L22
        LabelNode label11 = new LabelNode(); // L27
        LabelNode label12 = new LabelNode(); // L28
        LabelNode label13 = new LabelNode(); // L29
        LabelNode label14 = new LabelNode(); // L31
        LabelNode label15 = new LabelNode(); // L32
        LabelNode label16 = new LabelNode(); // L33
        LabelNode label17 = new LabelNode(); // L34
        LabelNode label18 = new LabelNode(); // L35
        LabelNode label19 = new LabelNode(); // L36
        LabelNode label20 = new LabelNode(); // L30
        LabelNode label21 = new LabelNode(); // L37
        LabelNode label22 = new LabelNode(); // L38
        LabelNode label23 = new LabelNode(); // L40
        LabelNode label24 = new LabelNode(); // L41
        LabelNode label25 = new LabelNode(); // L42
        LabelNode label26 = new LabelNode(); // L43
        LabelNode label27 = new LabelNode(); // L44
        LabelNode label28 = new LabelNode(); // L44
        
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "exploder", "Llq;"));
        instructions.add(new JumpInsnNode(IFNONNULL, label0));
        instructions.add(new TypeInsnNode(NEW, "pz"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "worldObj", "Lyc;"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "pz", "<init>", "(Lyc;)V"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "pz", "getBukkitEntity", "()Lorg/bukkit/craftbukkit/v1_4_R1/entity/CraftEntity;"));
        instructions.add(new JumpInsnNode(GOTO, label1));

        instructions.add(label0);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "exploder", "Llq;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "lq", "getBukkitEntity", "()Lorg/bukkit/craftbukkit/v1_4_R1/entity/CraftEntity;"));

        instructions.add(label1);
        instructions.add(new VarInsnNode(ASTORE, 5));

        instructions.add(label2);
        instructions.add(new TypeInsnNode(NEW, "org/bukkit/Location"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "worldObj", "Lyc;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "yc", "getWorld", "()Lorg/bukkit/craftbukkit/v1_4_R1/CraftWorld;"));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "explosionX", "D"));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "explosionY", "D"));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "explosionZ", "D"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "org/bukkit/Location", "<init>", "(Lorg/bukkit/World;DDD)V"));
        instructions.add(new VarInsnNode(ASTORE, 6));

        instructions.add(label3);
        instructions.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "destroyedBlockPositions", "Ljava/util/Map;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "size", "()I"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V"));
        instructions.add(new VarInsnNode(ASTORE, 7));

        instructions.add(label4);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "destroyedBlockPositions", "Ljava/util/Map;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "entrySet", "()Ljava/util/Set;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;"));
        instructions.add(new VarInsnNode(ASTORE, 8));

        instructions.add(label5);
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z"));
        instructions.add(new JumpInsnNode(IFEQ, label10));
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;"));
        instructions.add(new VarInsnNode(ASTORE, 9));

        instructions.add(label6);
        instructions.add(new VarInsnNode(ALOAD, 9));
        instructions.add(new TypeInsnNode(CHECKCAST, "java/util/Map$Entry"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map$Entry", "getKey", "()Ljava/lang/Object;"));
        instructions.add(new TypeInsnNode(CHECKCAST, "yv"));
        instructions.add(new VarInsnNode(ASTORE, 10));

        instructions.add(label7);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "worldObj", "Lyc;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "yc", "getWorld", "()Lorg/bukkit/craftbukkit/v1_4_R1/CraftWorld;"));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new FieldInsnNode(GETFIELD, "yv", "a", "I"));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new FieldInsnNode(GETFIELD, "yv", "b", "I"));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new FieldInsnNode(GETFIELD, "yv", "c", "I"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/craftbukkit/v1_4_R1/CraftWorld", "getBlockAt", "(III)Lorg/bukkit/block/Block;"));
        instructions.add(new VarInsnNode(ASTORE, 11));

        instructions.add(label8);
        instructions.add(new VarInsnNode(ALOAD, 11));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/block/Block", "getTypeId", "()I"));
        instructions.add(new JumpInsnNode(IFEQ, label9));
        instructions.add(new VarInsnNode(ALOAD, 7));
        instructions.add(new VarInsnNode(ALOAD, 11));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z"));
        instructions.add(new InsnNode(POP));

        instructions.add(label9);
        instructions.add(new JumpInsnNode(GOTO, label5));

        instructions.add(label10);
        instructions.add(new TypeInsnNode(NEW, "org/bukkit/event/entity/EntityExplodeEvent"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, 5));
        instructions.add(new VarInsnNode(ALOAD, 6));
        instructions.add(new VarInsnNode(ALOAD, 7));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "power", "F"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "org/bukkit/event/entity/EntityExplodeEvent", "<init>", "(Lorg/bukkit/entity/Entity;Lorg/bukkit/Location;Ljava/util/List;F)V"));
        instructions.add(new VarInsnNode(ASTORE, 8));

        instructions.add(label11);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "worldObj", "Lyc;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "yc", "getServer", "()Lorg/bukkit/craftbukkit/v1_4_R1/CraftServer;"));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/craftbukkit/v1_4_R1/CraftServer", "getPluginManager", "()Lorg/bukkit/plugin/PluginManager;"));
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/plugin/PluginManager", "callEvent", "(Lorg/bukkit/event/Event;)V"));

        instructions.add(label12);
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/event/entity/EntityExplodeEvent", "isCancelled", "()Z"));
        instructions.add(new JumpInsnNode(IFEQ, label13));
        instructions.add(new InsnNode(RETURN));

        instructions.add(label13);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "player", "Lorg/bukkit/entity/Player;"));
        instructions.add(new JumpInsnNode(IFNULL, label20));

        instructions.add(label14);
        instructions.add(new VarInsnNode(ALOAD, 7));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;"));
        instructions.add(new VarInsnNode(ASTORE, 10));

        instructions.add(label15);
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z"));
        instructions.add(new JumpInsnNode(IFEQ, label20));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;"));
        instructions.add(new TypeInsnNode(CHECKCAST, "org/bukkit/block/Block"));
        instructions.add(new VarInsnNode(ASTORE, 11));

        instructions.add(label16);
        instructions.add(new TypeInsnNode(NEW, "org/bukkit/event/block/BlockBreakEvent"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, 11));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "player", "Lorg/bukkit/entity/Player;"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "org/bukkit/event/block/BlockBreakEvent", "<init>", "(Lorg/bukkit/block/Block;Lorg/bukkit/entity/Player;)V"));
        instructions.add(new VarInsnNode(ASTORE, 9));

        instructions.add(label17);
        instructions.add(new MethodInsnNode(INVOKESTATIC, "org/bukkit/Bukkit", "getPluginManager", "()Lorg/bukkit/plugin/PluginManager;"));
        instructions.add(new VarInsnNode(ALOAD, 9));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/plugin/PluginManager", "callEvent", "(Lorg/bukkit/event/Event;)V"));

        instructions.add(label18);
        instructions.add(new VarInsnNode(ALOAD, 9));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/event/block/BlockBreakEvent", "isCancelled", "()Z"));
        instructions.add(new JumpInsnNode(IFEQ, label19));
        instructions.add(new InsnNode(RETURN));

        instructions.add(label19);
        instructions.add(new JumpInsnNode(GOTO, label15));

        instructions.add(label20);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "destroyedBlockPositions", "Ljava/util/Map;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "clear", "()V"));

        instructions.add(label21);
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, "org/bukkit/event/entity/EntityExplodeEvent", "blockList", "()Ljava/util/List;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;"));
        instructions.add(new VarInsnNode(ASTORE, 9));

        instructions.add(label22);
        instructions.add(new VarInsnNode(ALOAD, 9));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z"));
        instructions.add(new JumpInsnNode(IFEQ, label28));
        instructions.add(new VarInsnNode(ALOAD, 9));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;"));
        instructions.add(new TypeInsnNode(CHECKCAST, "org/bukkit/block/Block"));
        instructions.add(new VarInsnNode(ASTORE, 10));

        instructions.add(label23);
        instructions.add(new TypeInsnNode(NEW, "yv"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/block/Block", "getX", "()I"));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/block/Block", "getY", "()I"));
        instructions.add(new VarInsnNode(ALOAD, 10));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "org/bukkit/block/Block", "getZ", "()I"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "yv", "<init>", "(III)V"));
        instructions.add(new VarInsnNode(ASTORE, 11));

        instructions.add(label24);
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "destroyedBlockPositions", "Ljava/util/Map;"));
        instructions.add(new VarInsnNode(ALOAD, 11));
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new FieldInsnNode(GETFIELD, "ic2/core/ExplosionIC2", "power", "F"));
        instructions.add(new InsnNode(F2D));
        instructions.add(new LdcInsnNode(8.0));
        instructions.add(new InsnNode(DCMPG));
        instructions.add(new JumpInsnNode(IFGT, label25));
        instructions.add(new InsnNode(ICONST_1));
        instructions.add(new JumpInsnNode(GOTO, label26));

        instructions.add(label25);
        instructions.add(new InsnNode(ICONST_0));

        instructions.add(label26);
        instructions.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));
        instructions.add(new InsnNode(POP));

        instructions.add(label27);
        instructions.add(new JumpInsnNode(GOTO, label22));

        instructions.add(label28);
        
        LabelNode insertAtLabel = MethodUtil.getLabelWithInsnList(method, new LdcInsnNode(3.141592653589793), new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "atan", "(D)D"), new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "ceil", "(D)D"));
        method.instructions.insertBefore(insertAtLabel, instructions);
        
        /*
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "exploder", String.format("L%s;", Constants.EntityClass)));
        instructions.add(new JumpInsnNode(IFNONNULL, label));
        instructions.add(new TypeInsnNode(NEW, Constants.EntityTNTPrimedClass));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "worldObj", String.format("L%s;", Constants.WorldClass)));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, Constants.EntityTNTPrimedClass, "<init>", String.format("(L%s;)V", Constants.WorldClass)));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Constants.EntityTNTPrimedClass, "getBukkitEntity", String.format("()Lorg/bukkit/craftbukkit/%s/entity/CraftEntity;", Constants.CraftBukkitNMS)));
        instructions.add(new JumpInsnNode(GOTO, label2));
        
        instructions.add(label);
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "exploder", String.format("L%s;", Constants.EntityClass)));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Constants.EntityClass, "getBukkitEntity", String.format("()Lorg/bukkit/craftbukkit/%s/entity/CraftEntity;", Constants.CraftBukkitNMS)));
        
        instructions.add(label2);
        instructions.add(new VarInsnNode(ASTORE, 5));
        
        instructions.add(label3);
        instructions.add(new TypeInsnNode(NEW, "org/bukkit/Location"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "worldObj", String.format("L%s;", Constants.WorldClass)));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, Constants.WorldClass, "getWorld", String.format("()Lorg/bukkit/craftbukkit/%s/CraftWorld;", Constants.CraftBukkitNMS)));
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "explosionX", "D"));
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "explosionY", "D"));
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "explosionZ", "D"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "org/bukkit/Location", "<init>", "(Lorg/bukkit/World;DDD)V"));
        instructions.add(new VarInsnNode(ASTORE, locationIndex));
        
        instructions.add(label4);
        instructions.add(new TypeInsnNode(NEW, "java/util/ArrayList"));
        instructions.add(new InsnNode(DUP));
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "destroyedBlockPositions", "Ljava/util/Map;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "size", "()I"));
        instructions.add(new MethodInsnNode(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V"));
        instructions.add(new VarInsnNode(ASTORE, blockListIndex));
        
        instructions.add(label5);
        instructions.add(new VarInsnNode(ALOAD, thisIndex));
        instructions.add(new FieldInsnNode(GETFIELD, node.name, "destroyedBlockPositions", "Ljava/util/Map;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "entrySet", "()Ljava/util/Set;"));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;"));
        instructions.add(new VarInsnNode(ASTORE, interatorIndex));
        
        instructions.add(label6);
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z"));
        instructions.add(new JumpInsnNode(IFEQ, label7));
        instructions.add(new VarInsnNode(ALOAD, 8));
        instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object"));
        */
        FMLLog.fine("[PatchEngine - IC2] Patched ExplosionIC2 class");
    }
}