package org.hfyd.component.crouter.core.bus;

class MonitorCallback implements Bus.Callback {

    private Bus.Call call;
    private Bus.Callback origin;

    MonitorCallback(Bus.Call call, Bus.Callback origin) {
        this.call = call;
        this.origin = origin;
        if (call.getTimeout() != -1) {
            Monitor.monitorTimeout(call);
        }
    }

    @Override
    public void onComplete(Bus.Result result) {
        if (!call.isFinished()) {
            call.setFinished(true);
            Monitor.unMonitorTimeout(call);
            origin.onComplete(result);
        }
    }
}
