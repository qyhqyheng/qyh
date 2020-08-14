package com.justec.pillowalcohol.listview;

import java.util.ArrayList;
import java.util.List;

public class ChildBean {
	private String date;//item测试开始时间
	private String testTime;//item测试总时间，string为时 分 秒
    private int count;//item测试段内数据个数
	private String time;//item测试总时间 毫秒单位
    private String limiteValue;//item测试时当时限值

    private List<String> testValue =new ArrayList<>();
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }

	public String getdate() {
		return date;
	}

	public void setdate(String date) {
		this.date = date;
	}
	public String gettime() {
		return time;
	}

	public void settime(String time) {
		this.time = time;
	}

	public String getdateValue() {
		return testTime;
	}

	public void setdateValue(String testTime) {
		this.testTime = testTime;
	}

	public List<String> getTestValue() {
		return testValue;
	}
	public void setTestValue(List<String> testValue) {
		this.testValue = testValue;
	}

    public String getLimiteValue() {
        return limiteValue;
    }
    public void setLimiteValue(String limiteValue) {
        this.limiteValue = limiteValue;
    }
	public ChildBean(String date, String testTime, List<String > testValue) {
		this.date = date;
		this.testTime = testTime;
		this.testValue = testValue;
	}
	public ChildBean(String date, String testTime,int count,String time,String limiteValue) {
		this.date = date;
		this.testTime = testTime;
        this.count = count;
		this.time = time;
		this.limiteValue = limiteValue;
	}

}
