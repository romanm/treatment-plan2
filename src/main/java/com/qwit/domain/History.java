package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;

import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Date;


/**
 * The persistent class for the history database table.
 * 
 */
@Entity
public class History implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Integer idhistory;

	private Integer didhistory;

	private String hshow;

	private Integer idauthor;

	private Integer idhpatient;

	private Timestamp mdate;
	
	@OneToOne
	@JoinColumn(name="idhistory")
	private Tree tree;
	public Tree getTree() {return tree;}
	public void setTree(Tree tree) {
		this.tree = tree;
		idhistory=tree.getId();
	}

	public History() {}

	public Integer getIdhistory() {
		return this.idhistory;
	}

	public void setIdhistory(Integer idhistory) {
		this.idhistory = idhistory;
	}

	public Integer getDidhistory() {
		return this.didhistory;
	}

	public void setDidhistory(Integer didhistory) {
		this.didhistory = didhistory;
	}

	public String getHshow() {
		return this.hshow;
	}

	public void setHshow(String hshow) {
		this.hshow = hshow;
	}

	public Integer getIdauthor() {
		return this.idauthor;
	}

	public void setIdauthor(Integer idauthor) {
		this.idauthor = idauthor;
	}

	public Integer getIdhpatient() {
		return this.idhpatient;
	}

	public void setIdhpatient(Integer idhpatient) {
		this.idhpatient = idhpatient;
	}
	public DateTime getMdateDT() {
		return new DateTime(mdate.getTime());
	}
	public Timestamp getMdate() {
		return this.mdate;
	}

	public void setMdate(Date mdate) {
		Timestamp timestamp = new Timestamp(mdate.getTime());
		setMdate(timestamp);
	}
	public void setMdate(Timestamp mdate) {
		this.mdate = mdate;
	}

	public String toString(){
		return "history:"+idhistory+":mdate:"+mdate+":idauthor:"+idauthor;
	}
}