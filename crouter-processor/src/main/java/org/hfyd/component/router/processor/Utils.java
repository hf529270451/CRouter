package org.hfyd.component.router.processor;

class Utils {

    static boolean isPrimitive(Class clazz) {
        return clazz.isPrimitive();
    }

    //首字母转大写
    static String toUpperCaseFirstOne(String s){
        if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
    }

    static String getRealControllerClassName(String className) {
        return className.replace(".java", "").substring(className.lastIndexOf(".") + 1, className.length());
    }

    static String  getRealControllerClassPackage(String className) {
        return className.replace(".java", "").substring(0, className.lastIndexOf("."));
    }
}
