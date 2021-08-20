package com.wzc.findview.gradle.plugin.launch.core;


import com.wzc.findview.gradle.plugin.launch.utils.Setting;

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
     * 表示 ASM 开始扫描这个方法
     */
    @Override
    public void visitCode() {
        super.visitCode();
    }

    /**
     * 进入这个方法
     */
    @Override
    protected void onMethodEnter() {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("onMethodEnter");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitMethodInsn(INVOKESTATIC, "com/wzc/findview/api/BindViewHelper", "loadSwitch", "()V", false);
    }

    /**
     * 即将从这个方法退出
     * @param opcode
     */

    @Override
    protected void onMethodExit(int opcode) {
        if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("onMethodExit");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
    }

    /**
     * 方法中扫描到的visitInsn指令
     * @param opcode
     */
    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    /**
     * 该方法是 visitEnd 之前调用的方法，可以反复调用。用以确定类方法在执行时候的堆栈大小。
     * @param maxStack
     * @param maxLocals
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack+16, maxLocals);
    }

    /**
     * 表示方法扫码完毕
     */
    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
