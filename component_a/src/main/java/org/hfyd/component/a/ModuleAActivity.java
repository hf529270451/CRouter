package org.hfyd.component.a;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.hfyd.component.a.api.ModuleAParam;
import org.hfyd.component.base.BaseActivity;


public class ModuleAActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_a);
        Intent intent = getIntent();
        boolean p2 = intent.getBooleanExtra("p2", false);
        ModuleAParam p1 = (ModuleAParam) intent.getSerializableExtra("p1");

        Log.e("CRouter", "module:ModuleA method:startModuleAActivity params:{p1:" + p1 + ",p2:" + p2 + "}");
    }
}
