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

package com.qwit.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.config.AbstractRabbitConfiguration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SingleConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SerializerMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//TODO uncomment when using spring 3.1.0. Release
//@PropertySource("classpath:AbstractAmqpConfig.properties")
public abstract class AbstractAmqpConfig extends AbstractRabbitConfiguration {
	protected final Log log = LogFactory.getLog(getClass());

//	TODO uncomment when using spring 3.1.0. Release
//	@Autowired
//    Environment env;
	
	//spring 3.0.3.RELEASE variant to read from properties
	@Value("#{amqpProperties['server.ip']}")
	private String serverIP;
	@Value("#{amqpProperties['server.queue.name']}")
	private String serverQueueName;

	public AbstractAmqpConfig()
	{
		log.debug("-----------AbstractAmqpConfig----Constructor-------");
	}

	/**
	 * Shared topic exchange used for publishing any market data (e.g. stock quotes) 
	protected static String MARKET_DATA_EXCHANGE_NAME = "app.stock.marketdata";
	 */

	/**
	 * The server-side consumer's queue that provides point-to-point semantics for stock requests.
	 */
//	protected static String MTL_REQUEST_QUEUE_NAME = "mtl.doc.request";
	public String MTL_REQUEST_QUEUE_NAME;

	/**
	* Key that clients will use to send to the stock request queue via the default direct exchange.
	protected static String MTL_REQUEST_ROUTING_KEY = MTL_REQUEST_QUEUE_NAME;
	*/

	protected abstract void configureRabbitTemplate(RabbitTemplate template);

	private void configProxy() {
		log.debug("set proxy via system.property");
		System.setProperty("http.proxyHost", "10.10.0.120");
		System.setProperty("http.proxyPort", "8080");
		log.debug("system.property: http.proxyHost = "+System.getProperty("http.proxyHost"));
		log.debug("system.property: http.proxyPort = "+System.getProperty("http.proxyPort"));
		// Next connection will be through proxy.
		URL url;
		try {
			url = new URL("http://java.sun.com/");
//			InputStream ins = url.openStream();
//			log.debug("stream:"+ins.toString());

			InputStream openStream = url.openStream();
			log.debug("stream:"+openStream);
			InputStreamReader inputStreamReader = new InputStreamReader(openStream);
			log.debug("stream:"+inputStreamReader);
			BufferedReader in = new BufferedReader(inputStreamReader);
			log.debug("stream:"+in);
			log.debug("bufferedreader-stream:"+in.toString());
			String inputLine;
			int i=0;
			while ((inputLine = in.readLine()) != null&& i<20)
				log.debug(++i + inputLine);
			in.close();
		} catch (MalformedURLException e1) {
			log.debug("url:");
			e1.printStackTrace();
		} catch (IOException e1) {
			log.debug("stream:");
			e1.printStackTrace();
		}
		log.debug("a");
	}
	@Bean
	public ConnectionFactory connectionFactory()
	{
	
		//roman rem 2012.05.08
//		configProxy();


//		String ip 	= "localhost";
//		String ip 	= "schellente.imise.uni-leipzig.de";
//		String ip 	= "192.168.50.15";
//		String ip 	= "192.168.50.18"; //schellente
//		String ip 	= "imsel.imise.uni-leipzig.de";
		String ip 	= "139.18.18.43";  //imsel

		//spring 3.0.3.RELEASE variant to read from properties
		ip=serverIP;
		MTL_REQUEST_QUEUE_NAME = serverQueueName;

//		TODO uncomment when using spring 3.1.0. Release 
//		MTL_REQUEST_QUEUE_NAME = env.getProperty("server.queue.name");
//		String envip= env.getProperty("server.ip");
//		ip=envip;
//		log.debug("-----------connectionFactory----envip--------"+envip);

//		String user = "guest";	String pass = "guest";
		String user = "guest";	String pass = "ows123";

		log.debug("-----------connectionFactory----ip-----------"+ip);
		log.debug("-----------connectionFactory----queue name---"+MTL_REQUEST_QUEUE_NAME);
//		log.debug("-----------connectionFactory----ipViaValue---"+ipViaValue);
		SingleConnectionFactory connectionFactory = new SingleConnectionFactory(ip);
		log.debug("-----------connectionFactory-----------------"+connectionFactory);
		connectionFactory.setUsername(user);
		log.debug("-----------connectionFactory---username------" +user);
//		connectionFactory.setPassword("ows123");
		connectionFactory.setPassword(pass);
//		log.debug("-----------connectionFactory---password------" +pass);
		return connectionFactory;
	}
	
	@Bean
	public RabbitTemplate rabbitTemplate(){
		log.debug("a");
		ConnectionFactory connectionFactory = connectionFactory();
		log.debug("b "+connectionFactory);
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		log.debug("c "+template);
//		template.setMessageConverter(jsonMessageConverter());
		template.setMessageConverter(serializerMessageConverter());
		log.debug("d ");
		configureRabbitTemplate(template);
		log.debug("e ");
		return template;
	}

	@Bean public MessageConverter jsonMessageConverter() {return new JsonMessageConverter();}
	@Bean public SerializerMessageConverter serializerMessageConverter() {return new SerializerMessageConverter();}
//	@Bean public TopicExchange marketDataExchange() {return new TopicExchange(MARKET_DATA_EXCHANGE_NAME);}
}
