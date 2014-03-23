package com.qwit.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Url  implements MObject,Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="idurl")
	private Integer id;
	private String url,text;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}
	
	@Override
	public int compareTo(MObject t) {
		return 0;
	}
	public String toString(){
		return "url:"+getId()
		+":url:"+url
		+":text:"+text
		;
	}

}
