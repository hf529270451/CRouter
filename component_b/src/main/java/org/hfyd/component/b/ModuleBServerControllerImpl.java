package org.hfyd.component.b;

import android.content.Context;
import android.content.Intent;

import org.hfyd.component.b.api.ModuleBParam;
import org.hfyd.component.b.api.ModuleBServerController;

public class ModuleBServerControllerImpl implements ModuleBServerController {

    @Override
    public void startModuleB1Activity(String callId, ModuleBParam p, Context context) {
        Intent intent = new Intent(context, ModuleB1Activity.class);
        intent.putExtra("callId", callId);
        intent.putExtra("param", p);
        context.startActivity(intent);
    }
}
