package org.hfyd.component.crouter.core.client;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;


public class CRouterClientUtils {

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void startActivity(final Context context, final Intent intent) {
        if (context == null) {
            return;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (context instanceof Application) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                context.startActivity(intent);
            }
        });
    }
}
