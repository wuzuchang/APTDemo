package com.wzc.findview.gradle.plugin.launch.core;


import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.wzc.findview.gradle.plugin.launch.utils.Setting;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class FindViewTransform extends Transform {
    private File mFindViewApiSdkJar;

    @Override
    public String getName() {
        return "WZCFindView";
    }

    /**
     * 需要处理的数据类型，有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     *
     * @return
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 指 Transform 要操作内容的范围，官方文档 Scope 有 7 种类型：
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     *
     * @return
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    /**
     * 是否增量编译
     * 所谓增量编译，是指当源程序的局部发生变更后进重新编译的工作只限于修改的部分及与之相关部分的内容，而不需要对全部代码进行编译
     *
     * @return false：否
     */
    @Override
    public boolean isIncremental() {
        return false;
    }

    /**
     * 文档：https://google.github.io/android-gradle-dsl/javadoc/3.4/
     *
     * @param transformInvocation transformInvocation
     * @throws TransformException
     * @throws InterruptedException
     * @throws IOException
     */
    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        _transform(transformInvocation.getContext(), transformInvocation.getInputs(), transformInvocation.getOutputProvider(), transformInvocation.isIncremental());
    }

    /**
     * @param context
     * @param collectionInput
     * @param outputProvider
     * @param isIncremental   是否增量编译
     * @throws IOException
     * @throws TransformException
     * @throws InterruptedException
     */
    private void _transform(Context context, Collection<TransformInput> collectionInput, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

        if (!isIncremental) {
            //非增量,需要删除输出目录
            outputProvider.deleteAll();
        }
        boolean leftSlash = "/".equals(File.separator);
        for (TransformInput transformInput : collectionInput) {
            //源码
            Collection<DirectoryInput> directoryInputCollection = transformInput.getDirectoryInputs();
            for (DirectoryInput directoryInput : directoryInputCollection) {
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                String root = directoryInput.getFile().getAbsolutePath();
                if (!root.endsWith(File.separator)) {
                    root += File.separator;
                }
                String finalRoot = root;
                eachFileRecurse(directoryInput.getFile(), new FileCallBack() {
                    @Override
                    public void call(File file) {
                        String path = file.getAbsolutePath().replace(finalRoot, "");
                        if (!leftSlash) {
                            path = path.replaceAll("\\\\", "/");
                        }
                        System.out.println("path:" + path);
                        if (path.startsWith("com/wzc/findview/myaptdemo/MainActivity")) {
                            try {
                                scanClass(new FileInputStream(file));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                FileUtils.copyDirectory(directoryInput.getFile(), dest);
            }
            // JAR or AAR
            Collection<JarInput> JarInputCollection = transformInput.getJarInputs();
            for (JarInput jarInput : JarInputCollection) {
                String destName = jarInput.getName();
                // rename jar files
                String hexName = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
                // input file
                File src = jarInput.getFile();
                // output file
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

                //scan jar file to find classes
                if (shouldProcessPreDexJar(src.getAbsolutePath())) {
                    scanJar(src, dest);
                }
                FileUtils.copyFile(src, dest);
            }
            if (mFindViewApiSdkJar != null && mFindViewApiSdkJar.getName().endsWith(".jar")) {
                insertInitCodeIntoJarFile(mFindViewApiSdkJar);
            }
        }
    }

    public void eachFileRecurse(final File self, FileCallBack callBack)
            throws FileNotFoundException, IllegalArgumentException {
        checkDir(self);
        final File[] files = self.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                eachFileRecurse(file, callBack);
            } else if (file.isFile()) {
                callBack.call(file);
            }
        }
    }

    private void checkDir(File dir) throws FileNotFoundException, IllegalArgumentException {
        if (!dir.exists())
            throw new FileNotFoundException(dir.getAbsolutePath());
        if (!dir.isDirectory())
            throw new IllegalArgumentException("The provided File object is not a directory: " + dir.getAbsolutePath());
    }

    interface FileCallBack {

        void call(File file);
    }


    boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository");
    }

    void scanClass(InputStream inputStream) {
        try {
            ClassReader cr = new ClassReader(inputStream);
            ClassWriter cw = new ClassWriter(cr, 0);
            ScanClassVisitor sc = new ScanClassVisitor(Opcodes.ASM7, cw);
            cr.accept(sc, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void scanJar(File jarFile, File dest) {
        JarFile file = null;
        try {
            file = new JarFile(jarFile);
            Enumeration<JarEntry> enumeration = file.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.startsWith(Setting.MAIN_ACTIVITY_CLASS_PACKAGE_NAME)) {
                    InputStream inputStream = file.getInputStream(jarEntry);
                    scanClass(inputStream);
                    inputStream.close();
                } else if (Setting.GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {
                    // 如果是api中的类，记录这个jar
                    mFindViewApiSdkJar = dest;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                file.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private File insertInitCodeIntoJarFile(File jarFile) {

        try {
            File optJar = new File(jarFile.getParent(), jarFile.getName() + ".opt");
            if (optJar.exists())
                optJar.delete();
            JarFile file = new JarFile(jarFile);
            Enumeration<JarEntry> enumeration = file.entries();
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                String entryName = jarEntry.getName();
                ZipEntry zipEntry = new ZipEntry(entryName);
                InputStream inputStream = file.getInputStream(jarEntry);
                jarOutputStream.putNextEntry(zipEntry);
                if (Setting.GENERATE_TO_CLASS_FILE_NAME.equals(entryName)) {
                    System.out.println("Insert init code to class >> " + entryName);
                    byte[] bytes = referHackWhenInit(inputStream);
                    jarOutputStream.write(bytes);
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream));
                }
                inputStream.close();
                jarOutputStream.closeEntry();
            }
            jarOutputStream.close();
            file.close();

            if (jarFile.exists()) {
                jarFile.delete();
            }
            optJar.renameTo(jarFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jarFile;
    }

    private byte[] referHackWhenInit(InputStream inputStream){
        try {
            ClassReader cr = new ClassReader(inputStream);
            ClassWriter cw = new ClassWriter(cr, 0);
            FindViewHelperClassVisitor sc = new FindViewHelperClassVisitor(Opcodes.ASM7, cw);
            cr.accept(sc, ClassReader.EXPAND_FRAMES);
          return   cw.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}