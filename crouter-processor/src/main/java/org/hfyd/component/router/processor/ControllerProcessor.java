package org.hfyd.component.router.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import org.hfyd.component.crouter.anno.Controller;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Id;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;
import org.hfyd.component.crouter.anno.Scheme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

class ControllerProcessor {

    private ClassName requestClass = ClassName.get("org.hfyd.component.crouter.core.server", "ServerController.Request");
    private ParameterSpec request = ParameterSpec.builder(requestClass, "request").build();
    private ClassName contextClass = ClassName.get("android.content", "Context");
    private ClassName crouterClass = ClassName.get("org.hfyd.component.crouter", "CRouter");

    private Messager msg;
    private Filer filerUtils;
    private String moduleScheme;
    private String apiPackage;
    private Elements elementsUtils;

    ControllerProcessor(Messager msg, Filer filerUtils, Elements elementsUtils, String moduleScheme, String apiPackage) {
        this.msg = msg;
        this.filerUtils = filerUtils;
        this.moduleScheme = moduleScheme;
        this.apiPackage = apiPackage;
        this.elementsUtils = elementsUtils;
    }

    void process(Set<? extends Element> elements) {
        processRouteClass(elements);
    }

    private void processRouteClass(Set<? extends Element> elements) {
        List<ControllerEntity> controllerEntities = scanController(elements);

        if (controllerEntities.size() > 0) {
            genApiInterface(controllerEntities);
            genClass(controllerEntities);
        }
    }

    private List<ControllerEntity> scanController(Set<? extends Element> elements) {
        List<ControllerEntity> controllerEntities = new ArrayList<>();

        for (Element element : elements) {
            ControllerEntity entity = new ControllerEntity();
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                Scheme schemeAnnotation = element.getAnnotation(Scheme.class);
                Host hostAnnotation = element.getAnnotation(Host.class);
                Controller controllerAnnotation = element.getAnnotation(Controller.class);

                entity.scheme = schemeAnnotation == null ? null : schemeAnnotation.value();
                entity.host = hostAnnotation == null ? null : hostAnnotation.value();
                entity.className = typeElement.asType().toString();
                entity.controllerImpl = controllerAnnotation == null ? null : controllerAnnotation.value();

                if ((entity.scheme == null || entity.scheme.length() == 0) && (moduleScheme != null && moduleScheme.length() > 0)) {
                    entity.scheme = moduleScheme;
                }

                if (entity.host == null || entity.host.length() == 0) {
                    throw new IllegalArgumentException("@Host must not null class:" + entity.className);
                }

                if (entity.controllerImpl == null || entity.controllerImpl.length() == 0) {
                    throw new IllegalArgumentException("@Controller must not null class:" + entity.className);
                }

                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                for (Element enclosedElement : enclosedElements) {
                    if (enclosedElement instanceof ExecutableElement) {
                        ControllerEntity.Action action = new ControllerEntity.Action();
                        ExecutableElement executableElement = (ExecutableElement) enclosedElement;
                        Path pathAnnotation = executableElement.getAnnotation(Path.class);

                        if (pathAnnotation == null) {
                            throw new RuntimeException("method must add @Path class:" + entity.className);
                        }

                        action.path = pathAnnotation.value();
                        action.methodName = executableElement.getSimpleName().toString();

                        List<? extends VariableElement> parameterElements = executableElement.getParameters();
                        Map<String, ControllerEntity.ParamAttr> params = action.params;

                        boolean isIdDuplicate = false;
                        boolean isContextDuplicate = false;
                        for (VariableElement parameterElement : parameterElements) {
                            ControllerEntity.ParamAttr paramAttr = new ControllerEntity.ParamAttr();
                            // 参数名
                            String paramName = parameterElement.getSimpleName().toString();
                            // 参数类型
                            String paramTypeName = parameterElement.asType().toString();

                            Id idAnnotation = parameterElement.getAnnotation(Id.class);
                            Param paramAnnotation = parameterElement.getAnnotation(Param.class);
                            if (idAnnotation != null) {
                                if (isIdDuplicate) {
                                    throw new IllegalArgumentException("@Id duplicate host:" + entity.host + " path:" + action.path);
                                }
                                paramAttr.type = ControllerEntity.ParamAttr.Type.Id;
                                paramAttr.className = "java.lang.String";
                                isIdDuplicate = true;
                            } else if (paramTypeName.equals("android.content.Context") && paramAnnotation == null) {
                                if (isContextDuplicate) {
                                    throw new IllegalArgumentException("context duplicate host:" + entity.host + " path:" + action.path);
                                }
                                paramAttr.type = ControllerEntity.ParamAttr.Type.Context;
                                paramAttr.className = "android.content.Context";
                                isContextDuplicate = true;
                            } else if (paramAnnotation != null) {
                                paramAttr.type = ControllerEntity.ParamAttr.Type.Other;
                                paramAttr.className = paramTypeName;
                                paramAttr.paramAnnoName = paramAnnotation.value();
                            } else {
                                throw new RuntimeException("解析path失败，参数是否没有添加 @Param 注解");
                            }

                            paramAttr.paramName = parameterElement.getSimpleName().toString();
                            paramAttr.paramElement = parameterElement;
                            Collection<ControllerEntity.ParamAttr> values = params.values();
                            if (values.contains(paramAttr)) {
                                throw new IllegalArgumentException("param duplicate host:" + entity.host + " path:" + action.path + " param:" + paramAttr.paramAnnoName);
                            }

                            params.put(paramName, paramAttr);
                        }

                        if (entity.actions.contains(action)) {
                            throw new IllegalArgumentException("path duplicate host:" + entity.host + " path:" + action.path);
                        }

                        entity.actions.add(action);
                    }
                }

                controllerEntities.add(entity);
            }
        }

