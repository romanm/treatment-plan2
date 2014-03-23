package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.*;

/**
 * The persistent class for the checklist database table.
 * 
 */
@Entity
public class Checklist implements MObject,Serializable,Comparable<Checklist> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idchecklist")
	private Integer id;
	public void setId(Integer id) {this.id = id;}
	public Integer getId() {return id;}

	private String checklist, style, type;

	public Checklist() {}

	public String getChecklist() {return this.checklist;}
	public void setChecklist(String checklist) {this.checklist = checklist;}

	public String getStyle() {return this.style;}
	public void setStyle(String style) {this.style = style;}

	public String getType() {
		if(type==null)type="";
		return this.type;}
	public void setType(String type) {this.type = type;}

	public int compareTo(MObject t) {return compareTo((Checklist)t);}
	public int compareTo(Checklist o) {
		if(o!=null) return 1;
		else compareChecklist(o);
		return 0;
	}
	private int compareChecklist(Checklist o) {
		if(!hasChecklist())
			return o.hasChecklist()?1:compareStyle(o);
		else if(!o.hasChecklist())
			return -1;
		else{
			int c=checklist.compareTo(o.getChecklist());
			return c!=0?c:compareStyle(o);
		}
	}
	private int compareStyle(Checklist o) {
		if(!hasStyle())
			return o.hasStyle()?1:compareType(o);
		else if(!o.hasStyle())
			return -1;
		else{
			int c=checklist.compareTo(o.getChecklist());
			return c!=0?c:compareType(o);
		}
	}
	private int compareType(Checklist o) {
		if(!hasType())
			return o.hasType()?1:compareType(o);
		else
			return !o.hasStyle()?-1:checklist.compareTo(o.getChecklist());
	}
	public boolean hasChecklist() {return checklist!=null;}
	private boolean hasStyle() {return style!=null;}
	public boolean hasType() {return type!=null;}
	
	public String toString(){
		return "checklist:"+getId()+":checklist:"+checklist+":style:"+style+":type:"+type;
	}
	
	public Checklist addAtt(Map<String, Object> map) {
		setChecklist((String) map.get("checklist"));
		setType((String) map.get("type"));
		return this;
	}
}