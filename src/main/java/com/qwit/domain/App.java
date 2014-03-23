package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;



/**
 * The persistent class for the app database table.
 * 
 */
@Entity
public class App implements MObject,Serializable,Comparable<App> {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idapp")
	private Integer id;

	private Integer appapp;

	private Float fdr;

	private String h24;

	private String type;

	private String unit;

	//bi-directional one-to-one association to TreeL
	@OneToOne
	@JoinColumn(name="idapp")
	private Tree tree;

	public App() {}

	public Integer getAppapp() {
		return this.appapp;
	}

	public void setAppapp(Integer appapp) {
		this.appapp = appapp;
	}

	public Float getFdr() {
		return this.fdr;
	}

	public void setFdr(Float fdr) {
		this.fdr = fdr;
	}

	public String getH24() {
		return this.h24;
	}

	public void setH24(String h24) {
		this.h24 = h24;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUnit() {
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Tree getTree() {
		return this.tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}
	
	public String toString(){
		return "app::appapp:"+appapp+":unit:"+unit;
	}
	public int compareTo(MObject a) {return compareTo((App)a);}
	public int compareTo(App a) {
		if(a==null)	return -1;
		else		return compareUnit(a);
	}
	private int compareUnit(App a) {
		if(!hasUnit())
			return a.hasUnit()?1:	compareAppapp(a);
		else if(!a.hasUnit())
			return -1;
		else{
			int c = unit.compareTo(a.getUnit());
			return c!=0?c:			compareAppapp(a);
		}
	}
	private int compareAppapp(App a) {
		if(!hasAppapp())
			return a.hasAppapp()?1:0;
		else if(!a.hasAppapp())
			return -1;
		else
			return appapp.compareTo(a.getAppapp());
	}
	public boolean hasAppapp()	{return appapp!=null;}
	public boolean hasUnit()	{return unit!=null&&unit.length()>0;}
	public App addAtt(Map<String, Object> map) {
		setAppapp((Integer) map.get("appapp"));
		setUnit((String) map.get("unit"));
		return this;
	}
	public void isNull() {
		// TODO Auto-generated method stub
		
	}
	
}