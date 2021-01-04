package org.hfyd.component.crouter.core.bus;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class Utils {

    static final String META_DATA_KEY = "org.hfyd.component.crouter.SCHEME";

    public static String getScheme(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                return ai.metaData.getString(META_DATA_KEY);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
