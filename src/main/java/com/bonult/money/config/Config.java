package com.bonult.money.config;

public class Config {
	int screenWidth;
	int screenHeight;

	int problemAreaX;
	int problemAreaY;
	int problemAreaWidth;
	int problemAreaHeight;

	String adbPath;
	String imageTempPath;

	String bdOcrApiID;
	String bdOcrApiKey;
	String bdOcrApiToken;

	private boolean rmvQuesNum;
	private int maxOptionNum = 4;

	public int getScreenWidth(){
		return screenWidth;
	}
	public int getScreenHeight(){
		return screenHeight;
	}
	public int getProblemAreaX(){
		return problemAreaX;
	}
	public int getProblemAreaY(){
		return problemAreaY;
	}
	public int getProblemAreaWidth(){
		return problemAreaWidth;
	}
	public int getProblemAreaHeight(){
		return problemAreaHeight;
	}
	public String getAdbPath(){
		return adbPath;
	}
	public String getImageTempPath(){
		return imageTempPath;
	}
	public String getBdOcrApiID(){
		return bdOcrApiID;
	}
	public String getBdOcrApiKey(){
		return bdOcrApiKey;
	}
	public String getBdOcrApiToken(){
		return bdOcrApiToken;
	}
	public boolean isRmvQuesNum(){
		return rmvQuesNum;
	}
	public void setRmvQuesNum(boolean rmvQuesNum){
		this.rmvQuesNum = rmvQuesNum;
	}
	public int getMaxOptionNum(){
		return maxOptionNum;
	}
	public void setMaxOptionNum(int maxOptionNum){
		this.maxOptionNum = maxOptionNum;
	}
}
