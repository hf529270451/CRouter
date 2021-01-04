package org.hfyd.component.router.processor;

import com.google.auto.service.AutoService;

import org.hfyd.component.crouter.anno.Host;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "org.hfyd.component.crouter.anno.Scheme",
        "org.hfyd.component.crouter.anno.Host"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RouterControllerProcessor extends AbstractProcessor {

    private static final String SCHEME_KEY = "CROUTER_SCHEME";
    private static final String CROUTER_API_PACKAGE = "CROUTER_API_PACKAGE";

    private static final String ACTIVITY = "android.app.Activity";

    private Messager msg;
    private Filer filerUtils;
    private Elements elementUtils;
    private Types types;
    private ControllerProcessor controllerProcessor;
    private ActivityProcessor activityProcessor;

    private String moduleScheme;
    private String apiPackage;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        msg = processingEnvironment.getMessager();
        msg.printMessage(Diagnostic.Kind.NOTE, "RouterServerProcessor init");
        elementUtils = processingEnv.getElementUtils();
        types = processingEnv.getTypeUtils();
        filerUtils = processingEnv.getFiler();

        Map<String, String> options = processingEnv.getOptions();
        if (options != null && options.containsKey(SCHEME_KEY)) {
            moduleScheme = options.get(SCHEME_KEY);
            apiPackage = options.get(CROUTER_API_PACKAGE);
        }

        if (apiPackage == null || apiPackage.length() == 0) {
            throw new IllegalArgumentException("CROUTER_API_PACKAGE not registed");
        }

        controllerProcessor = new ControllerProcessor(msg, filerUtils, elementUtils, moduleScheme, apiPackage);
        activityProcessor = new ActivityProcessor(msg, elementUtils, filerUtils, moduleScheme, apiPackage);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> routeClassEleSet = roundEnvironment.getElementsAnnotatedWith(Host.class);

        Set<Element> activityClassEleSet = new HashSet<>();
        Set<Element> controllerClassEleSet = new HashSet<>();

        TypeMirror typeActivity = elementUtils.getTypeElement(ACTIVITY).asType();

        for (Element element : routeClassEleSet) {
            if (element instanceof TypeElement) {
                TypeMirror tm = element.asType();
                if (types.isSubtype(tm, typeActivity)) {
                    activityClassEleSet.add(element);
                } else {
                    controllerClassEleSet.add(element);
                }
            }
        }

        if (activityClassEleSet.size() == 0 && controllerClassEleSet.size() == 0) {
            return false;
        }

        activityProcessor.process(activityClassEleSet);
        controllerProcessor.process(controllerClassEleSet);

        return true;
    }


}
