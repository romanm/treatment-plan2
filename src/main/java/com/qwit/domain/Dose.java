package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * The persistent class for the dose database table.
 * 
 */
@Entity
public class Dose implements MObject,Serializable,Comparable<Dose> {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="iddose")
	private Integer id;

	private String type;
	private Float value;
	private String unit;
	private String app;	
	private String pro;

	

	//bi-directional one-to-one association to TreeL
//	@OneToOne
//	@JoinColumn(name="iddose")
//	private Tree tree;
//	public Tree getTree() {return this.tree;}
//	public void setTree(Tree tree) {this.tree = tree;}


    public Dose() {
    	setUnit("");
    	setApp("");
    	setPro("");
    	setType("p");
    }
    public Dose(Float value, String unit) {
    	this();
    	setValue(value);
    	setUnit(unit);
    }
    public Dose(Float value, String unit, String app, String pro, String type) {
    	this(value,unit);
    	setType(type);
    	setApp(app);
    	setPro(pro);
    }

	public Dose(Integer id, String unit, Float value) {
		this(value,unit);
		setId(id);
	}
	public String getApp() {
		if(app==null)	app="";
		return this.app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getPro() {
		if(pro==null)	pro="";
		return this.pro;
	}

	public void setPro(String pro) {
		this.pro = pro;
	}

	public String getType() {
		if(type==null)	type="p";
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCalcUnit() {
		String d = unit;
		String[] split = d.split("/");
		if(split.length>1)
			d=split[0];
		return d;
	}
	public String getUnit() {
		if(unit==null)unit="";
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public boolean isValueInt() {return null!=value && value==value.intValue();}
	public Float getValue() {
		if(value==null)value=new Float(0.0);
		return this.value;
	}
	public void setValue(Integer doseDef)	{setValue(doseDef.floatValue());}
	public void setValue(Float value)		{this.value = value;}
	public String toString(){
		return "dose:"+getId()+":type:"+type+":app:"+app+":unit:"+unit+":value:"+value+":pro:"+pro;
	}
	public int compareTo(MObject t) {return compareTo((Dose)t);}
	public int compareTo(Dose d) {
		if(d==null)		return -1;
		else			return copmareType(d);
	}
	private int copmareType(Dose d) {
		if(!hasType())
			return d.hasType()?1:	compareApp(d);
		else if(!d.hasType())
			return -1;
		else{
			int c = type.compareTo(d.getType());
			return c!=0?c:			compareApp(d);
		}
	}
	private int compareApp(Dose d) {
		if(!hasApp())
			return d.hasApp()?1:	compareUnit(d);
		else if(!d.hasApp())
			return -1;
		else{
			int c = app.compareTo(d.getApp());
			return c!=0?c:			compareUnit(d);
		}
	}
	private int compareUnit(Dose d) {
		if(!hasUnit())
			return d.hasUnit()?1:	compareValue(d);
		else if(!d.hasUnit())
			return -1;
		else{
			int c = unit.compareTo(d.getUnit());
			return c!=0?c:			compareValue(d);
		}
	}
	private int compareValue(Dose d) {
		if(!hasValue())
			return d.hasValue()?1:	comparePro(d);
		else if(!d.hasValue())
			return -1;
		else{
			int c = value.compareTo(d.getValue());
			return c!=0?c:			comparePro(d);
		}
	}
	private int comparePro(Dose d) {
		if(!hasPro())	
			return d.hasPro()?1:0;
		else if(!d.hasPro())
			return -1;
		else
			return pro.compareTo(d.getPro());
	}
	private boolean hasPro()	{return pro!=null&&pro.length()>0;}
	public boolean hasValue()	{return value!=null&&value!=0;}
	public boolean hasUnit()	{return unit!=null&&unit.length()>0;}
	public boolean hasApp()	{return app!=null&&app.length()>0;}
	private boolean hasType()	{return type!=null;}
	
	public boolean hasDose2DoseCalc(){return hasUnit()&&( unit.indexOf("/l")>0 ||unit.indexOf("/g")>0);}
	
	public Dose addAtt(Map<String, Object> map) {
		setValue((Float) map.get("value"));
		setUnit((String) map.get("unit"));
		setApp((String) map.get("app"));
		setPro((String) map.get("pro"));
		setType((String) map.get("type"));
		return this;
	}
	/*
	@Transient
	private String calcDoseR;
	public String getCalcDoseR() {return calcDoseR;}
	public void setCalcDoseR(String string) {
		this.calcDoseR=string;
		System.out.println(this+"...."+calcDoseR);
	}
	 * */
	
}
