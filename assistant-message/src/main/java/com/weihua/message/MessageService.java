package com.weihua.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
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
	public static Map<String, String> queuesConfig = new HashMap<String, String>();

	static {
		executor = Executors.newCachedThreadPool();
		queues = new ArrayList<Map<String, String>>();
		loadQueuesConfig();
	}

	private static void loadQueuesConfig() {
		ResourceBundle bundle = ResourceBundle.getBundle(MessageConstant.MESSAGE_CONFIG_PATH, Locale.getDefault());
		for (String key : bundle.keySet()) {
			queuesConfig.put(key, bundle.getString(key));
		}
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
					final Message message = deserialize(entity.getContent());
					if (message != null) {
						String queueClasses = queuesConfig.get(message.getMessageQuene());
						String[] queueClassArray = queueClasses.split(",");
						for (String queueClass : queueClassArray) {
							final MessageConsumer consumer = ClassUtil
									.<MessageConsumer> getInstanceByClassName(queueClass);
							if (consumer != null) {
								executor.execute(new Runnable() {
									@Override
									public void run() {
										try {
											consumer.doHandle(message);
										} catch (Exception e) {
											LOGGER.error(
													"Receive message error：[messageQuene:" + message.getMessageQuene()
															+ ",messageContent:" + message.getContent() + "]",
													e);
										}
									}
								});

							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	private static Message deserialize(String content) {
		if (!Strings.isNullOrEmpty(content)) {
			int start = content.indexOf(MessageConstant.MESSAGE_CONTENT_START);
			int end = content.indexOf(MessageConstant.MESSAGE_CONTENT_END);
			if (start != -1 && end > start) {
				String json = content.substring(MessageConstant.MESSAGE_CONTENT_START.length(), end);
				Message message = GsonUtil.getEntityFromJson(json, new TypeToken<Message>() {
				});
				return message;
			}
		}
		return null;
	}

	private static String serialize(Message message) {
		if (message != null) {
			String result = GsonUtil.toJson(message);
			return MessageConstant.MESSAGE_CONTENT_START + result + MessageConstant.MESSAGE_CONTENT_END;
		}
		return null;
	}
}
