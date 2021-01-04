package org.hfyd.component.a.api;

import android.content.Context;

import org.hfyd.component.crouter.anno.Controller;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Id;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;

@Host("moduleA")
@Controller("org.hfyd.component.a.ModuleAServerControllerImpl")
public interface ModuleAServerController {

    @Path("/getModuleAInfoSync")
    void getModuleAInfo(@Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") long p2);

    @Path("/getModuleAInfoAsync")
    void getModuleAInfoAsync(@Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") long p2);

    @Path("/startModuleAActivity")
    void startModuleAActivity(Context context, @Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") boolean p2);

    @Path("/startModuleAActivity4Result")
    void startModuleAActivityForResult(Context context, @Id String callId, @Param("p1") ModuleAParam p1, @Param("p2") float p2);
}
