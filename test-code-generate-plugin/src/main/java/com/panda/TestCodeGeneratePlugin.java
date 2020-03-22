package com.panda;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author wankun.pwk
 * @date 2020/3/22
 */
@Mojo(name = "test-code-generate")
public class TestCodeGeneratePlugin extends AbstractMojo {

    /**
     * 源码默认路径
     */
    @Parameter(defaultValue = "src/main/java/")
    private String sourcePath;

    @Parameter
    private String[] services;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("test-code-generate start");
        System.out.println(sourcePath);
        if (services != null && services.length > 0) {
            for (String service : services) {
                System.out.println("start generate service: " + service + "\n");
                try {
                    generateTestInterface(service);
                    generateTestImpl(service);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateTestInterface(String service) throws FileNotFoundException {
        String servicePackage = service.substring(0, service.lastIndexOf("."));
        String serviceName = service.substring(service.lastIndexOf(".") + 1);
        CompilationUnit testInterfaceUnit = new CompilationUnit();
        testInterfaceUnit.setPackageDeclaration(servicePackage);
        String servicePath = sourcePath + "/" + service.replaceAll("\\.", "/") + ".java";
        CompilationUnit serviceUnit = JavaParser.parse(new File(servicePath));
        for (ImportDeclaration d : serviceUnit.getImports()) {
            testInterfaceUnit.addImport(d.getNameAsString());
        }
        ClassOrInterfaceDeclaration testInterface = testInterfaceUnit.addClass("ITest" + serviceName)
                .setInterface(true)
                .setPublic(true);
        for (MethodDeclaration method : serviceUnit.getClassByName(serviceName).get().getMethods()) {
            if (method.isPublic()) {
                MethodDeclaration methodDeclaration = testInterface.addMethod(method.getName().asString(), Modifier.Keyword.PUBLIC);
                methodDeclaration.setType(method.getType());
                if (method.getJavadocComment().isPresent()) {
                    methodDeclaration.setJavadocComment(method.getJavadocComment().get());
                }
                methodDeclaration.setParameters(method.getParameters());
                methodDeclaration.setTypeParameters(method.getTypeParameters());
            }
        }

        String code = testInterfaceUnit.toString().replaceAll("\\s+\\{\\s+}", ";");
        String path = sourcePath + "/" + servicePackage.replaceAll("\\.", "/")
                + "/" + "ITest" + serviceName + ".java ";
        try {
            Files.write(Paths.get(path), code.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateTestImpl(String service) throws FileNotFoundException {
        String servicePackage = service.substring(0, service.lastIndexOf("."));
        String serviceName = service.substring(service.lastIndexOf(".") + 1);
        CompilationUnit testImplUnit = new CompilationUnit();
        testImplUnit.setPackageDeclaration(servicePackage);
        testImplUnit.addImport("org.springframework.stereotype.Service");
        testImplUnit.addImport("org.springframework.beans.factory.annotation.Autowired");
        String servicePath = sourcePath + "/" + service.replaceAll("\\.", "/") + ".java";
        CompilationUnit serviceUnit = JavaParser.parse(new File(servicePath));
        for (ImportDeclaration d : serviceUnit.getImports()) {
            testImplUnit.addImport(d.getNameAsString());
        }
        ClassOrInterfaceDeclaration testImpl = testImplUnit.addClass("ITest" + serviceName + "Impl")
                .setPublic(true)
                .addAnnotation("Service");

        FieldDeclaration field = testImpl.addField(serviceName, "m" + serviceName, Modifier.Keyword.PRIVATE);
        field.addAnnotation("Autowired");

        for (MethodDeclaration method : serviceUnit.getClassByName(serviceName).get().getMethods()) {
            if (method.isPublic()) {
                MethodDeclaration methodDeclaration = testImpl.addMethod(method.getName().asString(), Modifier.Keyword.PUBLIC);
                methodDeclaration.setType(method.getType());
                if (method.getJavadocComment().isPresent()) {
                    methodDeclaration.setJavadocComment(method.getJavadocComment().get());
                }
                methodDeclaration.setParameters(method.getParameters());
                methodDeclaration.setTypeParameters(method.getTypeParameters());

                BlockStmt blockStmt = new BlockStmt();
                StringBuilder stringBuilder = new StringBuilder();
                if (!method.getType().isVoidType()) {
                    stringBuilder.append("return ");
                }
                stringBuilder.append("m");
                stringBuilder.append(serviceName);
                stringBuilder.append(".");
                stringBuilder.append(method.getNameAsString());
                stringBuilder.append("(");
                for (int i = 0; i < method.getParameters().size(); i++) {
                    stringBuilder.append(method.getParameter(i).getNameAsString());
                    if (i < method.getParameters().size() - 1) {
                        stringBuilder.append(",");
                    }
                }
                stringBuilder.append(");");
                blockStmt.addStatement(stringBuilder.toString());
                methodDeclaration.setBody(blockStmt);
            }
        }

        String code = testImplUnit.toString();
        String path = sourcePath + "/" + servicePackage.replaceAll("\\.", "/")
                + "/" + "ITest" + serviceName + "Impl.java ";
        try {
            Files.write(Paths.get(path), code.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
