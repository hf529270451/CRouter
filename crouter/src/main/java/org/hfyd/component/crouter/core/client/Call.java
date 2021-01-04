package org.hfyd.component.crouter.core.client;

import java.util.concurrent.Callable;

public interface Call<T> extends Callable<T> {

    void cancel();
}
