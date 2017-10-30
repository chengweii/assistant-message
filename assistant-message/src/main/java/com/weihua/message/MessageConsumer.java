package com.weihua.message;

public interface MessageConsumer {
	void doHandle(Message message);
}
