package com.bonult;

import com.bonult.money.search.BaiduSearch;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

/**
 * Created by bonult on 2018/1/24.
 */
public class SearchTest {

	@Test
	public void testBaiduSearch(){
		String key = "在我国南方春节吃汤圆的寓意是";
//		String key = "“东风不与周郎便铜雀春深锁二乔”这里的东风指的是什么";
		List<?> r = new BaiduSearch().getSearchResult(key);
		for(Object o : r){
			System.out.println(o);
			System.out.println();
		}
	}

	//	@Test
	public void testStringReader() throws IOException{
		String x = "";
		StringReader sr = new StringReader(x);
		char[] buff = new char[5];
		System.out.println(sr.read(buff));
	}

}

