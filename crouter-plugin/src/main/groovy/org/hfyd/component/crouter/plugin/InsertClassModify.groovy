package org.hfyd.component.crouter.plugin

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class InsertClassModify implements TransformHelper.ClassModify {

    private static final serverRegisterClass = "org/hfyd/component/crouter/core/bus/ServerRegister"
    private static final activityBinderClass = "org/hfyd/component/crouter/core/client/activity/CRouterActivityBinder"

    @Override
    byte[] modify(byte[] srcClass) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new InsertClassVisitor(classWriter)
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES + ClassReader.SKIP_FRAMES)
        return classWriter.toByteArray()
    }

    class InsertClassVisitor extends ClassVisitor {

        public static int ASM_VERSION = Opcodes.ASM6
        private boolean isServerRegisterClass
        private boolean isActivityBinderClass

        InsertClassVisitor(ClassVisitor cv) {
            super(ASM_VERSION, cv)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            if (name == serverRegisterClass) {
                isServerRegisterClass = true
            } else if (name == activityBinderClass) {
                isActivityBinderClass = true
            }
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            Set<String> registerServerControllerClasses = RegisterCache.getAllServerControllers()
            if (isServerRegisterClass && name == 'rejectService' && registerServerControllerClasses.size() > 0) {
                return new InsertServerControllerMethodVisitor(ASM_VERSION, mv, registerServerControllerClasses)
            }

            Map<String,String> activityExtraBinders = RegisterCache.getAllActivityExtraBinders()
            if (isActivityBinderClass && name == 'inject' && activityExtraBinders.size() > 0) {
                return new InsertActivityBinderMethodVisitor(ASM_VERSION, mv, activityExtraBinders)
            }

            return mv
        }

        class InsertActivityBinderMethodVisitor extends MethodVisitor {
            private Map<String,String> activityExtraBinders

            InsertActivityBinderMethodVisitor(int api, MethodVisitor mv, Map<String,String> activityExtraBinders) {
                super(api, mv)
                this.activityExtraBinders = activityExtraBinders
            }

            @Override
            void visitInsn(int opcode) {
                this.activityExtraBinders.each { it ->
                    String activityClassName = it.key.replace("/", ".")
                    String activityExtraBinderClassName = it.value.replace("/", ".")

                    CLog.i("onMethodEnter $activityClassName bindTo $activityExtraBinderClassName")

                    mv.visitLdcInsn(activityClassName)
                    mv.visitLdcInsn(activityExtraBinderClassName)
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "org/hfyd/component/crouter/core/client/activity/CRouterActivityBinder",
                            "register",
                            "(Ljava/lang/String;Ljava/lang/String;)V",
                            false)

                    CLog.i("onMethodEnter end")
                }

                super.visitInsn(opcode)
            }

            @Override
            void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(maxStack + 2, maxLocals)
            }
        }

        class InsertServerControllerMethodVisitor extends MethodVisitor {

            private Set<String> registerClasses

            InsertServerControllerMethodVisitor(int api, MethodVisitor mv, Set<String> registerClasses) {
                super(api, mv)
                this.registerClasses = registerClasses
            }

            @Override
            void visitInsn(int opcode) {
                registerClasses.each { it ->
                    String registerClass = it.replace("/", ".")
                    CLog.i("onMethodEnter $registerClass start")

                    mv.visitLdcInsn(registerClass)
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                            "org/hfyd/component/crouter/core/bus/ServerRegister", "register",
                            "(Ljava/lang/String;)V",
                            false)

                    CLog.i("onMethodEnter $registerClass end")
                }
                super.visitInsn(opcode)
            }

            @Override
            void visitMaxs(int maxStack, int maxLocals) {
                super.visitMaxs(maxStack + 1, maxLocals)
            }
        }
    }
}

