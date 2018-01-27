package com.bonult.money.screenshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 获取屏幕截图
 *
 * @author bonult
 */
public interface GetScreenshot {

	default byte[] getImg(){
		File imgFile = getImgFile();
		if(imgFile == null){
			return null;
		}
		try{
			InputStream inputStream = new FileInputStream(imgFile);
			byte[] buffer = new byte[(int)imgFile.length()];
			inputStream.read(buffer, 0, buffer.length);
			inputStream.close();
			return buffer;
		}catch(Exception e){
		}
		return null;
	}

	default File getImgFile(){
		return null;
	}

}
