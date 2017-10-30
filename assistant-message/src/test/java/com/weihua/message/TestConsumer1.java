package com.weihua.message;

public class TestConsumer1 implements MessageConsumer {

	@Override
	public void doHandle(Message message) {
		System.out.println("TestConsumer1:" + message);
	}

}
