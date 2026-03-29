package ru.tkhapchaev.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class ProfilerClassFileTransformer implements ClassFileTransformer {
    private static final String APP_PACKAGE_PREFIX = "ru/tkhapchaev/";
    private static final String PROFILER_PACKAGE_PREFIX = "ru/tkhapchaev/profiler/";

    @Override
    public byte[] transform(
        Module module,
        ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain,
        byte[] classfileBuffer
    ) throws IllegalClassFormatException {
        if (!shouldInstrument(className)) {
            return null;
        }

        try {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassVisitor classVisitor = new ProfilerClassVisitor(classWriter, className);
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

            return classWriter.toByteArray();
        } catch (Throwable throwable) {
            System.err.printf("Profiler skipped class %s: %s%n", className, throwable.getMessage());

            return null;
        }
    }

    private boolean shouldInstrument(String className) {
        if (className == null || !className.startsWith(APP_PACKAGE_PREFIX)) {
            return false;
        }

        return !className.startsWith(PROFILER_PACKAGE_PREFIX);
    }
}