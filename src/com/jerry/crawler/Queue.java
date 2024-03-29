package com.jerry.crawler;

import java.util.LinkedList;

/**
 * 队列
 */
public class Queue<T> {

	private LinkedList<T> queue = new LinkedList<T>();

	public void enQueue(T t) {
		queue.addLast(t);
	}

	public T deQueue() {
		return queue.removeFirst();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public boolean contians(T t) {
		return queue.contains(t);
	}
}
