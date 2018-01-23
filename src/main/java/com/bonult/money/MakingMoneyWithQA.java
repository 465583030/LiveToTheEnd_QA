package com.bonult.money;

import com.bonult.money.config.ConfigHolder;
import com.bonult.money.ocr.OCR;
import com.bonult.money.screenshot.GetScreenshot;
import com.bonult.money.search.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
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

	public void run(){
		long start = System.currentTimeMillis();

		File imgFile = getScreenshot.getImg();
		if(imgFile == null){
			System.out.println("获取截图失败");
			return;
		}

		List<String> words = ocr.getWords(imgFile);
		if(words.size() == 0){
			System.out.println("没有识别到文字");
			return;
		}

		List<Option> options = new ArrayList<>(4);

		String questionSeg = separateQuestionAndAnswers(options, words);

		final String question = getQuestion(questionSeg);

		threadPool.submit(() -> openURL(question));

		search(options, question);

		System.out.println("\n=======用时: " + (System.currentTimeMillis() - start) + " ms=======");
	}

	private void search(List<Option> options, String question){
		AtomicInteger aiQA = new AtomicInteger(0);
		Callable<Long> callableQA = () -> {
			int index = aiQA.getAndIncrement();
			String A = options.get(index).option;
			return search.getSearchResultCount(question + " " + A);
		};

		AtomicInteger aiA = new AtomicInteger(0);
		Callable<Long> callableA = () -> {
			int index = aiA.getAndIncrement();
			String A = options.get(index).option;
			return search.getSearchResultCount(A);
		};

		List<Future<Long>> futuresQA = new ArrayList<>(options.size());
		List<Future<Long>> futuresA = new ArrayList<>(options.size());

		for(int i = 0; i < options.size(); i++){
			Future<Long> futureQA = threadPool.submit(callableQA);
			futuresQA.add(futureQA);
			Future<Long> futureA = threadPool.submit(callableA);
			futuresA.add(futureA);
		}

		for(int j = 0; j < options.size(); j++){
			try{
				options.get(j).quesAndOptCount = futuresQA.get(j).get();
			}catch(Exception e){
				LOGGER.error("并行任务出错", e);
			}
			try{
				options.get(j).optCount = futuresA.get(j).get();
			}catch(Exception e){
				LOGGER.error("并行任务出错", e);
			}
		}

		System.out.println(question + "?");
		System.out.println("查询结果：");
		if(options.size() > 0){
			int maxIndex = 0;
			double max = -1;
			for(int i = 0; i < options.size(); i++){
				if(options.get(i).getWeight() > max){
					max = options.get(i).getWeight();
					maxIndex = i;
				}
			}
			System.out.println(options.get(maxIndex).option + "\n");
		}

		double sum = 0;
		long countSum = 0;
		for(int j = options.size() - 1; j >= 0; j--){
			sum += options.get(j).getWeight();
			countSum += options.get(j).quesAndOptCount;
		}

		for(int j = options.size() - 1; j >= 0; j--){
			Option ops = options.get(j);
			System.out.printf("| %.1f%%", ops.getWeight() / sum * 100);
			System.out.printf(" | %.1f%% |  ", ops.quesAndOptCount * 1.0 / countSum * 100);
			System.out.println(ops.option);
		}
	}

	private String separateQuestionAndAnswers(List<Option> options, List<String> words){
		int lineCount = words.size();
		String questionSeg = "";

		int quesLineNum;
		for(quesLineNum = 0; quesLineNum < lineCount; quesLineNum++){
			int qIndex = words.get(quesLineNum).indexOf('?');
			if(qIndex > -1){
				break;
			}
		}

		if(lineCount >= 4){
			int i = lineCount - 1 - quesLineNum > 3 && ConfigHolder.CONFIG.getMaxOptionNum() == 4 ? 4 : 3; // 判断选项个数
			for(; i > 0; i--){
				lineCount--;
				options.add(new Option(removeABC(words.get(lineCount))));
			}
		}
		for(int i = 0; i < lineCount; i++){
			String q = words.get(i);
			int qIndex = q.indexOf('?');
			if(qIndex > -1){
				questionSeg += q.substring(0, qIndex);
				break;
			}else
				questionSeg += q;
		}
		return questionSeg;
	}

	private String getQuestion(String questionSeg){
		int quesStart = 0;
		for(; quesStart < questionSeg.length(); quesStart++){
			char p = questionSeg.charAt(quesStart);
			if((p >= '0' && p <= '9')){
				quesStart++;
			}else if(p == 'B'){ // 有时候OCR会把 8 识别为 B
				quesStart++;
				if(questionSeg.charAt(quesStart) == '.')
					quesStart++;
				break;
			}else if(p == '.'){
				quesStart++;
				break;
			}else{
				break;
			}
		}
		return questionSeg.substring(quesStart, questionSeg.length()).replaceAll("[,，;；'\"!`~:：《》]", "");
	}

	private String removeABC(String s){
		if(s.length() > 2 && s.charAt(1) == '.'){
			return s.substring(2);
		}else if(ConfigHolder.CONFIG.isRmvQuesNum() && s.length() > 1 && s.matches("^[ABCD].*")){
			return s.substring(1);
		}
		return s;
	}

	/**
	 * 打开浏览器，自动百度搜索问题
	 *
	 * @param word 问题
	 */
	private void openURL(String word){
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

class Option {
	String option;
	long quesAndOptCount;
	long optCount;
	private double weight;

	Option(String option){
		this.option = option;
		weight = -1;
	}

	public double getWeight(){
		if(weight < 0){
			weight = quesAndOptCount * 1.0 / optCount;
		}
		return weight;
	}
}
