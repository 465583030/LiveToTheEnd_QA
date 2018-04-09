package com.bonult.money;

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

	private boolean rmvOptNum = false;
	private boolean rmvQuesNum = true;
	private int maxOptionNum = 3;

	private boolean ok = false;

	private WorkMode workMode; // 1-APP截图，2-桌面模拟器截图，3-ADB截图

	private String lookAndFeel;
	private int port = 8080;

	private String currConf = "默认.properties";

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
	public WorkMode getWorkMode(){
		return workMode;
	}
	public void setWorkMode(WorkMode workMode){
		if(this.workMode != workMode || this.workMode == null){
			this.workMode = workMode;
			try{
				Main.restart();
			}catch(Exception e){
				Main.errorMsgShow("启动失败", "出错了");
			}
		}
	}
	public String getLookAndFeel(){
		return lookAndFeel;
	}
	public void setLookAndFeel(String lookAndFeel){
		this.lookAndFeel = lookAndFeel;
	}
	public int getPort(){
		return port;
	}
	public void setPort(int port){
		if(this.port != port){
			this.port = port;
			try{
				Main.restart();
			}catch(Exception e){
				Main.errorMsgShow("启动失败", "出错了");
			}
		}
	}
	public String getCurrConf(){
		return currConf;
	}
	public void setCurrConf(String currConf){
		if(this.currConf != currConf){
			ConfigHolder.loadConfigs(currConf);
			this.currConf = currConf;
		}
	}
	public boolean isRmvOptNum(){
		return rmvOptNum;
	}
	public void setRmvOptNum(boolean rmvOptNum){
		this.rmvOptNum = rmvOptNum;
	}
	public boolean isOk(){
		return ok;
	}
	public void setOk(boolean ok){
		this.ok = ok;
	}
}
