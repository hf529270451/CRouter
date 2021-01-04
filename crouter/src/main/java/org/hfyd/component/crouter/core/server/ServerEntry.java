package org.hfyd.component.crouter.core.server;

import org.hfyd.component.crouter.core.bus.Bus;

public interface ServerEntry {

    Bus.Result execute(Bus.Call call);

    void registerController(String host, ServerController controller);
}