package com.wzc.findview.gradle.plugin.launch.core;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * AdviceAdapter API：https://asm.ow2.io/javadoc/org/objectweb/asm/commons/AdviceAdapter.html
 */
public class FindViewCodeGenerator extends AdviceAdapter {


    /**
     * Constructs a new {@link AdviceAdapter}.
     *
     * @param api           the ASM API version implemented by this visitor. Must be one of {@link
     *                      Opcodes#ASM4}, {@link Opcodes#ASM5}, {@link Opcodes#ASM6} or {@link Opcodes#ASM7}.
     * @param methodVisitor the method visitor to which this adapter delegates calls.
     * @param access        the method's access flags (see {@link Opcodes}).
     * @param name          the method's name.
     * @param descriptor    the method's descriptor (see {@link Type Type}).
     */
    protected FindViewCodeGenerator(int api, MethodVisitor methodVisitor, int access, String name, String descriptor) {
        super(api, methodVisitor, access, name, descriptor);
    }

    /**
     * 进入方法时插入字节码
     */
    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
    }

    /**
     * 退出方法前可插入字节码
     * @param opcode
     */

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
    }

    /**
     * 访问操作数指令
     * @param opcode
     */
    @Override
    public void visitInsn(int opcode) {
        System.out.println("FindViewCodeGenerator>>>visitInsn>>>opcode="+ opcode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        System.out.println("FindViewCodeGenerator>>>visitVarInsn>>>opcode="+ opcode + ", var=" + var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("hahaha");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }
}
