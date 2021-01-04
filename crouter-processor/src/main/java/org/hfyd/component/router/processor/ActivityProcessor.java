package org.hfyd.component.router.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.hfyd.component.crouter.anno.Controller;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Id;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;
import org.hfyd.component.crouter.anno.TargetActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

class ActivityProcessor {

    private ClassName contextClass = ClassName.get("android.content", "Context");
    private ClassName stringClass = ClassName.get("java.lang", "String");

    private ClassName intentClass = ClassName.get("android.content", "Intent");
    private ClassName activityClass = ClassName.get("android.app", "Activity");
    private ClassName crouterUtilsClass = ClassName.get("org.hfyd.component.crouter.core.client", "CRouterClientUtils");
    private ClassName responseDispatchClass = ClassName.get("org.hfyd.component.crouter.core.server", "ResponseDispatch");
    private ClassName responseResultClass = ClassName.get("org.hfyd.component.crouter.core.server", "ResponseResult");

    private Messager msg;
    private Filer filerUtils;
    private String moduleScheme;
    private String apiPackage;
    private Elements elementUtils;

    ActivityProcessor(Messager msg, Elements elementUtils, Filer filerUtils, String moduleScheme, String apiPackage) {
        this.msg = msg;
        this.filerUtils = filerUtils;
        this.moduleScheme = moduleScheme;
        this.apiPackage = apiPackage;
        this.elementUtils = elementUtils;
    }

    void process(Set<? extends Element> elements) {
        Map<String, List<ActivityEntity>> activityEntitiesMap = scanActivity(elements);
        if (activityEntitiesMap.size() > 0) {
            genApi(activityEntitiesMap);
        }
    }

    private void genApi(Map<String, List<ActivityEntity>> activityEntitiesMap) {
        for (Map.Entry<String, List<ActivityEntity>> item : activityEntitiesMap.entrySet()) {
            ClassName className = genInterface(item);
            if (className != null) {
                genClass(item, className);
            }
        }
    }

    private ClassName genInterface(Map.Entry<String, List<ActivityEntity>> item) {
        String host = item.getKey();
        String className = ActivityEntity.getProxyControllerClassName(host);

        List<ActivityEntity> activityEntities = item.getValue();

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(className);
        interfaceBuilder.addModifiers(Modifier.PUBLIC);
        interfaceBuilder.addAnnotation(
                AnnotationSpec.builder(Host.class)
                        .addMember("value", "$S", host)
                        .build()
        );
        interfaceBuilder.addAnnotation(
                AnnotationSpec.builder(Controller.class)
                        .addMember("value", "$S", apiPackage + "_crouter_gen." + ActivityEntity.getProxyControllerClassName(host) + "Impl")
                        .build()
        );

        for (ActivityEntity activityEntity : activityEntities) {
            genInterfaceMethod(interfaceBuilder, activityEntity);
        }

        JavaFile javaFile = JavaFile.builder(apiPackage, interfaceBuilder.build()).build();
        try {
            javaFile.writeTo(filerUtils);
            return ClassName.get(apiPackage, className);
        } catch (IOException e) {
            e.printStackTrace();
            msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        }

        return null;
    }

    private void genInterfaceMethod(TypeSpec.Builder interfaceBuilder, ActivityEntity activityEntity) {
        genAbsStartTargetActivityMethod(interfaceBuilder, activityEntity);
    }

    private void genClass(Map.Entry<String, List<ActivityEntity>> item, ClassName interfaceClassName) {
        String host = item.getKey();
        List<ActivityEntity> activityEntities = item.getValue();

        TypeSpec.Builder classBuild = TypeSpec.classBuilder(ActivityEntity.getProxyControllerClassName(host) + "Impl");
        TypeElement activityExtraBinder = elementUtils.getTypeElement("org.hfyd.component.crouter.core.client.activity.ActivityExtraBinder");

        classBuild.addSuperinterface(interfaceClassName);
        classBuild.addSuperinterface(ClassName.get(activityExtraBinder));
        classBuild.addModifiers(Modifier.PUBLIC);
        for (ActivityEntity activityEntity : activityEntities) {
            genStartTargetActivityMethod(classBuild, activityEntity);
            genBindTargetActivityMethod(classBuild, activityEntity);
        }

        genBindActivityMethod(classBuild, activityEntities);

        JavaFile javaFile = JavaFile.builder(apiPackage + "_crouter_gen", classBuild.build()).build();
        try {
            javaFile.writeTo(filerUtils);
        } catch (IOException e) {
            e.printStackTrace();
            msg.printMessage(Diagnostic.Kind.WARNING, e.getMessage());
        }
    }

