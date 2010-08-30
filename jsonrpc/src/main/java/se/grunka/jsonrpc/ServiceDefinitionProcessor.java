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
import java.util.*;

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
        JavaFileObject object = processingEnv.getFiler().createSourceFile(className + "ParameterNames");
        Writer writer = object.openWriter();
        writer.write("package " + className.substring(0, className.lastIndexOf(".")) + ";");
        writer.write("public class " + typeElement.getSimpleName().toString() + "ParameterNames {");

        writer.write("public static java.util.Map<String, java.util.Map<Class<?>[], String[]>> getMapping() {");
        writer.write("java.util.Map<String, java.util.Map<Class<?>[], String[]>> methodMapping = new java.util.HashMap<String, java.util.Map<Class<?>[], String[]>>();");
        writer.write("java.util.Map<Class<?>[], String[]> mapping;");

        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement) {
                String methodName = element.getSimpleName().toString();
                writer.write("mapping = new java.util.HashMap<Class<?>[], String[]>();");
                writer.write("methodMapping.put(\"" + methodName + "\", mapping);");
                processMethod(writer, methodName, (ExecutableElement) element);
            }
        }

        writer.write("return methodMapping; }");
        writer.write("}");
        writer.close();
    }


    private void processMethod(Writer writer, String methodName, ExecutableElement element) throws IOException {
        String classes = "";
        String names = "";
        for (VariableElement variableElement : element.getParameters()) {
            String type = variableElement.asType().toString();
            String name = variableElement.getSimpleName().toString();
            classes += type + ".class,";
            names += "\"" + name + "\",";
        }
        writer.write("mapping.put(new Class<?>[]{");
        writer.write(classes.length() > 0 ? classes.substring(0, classes.length() - 1) : "");
        writer.write("},new String[]{");
        writer.write(names.length() > 0 ? names.substring(0, names.length() - 1) : "");
        writer.write("});");
    }
}
