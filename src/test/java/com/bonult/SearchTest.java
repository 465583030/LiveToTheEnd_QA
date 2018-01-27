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
		List<?> r = new BaiduSearch().getSearchResult("按行");
		for(Object o : r){
			System.out.println(o);
			System.out.println();
		}
	}

//	@Test
	public void testReader() throws IOException{
		String x = "";
		StringReader sr = new StringReader(x);
		char[] buff =new char[5];
		System.out.println(sr.read(buff));
	}

}

