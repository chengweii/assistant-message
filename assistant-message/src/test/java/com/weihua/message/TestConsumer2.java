package com.weihua.message;

public class TestConsumer2 implements MessageConsumer {

	@Override
	public void doHandle(Message message) {
		System.out.println("TestConsumer2:" + message);
	}

}
