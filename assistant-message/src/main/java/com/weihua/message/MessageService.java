package com.weihua.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.weihua.common.util.ClassUtil;
import com.weihua.common.util.EmailUtil;
import com.weihua.common.util.EmailUtil.EmailInfo;
import com.weihua.common.util.EmailUtil.ReceiveInfo;
import com.weihua.common.util.EmailUtil.SendInfo;
import com.weihua.common.util.GsonUtil;

public class MessageService {
	private static Logger LOGGER = Logger.getLogger(MessageService.class);
	private static ExecutorService executor;
	public static List<Map<String, String>> queues;

	static {
		executor = Executors.newCachedThreadPool();
		queues = new ArrayList<Map<String, String>>();
	}

	public static void send(final Message message) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					SendInfo sendInfo = new SendInfo();
					sendInfo.setHeadName(message.getMessageQuene());
					sendInfo.setSendHtml(serialize(message));
					EmailUtil.send(sendInfo);
				} catch (Exception e) {
					LOGGER.error("Send message error：[messageQuene:" + message.getMessageQuene() + ",messageContent:"
							+ message.getContent() + "]", e);
				}
			}
		});
	}

	public static void notifyConsumers() {
		try {
			ReceiveInfo rinfo = new ReceiveInfo();
			rinfo.setDelete(true);
			List<EmailInfo> mailList = EmailUtil.receive(rinfo);

			if (mailList != null && mailList.size() > 0) {
				LOGGER.info("Message count:" + mailList.size());
				for (EmailInfo entity : mailList) {
					String className = entity.getSubject();
					MessageConsumer consumer = ClassUtil.<MessageConsumer> getInstanceByClassName(className);
					if (consumer != null) {
						consumer.doHandle(deserialize(entity.getContent()));
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private static String deserialize(String content) {
		if (!Strings.isNullOrEmpty(content)) {
			int start = content.indexOf(MessageConstant.MAIL_CONTENT_START);
			int end = content.indexOf(MessageConstant.MAIL_CONTENT_END);
			if (start != -1 && end > start) {
				return content.substring(MessageConstant.MAIL_CONTENT_START.length(), end);
			}
		}
		return content;
	}

	private static String serialize(Message message) {
		if (message != null) {
			String result = GsonUtil.toJson(message);
			return MessageConstant.MAIL_CONTENT_START + result + MessageConstant.MAIL_CONTENT_END;
		}
		return null;
	}
}
