package com.justec.socketcontrol;

public class SocketManager {
    SocketInterface<SocketStatus> Callback;
    private static volatile SocketManager instance = null;
    private SocketManager (){}

    public static SocketManager getInstance(){
        if(instance==null){
            synchronized(SocketManager.class){
                if(instance==null){
                    instance=new SocketManager();
                }
            }
        }
        return instance;
    }

    public void initCallback(SocketInterface callback){
        if (callback != null)
            this.Callback = callback;
    }

    public void StatusChange(SocketStatus status){
        Callback.SocketStatusCallback(status);
    }
}
