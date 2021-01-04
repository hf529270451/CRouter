package org.hfyd.component.crouter.core.client.activity;

import android.app.Activity;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class CRouterActivityBinder {

    private static final Map<String, ActivityExtraBinder> sBinderMap = new HashMap<>();

    public static void init() {
        try {
            inject();
        } catch (Exception e) {
            Log.e("CRouter", Log.getStackTraceString(e));
        }
    }

    static void inject() throws Exception {

    }

    private static void register(String activityClassName, String binderClassName) throws Exception {
        sBinderMap.put(activityClassName, (ActivityExtraBinder) Class.forName(binderClassName).newInstance());
    }

    public static void bind(Activity activity) {
        ActivityExtraBinder activityExtraBinder = sBinderMap.get(activity.getClass().getCanonicalName());
        if (activityExtraBinder != null) {
            activityExtraBinder.bind(activity);
        }
    }
}
