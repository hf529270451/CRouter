package org.hfyd.component.b;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.hfyd.component.b.api.ModuleBParam;
import org.hfyd.component.b.api.ModuleBResult;
import org.hfyd.component.base.BaseActivity;
import org.hfyd.component.c.api.Module3Param;
import org.hfyd.component.c.api.ModuleCActivityControllerApi;
import org.hfyd.component.crouter.CRouter;
import org.hfyd.component.crouter.core.client.RouterCallback;
import org.hfyd.component.crouter.core.client.RouterResult;
import org.hfyd.component.crouter.core.server.ResponseDispatch;
import org.hfyd.component.crouter.core.server.ResponseResult;

public class ModuleB1Activity extends BaseActivity {

    private String callId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        ModuleBParam param = (ModuleBParam) getIntent().getSerializableExtra("param");
        callId = getIntent().getStringExtra("callId");
        Log.e("CRouter","ModuleB1Activity:" + param);

        final ModuleCActivityControllerApi moduleCActivityControllerApi = CRouter.api(ModuleCActivityControllerApi.class);

        findViewById(R.id.tv_start1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moduleCActivityControllerApi.startModuleC2Activity(ModuleB1Activity.this,
                        "from ModuleB1Activity startModuleC2Activity",2002, new Module3Param("from ModuleB1Activity startModuleC2Activity"))
                        .routeSync();
            }
        });

        findViewById(R.id.tv_start2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moduleCActivityControllerApi.startModuleC1Activity(ModuleB1Activity.this,
                        "from ModuleB1Activity startModuleC1Activity",2002, new Module3Param("from ModuleB1Activity startModuleC1Activity"))
                        .routeAsync(new RouterCallback() {
                            @Override
                            public void onComplete(RouterResult result) {
                                Log.e("CRouter", "ModuleB1Activity startModuleC1Activity result:" + result);
                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ResponseDispatch.send(callId, ResponseResult.success(new ModuleBResult(1001)));
    }
}
