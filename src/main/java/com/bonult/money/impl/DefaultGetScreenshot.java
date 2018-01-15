package com.bonult.money.impl;

import com.bonult.money.Config;
import com.bonult.money.GetScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 获取屏幕截图
 *
 * @author bonult
 */
public class DefaultGetScreenshot implements GetScreenshot {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGetScreenshot.class);

	public File getImg(){
//		String time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
		String imgFileName = Config.IMAGE_TEMP_PATH + /*time +*/ "b.png";
		try{
			Process process = Runtime.getRuntime().exec(Config.ADB_PATH + " shell /system/bin/screencap -p /sdcard/screenshot.png");
			process.waitFor();
			if(process.exitValue() == 0){
				process = Runtime.getRuntime().exec(Config.ADB_PATH + " pull /sdcard/screenshot.png " + imgFileName);
				process.waitFor();
				if(process.exitValue() == 0){
					File imgFile = new File(imgFileName);
					if(imgFile.exists()){ // 裁剪截图，取答题区域
						Iterator iterator = ImageIO.getImageReadersByFormatName("PNG"); // PNG BMP JPG
						ImageReader reader = (ImageReader)iterator.next();
						InputStream is = new FileInputStream(imgFile);
						ImageInputStream iis = ImageIO.createImageInputStream(is);
						reader.setInput(iis, true);

						ImageReadParam param = reader.getDefaultReadParam();
						Rectangle rectangle = new Rectangle(Config.PROBLEM_AREA_X, Config.PROBLEM_AREA_Y, Config.PROBLEM_AREA__WIDTH, Config.PROBLEM_AREA_HEIGHT);// 截取答题区域
						param.setSourceRegion(rectangle);
						BufferedImage bi = reader.read(0, param);

						File newFile = new File(imgFile.getAbsolutePath());
						ImageIO.write(bi, "PNG", newFile); // 会覆盖原图

						return newFile;
					}
				}
			}
		}catch(Exception e){
			LOGGER.error("获取截图失败！", e);
		}
		return null;
	}

}
