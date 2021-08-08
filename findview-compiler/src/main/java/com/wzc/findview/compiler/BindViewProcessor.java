package com.wzc.findview.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wzc.findview.annotation.BindView;

import java.io.IOException;
import java.util.HashSet;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

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

        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)) {
            if (element.getKind() != ElementKind.FIELD) {
                break;
            }
            try {
                String variableName = element.getSimpleName().toString();
                int id = element.getAnnotation(BindView.class).value();
                TypeElement classElement = (TypeElement) element.getEnclosingElement();
                PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
                String className = classElement.getSimpleName() + "_ViewBinding";
                TypeName classTypeName = TypeName.get(classElement.asType());

                CodeBlock.Builder builder = CodeBlock.builder()
                        .add("target.$L = ", variableName);
                builder.add("target.findViewById($L)", id);

                MethodSpec main = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(classTypeName, "target")
                        .addStatement("$L", builder.build())
                        .build();
                TypeSpec bindClass = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(main)
                        .build();

                JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), bindClass)
                        .build();

                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
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
        HashSet<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        return set;
    }

}