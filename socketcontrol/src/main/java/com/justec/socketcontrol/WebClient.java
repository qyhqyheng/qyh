package com.justec.socketcontrol;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebClient extends WebSocketClient {
    //超时时间
    private long timeout = 1000;
    private TimeOutThread timeOutThread;
    private static WebClient mSingleton;
    //当前连接状态
    private static SocketStatus mStatus = SocketStatus.INIT;

    public static WebClient getSingleTon(URI serverUri) {
        if (mSingleton == null) {
            mSingleton = new WebClient(serverUri, new Draft_17());
        }
        return mSingleton;
    }

    /**
     * 获取当前状态
     * @return
     */
    public SocketStatus getStatus() {
        return mStatus;
    }

    public WebClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    @Override
    public void connect() {
        mStatus = SocketStatus.CONNECTING;
        onConnecting();
        super.connect();
    }

    @Override
    public boolean connectBlocking() throws InterruptedException {
        mStatus = SocketStatus.CONNECTING;
        onConnecting();
        return super.connectBlocking();
    }

    public void onConnecting() {
        Log.e("connect","onConnecting正在连接！！！");
        timeOutThread = new TimeOutThread();//开启超时任务线程
        timeOutThread.start();
        SocketManager.getInstance().StatusChange(SocketStatus.CONNECTING);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.e("connect","onOpen连接成功！！！");
        timeOutThread.cancel();
        mStatus = SocketStatus.CONNECTED;
        SocketManager.getInstance().StatusChange(SocketStatus.CONNECTED);
    }

    @Override
    public void onMessage(String message) {}

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (mStatus != SocketStatus.TIMEOUT){
            Log.e("connect","onClose连接失败！！！");
            Log.d("connect", "连接关闭:" + code + "," + reason + "," + remote);
            mSingleton = null;
            mStatus = SocketStatus.DISCONNECTED;
            timeOutThread.cancel();
            SocketManager.getInstance().StatusChange(SocketStatus.DISCONNECTED);
        }
    }

    @Override
    public void onError(Exception ex) {
        if (mStatus != SocketStatus.TIMEOUT){
            Log.e("connect","onError连接错误！！！"+ex.getMessage());
            mStatus = SocketStatus.ERROR;
            timeOutThread.cancel();
            SocketManager.getInstance().StatusChange(SocketStatus.ERROR);
        }
    }

    /**
     * 连接超时检测线程
     * @author lucher
     *
     */
    public class TimeOutThread extends Thread {
        private boolean cancel;    //是否取消
        @Override
        public synchronized void run() {
            try {
                wait(timeout);
                if (!cancel) {
                    close();
                    SocketManager.getInstance().StatusChange(SocketStatus.TIMEOUT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         * 取消
         */
        public void cancel() {
            cancel = true;
        }
    }

    /**
     * 设置超时时间
     * @param timeout
     */
    public void setTimeOut(long timeout) {
        this.timeout = timeout;
    }
}
