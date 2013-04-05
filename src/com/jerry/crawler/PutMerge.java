package com.jerry.crawler;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class PutMerge implements Runnable {
	private String from = null;
	private String to = null;
	
	PutMerge(String from, String to) {
		this.from = from;
		this.to = to;
	}
    	
	@Override
	public void run() {
		System.out.println("In putmerge.");

        Configuration conf = new Configuration();
        FileSystem hdfs = null, local = null;
		try {
			hdfs = FileSystem.get(conf); // 与HDFS接口的FileSystem对象	        
			local = FileSystem.getLocal(conf); // 本地文件系统的FileSystem对象
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} 

        try {
        	Path inputDir = new Path(from); // 设定输入目录
        	Path hdfsFile = new Path(to); // 设定输出文件
        	if (!hdfs.exists(hdfsFile.getParent())) {
        		hdfs.mkdirs(hdfsFile.getParent());
        	}
            FileStatus[] inputFiles = local.listStatus(inputDir);
            FSDataOutputStream out = hdfs.create(hdfsFile);

            for (int i=0; i<inputFiles.length; i++) {
                System.out.println(inputFiles[i].getPath().getName());
                FSDataInputStream in = local.open(inputFiles[i].getPath());
                byte buffer[] = new byte[256];
                int bytesRead = 0;
                while( (bytesRead = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                in.close();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static void main(String[] args) {
		String fileName = "logs";
		ExecutorService exec = Executors.newFixedThreadPool(1);
		exec.execute(new PutMerge(new File(fileName).getAbsolutePath(), "/user/jerry/crawler1/" + fileName + ".txt"));
		exec.shutdown();
	}
}