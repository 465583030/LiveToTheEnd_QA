package com.bonult.money.impl;

import com.baidu.aip.ocr.AipOcr;
import com.bonult.money.Config;
import com.bonult.money.OCR;
import net.sf.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 识别图片上的文字
 */
public class DefaultOCR implements OCR {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOCR.class);

	public List<String> getWords(File imgFile){
		List<String> result = new ArrayList<>();
		if(imgFile == null || !imgFile.exists()){
			return result;
		}

		try{
			FileInputStream inputStream = new FileInputStream(imgFile);
			byte[] buffer = new byte[(int)imgFile.length()];
			inputStream.read(buffer, 0, buffer.length);

			AipOcr client = new AipOcr(Config.BD_OCR_APP_ID, Config.BD_OCR_API_KEY, Config.BD_OCR_API_TOKEN);

			HashMap<String,String> options = new HashMap<>();
			options.put("language_type", "CHN_ENG");

			JSONObject response = client.basicGeneral(buffer, options);
			net.sf.json.JSONObject object = net.sf.json.JSONObject.fromObject(response.toString());

			JSONArray jsonArray = object.getJSONArray("words_result");
			for(int i = 0; i < jsonArray.size(); i++){
				result.add(jsonArray.getJSONObject(i).getString("words"));
			}
		}catch(IOException e){
			LOGGER.error("图片识别失败", e);
		}

		return result;
	}

}
