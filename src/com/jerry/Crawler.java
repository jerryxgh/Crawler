package com.jerry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Crawler implements Runnable {
	private static boolean hasBegun = false;
	private static int count = 0;
	private final int threadId = count++;

	synchronized boolean  hasBegun() {
		return Crawler.hasBegun;
	}

	synchronized void begin() {
		Crawler.hasBegun = true;
		this.init();
	}

	private void init() {
		LinkDB.addUnvisitedUrl("http://www.java2s.com");
	}

	/* 爬取方法*/
	@Override
	public void run() {
		LinkFilter filter = new LinkFilter() {
			//提取以 http://www.java2s.com/Code 开头的链接
			public boolean accept(String url) {
				if(url.startsWith("http://www.java2s.com/Code") 
						&& !url.startsWith("http://www.java2s.com/Code/JavaDownload")
						&& !url.endsWith("zip"))
					return true;
				else
					return false;
			}
		};
		
		if (!this.hasBegun()) {
			this.begin();
		}		
		//循环条件：待抓取的链接不空时
		for (String visitUrl = LinkDB.unVisitedUrlDeQueue(); true; visitUrl = LinkDB.unVisitedUrlDeQueue()) {
			if (visitUrl == null) continue;	
			HtmlParser parser = null;
			try {
				parser = new HtmlParser(visitUrl, filter);
			} catch (IOException e) {
				LinkDB.addUnvisitedUrl(visitUrl);
				System.out.println(visitUrl + "can't be reached. But I will try it again.");
				continue;
			}
			//该 url 放入到已访问的 URL 中
			LinkDB.addVisitedUrl(visitUrl);

			// 保存网页中的代码（如果有的话）
			saveCode(visitUrl, parser);

			//提取出网页中的 URL
			Set<String> links = parser.getLinks();

			//新的未访问的 URL 入队
			for(String link : links) {
				LinkDB.addUnvisitedUrl(link);
			}                        
		}
	}

	/**
	 * 保存页面中的代码及其标题，目录结构与其对应的 url 相对应
	 * 其中代码的标题保存在 title 文件中，代码保存在 code0.txt code1.txt ... 中
	 * @param url 页面对应的绝对地址
	 * @param parser 页面的解析器
	 */
	public void saveCode(String url, HtmlParser parser) {
		// 页面中没有代码，返回
		System.out.println("正在访问的链接: " + url);
		if (parser.getCodeTitle() == null) {
			System.out.println("线程" + threadId + ": 该链接没有代码。。。");
			return;
		}

		System.out.println("CodeTitle:" + parser.getCodeTitle());
		//    	System.out.println(parser.getCodes().get(0));

		// 创建代码要保存的目录
		String directory = "D:" + url.substring(6, url.lastIndexOf('.'));
		File d = new File(directory);

		// 代码已经保存了
		if (d.exists()) {
			return;
		}
		d.mkdirs();

		// 保存代码的标题和代码
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(directory + "/title.txt");
			fileWriter.write(parser.getCodeTitle());
			fileWriter.close();

			List<String> codes = parser.getCodes();
			for (int i = 0; i < codes.size(); ++i) {
				fileWriter = new FileWriter(directory + "/code" + i + ".txt");
				fileWriter.write(codes.get(i));
				fileWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//main 方法入口
	public static void main(String[]args) {
		int theradsNum = 20;
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < theradsNum; ++i) {
			exec.execute(new Crawler());
		}
		exec.shutdown();
	}
}