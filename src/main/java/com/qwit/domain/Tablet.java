package com.qwit.domain;


import java.io.Serializable;
import java.util.Map;

import javax.persistence.*;



/**
 * The persistent class for the tablet database table.
 * 
 */
@Entity
public class Tablet implements MObject,Serializable,Comparable<Tablet> {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idtablet")
	private Integer id;

	private Float value;
	private Integer portion;
	private String type,unit;

	public Tablet() {}

	public Tablet(int i) {
		value= new Float(i);
	}
	public Integer getPortion() {return this.portion;}
	public void setPortion(Integer portion) {this.portion = portion;}
	public String getType() {return this.type;}
	public void setType(String type) {this.type = type;}
	public String getUnit() {return this.unit;}
	public void setUnit(String unit) {this.unit = unit;}
	public Float getValue() {return this.value;}
	public void setValue(float value) {this.value = value;}
	public void setValue(Integer value) {this.value = value.floatValue();}
	
	public Tablet addAtt(Map<String, Object> map) {
		setValue((Float) map.get("value"));
		setPortion((Integer) map.get("portion"));
		setUnit((String) map.get("unit"));
		setType((String) map.get("type"));
		return this;
	}

	public String toString(){
		return "tablet:"+id
		+":value:"+value
		+":unit:"+unit
		+":type:"+type
		+":portion:"+portion;
	}
	public int compareTo(MObject t) {return compareTo((Tablet)t);}
	public int compareTo(Tablet d) {
		if(d==null)		return -1;
		else			return copmareValue(d);
	}
	private int copmareValue(Tablet d) {
		if(!hasValue())
			return d.hasValue()?1:	comparePortion(d);
		else if(!d.hasValue())
			return -1;
		else{
			int c = value.compareTo(d.getValue());
			return c!=0?c:			comparePortion(d);
		}	}
	private int comparePortion(Tablet d) {
		if(!hasPortion())
			return d.hasPortion()?1:	compareUnit(d);
		else if(!d.hasPortion())
			return -1;
		else{
			int c = portion.compareTo(d.getPortion());
			return c!=0?c:			compareUnit(d);
		}
	}
	private int compareUnit(Tablet d) {
		if(!hasUnit())
			return d.hasUnit()?1:	compareType(d);
		else if(!d.hasUnit())
			return -1;
		else{
			int c = unit.compareTo(d.getUnit());
			return c!=0?c:			compareType(d);
		}
	}
	private int compareType(Tablet d) {
		if(!hasType())	
			return d.hasType()?1:0;
		else if(!d.hasType())
			return -1;
		else
			return type.compareTo(d.getType());
	}
	public boolean hasValue()	{return value!=null&&value!=0;}
	public boolean hasPortion()	{return portion!=null&&portion!=0;}
	public boolean hasUnit()	{return unit!=null&&unit.length()>0;}
	public boolean hasType()	{return type!=null&&type.length()>0;}
}