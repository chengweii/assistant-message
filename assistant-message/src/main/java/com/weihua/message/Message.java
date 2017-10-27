package com.weihua.message;

import java.util.Date;

public class Message {
	private String messageQuene;
	private Date sendTime;
	private String content;

	public String getMessageQuene() {
		return messageQuene;
	}

	public void setMessageQuene(String messageQuene) {
		this.messageQuene = messageQuene;
	}

	public Date getSendTime() {
		return sendTime == null ? new Date() : sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
