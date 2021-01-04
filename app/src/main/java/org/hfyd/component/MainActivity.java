package org.hfyd.component;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.hfyd.component.a.api.ModuleAParam;
import org.hfyd.component.a.api.ModuleAServerControllerApi;
import org.hfyd.component.crouter.CRouter;
import org.hfyd.component.crouter.core.CRouterLogger;
import org.hfyd.component.crouter.core.client.RouterCallback;
import org.hfyd.component.crouter.core.client.RouterResult;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CRouter.init(getApplication());
        CRouterLogger.debugger(BuildConfig.DEBUG);
    }

    public void getModuleAInfo(View view) {
        RouterResult result = CRouter.api(ModuleAServerControllerApi.class).getModuleAInfo(new ModuleAParam("ModuleAParam1"), 200)
                .routeSync();

        Log.e("CRouter", "MainActivity getModuleAInfo result:" + result);
    }

    public void testSchemeUri(View view) {

    }

    public void getModuleAInfoAsync(View view) {
        CRouter.api(ModuleAServerControllerApi.class).getModuleAInfoAsync(new ModuleAParam("ModuleAParam1"), 200)
                .timeout(5000)
                .routeAsync(new RouterCallback() {
                    @Override
                    public void onComplete(RouterResult result) {
                        Log.e("CRouter", "MainActivity getModuleAInfo result:" + result);
                    }
                });
    }

    public void startModuleAActivity(View view) {
        CRouter.api(ModuleAServerControllerApi.class).startModuleAActivity(this, new ModuleAParam("startModuleAActivity1111"), true)
                .routeSync();
    }

    public void startModuleAActivityForResult(View view) {
        CRouter.api(ModuleAServerControllerApi.class).startModuleAActivityForResult(this, new ModuleAParam("startModuleAActivityForResult222"), 1.2f)
                .routeAsync(new RouterCallback() {
                    @Override
                    public void onComplete(RouterResult result) {
                        Log.e("CRouter", "MainActivity getModuleAInfo result:" + result);
                    }
                });
    }
}
