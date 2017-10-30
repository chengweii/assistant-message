package com.weihua.message;

import java.util.Date;

import com.weihua.common.util.EmailUtil;

public class Test {
	public static void main(String[] args) {
		EmailUtil.initDefaultConfig("333@163.com", "333");
		MessageConstant.MESSAGE_CONFIG_PATH = "assets/mqtest";

		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					testSend();
				}
			}
		});
		t1.start();
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					testReceive();
				}
			}
		});
		t2.start();
	}

	public static void testSend() {
		Message message = new Message();
		message.setSendTime(new Date());
		message.setMessageQuene("test_queue");
		message.setContent("test message");
		MessageService.send(message);
	}

	public static void testReceive() {
		MessageService.notifyConsumers();
	}
}
