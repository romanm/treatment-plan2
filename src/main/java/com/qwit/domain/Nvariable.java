package com.qwit.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Nvariable implements MObject,Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name="idnvariable")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}
	
	private String nvariable;
	public String getNvariable() {return nvariable;}
	public void setNvariable(String nvariable) {this.nvariable = nvariable;}

	private BigDecimal nvalue;
	public BigDecimal getNvalue() {return nvalue;}
	public void setNvalue(BigDecimal nvalue) {this.nvalue = nvalue;}
	@Override
	public int compareTo(MObject t) {return compareTo((Nvariable)t);}
	public int compareTo(Nvariable d) {
		if(d==null)		return -1;
		else			return copmareNvariable(d);
	}
	private int copmareNvariable(Nvariable d) {
		if(!hasNvariable())
			return d.hasNvariable()?1:	compareIvalue(d);
		else if(!d.hasNvariable())
			return -1;
		else{
			int c = nvariable.compareTo(d.getNvariable());
			return c!=0?c:			compareIvalue(d);
		}
	}
	private int compareIvalue(Nvariable d) {
		if(!hasNvalue())
			return d.hasNvalue()?1:0;
		else if(!d.hasNvalue())
			return -1;
		else
			return nvalue.compareTo(d.getNvalue());
	}
	public boolean hasNvariable()	{return nvariable!=null&&nvariable.length()>0;}
	public boolean hasNvalue()		{return nvalue!=null;}
	public String toString(){
		return "nvariable:"+id+":nvariable:"+nvariable+":nvalue:"+nvalue;
	}
}
