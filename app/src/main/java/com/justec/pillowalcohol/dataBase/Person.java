package com.justec.pillowalcohol.dataBase;

import java.util.ArrayList;

/**
 * Created by tiancz on 2015/3/8.
 */
public class Person {
    private int _id;
    private String name;
    //private int age;
    private String info;

    public Person(){}

    public Person(String name, String info){
        this.name=name;
        //this.age=age;
        this.info=info;
    }
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

   /* public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

