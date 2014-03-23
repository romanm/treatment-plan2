package com.qwit.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Ivariable implements MObject,Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="idivariable")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}
	
	private String ivariable;
	private Integer ivalue;
	public Integer	getIvalue()		{return ivalue;}
	public String	getIvariable()	{return ivariable;}
	public void setIvalue(Integer ivalue)		{this.ivalue = ivalue;}
	public void setIvariable(String ivariable)	{this.ivariable = ivariable;}
	@Override
	public int compareTo(MObject t) {return compareTo((Ivariable)t);}
	public int compareTo(Ivariable d) {
		if(d==null)		return -1;
		else			return copmareIvariable(d);
	}
	private int copmareIvariable(Ivariable d) {
		if(!hasIvariable())
			return d.hasIvariable()?1:	compareIvalue(d);
		else if(!d.hasIvariable())
			return -1;
		else{
			int c = ivariable.compareTo(d.getIvariable());
			return c!=0?c:			compareIvalue(d);
		}
	}
	private int compareIvalue(Ivariable d) {
		if(!hasIvalue())
			return d.hasIvalue()?1:0;
		else if(!d.hasIvalue())
			return -1;
		else
			return ivalue.compareTo(d.getIvalue());
	}
	public boolean hasIvariable()	{return ivariable!=null&&ivariable.length()>0;}
	public boolean hasIvalue()		{return ivalue!=null;}
	public String toString(){
		return "ivariable:"+id+":ivariable:"+ivariable+":ivalue:"+ivalue;
	}
}
