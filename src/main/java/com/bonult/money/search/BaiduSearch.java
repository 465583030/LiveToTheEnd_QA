package com.bonult.money.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
			BufferedReader br = new BufferedReader(new InputStreamReader(get.openStream(), "UTF-8"));
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
			if(line == null){
				return num;
			}
			line = line.replaceAll(",", "");
			return Long.parseLong(line);
		}catch(Exception e){
			LOGGER.error("调用百度搜索失败", e);
		}
		return num;
	}

	public List<SearchResultItem> getSearchResult(String word){
		List<SearchResultItem> result = new ArrayList<>(50);
		try{
			String url = "http://www.baidu.com/s?ie=utf-8&tn=ichuner&lm=0&word=" + URLEncoder.encode(word, "UTF-8") + "&rn=50";
			URL get = new URL(url);

//			String fileName = "/Users/bonult/资源/"+ System.currentTimeMillis()+".txt";
//			InputStream in0 = get.openStream();
//			OutputStream o = new FileOutputStream(fileName);
//			byte[] buff = new byte[4096];
//			int rc;
//			while((rc = in0.read(buff, 0, 4096)) > 0){
//				o.write(buff, 0, rc);
//			}
//			o.close();
//			in0.close();

			InputStreamReader in = new InputStreamReader(get.openStream()/*new FileInputStream(fileName)*/, "UTF-8");
			moveToEnd(in, "百度为您找到相关结果约");
			long resultNum;
			try{
				resultNum = Long.parseLong(strUntil(in, '个').replace(",", ""));
			}catch(Exception e){
				resultNum = 49;
			}

			int len = 49;
			if(resultNum < 50){
				len = (int)resultNum;
				if(len == 0){
					return result;
				}
			}

			if(!moveToEnd(in, "div id=\"content_left\">"))
				return result;

			int id = 1;
			while(id <= len){
				moveToEnd(in, "id=\"" + id + "\"");
				moveTo(in, '>');
				moveTo(in, '<');
				String tagName = getTagName(in);
				if(!"h3".equals(tagName)){
					id++;
					continue;
				}

				moveToEnd(in, "<a");
				moveTo(in, '>');
				SearchResultItem item = new SearchResultItem(id++);
				result.add(item);
				item.title = strUntil(in, "</a>");

				if(!moveWithLimit(in, "c-abstract"))
					continue;
				moveTo(in, '>');

				int nextChar;
				String pre = "";
				while((nextChar = getNextChar(in)) == '<'){
					tagName = getTagName(in);
					if("em".equals(tagName)){
						pre = strUntil(in, '<');
						moveTo(in, '>');
						nextChar = getNextChar(in);
						break;
					}else{
						moveToEnd(in, "</" + tagName + ">");
					}
				}
				if(nextChar < 0)
					break;
				item.description = pre + (char)nextChar + strUntil(in, "</div>");
				int index = item.description.indexOf('<');
				if(index > -1){
					item.description = item.description.substring(0, index);
				}
			}
			in.close();
		}catch(Exception e){
			e.printStackTrace();
			LOGGER.error("调用百度搜索失败", e);
		}
		return result;
	}

	// 获取下一个非空白字符
	private int getNextChar(Reader reader) throws IOException{
		int c;
		while((c = reader.read()) < 33 && c > 0)
			;
		return c;
	}

	private boolean moveTo(Reader reader, char ch) throws IOException{
		int c;
		while((c = reader.read()) >= 0 && c != ch)
			;
		return c >= 0;
	}

	// HTML字符未转换 &lt; e.g.
	private String strUntil(Reader reader, char ch) throws IOException{
		int c;
		CharArrayWriter writer = new CharArrayWriter();
		while((c = reader.read()) >= 0 && c != ch){
			if(c == '<')
				while((c = reader.read()) >= 0 && c != '>')
					;
			else if(c > 31)
				writer.append((char)c);
		}
		return writer.toString();
	}

	private String getTagName(Reader reader) throws IOException{
		int c;
		CharArrayWriter writer = new CharArrayWriter();
		while((c = reader.read()) >= 0 && c != ' ' && c != '>'){
			if(c == '<')
				while((c = reader.read()) >= 0 && c != '>')
					;
			else if(c > 31)
				writer.append((char)c);
		}
		return writer.toString();
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
					sb.append((char)c);
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
				sb.append((char)c);
			}
		}
		return sb.toString().replaceAll("(<em>)|(</em>)", "");
	}

	private boolean moveWithLimit(Reader reader, String end) throws IOException{
		int length = end.length() - 1;
		char start = end.charAt(0);
		char[] tag = end.substring(1).toCharArray();
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
			}else{
				int j = 0;
				for(; j < length; j++)
					if(buff[j] != UNFOUNDED_TAG[j])
						break;
				if(j == length)
					return false;
			}
		}
		return false;
	}
	private final char[] UNFOUNDED_TAG = "-containe".toCharArray();

	public static void main0(String[] args) throws IOException{
		BaiduSearch baiduSearch = new BaiduSearch();
		Random rado = new Random();
		int c = 0;
		for(int i = 0; i < 9999; i++){
			StringBuilder sb = new StringBuilder(i + 20);
			for(int j = 0; j < i; j++){
				sb.append(rado.nextInt(10));
			}
			StringReader in = null;
			String tmp = "33";
			long resultNum = 0;
			try{
				in = new StringReader(sb.toString() + "百度为您找到相关结果约9,999个");

				baiduSearch.moveToEnd(in, "百度为您找到相关结果约");
				tmp = baiduSearch.strUntil(in, '个').replace(",", "");
				resultNum = Long.parseLong(tmp);
			}catch(Exception e){
				System.out.println(tmp);
				e.printStackTrace();
			}
			if(resultNum != 9999)
				System.out.println(i);
			else
				c++;
		}
		System.out.println("end: " + c);
	}

	public static int longestCommonSubstring(String s1, String s2){
		if(s1 == null || s2 == null || s1.length() == 0 || s2.length() == 0)
			return 0;
		char[] str1 = s1.toCharArray();
		char[] str2 = s2.toCharArray();
		int start1 = -1;
		int start2 = -1;
		int[][] results = new int[str2.length][str1.length];
		//最大长度
		int maxLength = 0;
		int compareNum = 0;
		for(int i = 0; i < str1.length; i++){
			results[0][i] = (str2[0] == str1[i] ? 1 : 0);
			compareNum++;
			for(int j = 1; j < str2.length; j++){
				results[j][0] = (str1[0] == str2[j] ? 1 : 0);
				if(i > 0 && j > 0){
					if(str1[i] == str2[j]){
						results[j][i] = results[j - 1][i - 1] + 1;
						compareNum++;
					}
				}
				if(maxLength < results[j][i]){
					maxLength = results[j][i];
					start1 = i - maxLength + 2;
					start2 = j - maxLength + 2;
				}
			}
		}
		System.out.println("比较次数" + (compareNum + str2.length) + "，s1起始位置：" + start1 + "，s2起始位置：" + start2);
		return maxLength;
	}
}
