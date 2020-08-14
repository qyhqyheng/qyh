package com.justec.pillowalcohol.event;

public class MediaManager {
    MediaManager instance;
    public MediaManager getInstance(){
        if(instance==null){
            synchronized (MediaManager.class){
                if(instance==null){
                    instance = new MediaManager();
                }
            }
        }
        return instance;
    }

//    private static class MediaManagerHolder{
//        private static MediaManager instance = new MediaManager();
//    }
//    private static MediaManager MediaManagerInstance(){
//        return MediaManagerHolder.instance;
//    }
}
