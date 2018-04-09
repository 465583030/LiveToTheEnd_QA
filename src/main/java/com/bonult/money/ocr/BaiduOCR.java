package com.bonult.money.ocr;

import com.baidu.aip.ocr.AipOcr;
import com.bonult.money.ConfigHolder;
import net.sf.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 识别图片上的文字
 */
public class BaiduOCR implements OCR {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaiduOCR.class);

	public List<String> getWords(byte[] bytes){
		List<String> result = new ArrayList<>();
		if(bytes == null || bytes.length == 0){
			return result;
		}
		AipOcr client = new AipOcr(ConfigHolder.CONFIG.getBdOcrApiID(), ConfigHolder.CONFIG.getBdOcrApiKey(), ConfigHolder.CONFIG.getBdOcrApiToken());

		HashMap<String,String> options = new HashMap<>();
		options.put("language_type", "CHN_ENG");

		JSONObject response = client.basicGeneral(bytes, options);
		net.sf.json.JSONObject object = net.sf.json.JSONObject.fromObject(response.toString());

		JSONArray jsonArray = object.getJSONArray("words_result");
		for(int i = 0; i < jsonArray.size(); i++){
			result.add(jsonArray.getJSONObject(i).getString("words"));
		}
		return result;
	}

}
