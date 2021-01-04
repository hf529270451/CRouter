package org.hfyd.component.crouter.core.client;

import android.app.Application;

import org.hfyd.component.crouter.core.bus.Utils;
import org.hfyd.component.crouter.core.client.interceptor.RouterClientInterceptor;

import java.util.ArrayList;
import java.util.List;

public class RouterGlobal {

    private static Application sApplication;

    private static String sDefaultScheme;

    private static List<RouterClientInterceptor> sGlobalBeforeRouterClientInterceptors = new ArrayList<>();
    private static List<RouterClientInterceptor> sGlobalAfterRouterClientInterceptors = new ArrayList<>();

    public static void init(Application application) {
        sApplication = application;
        sDefaultScheme = Utils.getScheme(application);
    }

    public static String getDefaultScheme() {
        return sDefaultScheme;
    }

    public static Application getApplication() {
        return sApplication;
    }

    public static List<RouterClientInterceptor> getGlobalBeforeRouterClientInterceptors() {
        return sGlobalBeforeRouterClientInterceptors;
    }

    public static List<RouterClientInterceptor> getGlobalAfterRouterClientInterceptors() {
        return sGlobalAfterRouterClientInterceptors;
    }

    public static void addGlobalAfterRouterClientInterceptor(RouterClientInterceptor interceptor) {
        sGlobalAfterRouterClientInterceptors.add(interceptor);
    }

    public static void addGlobalBeforeRouterClientInterceptor(RouterClientInterceptor interceptor) {
        sGlobalBeforeRouterClientInterceptors.add(interceptor);
    }
}
