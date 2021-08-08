package com.wzc.findview.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wzc.findview.annotation.TestAnnotation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class TestAnnotationProcessor extends AbstractProcessor {

    /**
     * 生成文件的工具类
     */
    private Filer filer;
    /**
     * 打印信息
     */
    private Messager messager;
    /**
     * 元素相关
     */
    private Elements elementUtils;
    private Types typeUtils;


    /**
     * 一些初始化操作，获取一些有用的系统工具类
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(TestAnnotation.class)) {
            BufferedWriter bufferedWriter = null;
            try {
                VariableElement variableElement = (VariableElement) element;
                System.out.println("variableElement >>> getSimpleName = " + variableElement.getSimpleName());
                boolean kind = variableElement.getKind() == ElementKind.FIELD;
                System.out.println("variableElement >>> getKind = " + kind);
                System.out.println("variableElement >>> asType = " + variableElement.asType().toString());
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                System.out.println("typeElement >>> getSimpleName = " + typeElement.getSimpleName());
                System.out.println("typeElement >>> getQualifiedName = " + typeElement.getQualifiedName());
                boolean typeElementKind = typeElement.getKind() == ElementKind.METHOD;
                System.out.println("typeElement >>> getKind = " + typeElementKind);
                System.out.println("typeElement >>> getModifiers = " + typeElement.getModifiers().toString());
                PackageElement packageElement = (PackageElement) typeElement.getEnclosingElement();
                System.out.println("typeElement >>> getSimpleName = " + packageElement.getSimpleName());
                System.out.println("typeElement >>> getQualifiedName = " + packageElement.getQualifiedName());
                boolean packageElementKind = packageElement.getKind() == ElementKind.CLASS;
                System.out.println("typeElement >>> getKind = " + packageElementKind);
                System.out.println("typeElement >>> getModifiers = " + packageElement.getModifiers().toString());
                JavaFileObject jfo = filer.createSourceFile("com.wzc.demo.HelloWorld");
                bufferedWriter = new BufferedWriter(jfo.openWriter());
                bufferedWriter.append("package ").append(packageElement.getQualifiedName()).append(";\n");
                bufferedWriter.append("public class ").append("HelloWorld {\n");
                bufferedWriter.newLine();
                bufferedWriter.append("public static void main(String[] args) {\n");
                bufferedWriter.newLine();
                bufferedWriter.append("System.out.println(\"Hello, World!\");\n");
                bufferedWriter.newLine();
                bufferedWriter.append("}");
                bufferedWriter.newLine();
                bufferedWriter.append("}");
                bufferedWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(void.class)
                    .addParameter(String[].class, "args")
                    .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                    .build();

            TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld2")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .build();

            JavaFile javaFile = JavaFile.builder("com.example.helloworld", helloWorld)
                    .build();

            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Set<String> getSupportedOptions() {

        HashSet<String> set = new HashSet<>();
        set.add("MODULE_NAME");
        return set;
    }

    /**
     * 设置支持的版本
     *
     * @return 这里用最新的就好
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 设置支持的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //添加支持的注解
        HashSet<String> set = new HashSet<>();
        set.add(TestAnnotation.class.getCanonicalName());
        return set;
    }


    public static boolean mapIsEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

}