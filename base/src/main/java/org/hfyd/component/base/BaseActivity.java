package org.hfyd.component.base;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import org.hfyd.component.crouter.core.client.activity.CRouterActivityBinder;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CRouterActivityBinder.bind(this);
    }
}
