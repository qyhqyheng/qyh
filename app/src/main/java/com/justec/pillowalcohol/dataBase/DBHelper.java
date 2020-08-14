package com.justec.pillowalcohol.dataBase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.justec.pillowalcohol.helper.SDHelper;

import java.io.File;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = SDHelper.DB_DIR + File.separator +"record_data.db";
    //private static final String DATABASE_NAME = "record_data.db";
    private static final int DATABASE_VERSION = 1;
    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS person(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name VARCHAR, info TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS itemtime(itemId INTEGER PRIMARY KEY AUTOINCREMENT," +
                " item VARCHAR, timeTotal TEXT,itemCount INTEGER,alarmLimite INTEGER)");
        Log.d("Jerry.Xiao","onCreate database");
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("ALTER TABLE person COLUMN other STRING");
        db.execSQL("ALTER TABLE ItemTime COLUMN other STRING");

    }
    public static void deletDatabase(Context context){

        context.deleteDatabase(DATABASE_NAME);
    }
}
