package com.bonult.money;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 程序主入口
 *
 * @author bonult
 */
public class MakingMoneyWithQA {

	private static final Logger LOGGER = LoggerFactory.getLogger(MakingMoneyWithQA.class);

	private GetScreenshot getScreenshot;
	private OCR ocr;
	private Search search;
	private ExecutorService threadPool;

	public MakingMoneyWithQA(GetScreenshot getScreenshot, OCR ocr, Search search){
		this.getScreenshot = getScreenshot;
		this.ocr = ocr;
		this.search = search;
		threadPool = Executors.newFixedThreadPool(10);
	}

	public void shutdown(){
		threadPool.shutdown();
	}

	private static class Answer {
		private String answer;
		private long qaCount;
//		private long aCount;

		Answer(String answer){
			this.answer = answer;
		}
	}

	public void run(){
		long start = System.currentTimeMillis();
		File imgFile = getScreenshot.getImg();
		if(imgFile == null){
			System.out.println("获取截图失败");
			return;
		}

		List<String> words = ocr.getWords(imgFile);

		int lineCount = words.size();

		String questionSeg = "";
		List<Answer> answers = new ArrayList<>(4);

		if(lineCount >= 4){
			for(int i = 3; i > 0; i--){// TODO 四个选项的情况
				answers.add(new Answer(words.get(lineCount - i)));
			}
			for(int i = 0; i < lineCount - 3; i++){
				questionSeg += words.get(i);
			}
		}else{
			// TODO 意外情况
		}

		int quesStart = 0;
		for(; quesStart < questionSeg.length(); quesStart++){
			char p = questionSeg.charAt(quesStart);
			if((p >= '0' && p <= '9')){
				quesStart++;
			}else if(p == '.'){
				quesStart++;
				break;
			}else{
				break;
			}
		}
		questionSeg = questionSeg.substring(quesStart, questionSeg.charAt(questionSeg.length() - 1) == '?' ? questionSeg.length() - 1 : questionSeg.length());

		final String question = questionSeg;

		AtomicInteger aiQA = new AtomicInteger(0);
		Callable<Long> callableQA = () -> {
			int index = aiQA.getAndIncrement();
			String A = answers.get(index).answer;
			return search.getSearchResultCount(question + " " + A);
		};
//		AtomicInteger aiA = new AtomicInteger(0);
//		Callable<Long> callableA = () -> {
//			int index = aiA.getAndIncrement();
//			String A = answers.get(index).answer;
//			return search.getSearchResultCount(A);
//		};
//		Callable<Long> callableQ = () -> search.getSearchResultCount(question);

		List<Future<Long>> futuresQA = new ArrayList<>(answers.size());
//		List<Future<Long>> futuresA = new ArrayList<>(answers.size());

//		Future<Long> futureQ = threadPool.submit(callableQ);
		threadPool.submit(() -> {
			openURL(question);
		});
		for(int i = 0; i < answers.size(); i++){
			Future<Long> future = threadPool.submit(callableQA);
			futuresQA.add(future);
//			Future<Long> futureA = threadPool.submit(callableA);
//			futuresA.add(futureA);
		}

		for(int j = 0; j < futuresQA.size(); j++){
			try{
				answers.get(j).qaCount = futuresQA.get(j).get();
			}catch(Exception e){
				LOGGER.error("并行任务出错", e);
			}
//			try{
//				answers.get(i).aCount=futuresA.get(i).get();
//			}catch(Exception e){
//				LOGGER.error("并行任务出错",e);
//			}
		}
//		long qCount = 0;
//		try{
//			qCount = futureQ.get();
//		}catch(Exception e){
//			LOGGER.error("并行任务出错", e);
//		}

		int maxLength = 0;
		for(Answer answer : answers){
			if(answer.answer.length() > maxLength)
				maxLength = answer.answer.length();
		}

		answers.sort(Comparator.comparingLong(a -> a.qaCount));

		System.out.println(question);
		System.out.println("查询结果：");
		if(answers.size() > 0)
			System.out.println(answers.get(answers.size() - 1).answer);

		System.out.println("\n");
		for(int j = answers.size() - 1; j >= 0; j--){
			Answer ans = answers.get(j);
			System.out.print(ans.answer);
			int l = ans.answer.length();
			while(l < maxLength){
				System.out.print(" ");
				l++;
			}
			System.out.println("| " + ans.qaCount);
		}

		System.out.println("\n\n=====================用时: " + (System.currentTimeMillis() - start) + " ms=====================");
	}

	public boolean checkArgs(String[] args){
		Map<String,String> params = new HashMap<>();
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			if(arg.charAt(0) == '-' && i + 1 < args.length && args[i + 1].charAt(0) != '-'){
				params.put(arg.substring(1), args[i + 1]);
				i++;
			}else if(arg.charAt(0) == '-'){
				params.put(arg.substring(1), "");
			}
		}

		if(params.containsKey("auto-config")){
			new AutoConfig().config(params.get("auto-config"), getScreenshot);
			return false;
		}

		if("".equals(Config.BD_OCR_API_KEY) || "".equals(Config.BD_OCR_API_TOKEN) || "".equals(Config.BD_OCR_APP_ID)){
			System.out.println("请填写完整OCR配置");
			return false;
		}

		return true;
	}

	/**
	 * 打开浏览器，自动百度搜索问题
	 *
	 * @param word 问题
	 */
	public void openURL(String word){
		// 见http://yuncode.net/code/c_516d4130f11f245
		try{
			String url = "http://www.baidu.com/s?ie=utf-8&tn=ichuner&lm=0&word=" + URLEncoder.encode(word, "UTF-8") + "&rn=20";
			String os = System.getProperty("os.name", "");
			if(os.startsWith("Windows")){
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}else if(os.startsWith("Mac OS")){
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
				openURL.invoke(null, new Object[]{url});
			}else{
				// Unix or Linux
				String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
				String browser = null;
				for(int count = 0; count < browsers.length && browser == null; count++)
					if(Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0)
						browser = browsers[count];
				if(browser == null)
					throw new Exception("Could not find web browser");
				else
					Runtime.getRuntime().exec(new String[]{browser, url});
			}
		}catch(Exception e){
			LOGGER.error("打开浏览器失败", e);
		}
	}
}
