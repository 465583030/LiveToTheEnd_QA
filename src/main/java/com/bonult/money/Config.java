package com.bonult.money;

import com.bonult.money.tools.OrderedProperties;
import com.bonult.money.tools.PropsTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * 配置信息
 *
 * @author bonult
 */
public class Config {

	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

//	public static final int SCREEN_WIDTH;
//	public static final int SCREEN_HEIGHT;

	public static final int PROBLEM_AREA_X;
	public static final int PROBLEM_AREA_Y;
	public static final int PROBLEM_AREA__WIDTH;
	public static final int PROBLEM_AREA_HEIGHT;

	public static final String ADB_PATH;
	public static final String IMAGE_TEMP_PATH;

	public static final String BD_OCR_APP_ID;
	public static final String BD_OCR_API_KEY;
	public static final String BD_OCR_API_TOKEN;
	public static final String USER_DIR;

	static{
		USER_DIR = System.getProperty("user.dir") + File.separator;
		Properties props;
		try{
			props = PropsTool.loadProps(USER_DIR + "config.properties");
		}catch(IOException e){
			LOGGER.info("配置文件已经自动生成");
			props = new OrderedProperties();
			props.put("SCREEN_WIDTH", "1080");
			props.put("SCREEN_HEIGHT", "1920");

			props.put("PROBLEM_AREA_X", "100");
			props.put("PROBLEM_AREA_Y", "300");
			props.put("PROBLEM_AREA__WIDTH", "900");
			props.put("PROBLEM_AREA_HEIGHT", "950");

			props.put("ADB_PATH", "adb");
			props.put("IMAGE_TEMP_PATH", "");

			props.put("BD_OCR_APP_ID", "");
			props.put("BD_OCR_API_KEY", "");
			props.put("BD_OCR_API_TOKEN", "");
			writeConfig(USER_DIR, props);
		}

//		SCREEN_WIDTH = PropsTool.getInt(props, "SCREEN_WIDTH");
//		SCREEN_HEIGHT = PropsTool.getInt(props, "SCREEN_HEIGHT");

		PROBLEM_AREA_X = PropsTool.getInt(props, "PROBLEM_AREA_X");
		PROBLEM_AREA_Y = PropsTool.getInt(props, "PROBLEM_AREA_Y");
		PROBLEM_AREA__WIDTH = PropsTool.getInt(props, "PROBLEM_AREA__WIDTH");
		PROBLEM_AREA_HEIGHT = PropsTool.getInt(props, "PROBLEM_AREA_HEIGHT");

		ADB_PATH = PropsTool.getString(props, "ADB_PATH");
		IMAGE_TEMP_PATH = PropsTool.getString(props, "IMAGE_TEMP_PATH");

		BD_OCR_APP_ID = PropsTool.getString(props, "BD_OCR_APP_ID");
		BD_OCR_API_KEY = PropsTool.getString(props, "BD_OCR_API_KEY");
		BD_OCR_API_TOKEN = PropsTool.getString(props, "BD_OCR_API_TOKEN");

	}

	static void writeConfig(String path, Properties props){
		OutputStream fileOutputStream;
		try{
			fileOutputStream = new FileOutputStream(path + "config.properties", false);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
			props.store(outputStreamWriter, "notice baidu ocr config");
		}catch(Exception e){
			LOGGER.error("文件写入错误", e);
		}
	}

}