        return controllerEntities;
    }


    private void genApiInterface(List<ControllerEntity> controllerEntities) {
        for (ControllerEntity controllerEntity : controllerEntities) {
            TypeSpec.Builder classBuild = TypeSpec.interfaceBuilder(controllerEntity.getProxyControllerApiClassName());
            classBuild.addModifiers(Modifier.PUBLIC);

            genApiInterfaceMethod(classBuild, controllerEntity);

            JavaFile javaFile = JavaFile.builder(apiPackage, classBuild.build()).build();
            try {
                msg.printMessage(Diagnostic.Kind.WARNING, "RouterComponentProcessor writeTo:" +
                        " package:" + apiPackage +
                        " class:" + controllerEntity.getRealControllerClassName());
                javaFile.writeTo(filerUtils);
            } catch (IOException e) {
                e.printStackTrace();
                msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
            }
        }
    }

    private void genApiInterfaceMethod(TypeSpec.Builder classBuild, ControllerEntity controllerEntity) {
        List<ControllerEntity.Action> actions = controllerEntity.actions;
        if (actions.size() > 0) {
            for (ControllerEntity.Action action : actions) {
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(action.methodName)
                        .addModifiers(Modifier.PUBLIC)
                        .addModifiers(Modifier.ABSTRACT);

                String scheme = controllerEntity.scheme;
                if (scheme != null && scheme.length() > 0) {
                    methodBuilder.addAnnotation(AnnotationSpec.builder(Scheme.class).addMember("value", "$S", scheme).build());
                }

                String host = controllerEntity.host;
                if (host != null && host.length() > 0) {
                    methodBuilder.addAnnotation(AnnotationSpec.builder(Host.class).addMember("value", "$S", host).build());
                }

                methodBuilder.addAnnotation(AnnotationSpec.builder(Path.class).addMember("value", "$S", action.path).build());


                Map<String, ControllerEntity.ParamAttr> params = action.params;
                for (Map.Entry<String, ControllerEntity.ParamAttr> param : params.entrySet()) {
                    String paramName = param.getKey();
                    ControllerEntity.ParamAttr paramAttr = param.getValue();

                    if (paramAttr.type == ControllerEntity.ParamAttr.Type.Context) {
                        methodBuilder.addParameter(ParameterSpec.builder(contextClass, paramName).build());
                    } else if (paramAttr.type == ControllerEntity.ParamAttr.Type.Other) {
                        methodBuilder.addParameter(
                                ParameterSpec.builder(ClassName.get(paramAttr.paramElement.asType()), paramName)
                                        .addAnnotation(AnnotationSpec.builder(Param.class).addMember("value", "$S", paramAttr.paramAnnoName).build())
                                        .build()
                        );
                    }

                    methodBuilder.returns(crouterClass);
                }

                classBuild.addMethod(methodBuilder.build());
            }
        }
    }

    private void genClass(List<ControllerEntity> controllerEntities) {
        ClassName superClass = ClassName.get("org.hfyd.component.crouter.core.server", "ServerController");
        for (ControllerEntity controllerEntity : controllerEntities) {
            TypeSpec.Builder classBuild = TypeSpec.classBuilder(controllerEntity.getProxyControllerClassName());
            classBuild.superclass(superClass);
            classBuild.addModifiers(Modifier.PUBLIC);

            // realController
            classBuild.addField(genRealController(controllerEntity));

            // host
            classBuild.addMethod(genHostMethod(controllerEntity.host));

            // scheme
            if (controllerEntity.scheme != null && controllerEntity.scheme.length() > 0) {
                classBuild.addMethod(genSchemeMethod(controllerEntity.scheme));
            }

            classBuild.addMethod(genDispatchPathMethod(controllerEntity.actions));

            // 生成每个方法
            genDispatchCellMethod(classBuild, controllerEntity.actions);

            JavaFile javaFile = JavaFile.builder(apiPackage + "_crouter_gen", classBuild.build()).build();
            try {
                msg.printMessage(Diagnostic.Kind.WARNING, "RouterComponentProcessor writeTo:" +
                        " package:" + apiPackage +
                        " class:" + controllerEntity.getRealControllerClassName());
                javaFile.writeTo(filerUtils);
            } catch (IOException e) {
                e.printStackTrace();
                msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
            }
        }
    }

    private void genDispatchCellMethod(TypeSpec.Builder classBuild, List<ControllerEntity.Action> actions) {
        for (ControllerEntity.Action action : actions) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("dispatch" + action.methodName)
                    .addModifiers(Modifier.PRIVATE)
                    .addParameter(request);

            Map<String, ControllerEntity.ParamAttr> params = action.params;
            String methodName = action.methodName;
            if (params.size() == 0) {
                methodBuilder.addStatement("realController.$N()", methodName);
            } else {
                List<String> paramNames = new ArrayList<>();
                for (Map.Entry<String, ControllerEntity.ParamAttr> entry : params.entrySet()) {
                    String paramName = entry.getKey();
                    ControllerEntity.ParamAttr paramAttr = entry.getValue();
                    if (paramAttr == null) {
                        throw new IllegalArgumentException("param error");
                    }

                    ControllerEntity.ParamAttr.Type paramType = paramAttr.type;
                    String paramClassName = paramAttr.className;
                    String paramAnnoName = paramAttr.paramAnnoName;

                    if (paramType == ControllerEntity.ParamAttr.Type.Id) {
                        methodBuilder.addStatement("String callId = request.getCallId()");
                    } else if (paramType == ControllerEntity.ParamAttr.Type.Context) {
                        methodBuilder.addStatement("android.content.Context context = request.getContext()");
                    } else if (paramType == ControllerEntity.ParamAttr.Type.Other) {
                        if (paramClassName.equals("byte") || paramClassName.equals("short") || paramClassName.equals("int") ||
                                paramClassName.equals("long") || paramClassName.equals("float") || paramClassName.equals("double") ||
                                paramClassName.equals("char")) {
                            methodBuilder.addStatement("$N $N = 0", paramClassName, paramName);

                        } else if (paramClassName.equals("boolean")) {
                            methodBuilder.addStatement("$N $N = false", paramClassName, paramName);
                        } else {
                            methodBuilder.addStatement("$N $N = null", paramClassName, paramName);
                        }

                        CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("if (request.getRequestParamByName($S) != null)", paramAnnoName);

                        if (paramClassName.equals("byte") || paramClassName.equals("short") || paramClassName.equals("int") ||
                                paramClassName.equals("long") || paramClassName.equals("float") || paramClassName.equals("double") ||
                                paramClassName.equals("char") || paramClassName.equals("boolean")) {
                            caseBlock.addStatement("$N = ($N) request.getRequestParamByNameFor$N($S)", paramName, paramClassName, Utils.toUpperCaseFirstOne(paramClassName), paramAnnoName);
                        } else {
                            caseBlock.addStatement("$N = ($N) request.getRequestParamByName($S)", paramName, paramClassName, paramAnnoName);
                        }

                        caseBlock.endControlFlow();
                        methodBuilder.addCode(caseBlock.build());
                    } else {
                        throw new IllegalStateException("生成代码失败，不支持的type");
                    }

                    paramNames.add(paramName);
                }

                StringBuilder builder = new StringBuilder();

                for (int i = 0; i < paramNames.size(); i++) {
                    builder.append(paramNames.get(i));
                    if (i != paramNames.size() - 1) {
                        builder.append(", ");
                    }
                }

                methodBuilder.addCode("\n");
                methodBuilder.addStatement("realController.$N($N)", methodName, builder.toString());
            }

            classBuild.addMethod(methodBuilder.build());
        }
    }

    private FieldSpec genRealController(ControllerEntity controllerEntity) {
        ClassName realController = ClassName.get(apiPackage
                , controllerEntity.getRealControllerClassName());
        return FieldSpec.builder(realController, "realController")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $N()", controllerEntity.controllerImpl)
                .build();
    }

    private MethodSpec genDispatchPathMethod(List<ControllerEntity.Action> actions) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("dispatchPath")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(request)
                .returns(boolean.class)
                .addAnnotation(Override.class);

        if (actions.size() > 0) {
            CodeBlock.Builder caseBlock = CodeBlock.builder().beginControlFlow("switch (request.getPath())");

            for (ControllerEntity.Action action : actions) {
                caseBlock.add("case $S:\n", action.path)
                        .indent()
                        .addStatement("dispatch" + action.methodName + "($N)", request)
                        .addStatement("return true")
                        .unindent();
            }

            caseBlock.endControlFlow();
            builder.addCode(caseBlock.build());
        }

        builder.addStatement("return false");

        return builder.build();
    }

    private MethodSpec genSchemeMethod(String scheme) {
        return MethodSpec.methodBuilder("getScheme")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addAnnotation(Override.class)
                .addStatement("return $S", scheme)
                .build();
    }

    private MethodSpec genHostMethod(String host) {
        return MethodSpec.methodBuilder("getHost")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addAnnotation(Override.class)
                .addStatement("return $S", host)
                .build();
    }
}
