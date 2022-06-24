package com.hulk.model.pc.core;

/* loaded from: HulkUtilLib_v3.jar:com/hulk/model/pc/core/ConsumerBase.class */
public abstract class ConsumerBase<T> implements Runnable {
    private static final String TAG = "ConsumerBase";
    public static final long DEFAULT_ONCE_SLEEP_TIME = 50;
    protected IWarehouse<T> mWarehouse;
    protected boolean running = false;
    protected boolean stopped = false;
    protected int loopCount = 0;
    private boolean mDebugMode = false;

    protected abstract boolean doConsume(T t);

    public ConsumerBase(IWarehouse<T> warehouse) {
        this.mWarehouse = warehouse;
    }

    @Override // java.lang.Runnable
    public void run() {
        SysLog.i(TAG, "run: Stasrting...");
        this.running = true;
        doRun();
        SysLog.w(TAG, "run: Finished");
        this.running = false;
    }

    private void doRun() {
        while (true) {
            this.loopCount++;
            try {
            } catch (InterruptedException e) {
                SysLog.e(TAG, "doRun Interrupted: " + e);
            } finally {
                doFinal();
            }
            if (isStopped()) {
                String thread = getCurrentThreadInfo();
                SysLog.w(TAG, "doRun: Stopped, loopCount= " + this.loopCount + ", " + thread);
                return;
            }
            T product = this.mWarehouse.get();
            boolean consumed = doConsume(product);
            if (!consumed && isDebugMode()) {
                SysLog.e(TAG, "doRun: Failed consume product=" + product);
            }
            long sleepTime = sleepTime();
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
            doFinal();
        }
    }

    protected long sleepTime() {
        return 50L;
    }

    protected String getCurrentThreadInfo() {
        return SysLog.getCurrentThreadInfo();
    }

    protected void doFinal() {
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean onStopped() {
        return false;
    }

    public boolean isStopped() {
        if (onStopped()) {
            return true;
        }
        return this.stopped;
    }

    public void stopp() {
        this.stopped = true;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setDebugMode(boolean debugMode) {
        this.mDebugMode = debugMode;
    }

    public boolean isDebugMode() {
        return this.mDebugMode;
    }

    public int getLoopCount() {
        return this.loopCount;
    }
}