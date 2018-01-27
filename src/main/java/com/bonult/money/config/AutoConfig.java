package com.bonult.money.config;

import com.bonult.money.screenshot.GetScreenshot;
import com.bonult.money.tools.OrderedProperties;
import com.bonult.money.tools.PropsTool;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * 自动配置一些图片相关的参数
 *
 * @author bonult
 */
public class AutoConfig {

	public void config(String filePath, GetScreenshot getScreenshot, String configFileName){
		File imgFile;
		if("".equals(filePath)){
			imgFile = getScreenshot.getImgFile();
		}else{
			imgFile = new File(filePath);
			if(!imgFile.exists()){
				System.out.println("图片不存在");
				return;
			}
		}

		BufferedImage image = toBufferedImage(new ImageIcon(imgFile.getAbsolutePath()).getImage());

		Properties props = new OrderedProperties();
		int width = image.getWidth();
		int height = image.getHeight();
		props.put("SCREEN_WIDTH", width + "");
		props.put("SCREEN_HEIGHT", height + "");
		int x1 = 100, y1 = 300, x2 = 1000, y2 = 1250;
		int[] pixs = image.getRGB(0, 0, width, height, null, 0, width);
		int continuous = 0, preCount = 0;
		for(int h = 0; h < height; h++){
			int index = h * width;
			int w = getX(width, pixs, index);
			int count = getCount(width, pixs, index, w);
			if(count * 1.0 / width > 0.7 && preCount == count){
				continuous++;
				if(continuous > 20){
					x1 = w;
					y1 = h - 20;
					x2 = x1 + count - 4;
					break;
				}else{
					continuous = 0;
				}
			}
			preCount = count;
		}
		preCount = 0;
		for(int h = height - 1; h >= 0; h--){
			int index = h * width;
			int w = getX(width, pixs, index);
			int count = getCount(width, pixs, index, w);
			if(count * 1.0 / width > 0.7 && preCount == count){
				y2 = h;
				break;
			}
			preCount = count;
		}

		Properties p;
		try{
			p = PropsTool.loadProps(ConfigHolder.USER_DIR + "config.properties");
		}catch(IOException e){
			System.out.println("wtf");
			return;
		}

		props.put("PROBLEM_AREA_X", x1 + "");
		props.put("PROBLEM_AREA_Y", y1 + "");
		props.put("PROBLEM_AREA__WIDTH", x2 - x1 + "");
		props.put("PROBLEM_AREA_HEIGHT", y2 - y1 + "");

		props.put("ADB_PATH", PropsTool.getString(p, "ADB_PATH"));
		props.put("IMAGE_TEMP_PATH", PropsTool.getString(p, "IMAGE_TEMP_PATH"));

		props.put("BD_OCR_APP_ID", PropsTool.getString(p, "BD_OCR_APP_ID"));
		props.put("BD_OCR_API_KEY", PropsTool.getString(p, "BD_OCR_API_KEY"));
		props.put("BD_OCR_API_TOKEN", PropsTool.getString(p, "BD_OCR_API_TOKEN"));
		ConfigHolder.writeConfig(ConfigHolder.USER_DIR, props, configFileName == null ? "config.properties" : configFileName);

	}

	private int getCount(int width, int[] pixs, int index, int w){
		int count = 0;
		while(w < width){
			int n = pixs[index + w] & 0x00FFFFFF;
			if(n >> 16 >= 200 && (n & 0x0000FF00) >> 8 > 200 && (n & 0x000000FF) > 200){
				count++;
				w++;
			}else{
				break;
			}
		}
		return count;
	}

	private int getX(int width, int[] pixs, int index){
		int w = 0;
		while(w < width){
			int n = pixs[index + w] & 0x00FFFFFF;
			if(!(n >> 16 >= 200 && (n & 0x0000FF00) >> 8 > 200 && (n & 0x000000FF) > 200)){
				w++;
			}else{
				break;
			}
		}
		return w;
	}

	// 来自 http://blog.csdn.net/ncepuzhuang/article/details/8083354
	private static BufferedImage toBufferedImage(Image image){
		if(image instanceof BufferedImage){
			return (BufferedImage)image;
		}
		boolean hasAlpha = hasAlpha(image);
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try{
			int transparency = Transparency.OPAQUE;
			if(hasAlpha){
				transparency = Transparency.BITMASK;
			}
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		}catch(HeadlessException e){
		}

		if(bimage == null){
			int type = BufferedImage.TYPE_INT_RGB;
			if(hasAlpha){
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		Graphics g = bimage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bimage;
	}

	// 来自网络 http://blog.csdn.net/ncepuzhuang/article/details/8083354
	private static boolean hasAlpha(Image image){
		if(image instanceof BufferedImage){
			BufferedImage bimage = (BufferedImage)image;
			return bimage.getColorModel().hasAlpha();
		}
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try{
			pg.grabPixels();
		}catch(InterruptedException e){
		}
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

}
