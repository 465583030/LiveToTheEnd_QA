package com.bonult.money.impl;

import com.bonult.money.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 调用百度搜索，返回特定结果
 *
 * @author bonult
 */
public class DefaultSearch implements Search {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSearch.class);

	public long getSearchResultCount(String word){
		long num = 0;
		try{
			String url = "http://www.baidu.com/s?ie=utf-8&tn=ichuner&lm=0&word=" + URLEncoder.encode(word, "UTF-8") + "&rn=1";
			URL get = new URL(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(get.openStream()));
			String line;
			while((line = br.readLine()) != null){
				int start = line.indexOf("百度为您找到相关结果约");
				if(start == -1){
					continue;
				}
				start += 11;
				int end = line.indexOf("个", start);
				line = line.substring(start, end);
				break;
			}
			br.close();

			line = line.replaceAll(",", "");
			return Long.parseLong(line);
		}catch(UnsupportedEncodingException | MalformedURLException e){
			LOGGER.error("wtf", e);
		}catch(IOException e){
			LOGGER.error("调用百度搜索失败", e);
		}
		return num;
	}

	public List<String> getSearchResult(String word){
		List<String> result = new ArrayList<>(100);
		try{
			String url = "http://www.baidu.com/s?ie=utf-8&tn=ichuner&lm=0&word=" + URLEncoder.encode(word, "UTF-8") + "&rn=50";
			URL get = new URL(url);
			BufferedReader br = new BufferedReader(new InputStreamReader(get.openStream()));
			String line;
			// TODO 字符串匹配
			while((line = br.readLine()) != null){
				line.equals("<div id=\"content_left\">");

				break;
			}
			br.close();

			line = line.replaceAll(",", "");
		}catch(UnsupportedEncodingException | MalformedURLException e){
			LOGGER.error("wtf", e);
		}catch(IOException e){
			LOGGER.error("调用百度搜索失败", e);
		}
		return result;
	}

}
