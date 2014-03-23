package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the studyarm database table.
 * 
 */
@Entity
@Table(name="studyarm")
public class Arm implements MObject,Serializable,Comparable<Arm> {
	private static final long serialVersionUID = 1L;

	public String toString(){
		return "arm:"+id+":arm:"+arm;
	}
	@Id
	@Column(name="idstudyarm")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}


	public Arm() {}

	@Column(name="studyarm")
	private String arm;
	public String getArm() {
		if(arm==null)
			arm="";
		return arm;
	}
	public void setArm(String arm) {this.arm = arm;}
	public int compareTo(Arm s) {
		if(s==null)
			return 1;
		else
			return compareStudyarm(s);
	}

	private int compareStudyarm(Arm s) {
		if(!hasArm())
			return s.hasArm()?1:0;
		else if(!s.hasArm())
			return 1;
		else
			return arm.compareTo(s.getArm());
	}

	public boolean hasArm() {return arm!=null&&arm.length()>0;}
	public int compareTo(MObject t) {return compareStudyarm((Arm)t);}

}