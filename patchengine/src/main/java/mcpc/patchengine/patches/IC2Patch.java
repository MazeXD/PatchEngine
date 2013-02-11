package mcpc.patchengine.patches;

import static org.objectweb.asm.Opcodes.*;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

import cpw.mods.fml.common.FMLLog;

import mcpc.patchengine.api.IPatch;
import mcpc.patchengine.asm.util.ClassUtil;
import mcpc.patchengine.common.Configuration;

public class IC2Patch implements IPatch {
    private boolean _enabled = true;

    @Override
    public String[] getClassNames() {
        return new String[] { "ic2.core.IC2" };
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
    }

    private void patchIC2(ClassNode node) {
        @SuppressWarnings("serial")
        Map<String, Boolean> booleanFields = new HashMap<String, Boolean>() {
            {
                put("enableCraftingQuantum", true);
                put("enableCraftingNano", true);
                put("enableCraftingNanoSaber", true);
                put("enableCraftingJetpacks", true);
                put("enableCraftingLappack", true);
                put("enableCraftingMachines", true);
                put("enableCraftingBatBox", true);
                put("enableCraftingMFE", true);
                put("enableCraftingMFSU", true);
                put("enableCraftingDrill", true);
                put("enableCraftingDiamondDrill", true);
                put("enableCraftingEnergyCrystal", true);
                put("enableCraftingLapotronCrystal", true);
                put("enableCraftingREBattery", true);
                put("enableCraftingElCircuit", true);
                put("enableCraftingAdCircuit", true);
                put("enableCraftingIridiumPlate", true);
                put("enableCraftingOVScanner", true);
                put("enableCraftingDynamite", true);
                put("enableCraftingLaser", true);
                put("enableMiner", true);
                put("losslessWrench", false);
            }
        };

        // Add custom fields
        for (Map.Entry<String, Boolean> entry : booleanFields.entrySet()) {
            if (!ClassUtil.addField(node, ACC_PUBLIC + ACC_STATIC, "patch_" + entry.getKey(), entry.getValue())) {
                FMLLog.warning("[PatchEngine] Failed to add field patch_%s", entry.getKey());
                return;
            }
        }

        /*
         * for (String field : customFields) { node.fields.add(new
         * FieldNode(ACC_PUBLIC + ACC_STATIC, "patch_" + field, "Z", null,
         * null)); }
         * 
         * // Set default value of fields MethodNode method =
         * ASMUtil.getMethod(node, "<clinit>"); if (method != null) { InsnList
         * instructions = new InsnList();
         * 
         * for (String field : customFields) {
         * ASMUtil.setDefaultFieldValue(instructions, className, "patch_" +
         * field, field.equals("losslessWrench") ? false : true); }
         * 
         * method.instructions.insertBefore(method.instructions.getLast(),
         * instructions); }
         * 
         * // Add configuration properties method = ASMUtil.getMethod(node,
         * "load", "(Lcpw/mods/fml/common/event/FMLPreInitializationEvent;)V");
         * if (method != null) { LabelNode label =
         * ASMUtil.getLabelWithInsn(method, new MethodInsnNode(INVOKEVIRTUAL,
         * "net/minecraftforge/common/Configuration", "save", "()V"));
         * 
         * if (label != null) { AbstractInsnNode configInstruction = new
         * VarInsnNode(ALOAD, 4); InsnList instructions = new InsnList();
         * 
         * for (String field : customFields) {
         * ASMUtil.addBooleanProperty(instructions, configInstruction, "Patch",
         * field, className, "patch_" + field); }
         * 
         * method.instructions.insertBefore(label, instructions); } }
         * 
         * // Insert custom conditions method = ASMUtil.getMethod(node,
         * "registerCraftingRecipes"); if (method != null) { MethodInsnNode
         * addRecipeNode = new MethodInsnNode(INVOKESTATIC,
         * "ic2/api/Ic2Recipes", "addCraftingRecipe",
         * "(Lur;[Ljava/lang/Object;)V");
         * 
         * // enableCraftingQuantum ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "quantumHelmet", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "quantumBoots", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingQuantum");
         * 
         * // enableCraftingNano ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "nanoHelmet", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "nanoBoots", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingNano");
         * 
         * // enableCraftingNanoSaber ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "nanoSaber", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "nanoSaber", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingNanoSaber");
         * 
         * // enableCraftingJetpacks ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getPreviousLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "electricJetpack",
         * "Lur;"), addRecipeNode, false, false)),
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "electricJetpack", "Lur;"), addRecipeNode,
         * false, true), className, "patch_enableCraftingJetpacks");
         * 
         * // enableCraftingLappack ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "lapPack", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "lapPack", "Lur;"), addRecipeNode, false, true),
         * className, "patch_enableCraftingLappack");
         * 
         * // enableCraftingMachines ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "ironFurnace", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "electrolyzer", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingMachines");
         * ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "personalSafe", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "reactorChamber", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingMachines");
         * 
         * // enableCraftingBatBox ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "batBox", "Lur;"), addRecipeNode, false, false),
         * ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "batBox", "Lur;"),
         * addRecipeNode, false, true)), className,
         * "patch_enableCraftingBatBox");
         * 
         * // enableCraftingMFE ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "mfeUnit", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "mfeUnit", "Lur;"), addRecipeNode, false, true),
         * className, "patch_enableCraftingMFE");
         * 
         * // enableCraftingMFSU ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "mfsUnit", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "mfsUnit", "Lur;"), addRecipeNode, false, true),
         * className, "patch_enableCraftingMFSU");
         * 
         * // enableCraftingDrill ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "miningDrill", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "miningDrill", "Lur;"),
         * addRecipeNode, false, true)), className,
         * "patch_enableCraftingDrill");
         * 
         * // enableCraftingDiamondDrill ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "diamondDrill", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "diamondDrill",
         * "Lur;"), addRecipeNode, false, true)), className,
         * "patch_enableCraftingDiamondDrill");
         * 
         * // enableCraftingEnergyCrystal ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "energyCrystal", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "energyCrystal",
         * "Lur;"), addRecipeNode, false, true)), className,
         * "patch_enableCraftingEnergyCrystal");
         * 
         * // enableCraftingLapotronCrystal
         * ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "lapotronCrystal", "Lur;"), addRecipeNode,
         * false, false), ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "lapotronCrystal",
         * "Lur;"), addRecipeNode, false, true), className,
         * "patch_enableCraftingLapotronCrystal");
         * 
         * // enableCraftingREBattery ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "reBattery", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "reBattery", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingREBattery");
         * 
         * // enableCraftingElCircuit ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "electronicCircuit", "Lur;"), addRecipeNode,
         * false, false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method,
         * new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items",
         * "electronicCircuit", "Lur;"), addRecipeNode, false, true)),
         * className, "patch_enableCraftingElCircuit");
         * 
         * // enableCraftingAdCircuit ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "advancedCircuit", "Lur;"), addRecipeNode,
         * false, false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method,
         * new FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "advancedCircuit",
         * "Lur;"), addRecipeNode, false, true)), className,
         * "patch_enableCraftingAdCircuit");
         * 
         * // enableCraftingIridiumPlate ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "iridiumPlate", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "iridiumPlate",
         * "Lur;"), addRecipeNode, false, true)), className,
         * "patch_enableCraftingIridiumPlate");
         * 
         * // enableCraftingOVScanner ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "ovScanner", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "ovScanner", "Lur;"),
         * addRecipeNode, false, true)), className,
         * "patch_enableCraftingOVScanner");
         * 
         * // enableCraftingDynamite ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "stickyDynamite", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getNextLabel(ASMUtil.getLabelWithInsn(method, new
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "stickyDynamite",
         * "Lur;"), addRecipeNode, false, true)), className,
         * "patch_enableCraftingDynamite");
         * 
         * // enableCraftingLaser ASMUtil.wrapInsnWithCondition(method,
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "miningLaser", "Lur;"), addRecipeNode, false,
         * false), ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC,
         * "ic2/core/Ic2Items", "miningLaser", "Lur;"), addRecipeNode, false,
         * true), className, "patch_enableCraftingLaser");
         * 
         * // enableMiner - TODO // ASMUtil.wrapInsnWithCondition(method, //
         * ASMUtil.getLabelWithInsn(method, new FieldInsnNode(GETSTATIC, //
         * "ic2/core/Ic2Items", "lapPack", "Lur;"), addRecipeNode, false, //
         * false), ASMUtil.getLabelWithInsn(method, new //
         * FieldInsnNode(GETSTATIC, "ic2/core/Ic2Items", "lapPack", "Lur;"), //
         * addRecipeNode, false, true), className, "enableMiner");
         * 
         * // losslessWrench }
         */

        FMLLog.fine("[PatchEngine] Patched IC2 class");
    }
}