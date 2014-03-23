package com.qwit.config.server;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qwit.amqp.ServerHandler;
import com.qwit.config.AbstractAmqpConfig;
import com.qwit.service.DocumentService;

/**
 * Configures RabbitTemplate for the server.
 * 
 * @author Mark Pollack
 * @author Mark Fisher
 */
@Configuration
public class AmqpConfigServer extends AbstractAmqpConfig {
	@Autowired public DocumentService ds;

	public AmqpConfigServer()
	{
		log.debug("---------AmqpConfigServer------Constructor-------");
	}
	
	@Bean 
	public SimpleMessageListenerContainer messageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
		log.debug("a "+container);
		Queue docRqQueue = docRqQueue();
		log.debug("b "+docRqQueue);
		container.setQueues(docRqQueue);
		log.debug("c ");
		MessageListenerAdapter messageListenerAdapter = messageListenerAdapter();
		log.debug("c "+messageListenerAdapter);
		container.setMessageListener(messageListenerAdapter);
		log.debug("d ");
		return container;
	}
	
	@Bean 
	public MessageListenerAdapter messageListenerAdapter() {
		log.debug("a ");
		ServerHandler serverHandler = serverHandler();
		log.debug("a "+serverHandler);
		return new MessageListenerAdapter(serverHandler);
//		return new MessageListenerAdapter(serverHandler(), jsonMessageConverter());
	}

	@Bean public ServerHandler serverHandler(){return new ServerHandler();}

	/**
	 * The server's template will by default send to the topic exchange named
	 * {@link AbstractStockAppRabbitConfiguration#MARKET_DATA_EXCHANGE_NAME}.
		public void configureRabbitTemplate(RabbitTemplate rabbitTemplate) {rabbitTemplate.setExchange(MARKET_DATA_EXCHANGE_NAME);}
	 */
	@Override
	protected void configureRabbitTemplate(RabbitTemplate template) 
	{
	}

	/**
	 * We don't need to define any binding for the stock request queue, since it's relying
	 * on the default (no-name) direct exchange to which every queue is implicitly bound.
	 */
	@Bean public Queue docRqQueue() {return new Queue(MTL_REQUEST_QUEUE_NAME);}
}
