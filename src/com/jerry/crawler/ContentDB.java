package com.jerry.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mortbay.log.Log;

/**
 * 存储网页内容到磁盘和HDFS
 * @author jerry
 *
 */
public class ContentDB {
	private String fileName = "";
	private String toHDFS = "/user/jerry/";
	private String fileNameDateFormat = "yyyyMMddHH";
	
	public ContentDB(String toHDFS, String fileNameDateFormat) {
		this.toHDFS = toHDFS;
		this.fileNameDateFormat = fileNameDateFormat;
	}
	
	/**
	 * 按照小时将抓取的网页保存到不同的目录中
	 */
	synchronized public void save(long pageindex, String pageContent) {
		SimpleDateFormat format = new SimpleDateFormat(fileNameDateFormat);
		String fileNameNew = format.format(Calendar.getInstance().getTime());
		if (!fileNameNew.equals(fileName)) {
			if (new File(fileName).exists()) {
				ExecutorService exec = Executors.newFixedThreadPool(1);
				exec.execute(new PutMerge(new File(fileName).getAbsolutePath(), toHDFS + fileName + ".txt"));
				exec.shutdown();
			}
			fileName = fileNameNew;
		}
		
		File saveDir = new File(fileName);
		if (!saveDir.exists())
			saveDir.mkdirs();
		try {
			BufferedWriter bis = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName + "/" + pageindex)));
			bis.write(pageContent);
			bis.close();
		} catch (IOException e) {
		}
//		System.out.println(fileName);
	}
	
	/**
	 * 测试代码
	 * @param args
	 */
	public static void main(String[] args) {
//		ContentDB cdb = new ContentDB("/user/jerry/crawler/");
//		cdb.save(1, "hello!aaaa");
	}
}
