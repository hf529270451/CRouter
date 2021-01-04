package org.hfyd.component.c;

import android.os.Bundle;
import android.util.Log;

import org.hfyd.component.base.BaseActivity;
import org.hfyd.component.c.api.Module3Param;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;

@Host("moduleC")
@Path("/moduleCSchemeUriActivity")
public class ModuleCSchemeUriActivity extends BaseActivity {

    @Param("p1")
    public String p1;
    @Param("p2")
    public long p2;
    @Param("p3")
    public boolean p3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_2);

        Log.e("CRouter", "ModuleC2Activity " + p1 + " " + p2 + " " + p3);
    }
}
