package com.example.processor;

import com.example.annotations.MyAnnotation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("com.example.annotations.MyAnnotation")
//@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
public class ClassProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Messager environmentMessager;
    private HashMap<String , ClassCreatorProxy> mHashMapCache = new HashMap<>();
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        environmentMessager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedOptions() {
        HashSet hashSet = new LinkedHashSet();
        hashSet.add(MyAnnotation.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mHashMapCache.clear();
        environmentMessager.printMessage(Diagnostic.Kind.NOTE, "董鹏 processing...");
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(MyAnnotation.class);
        for (Element element : elements) {
            //字段
            VariableElement variableElement = (VariableElement) element;
            //  获取成员变量所在的类。（父元素的全限定名）
            TypeElement classElement = (TypeElement) variableElement.getEnclosingElement();
            // // 获取类的全限定名。com.xx.xx.MainActivity.class
            String fullClassName = classElement.getQualifiedName().toString();
            System.out.println(fullClassName);
            //elements的信息保存到mProxyMap中
            ClassCreatorProxy proxy = mHashMapCache.get(fullClassName);
            if (proxy == null) {
                proxy = new ClassCreatorProxy(elementUtils, classElement);
                mHashMapCache.put(fullClassName, proxy);
            }
            MyAnnotation bindAnnotation = element.getAnnotation(MyAnnotation.class);
            int id = bindAnnotation.id();

            proxy.putElement(id, variableElement);
        }
//
//        for (String key : mHashMapCache.keySet()) {
//            ClassCreatorProxy proxyInfo = mHashMapCache.get(key);
//            //通过square公司的库生成Java文件
//            JavaFile javaFile = JavaFile.builder(proxyInfo.getPackageName(), proxyInfo.generateJavaCode2()).build();
//            try {
//                //　生成文件
//                javaFile.writeTo(processingEnv.getFiler());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        environmentMessager.printMessage(Diagnostic.Kind.NOTE, "注意process finish ...");
//        return true;

        // 通过遍历mProxy，创建java文件
        for (String key : mHashMapCache.keySet()) {
            ClassCreatorProxy proxyInfo = mHashMapCache.get(key);
            environmentMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxyInfo.getProxyClassFullName());
            try {
                JavaFileObject jfo = processingEnv.getFiler().createSourceFile(proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
                Writer writer = jfo.openWriter();
                writer.write(proxyInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                environmentMessager.printMessage(Diagnostic.Kind.NOTE, " --> create " + proxyInfo.getProxyClassFullName() + " error!");
            }
        }
        environmentMessager.printMessage(Diagnostic.Kind.NOTE, "process finish ...");
        return true;

    }
}
