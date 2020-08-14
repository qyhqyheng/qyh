package com.justec.pillowalcohol.media;

import android.net.Uri;

public class SonesConfigue {
    private String name;
    private Uri uri;
    public SonesConfigue(String name,Uri uri){
        this.name = name;
        this.uri = uri;
    }
    public SonesConfigue(){}

    public String getName(){
        return this.name;
    }

    public Uri getUri(){
        return this.uri;
    }
}
