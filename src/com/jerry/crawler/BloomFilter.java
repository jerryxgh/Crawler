package com.jerry.crawler;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Random;

import org.apache.log4j.Logger;

import com.jerry.hmachash.SHA1;

class BloomFilter<E> {
	private Logger log = Logger.getLogger(Crawler.class);
	
    private BitSet bf;
    private int bitArraySize = 100000000;
    private int numHashFunc = 6;
    
    public BloomFilter() {
        bf = new BitSet(bitArraySize);
    }
    
    public void add(E obj) {
        int[] indexes = getHashIndexes(obj);
        
        for (int index : indexes) {
            bf.set(index);
        }
    }
    
    public boolean contains(E obj) {
        int[] indexes = getHashIndexes(obj);
        
        for (int index : indexes) {
            if (bf.get(index) == false) {
                return false;
            }
        }
        return true;
    }
    
    public void union(BloomFilter<E> other) {
        bf.or(other.bf);
    }
    
    protected int[] getHashIndexes(E obj) {
        int[] indexes = new int[numHashFunc];
        
        long seed = 0;
        byte[] digest;
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            md.update(obj.toString().getBytes());
//            digest = md.digest();
        	SHA1 sha1 = new SHA1();
        	digest = sha1.getDigestOfBytes(obj.toString().getBytes());
            
            for (int i = 0; i < 6; i++) { 
                seed = seed | (((long)digest[i] & 0xFF))<<(8*i);
            }
//        } catch (NoSuchAlgorithmException  e) {}
        
        Random gen = new Random(seed);
        
        for (int i = 0; i < numHashFunc; i++) {
            indexes[i] = gen.nextInt(bitArraySize);
        }
        
        return indexes;
    }
    
    public void write(DataOutput out) throws IOException {
        int byteArraySize = (int)(bitArraySize / 8);
        
        byte[] byteArray = new byte[byteArraySize];
        for (int i = 0; i < byteArraySize; i++) {
            byte nextElement = 0;
            for (int j = 0; j < 8; j++) {
                if (bf.get(8 * i + j)) {
                    nextElement |= 1<<j;
                }
            }
            
            byteArray[i] = nextElement;
        }
        out.write(byteArray);
    }
    
    public void readFields(DataInput in) throws IOException {
        int byteArraySize = (int)(bitArraySize / 8);
        byte[] byteArray = new byte[byteArraySize];
        
        in.readFully(byteArray);
        
        for (int i = 0; i < byteArraySize; i++ ) {
            byte nextByte = byteArray[i];
            for (int j = 0; j < 8; j++) {
                if (((int)nextByte & (1<<j)) != 0) {
                    bf.set(8 * i + j);
                }
            }
        }
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
    	BloomFilter<String> bf = new BloomFilter<String>();
    	for(int i = 0; i < 1; ++i) {
    	long startTime = System.nanoTime();
    	bf.add("hello" + i);
    	bf.log.info(bf.contains("hello" + i));
    	bf.log.info(System.nanoTime() - startTime);
    	}
    	
//    	String file = "BloomFilter.bitset";
//    	RandomAccessFile raf = null;
//    	BloomFilter<String> bf = new BloomFilter<String>();
////    	bf.add(file);
//    	
//    	try {
//			raf = new RandomAccessFile(file, "rw");
//		} catch (FileNotFoundException e) {
//			System.out.println("文件" + file + "不存在！");
//			e.printStackTrace();
//		}
//    	
//    	try {
//    		bf.write(raf);
//    		bf.readFields(raf);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		System.out.println(bf.contains(file));
//		System.out.println(bf.contains(file + "abcdefg1"));
	}
}