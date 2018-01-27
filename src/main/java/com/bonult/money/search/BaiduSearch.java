package com.bonult.money.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
public class BaiduSearch implements Search {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaiduSearch.class);

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

	// TODO 略复杂，有bug
	public List<SearchResultItem> getSearchResult(String word){
		List<SearchResultItem> result = new ArrayList<>(55);
		try{
			String url = "http://www.baidu.com/s?ie=utf-8&tn=ichuner&lm=0&word=" + URLEncoder.encode(word, "UTF-8") + "&rn=5";
			URL get = new URL(url);
			InputStreamReader in = new InputStreamReader(get.openStream());
			int id = 1;
			if(moveToEnd(in, "div id=\"content_left\">")){
				while(id <= 50){
					moveToEnd(in, "id=\"" + id + "\"");
					moveTo(in, '>');
					moveToEnd(in, "<a");
					moveTo(in, '>');
					SearchResultItem item = new SearchResultItem();
					item.title = strUntil(in, "</a>");

					moveToEnd(in, "<div");
					moveTo(in, '<');
					int x = getNextChar(in);
					if(x == 'd'){
						moveTo(in, '>');
						x = getNextChar(in);
						if(x == '<'){
							x = getNextChar(in);
							if(x == 'p'){
								moveTo(in, '>');
								item.description = strUntil(in, "</p>");
							}else if(x == 'e'){
								item.description = "<e" + strUntil(in, "</div>");
							}else if(x == 's'){
								moveToEnd(in, "</span>");
								item.description = "<e" + strUntil(in, "</div>");
							}
						}else{
							item.description = strUntil(in, "</div>");
						}
					}else if(x == 's'){
						moveToEnd(in, "</span>");
						item.description = "<e" + strUntil(in, "</div>");
					}else{
						System.out.println("########################################");
					}
					result.add(item);
					id++;
				}
			}


			//<div style=\"clear:both;height:0;\"></div>")){


		}catch(UnsupportedEncodingException | MalformedURLException e){
			LOGGER.error("wtf", e);
		}catch(IOException e){
			LOGGER.error("调用百度搜索失败", e);
		}
		return result;
	}

	private int getNextChar(Reader reader) throws IOException{
		int c;
		while((c = reader.read()) > 32)
			;
		return c;
	}

	private boolean moveTo(Reader reader, char ch) throws IOException{
		int c;
		while((c = reader.read()) >= 0 && c != ch)
			;
		return c >= 0;
	}

	private String strUntil(Reader reader, char ch) throws IOException{
		int c;
		StringBuilder sb = new StringBuilder();
		while((c = reader.read()) >= 0 && c != ch)
			sb.append(c);
		return sb.toString();
	}

	private boolean moveToEnd(Reader reader, String str) throws IOException{
		if(str == null || str.length() == 0){
			return false;
		}else if(str.length() == 1)
			return moveTo(reader, str.charAt(0));

		int length = str.length() - 1;
		char start = str.charAt(0);
		char[] tag = str.substring(1).toCharArray();
		char[] buff = new char[length];
		int i = length;
		while(true){
			for(; i < length; i++){
				if(buff[i] == start)
					break;
			}
			int readN;
			if(i == length){
				if(!moveTo(reader, start))
					break;
				readN = reader.read(buff);
			}else if(i == length - 1){
				readN = reader.read(buff);
			}else{
				int tmp = ++i;
				for(; i < length; i++){
					buff[i - tmp] = buff[i];
				}
				readN = reader.read(buff, length - tmp, tmp);
				i = tmp;
			}
			if(readN == -1){
				break;
			}else if(i >= length && readN < length){
				break;
			}else if(readN < i){
				break;
			}
			i = 0;
			for(; i < length; i++)
				if(buff[i] != tag[i])
					break;
			if(i == length){
				return true;
			}
		}
		return false;
	}

	private String strUntil(Reader reader, String str) throws IOException{
		if(str == null || str.length() == 0){
			return "";
		}else if(str.length() == 1)
			return strUntil(reader, str.charAt(0));

		final int length = str.length() - 1;
		final char start = str.charAt(0);
		char[] tag = str.substring(1).toCharArray();
		char[] buff = new char[length];
		StringBuilder sb = new StringBuilder();
		int c, i = length;
		while(true){
			for(; i < length; i++){
				if(buff[i] != start)
					sb.append(buff[i]);
				else
					break;
			}
			int readN;
			if(i == length){
				while((c = reader.read()) >= 0 && c != start)
					sb.append(c);
				if(c < 0)
					break;
				readN = reader.read(buff);
			}else if(i == length - 1){
				readN = reader.read(buff);
				c = start;
			}else{
				int tmp = ++i;
				for(; i < length; i++){
					buff[i - tmp] = buff[i];
				}
				readN = reader.read(buff, length - tmp, tmp);
				c = start;
				i = tmp;
			}
			if(readN == -1){
				break;
			}else if(i >= length && readN < length){
				for(int j = 0; j < readN; j++){
					sb.append(buff[j]);
				}
				break;
			}else if(readN < i){
				for(int j = 0; j < readN; j++){
					sb.append(buff[length - i + j]);
				}
				break;
			}
			i = 0;
			for(; i < length; i++)
				if(buff[i] != tag[i])
					break;
			if(i == length){
				break;
			}else{
				sb.append(c);
			}
		}
		return sb.toString();
	}

}

class SearchResultItem {
	String title;
	String description;

	@Override
	public String toString(){
		return title + "\n" + description;
	}
}
