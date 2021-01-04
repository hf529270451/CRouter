package org.hfyd.component.crouter.core.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ControllerRegister {

    private Map<String, List<ServerController>> controllerMap = new HashMap<>();

    void register(String host, ServerController controller) {
        List<ServerController> serverControllers = controllerMap.get(host);
        if (serverControllers == null) {
            controllerMap.put(host, serverControllers = new ArrayList<>());
        }
        serverControllers.add(controller);
    }

    List<ServerController> findController(String host) {
        return controllerMap.get(host);
    }


}
