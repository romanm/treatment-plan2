package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the symptom database table.
 * 
 */
@Entity
public class Symptom implements MObject,Serializable,Comparable<Symptom> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idsymptom")
	private Integer id;
	private String symptom, type, unit;
	public String toString(){
		return "symptom:"+id+":symptom:"+symptom+":type:"+type+":unit:"+unit;
	}
	
	public Integer getId() {return id;}
	public void setId(Integer id) {	this.id=id;}


    public Symptom() {
    	setUnit("");
    	setType("");
    }

	


	public Symptom(Integer idSymptom, String symptom) {
		setId(idSymptom);
		setSymptom(symptom);
	}

	public String getSymptom() {
		return this.symptom;
	}

	public void setSymptom(String symptom) {
		this.symptom = symptom;
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

	public int compareTo(MObject t) {return compareTo((Symptom)t);}

	public int compareTo(Symptom s) {
		if(s==null)	return	-1;
		else		return	compareSymptom(s);
	}
	private int compareSymptom(Symptom s) {
		if(!hasSymptom())
			return s.hasSymptom()?1:0;
		else if(!s.hasSymptom())
			return -1;
		else{
			int c = symptom.compareTo(s.getSymptom());
			return c!=0?c:			compareUnit(s);
		}
	}
	private int compareUnit(Symptom s) {
		if(!hasUnit())
			return s.hasUnit()?1:0;
		else if(!s.hasUnit())
			return -1;
		else{
			int c = unit.compareTo(s.getUnit());
			return c!=0?c:			compareType(s);
		}
	}
	private int compareType(Symptom s) {
		if(!hasType())
			return s.hasType()?1:0;
		else if(!s.hasType())
			return -1;
		else{
			int c = type.compareTo(s.getType());
			return c;
		}
	}
	public boolean hasSymptom() {return symptom!=null&&symptom.length()!=0;}
	private boolean hasUnit() {return unit!=null;}
	private boolean hasType() {return type!=null;}

}