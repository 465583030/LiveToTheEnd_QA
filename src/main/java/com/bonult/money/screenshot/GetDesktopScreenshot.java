package com.bonult.money.screenshot;

import com.bonult.money.config.ConfigHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * 获取电脑上安卓模拟器截图（实际上是电脑屏幕截图）
 *
 * @author bonult
 */
public class GetDesktopScreenshot implements GetScreenshot {

	private static final Logger LOGGER = LoggerFactory.getLogger(GetDesktopScreenshot.class);

	@Override
	public File getImgFile(){
		String imgFileName = ConfigHolder.CONFIG.getImageTempPath() + "desktop.png";
		try{
			Rectangle rectangle = new Rectangle(ConfigHolder.CONFIG.getProblemAreaX(), ConfigHolder.CONFIG.getProblemAreaY(), ConfigHolder.CONFIG.getProblemAreaWidth(), ConfigHolder.CONFIG.getProblemAreaHeight());// 截取答题区域
			Robot robot = new Robot();
			BufferedImage image = robot.createScreenCapture(rectangle);
			File screenFile = new File(imgFileName);
			ImageIO.write(image, "png", screenFile);
			return screenFile;
		}catch(Exception e){
			LOGGER.error("获取截图失败！", e);
		}
		return null;
	}
}
