package org.hfyd.component.router.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;
import javax.xml.bind.Element;

class ControllerEntity {

    String controllerImpl;
    String className;
    String scheme;
    String host;
    List<Action> actions = new ArrayList<>();

    String getProxyControllerApiClassName() {
        return getRealControllerClassName().replace(".java", "") + "Api";
    }

    String getProxyControllerClassName() {
        return getRealControllerClassName().replace(".java", "") + "_ServerController";
    }

    String getRealControllerClassName() {
        return className.replace(".java", "").substring(className.lastIndexOf(".") + 1, className.length());
    }

    String getRealControllerClassPackage() {
        return className.replace(".java", "").substring(0, className.lastIndexOf("."));
    }

    static class Action {
        String path;
        String methodName;
        Map<String, ParamAttr> params = new LinkedHashMap<>();

        @Override
        public String toString() {
            return "Action{" +
                    "path='" + path + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", params=" + params +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Action) {
                Action other = (Action) o;
                if (other.path != null) {
                    return other.path.equals(path);
                }
            }
            return super.equals(o);
        }
    }

    static class ParamAttr {
        enum Type {
            Id,
            Context,
            Other
        }

        Type type;
        String className;
        String paramAnnoName;
        String paramName;
        VariableElement paramElement;

        @Override
        public String toString() {
            return "Param{" +
                    "type=" + type +
                    ", className='" + className + '\'' +
                    ", paramName='" + paramName + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ParamAttr) {
                ParamAttr other = (ParamAttr) o;
                if (other.paramAnnoName != null) {
                    return other.paramAnnoName.equals(paramAnnoName);
                }
            }
            return super.equals(o);
        }
    }

    @Override
    public String toString() {
        return "ControllerEntity{" +
                "scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", actions=" + actions +
                '}';
    }
}
