package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.*;

/**
 * Paradox variable.
 * The persistent class for the pvariable database table.
 * 
 */
@Entity
public class Pvariable implements MObject,Serializable,Comparable<Pvariable>  {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idpvariable")
	private Integer id;
	private String pvariable,pvalue,unit;

	public Pvariable() {}

	public Pvariable(String pvariable, String pvalue, String unit) {
		setPvariable(pvariable);
		setPvalue(pvalue);
		setUnit(unit);
	}
	public String getPvalue() {
		if(pvalue==null)pvalue="";
		return this.pvalue;
	}

	public void setPvalue(String pvalue) {
		this.pvalue = pvalue;
	}

	public String getPvariable() {
		if(pvariable==null)pvariable="";
		return this.pvariable;
	}

	public void setPvariable(String pvariable) {
		if(pvariable==null)pvariable="";
		this.pvariable = pvariable;
	}

	public String getUnit() {
		if(unit==null) unit="";
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String toString(){
		return "pvariable:"+id+":pvariable:"+pvariable+":pvalue:"+pvalue+":unit:"+unit;
	}

	public int compareTo(MObject t) {return compareTo((Pvariable)t);}

	public int compareTo(Pvariable d) {
		if(d==null)		return -1;
		else			return copmarePvariable(d);
	}
	private int copmarePvariable(Pvariable d) {
		if(!hasPvariable())
			return d.hasPvariable()?1:	comparePvalue(d);
		else if(!d.hasPvariable())
			return -1;
		else{
			int c = pvariable.compareTo(d.getPvariable());
			return c!=0?c:			comparePvalue(d);
		}
	}
	private int comparePvalue(Pvariable d) {
		if(!hasPvalue())
			return d.hasPvalue()?1:	compareUnit(d);
		else if(!d.hasPvalue())
			return -1;
		else{
			int c = pvalue.compareTo(d.getPvalue());
			return c!=0?c:			compareUnit(d);
		}
	}
	private int compareUnit(Pvariable d) {
		if(!hasUnit())
			return d.hasUnit()?1:0;
		else if(!d.hasUnit())
			return -1;
		else
			return unit.compareTo(d.getUnit());
	}
	public boolean hasUnit()		{return unit!=null&&unit.length()>0;}
	public boolean hasPvalue()		{return pvalue!=null&&pvalue.length()>0;}
	public boolean hasPvariable()	{return pvariable!=null&&pvariable.length()>0;}
	public Pvariable addAtt(Map<String, Object> map) {
		setPvariable((String) map.get("pvariable"));
		setPvalue((String) map.get("pvalue"));
		setUnit((String) map.get("unit"));
		return this;
	}
}