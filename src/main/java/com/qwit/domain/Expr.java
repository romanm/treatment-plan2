package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.*;



/**
 * The persistent class for the expr database table.
 * 
 */
@Entity
public class Expr implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

	//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idexpr")
	private Integer id;

	private String expr;

	private String type;

	private String unit;

	private String value;



	public Expr() {
	}

	public Expr(String expr, String value,String unit, String type) {
		setExpr(expr);
		setValue(value);
		setUnit(unit);
		setType(type);
	}
	public Expr(Integer idExpr, String expr2, String value2) {
		setId(idExpr);
		setExpr(expr2);
		setValue(value2);
	}
	public String getExpr() {
		if(expr==null)expr="";
		return this.expr;
	}

	public void setExpr(String expr) {
		this.expr = expr;
	}

	public String getType() {
		if(type==null)type="";
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUnit() {
		if(unit==null)unit="";
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getValue() {
		if(value==null)value="";
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String toString(){
		return "exprId:"+getId()+":expr:"+expr+":type:"+type+":unit:"+unit+":value:"+value;
	}

	public int compareTo(MObject e) {
		return  compareTo((Expr)e);
	}

	public int compareTo(Expr e) {
		if(e==null)		return -1;
		else			return copmareExpr(e);
	}

	private int copmareExpr(Expr e) {
		if(!hasExpr())
			return e.hasExpr()?1:		compareType(e);
		else if(!e.hasExpr())
			return -1;
		else{
			int c = expr.compareTo(e.getExpr());
			//System.out.println("---c:"+c);
			return c!=0?c:				compareType(e);
		}
	}

	private int compareType(Expr e) {
		if(!hasType())	
			return e.hasType()?1:		compareUnit(e);
		else if(!e.hasType())
			return -1;
		else {
			int c = expr.compareTo(e.getType());
			return c!=0?c:				compareUnit(e);
		}
	}

	private int compareUnit(Expr e) {
		if(!hasUnit())
			return e.hasUnit()?1:		compareValue(e);
		else if(!e.hasUnit())
			return -1;
		else{
			int c = expr.compareTo(e.getUnit());
			return c!=0?c:				compareValue(e);
		}
	}

	private int compareValue(Expr e) {
		if(!hasValue())	
			return e.hasValue()?1:0;
		else if(!e.hasValue())
			return -1;
		else
			return type.compareTo(e.getValue());
	}

	public boolean hasExpr()		{return expr!=null && expr.length()>0;}
	public boolean hasType()	{return type!=null && type.length()>0;}
	public boolean hasUnit()	{return unit!=null && unit.length()>0;}
	public boolean hasValue()	{return value!=null && value.length()>0;}
	public Expr addAtt(Map<String, Object> map) {
		setExpr((String) map.get("expr"));
		setValue((String) map.get("value"));
		setUnit((String) map.get("unit"));
		setType((String) map.get("type"));
		return this;
	}
}