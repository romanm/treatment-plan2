package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;



/**
 * The persistent class for the checkitem database table.
 * 
 */
@Entity
public class Checkitem implements MObject,Serializable,Comparable<Checkitem> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idcheckitem")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}

    public Checkitem() {}

    private String checkitem, type;

	public String getCheckitem() {return this.checkitem;}
	public void setCheckitem(String checkitem) {this.checkitem = checkitem;}

	public String getType() {
		if(type==null) type="";
		return this.type;
	}
	public void setType(String type) {this.type = type;}
	
	public int compareTo(MObject t) {return compareTo((Checkitem)t);}
	public int compareTo(Checkitem c) {
		if(c==null)return 1;
		else return compareCheckitem(c);
	}
	private int compareCheckitem(Checkitem c) {
		if(!hasCheckitem())
			return c.hasCheckitem()?1:compareType(c);
		else if(!c.hasCheckitem())
			return 1;
		else{
			int ct = checkitem.compareTo(c.getCheckitem());
			return ct!=0?ct:compareType(c);
		}
	}
	private int compareType(Checkitem c) {
		if(!hasType())
			return c.hasType()?1:0;
		else 
			return !c.hasType()?-1:type.compareTo(c.getType());
	}
	public boolean hasType()		{return type!=null&&type.length()>0;}
	public boolean hasCheckitem()	{return checkitem!=null&&checkitem.length()>0;}
	
	public String toString(){
		return "checkitem"+id+":checkitem:"+checkitem+":type:"+type;
	}
}