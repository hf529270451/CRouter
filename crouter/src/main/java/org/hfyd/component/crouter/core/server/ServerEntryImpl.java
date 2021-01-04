package org.hfyd.component.crouter.core.server;

import android.util.Log;

import org.hfyd.component.crouter.core.CRouterLogger;
import org.hfyd.component.crouter.core.bus.Bus;
import org.hfyd.component.crouter.core.bus.ProtocolCode;

import java.util.List;

public class ServerEntryImpl implements ServerEntry {

    private ControllerRegister mControllerRegister;

    public ServerEntryImpl() {
        mControllerRegister = new ControllerRegister();
    }

    public void registerController(String host, ServerController controller) {
        mControllerRegister.register(host, controller);
    }

    @Override
    public Bus.Result execute(Bus.Call call) {
        Bus.Result busResult;

        String host = call.getHost();
        List<ServerController> serverControllers = mControllerRegister.findController(host);
        if (serverControllers == null || serverControllers.size() == 0) {
            return Bus.Result.error(ProtocolCode.PATH_NOT_FIND.getCode());
        }
        ServerController.Request request = ServerController.Request.from(call);
        String callId = request.getCallId();

        ServerController.Response response = new ServerController.Response();
        response.setAsync(call.isAsync());
        if (call.isAsync() && call.getCallback() != null) {
            response.setCallback(call.getCallback());
        }

        ResponseDispatch.register(callId, response);

        boolean isSuccess = dispatchRequest(serverControllers, request, response);
        if (call.isAsync()) {
            busResult = new Bus.Result(response.getCode(), response.getMsg(), null);
            if (!isSuccess) {
                ResponseDispatch.send(request.getCallId(), ResponseResult.error(busResult.getCode(), busResult.getMsg()));
            }
        } else {
            busResult = new Bus.Result(response.getCode(), response.getMsg(), response.getBody());
        }

        if (busResult.getCode() != ProtocolCode.SUCCESS.getCode()) {
            ResponseDispatch.unregister(callId);
        }

        return busResult;
    }

    private boolean dispatchRequest(List<ServerController> serverControllers, ServerController.Request request, ServerController.Response response) {
        int code = ProtocolCode.SUCCESS.getCode();
        String msg = ProtocolCode.SUCCESS.getMsg();
        try {
            boolean isDispatch = false;
            for (ServerController serverController : serverControllers) {
                isDispatch = serverController.dispatchPath(request);
                if (isDispatch) {
                    break;
                }
            }

            if (!isDispatch) {
                code = ProtocolCode.PATH_NOT_FIND.getCode();
                msg = ProtocolCode.PATH_NOT_FIND.getMsg();
            }
        } catch (Exception e) {
            code = ProtocolCode.PATH_EXE_ERROR.getCode();
            msg = e.getMessage();
            CRouterLogger.error(Log.getStackTraceString(e));
        }

        response.setCode(code);
        response.setMsg(msg);
        return code == ProtocolCode.SUCCESS.getCode();
    }
}
