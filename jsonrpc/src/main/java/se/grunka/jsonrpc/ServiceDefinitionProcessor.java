package se.grunka.jsonrpc;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("se.grunka.jsonrpc.ServiceDefinition")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SuppressWarnings("unused")
public class ServiceDefinitionProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getRootElements()) {
                if (element.getKind() == ElementKind.INTERFACE && element instanceof TypeElement) {
                    processInterface((TypeElement) element);
                }
            }
        } catch (IOException e) {
            throw new Error("Could not write class", e);
        }
        return true;
    }

    private void processInterface(TypeElement typeElement) throws IOException {
        String className = typeElement.getQualifiedName().toString();
        StringBuilder builder = new StringBuilder();
        builder.append("package ").append(className.substring(0, className.lastIndexOf("."))).append(";");
        builder.append("import java.util.Map; import java.util.HashMap;");
        builder.append("public class ").append(typeElement.getSimpleName().toString()).append("ParameterNames {");

        builder.append("public static Map<String, Map<Class<?>[], String[]>> getMapping() {");
        builder.append("Map<String, Map<Class<?>[], String[]>> methodMapping = new HashMap<String, Map<Class<?>[], String[]>>();");
        builder.append("Map<Class<?>[], String[]> mapping;");

        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement) {
                String methodName = element.getSimpleName().toString();
                builder.append("mapping = new HashMap<Class<?>[], String[]>();");
                builder.append("methodMapping.put(\"").append(methodName).append("\", mapping);");
                processMethod(builder, methodName, (ExecutableElement) element);
            }
        }

        builder.append("return methodMapping; } }");
        JavaFileObject object = processingEnv.getFiler().createSourceFile(className + "ParameterNames");
        Writer writer = object.openWriter();
        writer.write(builder.toString());
        writer.close();
    }

    private void processMethod(StringBuilder builder, String methodName, ExecutableElement element) {
        StringBuilder classes = new StringBuilder();
        StringBuilder names = new StringBuilder();
        boolean first = true;
        for (VariableElement variableElement : element.getParameters()) {
            if (!first) {
                classes.append(",");
                names.append(",");
            } else {
                first = false;
            }
            String type = variableElement.asType().toString();
            classes.append(type).append(".class");
            String name = variableElement.getSimpleName().toString();
            names.append("\"").append(name).append("\"");
        }
        builder.append("mapping.put(new Class<?>[]{");
        builder.append(classes.toString());
        builder.append("},new String[]{");
        builder.append(names.toString());
        builder.append("});");
    }
}
