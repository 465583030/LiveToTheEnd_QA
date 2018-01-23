package com.bonult.money.config;

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
public class ConfigHolder {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigHolder.class);

	static final String USER_DIR = System.getProperty("user.dir") + File.separator;

	public static final Config CONFIG = new Config();

	public static void loadConfigs(String fileName){
		Properties props;
		try{
			props = PropsTool.loadProps(USER_DIR + fileName);
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
			writeConfig(USER_DIR, props, fileName);
		}

		CONFIG.screenWidth = PropsTool.getInt(props, "SCREEN_WIDTH");
		CONFIG.screenHeight = PropsTool.getInt(props, "SCREEN_HEIGHT");

		CONFIG.problemAreaX = PropsTool.getInt(props, "PROBLEM_AREA_X");
		CONFIG.problemAreaY = PropsTool.getInt(props, "PROBLEM_AREA_Y");
		CONFIG.problemAreaWidth = PropsTool.getInt(props, "PROBLEM_AREA__WIDTH");
		CONFIG.problemAreaHeight = PropsTool.getInt(props, "PROBLEM_AREA_HEIGHT");

		CONFIG.adbPath = PropsTool.getString(props, "ADB_PATH");
		CONFIG.imageTempPath = PropsTool.getString(props, "IMAGE_TEMP_PATH");

		CONFIG.bdOcrApiID = PropsTool.getString(props, "BD_OCR_APP_ID");
		CONFIG.bdOcrApiKey = PropsTool.getString(props, "BD_OCR_API_KEY");
		CONFIG.bdOcrApiToken = PropsTool.getString(props, "BD_OCR_API_TOKEN");
	}

	static void writeConfig(String path, Properties props, String fileName){
		OutputStream fileOutputStream;
		try{
			fileOutputStream = new FileOutputStream(path + fileName, false);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
			props.store(outputStreamWriter, "notice baidu ocr config");
		}catch(Exception e){
			LOGGER.error("文件写入错误", e);
		}
	}

}
