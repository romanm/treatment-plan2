package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the document database table.
 * 
 */
@Entity
public class Document implements MObject,Serializable,Comparable<Document>  {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="iddocument")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}

//	private byte[] content;

	private String document;
	private String contenttype;

	private String filename;
	private String lang;
	private String type;
	private String url;

    public Document(){}
//
//	public byte[] getContent() {
//		return this.content;
//	}
//
//	public void setContent(byte[] content) {
//		this.content = content;
//	}

	public String getContenttype() {
		return this.contenttype;
	}

	public void setContenttype(String contenttype) {
		this.contenttype = contenttype;
	}

	public String getDocument() {
		return this.document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getLang() {
		return this.lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public int compareTo(MObject t) {return compareTo((Document)t);}
	public int compareTo(Document o) {
		if(o==null)return 1;
		else return compareContenttype(o);
	}
	private int compareContenttype(Document o) {
		if(!hasContenttype())
			return o.hasContenttype()?1:compareFilename(o);
		else if(!o.hasContenttype())
			return 1;
		else{
			int ct = contenttype.compareTo(o.getContenttype());
			return ct!=0?ct:compareFilename(o);
		}
	}
	private int compareFilename(Document o) {
		if(!hasFilename())
			return o.hasFilename()?1:compareType(o);
		else if(!o.hasFilename())
			return 1;
		else{
			int ct = filename.compareTo(o.getFilename());
			return ct!=0?ct:compareType(o);
		}
	}
	private int compareType(Document o) {
		if(!hasType())
			return o.hasType()?1:compareUrl(o);
		else if(!o.hasType())
			return 1;
		else{
			int ct = type.compareTo(o.getType());
			return ct!=0?ct:compareUrl(o);
		}
	}
	private int compareUrl(Document o) {
		if(!hasUrl())
			return o.hasUrl()?1:0;
		else
			return !o.hasType()?-1:url.compareTo(o.getUrl());
	}
	public boolean hasUrl() {return url!=null&&url.length()>0;}
	public boolean hasType() {return type!=null&&type.length()>0;}
	public boolean hasFilename() {return filename!=null&&filename.length()>0;}
	public boolean hasContenttype() {return contenttype!=null&&contenttype.length()>0;}
	public String toString(){return "document:"+id+":document:"+document;}
}