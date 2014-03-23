package com.qwit.amqp;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Address;

public class OisDocServiceGateway extends RabbitGatewaySupport implements
		MtlDocServiceGateway {
	protected final Log log = LogFactory.getLog(getClass());
	private String defaultReplyToQueue;

	public void setDefaultReplyToQueue(String defaultReplyToQueue) {
		this.defaultReplyToQueue = defaultReplyToQueue;
	}

	public void setDefaultReplyToQueue(Queue defaultReplyToQueue) {
		this.defaultReplyToQueue = defaultReplyToQueue.getName();
	}

	@Override
	public void send(MtlDocRequest mtlDocRq) {
		log.debug(1);
		log.debug(mtlDocRq);
		getRabbitTemplate().convertAndSend(mtlDocRq,
				new MessagePostProcessor() {
					public Message postProcessMessage(Message message)
							throws AmqpException {
						log.debug(2);
						log.debug(message.getMessageProperties()
								.getReceivedRoutingKey());
						log.debug(message.getMessageProperties());
						log.debug(3);
						log.debug("defaultReplyToQueue =" + defaultReplyToQueue);
						Address address = new Address(defaultReplyToQueue);
						log.debug(address);
						message.getMessageProperties().setReplyTo(address);
						try {
							String uuid = UUID.randomUUID().toString();
							log.debug("uuid = " + uuid);
							message.getMessageProperties().setCorrelationId(
									uuid.getBytes("UTF-8"));
						} catch (UnsupportedEncodingException e) {
							throw new AmqpException(e);
						}
						log.debug(4);
						// log.debug(message);
						return message;
					}
				});
		log.debug(5);
	}

}
