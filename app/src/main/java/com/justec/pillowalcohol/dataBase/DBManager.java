package com.justec.pillowalcohol.dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.justec.blemanager.utils.BleLog;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context){
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }

    public void add(List<Person> persons){
        db.beginTransaction();
        try{
            for (Person p:persons){
                db.execSQL("INSERT INTO person VALUES(null,?,?)",
                        new Object[]{p.getName(),p.getInfo()});
            }
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public void delete(List<Person> persons){
        db.beginTransaction();
        try{
            for (Person p:persons){
                db.execSQL("DELETE FROM person WHERE(?)",
                        new Object[]{p.getName(),p.getInfo()});
            }
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }

    public void updateAge(Person p){
        ContentValues cv = new ContentValues();
        //cv.put("age",p.getAge());
        db.update("person",cv,"name=?",new String[]{p.getName()});
    }


    public List<Person> findAllPerson(){
        ArrayList<Person> persons = new ArrayList<Person>();
        Cursor c = db.rawQuery("SELECT * FROM person", null);
        while(c.moveToNext()){
            Person p = new Person();
            p.set_id(c.getInt(c.getColumnIndex("_id")));
            p.setName(c.getString(c.getColumnIndex("name")));
            //p.setAge(c.getInt(c.getColumnIndex("age")));
            p.setInfo(c.getString(c.getColumnIndex("info")));
            persons.add(p);
        }
        c.close();
        return persons;
    }
    public List<Person> findFromId(int startId,int endId){
        ArrayList<Person> persons = new ArrayList<Person>();
        Cursor c = db.rawQuery("SELECT * FROM person where _id>="+startId+ " and _id<"+endId, null);
        while(c.moveToNext()){
            Person p = new Person();
            p.set_id(c.getInt(c.getColumnIndex("_id")));
            p.setName(c.getString(c.getColumnIndex("name")));
            //p.setAge(c.getInt(c.getColumnIndex("age")));
            p.setInfo(c.getString(c.getColumnIndex("info")));
            persons.add(p);
        }
        c.close();
        return persons;
    }
    public void deletTable(){
        try {
            db.execSQL("drop table itemtime ");
        }catch (Exception e){
            BleLog.e("deletTable---------"+e.toString());
        }
    }

    public void closeDB(){
        db.close();
    }

    
    public void addItem(List<ItemTime> itemTime){
        Log.d("Jerry.Xiao","itemTime.size = "+itemTime.size());
        db.beginTransaction();
        try{
            for (ItemTime item:itemTime){
                db.execSQL("INSERT INTO itemtime VALUES(null,?,?,?,?)",
                        new Object[]{item.getItemInfo(),item.getTimeTotal(),item.getItemCount(),item.getAlarmLimite()});
            }
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
    }
    /*
    * 查询item按照时间降序显示
    * */
    public List<ItemTime> queryItem(){
        Cursor c1=db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='itemtime'", null);
        if (c1.moveToNext()) {
            if(c1.getInt(0)<=0){
                c1.close();
                Log.d("Jerry.Xiao","no table-=--- ");
                return null;
            }
        }
        ArrayList<ItemTime> ItemTime = new ArrayList<ItemTime>();
        Cursor c = db.rawQuery("SELECT * FROM itemtime order by item DESC", null);
        while(c.moveToNext()){
            ItemTime item = new ItemTime();
            item.setItemInfo(c.getString(c.getColumnIndex("item")));
            item.setTimeTotal(c.getString(c.getColumnIndex("timeTotal")));
            item.setItemCount(c.getInt(c.getColumnIndex("itemCount")));
            item.setAlarmLimite(c.getString(c.getColumnIndex("alarmLimite")));
            ItemTime.add(item);
        }
        c.close();
        return ItemTime;
    }

    public int query(String time ){
        Cursor c = db.rawQuery("SELECT * FROM person where name =?", new String[]{time});
        int itemid = 0;
        while(c.moveToNext()){
            itemid = c.getInt(c.getColumnIndex("_id"));
            Log.d("Jerry.Xiao","itemid = "+itemid);
            return itemid;
        }
        return itemid;
    }
    /** 根据name删除数据 */
    public int deleteItem(String item) {
        Log.d("Jerry.Xiao","item = "+item);
        return db.delete("itemtime", "item=?", new String[] { item });
    }

    public void createTable(){
        db.execSQL("CREATE TABLE IF NOT EXISTS person(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " name VARCHAR, info TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS itemtime(itemId INTEGER PRIMARY KEY AUTOINCREMENT," +
                " item VARCHAR, timeTotal TEXT,itemCount INTEGER,alarmLimite INTEGER)");
    }
}