    private void genBindActivityMethod(TypeSpec.Builder classBuild, List<ActivityEntity> activityEntities) {
        MethodSpec.Builder bindActivityMethodBuilder = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(activityClass, "activity");

        CodeBlock.Builder builder = CodeBlock.builder();
        for (int i = 0; i < activityEntities.size(); i++) {
            ActivityEntity entity = activityEntities.get(i);
            if (i == 0) {
                builder.beginControlFlow("if (activity instanceof $N)", entity.className);
            } else {
                builder.nextControlFlow("else if (activity instanceof $N)", entity.className);
            }

            builder.addStatement("bindActivity(($N) activity)", entity.className);

            if (i == activityEntities.size() - 1) {
                builder.endControlFlow();
            }
        }

        bindActivityMethodBuilder.addCode(builder.build());

        classBuild.addMethod(bindActivityMethodBuilder.build());
    }

    private void genBindTargetActivityMethod(TypeSpec.Builder classBuild, ActivityEntity activityEntity) {
        TypeMirror targetActivity = elementUtils.getTypeElement(activityEntity.className).asType();

        MethodSpec.Builder methodBuild = MethodSpec.methodBuilder("bindActivity")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterSpec.builder(TypeName.get(targetActivity), "activity").build());

        genBindTargetActivityStatements(methodBuild, activityEntity);

