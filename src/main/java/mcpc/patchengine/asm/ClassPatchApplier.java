package mcpc.patchengine.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import mcpc.patchengine.api.IPatch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import com.google.common.collect.HashMultimap;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IClassTransformer;

public class ClassPatchApplier implements IClassTransformer {
    private static ClassPatchApplier _instance;
    private static File _debugFolder = new File("debug").getAbsoluteFile();
    private HashMultimap<String, IPatch> _patches = HashMultimap.create();

    public ClassPatchApplier() {
        _instance = this;
    }

    public void addPatch(IPatch patch) {
        for (String className : patch.getClassNames()) {
            _patches.put(className, patch);
        }
    }

    public byte[] transform(String name, byte[] bytes) {
        if (EngineCorePlugin.isEnabled()) {
            if (_patches.containsKey(name)) {
                Set<IPatch> patches = _patches.get(name);

                FMLLog.fine("[ClassPatcher] Found %s patches for \"%s\"", patches.size(), name);

                ClassNode classNode = new ClassNode();
                ClassReader classReader = new ClassReader(bytes);

                classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

                for (IPatch patch : patches) {
                    patch.transform(name, classNode);
                }

                try {
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                    ClassVisitor visitor = classWriter;

                    PrintWriter printWriter = null;

                    if (EngineCorePlugin.isDebug()) {
                        if (!_debugFolder.exists()) {
                            _debugFolder.mkdirs();
                        }

                        printWriter = new PrintWriter(new FileWriter(new File(_debugFolder, name.replace('/', '.') + ".log")));

                        visitor = new TraceClassVisitor(visitor, printWriter);
                        visitor = new CheckClassAdapter(visitor);
                    }

                    classNode.accept(visitor);

                    if (printWriter != null) {
                        printWriter.close();
                    }

                    bytes = classWriter.toByteArray();

                    if (EngineCorePlugin.isDebug()) {
                        FileOutputStream output = new FileOutputStream(new File(_debugFolder, name.replace('/', '.') + ".class"));

                        output.write(bytes);

                        output.flush();
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bytes;
    }

    public static ClassPatchApplier instance() {
        return _instance;
    }
}
