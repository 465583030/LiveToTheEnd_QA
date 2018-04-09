package com.bonult.money.search;

/**
 * Created by bonult on 2018/1/29.
 */
public class SearchResultItem {
	public int id;
	public String title="";
	public String description="";

	SearchResultItem(int id){
		this.id = id;
	}

	@Override
	public String toString(){
		return id + "\n" + title + "\n" + description;
	}
}
