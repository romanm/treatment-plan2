//package com.qwit.config.client;
//
//import org.springframework.beans.factory.config.PropertiesFactoryBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//
//@Configuration
//public class Serverconfig 
//{
//	@Bean
//	public PropertiesFactoryBean amqpProperties(){
//		PropertiesFactoryBean p =  new PropertiesFactoryBean();
//		Resource l = new ClassPathResource ("rabbitmq.properties");		
//		p.setLocation(l);
//		p.setSingleton(true);
//		return p;
//	}
//}