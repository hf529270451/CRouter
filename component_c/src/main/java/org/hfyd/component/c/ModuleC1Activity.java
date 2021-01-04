package org.hfyd.component.c;

import android.os.Bundle;
import android.util.Log;

import org.hfyd.component.base.BaseActivity;
import org.hfyd.component.c.api.Module3Param;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Id;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;
import org.hfyd.component.crouter.core.server.ResponseDispatch;
import org.hfyd.component.crouter.core.server.ResponseResult;

@Host("moduleC")
@Path("/startModuleC1Activity4Result")
public class ModuleC1Activity extends BaseActivity {

    @Id
    public String callId;
    @Param("p1")
    public String p1;
    @Param("p2")
    public long p2;
    @Param("p3")
    public Module3Param p3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_1);
        Log.e("CRouter", "ModuleC1Activity " + p1 + " " + p2 + " " + p3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ResponseDispatch.send(callId, ResponseResult.success("我是ModuleC1Activity的结果"));
    }
}
