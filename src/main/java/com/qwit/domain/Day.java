package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;



/**
 * The persistent class for the day database table.
 * 
 */
@Entity
public class Day implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idday")
	private Integer id;

	private String abs;
	private String newtype;

	private Integer apporder;
	private Integer numd;
	private Integer type;

	//bi-directional one-to-one association to TreeL
	@OneToOne
	@JoinColumn(name="idday")
	private Tree tree;

	public Day() {
	}

	public Day(String abs, String newType) {
		super();
		setAbs(abs);
		setNewtype(newType);
	}
	public String getAbs() {
		if(abs==null)abs="";
		return this.abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public Integer getApporder() {
		return this.apporder;
	}

	public void setApporder(Integer apporder) {
		this.apporder = apporder;
	}

	public String getNewtype() {
		if(newtype==null)newtype="";
		return this.newtype;
	}

	public void setNewtype(String newtype) {
		this.newtype = newtype;
	}

	public Integer getNumd() {
		return this.numd;
	}

	public void setNumd(Integer numd) {
		this.numd = numd;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Tree getTree() {
		return this.tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}
	
	public String toString(){
		return "day:"+getId()+":abs:"+abs+":newtype:"+newtype;
	}
	
	public int compareTo(MObject d) {
		return  compareTo((Day)d);
	}
	
	public int compareTo(Day d) {
		if(d==null)		return -1;
		else			return copmareAbs(d);
	}
		
	private int copmareAbs(Day d) {
		if(!hasAbs())
			return d.hasAbs()?1:	compareNewtype(d);
		else if(!d.hasAbs())
			return -1;
		else{
			int c = abs.compareTo(d.getAbs());
			return c!=0?c:			compareNewtype(d);
		}
	}
	
	private int compareNewtype(Day d) {
		if(!hasNewtype())	
			return d.hasNewtype()?1:0;
		else if(!d.hasNewtype())
			return -1;
		else
			return newtype.compareTo(d.getNewtype());
	}
	
	public boolean hasAbs()		{return abs!=null && abs.length()>0;}	
	public boolean hasNewtype()	{return newtype!=null && newtype.length()>0;}
	public Day addAtt(Map<String, Object> map) {
		setAbs((String) map.get("abs"));
		setNewtype((String) map.get("newtype"));
		return this;
	}
}