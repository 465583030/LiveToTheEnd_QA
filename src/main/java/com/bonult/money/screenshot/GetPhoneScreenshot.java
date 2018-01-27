package com.bonult.money.screenshot;

import com.bonult.money.config.ConfigHolder;
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
public class GetPhoneScreenshot implements GetScreenshot {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetPhoneScreenshot.class);

	@Override
	public File getImgFile(){
		String imgFileName = ConfigHolder.CONFIG.getImageTempPath() + "phone.png";
		try{
			Process process = Runtime.getRuntime().exec(ConfigHolder.CONFIG.getAdbPath() + " shell /system/bin/screencap -p /sdcard/screenshot.png");
			process.waitFor();
			if(process.exitValue() == 0){
				process = Runtime.getRuntime().exec(ConfigHolder.CONFIG.getAdbPath() + " pull /sdcard/screenshot.png " + imgFileName);
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
						Rectangle rectangle = new Rectangle(ConfigHolder.CONFIG.getProblemAreaX(), ConfigHolder.CONFIG.getProblemAreaY(), ConfigHolder.CONFIG.getProblemAreaWidth(), ConfigHolder.CONFIG.getProblemAreaHeight());// 截取答题区域
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
