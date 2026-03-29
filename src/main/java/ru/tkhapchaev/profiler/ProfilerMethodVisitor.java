package ru.tkhapchaev.profiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;

public class ProfilerMethodVisitor extends AdviceAdapter {
    private static final Type PROFILER_RUNTIME_TYPE = Type.getType(ProfilerRuntime.class);
    private static final Method ENTER_METHOD = Method.getMethod("void enter(String)");
    private static final Method EXIT_METHOD = Method.getMethod("void exit(String)");

    private final String methodId;
    private final Label startFinally = new Label();
    private final Label endFinally = new Label();

    public ProfilerMethodVisitor(
        MethodVisitor methodVisitor,
        int access,
        String name,
        String descriptor,
        String methodId
    ) {
        super(Opcodes.ASM9, methodVisitor, access, name, descriptor);

        this.methodId = methodId;
    }

    @Override
    public void visitCode() {
        super.visitCode();

        mark(startFinally);
    }

    @Override
    protected void onMethodEnter() {
        push(methodId);
        invokeStatic(PROFILER_RUNTIME_TYPE, ENTER_METHOD);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == ATHROW) {
            return;
        }

        push(methodId);
        invokeStatic(PROFILER_RUNTIME_TYPE, EXIT_METHOD);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        mark(endFinally);
        catchException(startFinally, endFinally, Type.getType(Throwable.class));

        int throwableSlot = newLocal(Type.getType(Throwable.class));
        storeLocal(throwableSlot);

        push(methodId);
        invokeStatic(PROFILER_RUNTIME_TYPE, EXIT_METHOD);

        loadLocal(throwableSlot);
        throwException();

        super.visitMaxs(maxStack, maxLocals);
    }
}