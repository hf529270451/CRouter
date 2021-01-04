package org.hfyd.component.crouter.plugin

class RegisterCache {

    private static Set<String> registerServerControllerClasses = new HashSet<>();
    private static Map<String,String> registerActivityExtraBinderClasses = new HashMap<>();

    static void registerServerController(String serverRegisterClass) {
        registerServerControllerClasses.add(serverRegisterClass);
    }

    static Set<String> getAllServerControllers() {
        return registerServerControllerClasses;
    }

    static void registerActivityControllerClasses(String targetActivity, String activityExtraBinder) {
        registerActivityExtraBinderClasses.put(targetActivity, activityExtraBinder);
    }

    static Map<String,String> getAllActivityExtraBinders() {
        return registerActivityExtraBinderClasses;
    }
}