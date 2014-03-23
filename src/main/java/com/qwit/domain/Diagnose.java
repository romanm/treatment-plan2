package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;



/**
 * The persistent class for the diagnose database table.
 */
@Entity
public class Diagnose implements MObject,Serializable,Comparable<Diagnose> {
	private static final long serialVersionUID = 1L;

	public String toString(){
		return "diagnose:"+id+":diagnose:"+diagnose;
	}
	@Id
	@Column(name="iddiagnose")
	private Integer id;

	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}

	private String diagnose;

    public Diagnose() {}

	public String getDiagnose() {
		return this.diagnose;
	}

	public void setDiagnose(String diagnose) {
		this.diagnose = diagnose;
	}
	
	public int compareTo(Diagnose d) {
		if(d==null)
			return -1;
		else
			return compareDiagnose(d);
	}
	private int compareDiagnose(Diagnose d) {
		if(!hasDiagnose())
			return d.hasDiagnose()?1:0;
		else if(!d.hasDiagnose())
			return -1;
		else
			return diagnose.compareTo(d.getDiagnose());
	}
	public boolean hasDiagnose() {return diagnose!=null;}
	public int compareTo(MObject d) {return compareDiagnose((Diagnose)d);}


}