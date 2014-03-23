package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the literature database table.
 * 
 */
@Entity
public class Literature implements MObject,Serializable,Comparable<Literature>  {
	private static final long serialVersionUID = 1L;
	public String toString(){
		return "literature:"+id
		+":title:"+title
		+":authors:"+authors
		+":spring:"+spring
		+":springtype:"+springtype
		+":year:"+year
		+":volume:"+volume
		+":page:"+page
		+":url:"+url
		;
	}

	@Id
	@Column(name="idliterature")
	private Integer id;

	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}

	private String title,authors,spring,springtype,volume;

	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}

	private String page;
	private Integer year;
	private String url;

    public Literature() {
    }

	public Literature(Integer idliterature, String title, String authors,
			String spring, String springtype, Integer year, String page,
			String url) {
		setId(idliterature);
		setTitle(title);
		setAuthors(authors);
		setSpring(springtype);
		setSpringtype(springtype);
		setYear(year);
		setPage(page);
		setUrl(url);
	}
	public String getAuthors() {
		if(authors==null)
			authors="";
		return this.authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	public String getPage() {
		if(page==null)
			page="";
		return this.page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getSpring() {
		if(spring==null)
			spring="";
		return this.spring;
	}

	public void setSpring(String spring) {
		this.spring = spring;
	}

	public String getSpringtype() {
		if(springtype==null)
			springtype="";
		return this.springtype.trim();
	}

	public void setSpringtype(String springtype) {
		this.springtype = springtype;
	}

	public String getTitle() {
		if(title==null)
			title="";
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		if(url==null)
			url="";
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getYear() {
		if(year==null)
			year=0;
		return this.year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	public int compareTo(MObject t) {return compareTo((Literature)t);}
	public int compareTo(Literature t) {
		if(t==null)		return -1;
		else			return copmareTitel(t);
	}
	private int copmareTitel(Literature t) {
		if(!hasTitel())
			return t.hasTitel()?1:0;
		else if(!t.hasTitel())
			return -1;
		else{
			int c = title.compareTo(t.getTitle());
			return c!=0?c:			compareAuthors(t);
		}
	}
	private int compareAuthors(Literature t) {
		if(!hasAuthors())
			return t.hasAuthors()?1:0;
		else if(!t.hasAuthors())
			return -1;
		else{
			int c = authors.compareTo(t.getAuthors());
			return c!=0?c:			compareSpring(t);
		}
	}
	private int compareSpring(Literature t) {
		if(!hasSpring())
			return hasSpring()?1:0;
		else if(!t.hasSpring())
			return -1;
		else{
			int c = spring.compareTo(t.getSpring());
			return c!=0?c:			compareSpringtype(t);
		}
	}
	private int compareSpringtype(Literature t) {
		if(!hasSpringtype())
			return hasSpringtype()?1:0;
		else
			return !t.hasSpringtype()?-1:spring.compareTo(t.getSpringtype());
	}
	public boolean hasSpringtype() {return springtype!=null&&springtype.length()>0;}
	public boolean hasSpring() {return spring!=null&&spring.length()>0;}
	public boolean hasAuthors() {return authors!=null&&authors.length()>0;}
	public boolean hasTitel() {return title!=null&&title.length()>0;}

}