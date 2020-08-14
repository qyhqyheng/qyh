package com.justec.pillowalcohol.helper;

import android.os.Environment;

import java.io.File;

public class SDHelper {
    public static final String DB_DIR = Environment.getExternalStorageDirectory().getPath()
            + File.separator + "database" + File.separator
            + SDHelper.class.getPackage().getName();
    static {
        while(! Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        File dbFolder = new File(DB_DIR);
        // 目录不存在则自动创建目录
        if (!dbFolder.exists()){
            dbFolder.mkdirs();
        }
    }
}
