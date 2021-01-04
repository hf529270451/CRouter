package org.hfyd.component.crouter.plugin

import com.android.build.api.transform.*
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class TransformHelper {

    private TransformInvocation mTransformInvocation
    private ClassModify mClassModify;

    interface ClassModify {
        byte[] modify(byte[] srcClass)
    }

    TransformHelper(TransformInvocation transformInvocation) {
        this.mTransformInvocation = transformInvocation
    }

    void transform(ClassModify classModify){
        this.mClassModify = classModify
        Context context = mTransformInvocation.context
        Collection<TransformInput> inputs = mTransformInvocation.inputs
        TransformOutputProvider outputProvider = mTransformInvocation.outputProvider
        boolean isIncremental = mTransformInvocation.incremental

        inputs.each { TransformInput input ->

            input.jarInputs.each { JarInput jarInput ->
                forEachJar(isIncremental, jarInput, outputProvider, context)
            }

            input.directoryInputs.each { DirectoryInput directoryInput ->
                forEachDirectory(isIncremental, directoryInput, outputProvider, context)
            }
        }
    }

    void forEachJar(boolean isIncremental, JarInput jarInput, TransformOutputProvider outputProvider, Context context) {
        String destName = jarInput.file.name
        //截取文件路径的 md5 值重命名输出文件，因为可能同名，会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        //获得输出文件
        File destFile = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isIncremental) {
            Status status = jarInput.getStatus()
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    transformJar(destFile, jarInput, context)
                    break
                case Status.REMOVED:
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    break
                default:
                    break
            }
        } else {
            transformJar(destFile, jarInput, context)
        }
    }

    void transformJar(File dest, JarInput jarInput, Context context) {
        CLog.i("开始遍历 jar：" + jarInput.file.absolutePath)
        def modifiedJar = modifyJarFile(jarInput.file, context.getTemporaryDir())
        CLog.i("结束遍历 jar：" + jarInput.file.absolutePath)

        if (modifiedJar == null) {
            modifiedJar = jarInput.file
        }
        FileUtils.copyFile(modifiedJar, dest)
    }

    /**
     * 修改 jar 文件中对应字节码
     */
    private File modifyJarFile(File jarFile, File tempDir) {
        if (jarFile) {
            return modifyJar(jarFile, tempDir, true)

        }
        return null
    }

    private File modifyJar(File jarFile, File tempDir, boolean isNameHex) {
        //取原 jar, verify 参数传 false, 代表对 jar 包不进行签名校验
        def file = new JarFile(jarFile, false)
        //设置输出到的 jar
        def tmpNameHex = ""
        if (isNameHex) {
            tmpNameHex = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, tmpNameHex + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()

        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                e.printStackTrace()
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                JarEntry entry = new JarEntry(entryName)
                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes
                try {
                    jarOutputStream.putNextEntry(entry)
                    sourceClassBytes = IOUtils.toByteArray(inputStream)
                } catch (Exception e) {
                    CLog.e("Exception encountered while processing jar: " + jarFile.getAbsolutePath())
                    e.printStackTrace()
                    return null
                }
                if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                    modifiedClassBytes = modifyClass(sourceClassBytes)
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(sourceClassBytes)
                } else {
                    jarOutputStream.write(modifiedClassBytes)
                }
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    void forEachDirectory(boolean isIncremental, DirectoryInput directoryInput, TransformOutputProvider outputProvider, Context context) {
        File dir = directoryInput.file
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY)
        FileUtils.forceMkdir(dest)
        String srcDirPath = dir.absolutePath
        String destDirPath = dest.absolutePath
        if (isIncremental) {
            Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
            for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                Status status = changedFile.getValue()
                File inputFile = changedFile.getKey()
                String destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
                File destFile = new File(destFilePath)
                switch (status) {
                    case Status.NOTCHANGED:
                        break
                    case Status.REMOVED:
                        CLog.i("目录 status = $status:$inputFile.absolutePath")
                        if (destFile.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            destFile.delete()
                        }
                        break
                    case Status.ADDED:
                    case Status.CHANGED:
                        CLog.i("目录 status = $status:$inputFile.absolutePath")
                        File modified = modifyClassFile(dir, inputFile, context.getTemporaryDir())
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                        if (modified != null) {
                            FileUtils.copyFile(modified, destFile)
                            modified.delete()
                        } else {
                            FileUtils.copyFile(inputFile, destFile)
                        }
                        break
                    default:
                        break
                }
            }
        } else {
            FileUtils.copyDirectory(dir, dest)
            dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                File inputFile ->
                    forEachDir(dir, inputFile, context, srcDirPath, destDirPath)
            }
        }
    }

    void forEachDir(File dir, File inputFile, Context context, String srcDirPath, String destDirPath) {
        File modified = modifyClassFile(dir, inputFile, context.getTemporaryDir())
        if (modified != null) {
            File target = new File(inputFile.absolutePath.replace(srcDirPath, destDirPath))
            if (target.exists()) {
                target.delete()
            }
            FileUtils.copyFile(modified, target)
            modified.delete()
        }
    }

    /**
     * 目录文件中修改对应字节码
     */
    private File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        FileOutputStream outputStream = null
        try {
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
            if (modifiedClassBytes) {
                String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
                modified = new File(tempDir, className.replace('.', '') + '.class')
                if (modified.exists()) {
                    modified.delete()
                }
                modified.createNewFile()
                outputStream = new FileOutputStream(modified)
                outputStream.write(modifiedClassBytes)
            }
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            IOUtils.closeQuietly(outputStream)
        }
        return modified
    }

    /**
     * 真正修改类中方法字节码
     */
    private byte[] modifyClass(byte[] srcClass) {
        if (this.mClassModify) {
            return this.mClassModify.modify(srcClass)
        }

        return srcClass
    }

    private static String path2ClassName(String pathName) {
        pathName.replace(File.separator, ".").replace(".class", "")
    }
}