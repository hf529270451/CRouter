package org.hfyd.component.crouter.plugin

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ScanClassModify implements TransformHelper.ClassModify {

    @Override
    byte[] modify(byte[] srcClass) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new ScanClassVisitor(classWriter)
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(classVisitor, ClassReader.EXPAND_FRAMES + ClassReader.SKIP_FRAMES)
        return classWriter.toByteArray()
    }

    class ScanClassVisitor extends ClassVisitor {

        public static int ASM_VERSION = Opcodes.ASM6

        private String mClassName
        private String mSuperName
        private String[] mInterfaces

        private boolean isActivityExtraBinder

        ScanClassVisitor(ClassVisitor cv) {
            super(ASM_VERSION, cv)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            this.mClassName = name
            this.mSuperName = superName
            this.mInterfaces = interfaces

            if (superName == "org/hfyd/component/crouter/core/server/ServerController") {
                RegisterCache.registerServerController(name)
            } else {
                if (interfaces != null && interfaces.size() > 0) {
                    for (String it : interfaces) {
                        if (it == 'org/hfyd/component/crouter/core/client/activity/ActivityExtraBinder') {
                            isActivityExtraBinder = true
                            return
                        }
                    }
                }
            }
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (isActivityExtraBinder) {
                return new MethodVisitor(ASM_VERSION, mv) {
                    @Override
                    AnnotationVisitor visitAnnotation(String annoDesc, boolean visible) {
                        AnnotationVisitor annoMv = super.visitAnnotation(desc, visible)
                        if (annoDesc == 'Lorg/hfyd/component/crouter/anno/TargetActivity;') {
                            CLog.i("scan anno2")
                            return new AnnotationVisitor(ASM_VERSION, annoMv) {
                                @Override
                                void visit(String annoDesc2, Object value) {
                                    RegisterCache.registerActivityControllerClasses(value, mClassName)
                                    super.visit(name, value)
                                }
                            }
                        }
                        return annoMv
                    }
                }
            }
            return mv
        }


    }
}

