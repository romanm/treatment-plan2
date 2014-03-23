/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qwit.config.client;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.qwit.config.AbstractAmqpConfig;

/**
 * Configures RabbitTemplate for the server.
 *
 * @author Mark Pollack
 * @author Mark Fisher
 */
@Configuration
public class AmqpConfigClient extends AbstractAmqpConfig {

	public AmqpConfigClient()
	{
		log.debug("---------AmqpConfigClient------Constructor-------");
	}
	
	@Bean
	public PropertiesFactoryBean amqpProperties(){
		PropertiesFactoryBean p =  new PropertiesFactoryBean();
		Resource l = new ClassPathResource ("rabbitmq.properties");		
		p.setLocation(l);
		p.setSingleton(true);
		return p;
	}
	
	// Create the Queue definitions that write up the Message listener container
	//private Queue marketDataQueue = new UniquelyNamedQueue("mktdata");
	//private Queue traderJoeQueue = new UniquelyNamedQueue("joe");

	/**
	 * The client's template will by default send to the exchange defined 
	 * in {@link org.springframework.amqp.rabbit.config.AbstractRabbitConfiguration#rabbitTemplate()}
	 * with the routing key {@link AbstractStockAppRabbitConfiguration#MTL_REQUEST_QUEUE_NAME}
	 * <p>
	 * 
	 * The default exchange will delivery to a queue whose name matches the routing key value.
	 */

	@Override
	public void configureRabbitTemplate(RabbitTemplate rabbitTemplate) {
		log.debug("configureRabbitTemplate---a---");
		log.debug("server request queue name: " + MTL_REQUEST_QUEUE_NAME);		
		rabbitTemplate.setRoutingKey(MTL_REQUEST_QUEUE_NAME);
		//rabbitTemplate.setRoutingKey("oldcharite.doc.request");
		log.debug("configureRabbitTemplate---b---");
	}

	@Bean public RabbitGatewaySupport mtlDServiceGateway() {
		RabbitGatewaySupport rabbitGatewaySupport = new RabbitGatewaySupport();
		rabbitGatewaySupport.setRabbitTemplate(rabbitTemplate());
		return rabbitGatewaySupport;
	}

	/*
	@Bean
	public MtlDocServiceGateway mtlDocServiceGateway() {
		OisDocServiceGateway gateway = new OisDocServiceGateway();
		gateway.setRabbitTemplate(rabbitTemplate());
		gateway.setDefaultReplyToQueue(rsQueue());
		return gateway;
	}
	 * */

	/*

	@Bean 
	public SimpleMessageListenerContainer messageListenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());		
		container.setQueues(rsQueue());
		container.setMessageListener(messageListenerAdapter());
		return container;
	}

	@Bean 
	public MessageListenerAdapter messageListenerAdapter() {
		return new MessageListenerAdapter(clientHandler(), jsonMessageConverter());
//		return new MessageListenerAdapter(serverHandler, jsonMessageConverter());
	}

	@Bean	public ClientHandler clientHandler(){return new ClientHandler();}
 * */

	/**
	 * This queue does not need a binding, since it relies on the default exchange.
	@Bean	public Queue rsQueue() {return amqpAdmin().declareQueue();}
	 */

}
