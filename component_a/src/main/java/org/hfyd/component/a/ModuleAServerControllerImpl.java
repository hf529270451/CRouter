package org.hfyd.component.a;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import org.hfyd.component.a.api.ModuleAParam;
import org.hfyd.component.a.api.ModuleAResult;
import org.hfyd.component.a.api.ModuleAServerController;
import org.hfyd.component.crouter.core.server.ResponseDispatch;
import org.hfyd.component.crouter.core.server.ResponseResult;


public class ModuleAServerControllerImpl implements ModuleAServerController {

    private static Handler sMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void getModuleAInfo(String callId, ModuleAParam p1, long p2) {
        Log.e("CRouter", "module:ModuleA method:getModuleAInfo params:{p1:" + p1 + ",p2:" + p2 + "}");
        ResponseDispatch.send(callId, ResponseResult.success(new ModuleAResult(p1.getName(), p2)));
    }

    @Override
    public void getModuleAInfoAsync(final String callId, final ModuleAParam p1, final long p2) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(1000);
                Log.e("CRouter", "module:ModuleA method:getModuleAInfoAsync params:{p1:" + p1 + ",p2:" + p2 + "}");
                ResponseDispatch.send(callId, ResponseResult.success(new ModuleAResult(p1.getName(), p2)));
            }
        }
        ).start();
    }

    @Override
    public void startModuleAActivity(final Context context, final String callId, final ModuleAParam p1, final boolean p2) {
        Intent intent = new Intent(context, ModuleAActivity.class);
        intent.putExtra("p1", p1);
        intent.putExtra("p2", p2);
        context.startActivity(intent);
        ResponseDispatch.send(callId, ResponseResult.success(new ModuleAResult("startModuleAActivityResult", 100)));
    }

    @Override
    public void startModuleAActivityForResult(final Context context, final String callId, final ModuleAParam p1, final float p2) {
        sMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, ModuleABctivity.class);
                intent.putExtra("callId", callId);
                intent.putExtra("p1", p1);
                intent.putExtra("p2", p2);
                context.startActivity(intent);
            }
        });
    }


}