        classBuild.addMethod(methodBuild.build());
    }

    private void genBindTargetActivityStatements(MethodSpec.Builder methodBuild, ActivityEntity activityEntity) {
        methodBuild.addStatement("$T intent = activity.getIntent()", intentClass);
        List<ActivityEntity.ExtraAttr> extras = activityEntity.extras;
        for (ActivityEntity.ExtraAttr extra : extras) {
            Element typeElement = extra.typeElement;
            String type = typeElement.asType().toString();
            if (extra.type == ActivityEntity.ExtraAttr.Type.Id) {
                methodBuild.addStatement("activity.$N = intent.getStringExtra($S)", extra.paramName, "callId");
            } else if (type.equals("int")) {
                methodBuild.addStatement("activity.$N = intent.getIntExtra($S, 0)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("short")) {
                methodBuild.addStatement("activity.$N = intent.getShortExtra($S, (short) 0)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("float")) {
                methodBuild.addStatement("activity.$N = intent.getFloatExtra($S, 0)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("char")) {
                methodBuild.addStatement("activity.$N = intent.getCharExtra($S, '\\u0000')", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("long")) {
                methodBuild.addStatement("activity.$N = intent.getLongExtra($S, 0)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("double")) {
                methodBuild.addStatement("activity.$N = intent.getDoubleExtra($S, 0)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("byte")) {
                methodBuild.addStatement("activity.$N = intent.getByteExtra($S, (byte) 0)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("boolean")) {
                methodBuild.addStatement("activity.$N = intent.getBooleanExtra($S, false)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("android.os.Bundle")) {
                methodBuild.addStatement("activity.$N = intent.getBundleExtra($S)", extra.paramName, extra.paramAnnoName);
            } else if (type.equals("java.lang.String")) {
                methodBuild.addStatement("activity.$N = intent.getStringExtra($S)", extra.paramName, extra.paramAnnoName);
            } else {
                methodBuild.addStatement("activity.$N = ($N) intent.getSerializableExtra($S)", extra.paramName, extra.className, extra.paramAnnoName);
            }
        }
    }

    private void genAbsStartTargetActivityMethod(TypeSpec.Builder classBuild, ActivityEntity activityEntity) {
        String simpleClassName = Utils.getRealControllerClassName(activityEntity.className);

        MethodSpec.Builder methodBuild = MethodSpec.methodBuilder("start" + simpleClassName)
                .addModifiers(Modifier.ABSTRACT)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value", "$S", activityEntity.path).build())
                .addParameters(genAbsStartActivityParameters(activityEntity.extras));

        classBuild.addMethod(methodBuild.build());
    }

    private void genStartTargetActivityMethod(TypeSpec.Builder classBuild, ActivityEntity activityEntity) {
        String simpleClassName = Utils.getRealControllerClassName(activityEntity.className);

        MethodSpec.Builder methodBuild = MethodSpec.methodBuilder("start" + simpleClassName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(TargetActivity.class).addMember("value", "$S", activityEntity.className).build())
                .addAnnotation(Override.class)
                .addParameters(genStartActivityParameters(activityEntity.extras));

        genStartTargetActivityStatements(activityEntity, methodBuild);

        classBuild.addMethod(methodBuild.build());
    }

    private void genStartTargetActivityStatements(ActivityEntity activityEntity, MethodSpec.Builder methodBuild) {
        String activityClass = activityEntity.className;
        methodBuild.addStatement("$T intent = new $T(context, $N)", intentClass, intentClass, activityClass + ".class");
        boolean hasId = false;
        List<ActivityEntity.ExtraAttr> extras = activityEntity.extras;
        for (ActivityEntity.ExtraAttr extra : extras) {
            if (extra.type == ActivityEntity.ExtraAttr.Type.Id) {
                methodBuild.addStatement("intent.putExtra($S, $N)", "callId", "callId");
                hasId = true;
            } else {
                methodBuild.addStatement("intent.putExtra($S, $N)", extra.paramAnnoName, extra.paramName);
            }
        }

        methodBuild.addStatement("$T.startActivity(context, intent);", crouterUtilsClass);

        if (!hasId) {
            methodBuild.addStatement("$T.send(callId, $T.success())", responseDispatchClass, responseResultClass);
        }
    }

    private Iterable<ParameterSpec> genAbsStartActivityParameters(List<ActivityEntity.ExtraAttr> extras) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();

        parameterSpecs.add(ParameterSpec.builder(contextClass, "context").build());
        parameterSpecs.add(ParameterSpec.builder(stringClass, "callId").addAnnotation(AnnotationSpec.builder(Id.class).build()).build());
        for (ActivityEntity.ExtraAttr extra : extras) {
            if (extra.type == ActivityEntity.ExtraAttr.Type.Param) {
                parameterSpecs.add(
                        ParameterSpec.builder(TypeName.get(extra.typeElement.asType()), extra.paramName)
                                .addAnnotation(
                                        AnnotationSpec.builder(Param.class)
                                                .addMember("value", "$S", extra.paramAnnoName)
                                                .build()
                                )
                                .build()
                );
            }
        }
        return parameterSpecs;
    }

    private Iterable<ParameterSpec> genStartActivityParameters(List<ActivityEntity.ExtraAttr> extras) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();

        parameterSpecs.add(ParameterSpec.builder(contextClass, "context").build());
        parameterSpecs.add(ParameterSpec.builder(stringClass, "callId").build());
        for (ActivityEntity.ExtraAttr extra : extras) {
            if (extra.type == ActivityEntity.ExtraAttr.Type.Param) {
                parameterSpecs.add(
                        ParameterSpec.builder(TypeName.get(extra.typeElement.asType()), extra.paramName).build()
                );
            }
        }
        return parameterSpecs;
    }

    private Map<String, List<ActivityEntity>> scanActivity(Set<? extends Element> elements) {
        Map<String, List<ActivityEntity>> activityEntitiesMap = new HashMap<>();
        for (Element element : elements) {
            ActivityEntity entity = new ActivityEntity();
            if (element instanceof TypeElement) {
                TypeElement typeElement = (TypeElement) element;
                Host hostAnnotation = element.getAnnotation(Host.class);
                Path pathAnnotation = element.getAnnotation(Path.class);

                if (hostAnnotation == null || hostAnnotation.value().length() == 0) {
                    throw new IllegalArgumentException("@Host value must not null class:" + entity.className);
                }

                if (pathAnnotation == null || pathAnnotation.value().length() == 0) {
                    throw new IllegalArgumentException("@Path value must not null class:" + entity.className);
                }

                entity.host = hostAnnotation.value();
                entity.path = pathAnnotation.value();
                entity.className = typeElement.asType().toString();

                if (moduleScheme != null && moduleScheme.length() > 0) {
                    entity.scheme = moduleScheme;
                }

                List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
                for (Element enclosedElement : enclosedElements) {
                    ActivityEntity.ExtraAttr extraAttr = new ActivityEntity.ExtraAttr();
                    if (enclosedElement instanceof VariableElement) {
                        VariableElement variableElement = (VariableElement) enclosedElement;
                        Param paramAnno = variableElement.getAnnotation(Param.class);
                        Id idAnno = variableElement.getAnnotation(Id.class);

                        if (paramAnno == null && idAnno == null) {
                            continue;
                        }

                        String paramName = variableElement.getSimpleName().toString();
                        String paramClassName = variableElement.asType().toString();

                        if (idAnno != null) {
                            extraAttr.type = ActivityEntity.ExtraAttr.Type.Id;
                        } else if (paramAnno != null) {
                            String paramAnnoName = paramAnno.value();
                            extraAttr.type = ActivityEntity.ExtraAttr.Type.Param;
                            extraAttr.paramAnnoName = paramAnnoName;
                        } else {
                            throw new IllegalArgumentException("@Param value must not null class:" + entity.className + " param:" + paramName);
                        }

                        extraAttr.paramName = paramName;
                        extraAttr.className = paramClassName;
                        extraAttr.typeElement = variableElement;
                        entity.extras.add(extraAttr);
                    }
                }

                List<ActivityEntity> activityEntities = activityEntitiesMap.get(hostAnnotation.value());
                if (activityEntities == null) {
                    activityEntitiesMap.put(hostAnnotation.value(), activityEntities = new ArrayList<>());
                }

                activityEntities.add(entity);
            }
        }


        return activityEntitiesMap;
    }


}
