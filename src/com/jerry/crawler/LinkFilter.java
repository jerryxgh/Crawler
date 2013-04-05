package com.jerry.crawler;

public interface LinkFilter {
	public boolean accept(String url);
}
