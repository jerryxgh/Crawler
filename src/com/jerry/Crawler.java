package com.jerry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Crawler implements Runnable {
	private static int count = 0;
	private final int threadId = count++;
	private LinkDB linkDB;
	Crawler(LinkDB linkDB) {
		this.linkDB = linkDB;
	}
	private static LinkFilter filter = new LinkFilter() {
		public boolean accept(String url) {
			if(!url.endsWith(".ppt") && !url.endsWith(".xls")
					&& !url.endsWith(".doc") && !url.endsWith(".pdf")
					&& !url.endsWith(".zip") && !url.endsWith(".docx")
					&& !url.startsWith("mailto"))
				return true;
			else
				return false;
		}
	};

	/* 爬取线程 */
	@Override
	public void run() {	
		String visitUrl;
		while (!Thread.interrupted()) { // 始终循环
			visitUrl = linkDB.unVisitedUrlDeQueue();
			if (visitUrl == null) continue;	
			HtmlParser parser = null;
			try {
				parser = new HtmlParser(visitUrl, filter);
			} catch (IOException e) {
//				linkDB.addUnvisitedUrl(visitUrl);
//				System.out.println(visitUrl + "无法到达!");
				continue;
			}
//			System.out.println("您好，我是线程：" + this.threadId);
//			System.out.println(visitUrl);
//			System.out.println(parser.getTitle());
//			System.out.println(parser.getText());
			linkDB.addVisitedUrl(visitUrl); // 该 url 放入到已访问的 URL 中	
			List<String> links = parser.getLinks(); // 提取出网页中的 URL
			for(String link : links) { // 新的未访问的 URL 入队
				linkDB.addUnvisitedUrl(link);
			}
		}
	}

	/**
	 * 主程序入口
	 * @param args
	 */
	public static void main(String[]args) {
		int theradsNum = 2;
		LinkDB linkDB = new LinkDB();
		linkDB.addUnvisitedUrl("http://www.hhu.edu.cn");
		ExecutorService exec = Executors.newFixedThreadPool(theradsNum);
		for (int i = 0; i < theradsNum; ++i) {
			exec.submit(new Crawler(linkDB));
		}
		System.out.println("您好，已经正在为您努力抓取网页，您可以输入 exit 退出：");
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
		String signal;
		try {
			while((signal = stdin.readLine()) != null && !signal.equals("exit")) {
				System.out.println(signal);
			}
			exec.shutdownNow();
			System.out.println("再见！\n");
			linkDB.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}