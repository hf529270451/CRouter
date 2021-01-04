package org.hfyd.component.b.api;

import android.content.Context;

import org.hfyd.component.crouter.anno.Controller;
import org.hfyd.component.crouter.anno.Host;
import org.hfyd.component.crouter.anno.Id;
import org.hfyd.component.crouter.anno.Param;
import org.hfyd.component.crouter.anno.Path;

@Host("moduleB")
@Controller("org.hfyd.component.b.ModuleBServerControllerImpl")
public interface ModuleBServerController {

    @Path("startModuleB1Activity")
    void startModuleB1Activity(@Id String callId, @Param("param") ModuleBParam p, Context context);
}
