package org.hfyd.component.router.processor;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

class ActivityEntity {

    String className;
    String scheme;
    String host;
    String path;

    List<ExtraAttr> extras = new ArrayList<>();

    static String getProxyControllerClassName(String host) {
        return host.substring(0, 1).toUpperCase() + host.substring(1) + "ActivityController";
    }

    static class ExtraAttr {
        Type type;
        String paramName;
        String paramAnnoName;
        String className;
        Element typeElement;

        enum Type {
            Id,
            Param;
        }

        @Override
        public String toString() {
            return "ExtraAttr{" +
                    "type=" + type +
                    ", paramName='" + paramName + '\'' +
                    ", paramAnnoName='" + paramAnnoName + '\'' +
                    ", className='" + className + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ActivityEntity{" +
                "className='" + className + '\'' +
                ", scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", path='" + path + '\'' +
                ", extras=" + extras +
                '}';
    }
}
