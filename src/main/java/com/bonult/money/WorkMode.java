package com.bonult.money;

/**
 * Created by bonult on 2018/1/31.
 */
public enum WorkMode {
	ANDROID_APP("Android App"),DESKTOP("Desktop"),ADB("ADB");

	WorkMode(String name){
		this.name = name;
	}

	private String name;

	public String getName(){
		return name;
	}
}
