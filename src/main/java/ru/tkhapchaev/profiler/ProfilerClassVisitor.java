package ru.tkhapchaev.profiler;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ProfilerClassVisitor extends ClassVisitor {
    private final String className;

    public ProfilerClassVisitor(ClassVisitor classVisitor, String className) {
        super(Opcodes.ASM9, classVisitor);

        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(
        int access,
        String name,
        String descriptor,
        String signature,
        String[] exceptions
    ) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (methodVisitor == null || shouldSkipMethod(access, name)) {
            return methodVisitor;
        }

        String methodId = className.replace('/', '.') + "#" + name + descriptor;

        return new ProfilerMethodVisitor(methodVisitor, access, name, descriptor, methodId);
    }

    private boolean shouldSkipMethod(int access, String name) {
        if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0) {
            return true;
        }

        return "<clinit>".equals(name) || "<init>".equals(name);
    }
}