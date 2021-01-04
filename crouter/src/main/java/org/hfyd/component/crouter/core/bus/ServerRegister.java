package org.hfyd.component.crouter.core.bus;

import android.content.Context;
import android.util.Log;

import org.hfyd.component.crouter.core.server.ServerController;
import org.hfyd.component.crouter.core.server.ServerEntry;
import org.hfyd.component.crouter.core.server.ServerEntryImpl;

import java.util.HashMap;
import java.util.Map;

public class ServerRegister {

    private static Map<String, ServerEntry> sService = new HashMap<>();

    public static void init(Context context) {
        try {
            rejectService();
        } catch (Exception e) {
            Log.e("ServerRegister", Log.getStackTraceString(e));
        }
    }

    private static void rejectService() throws Exception {
    }

    public static void register(String className) throws Exception {
        register((ServerController) Class.forName(className).newInstance());
    }

    public static void register(ServerController serverController) {
        register(serverController.getScheme(), serverController.getHost(), serverController);
    }

    private static void register(String scheme, String host, ServerController serverController) {
        ServerEntry serverEntry = sService.get(scheme);
        if (serverEntry == null) {
            serverEntry = new ServerEntryImpl();
            sService.put(scheme, serverEntry);
        }

        serverEntry.registerController(host, serverController);

    }

    static ServerEntry findServerEntry(String scheme) {
        return sService.get(scheme);
    }
}
