package org.hfyd.component.a;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.hfyd.component.a.api.ModuleAParam;
import org.hfyd.component.a.api.ModuleAResult;
import org.hfyd.component.b.api.ModuleBInnerParam;
import org.hfyd.component.b.api.ModuleBParam;
import org.hfyd.component.b.api.ModuleBServerControllerApi;
import org.hfyd.component.base.BaseActivity;
import org.hfyd.component.crouter.CRouter;
import org.hfyd.component.crouter.core.client.RouterCallback;
import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.server.ResponseDispatch;
import org.hfyd.component.crouter.core.server.ResponseResult;

public class ModuleABctivity extends BaseActivity {

    private String callId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_2);
        Intent intent = getIntent();
        float p2 = intent.getFloatExtra("p2", 0);
        ModuleAParam p1 = (ModuleAParam) intent.getSerializableExtra("p1");
        callId = intent.getStringExtra("callId");

        Log.e("CRouter", "module:ModuleA method:startModuleABctivity params:{p1:" + p1 + ",p2:" + p2 + "}");
        final ModuleBServerControllerApi moduleBServerControllerApi = CRouter.api(ModuleBServerControllerApi.class);
        findViewById(R.id.bt_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moduleBServerControllerApi.startModuleB1Activity(new ModuleBParam(new ModuleBInnerParam("from ModuleABctivity")), ModuleABctivity.this)
                        .routeAsync(new RouterCallback() {
                            @Override
                            public void onComplete(RouterResult result) {
                                Log.e("CRouter", "startModuleB1ActivityForResult" + result);
                            }
                        });
            }
        });

        findViewById(R.id.bt_click_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RouterResult result = CRouter.newInstance()
                        .with(ModuleABctivity.this)
                        .uri(Uri.parse("czb366://moduleC/moduleCSchemeUriActivity?p1=fromMainActivity&p2=2003&p3=true"))
                        .routeSync();

                Log.e("CRouter", "MainActivity testSchemeUri:" + result);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ResponseDispatch.send(callId, ResponseResult.success(new ModuleAResult("ModuleABctivityResult", -10)));
    }
}
