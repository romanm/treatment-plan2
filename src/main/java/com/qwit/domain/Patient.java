package com.qwit.domain;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * The persistent class for the patient database table.
 * 
 */
@Entity
public class Patient implements MObject,Serializable,Comparable<Patient> {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idpatient")
	private Integer id;
	
	//bi-directional one-to-one association to TreeL
	//@ManyToOne
	//@JoinColumn(name="idpatient", referencedColumnName = "id", nullable=false)
	//@JoinColumn(name="id")
	//private Tree treeT;
	
	
	private Timestamp birthdate;

	private String patient, forename;

	private String infostatus;

	private String insurance;

	private String kpid;


	private String pid;

	private String sex;

	private String ward;


	public Timestamp getBirthdate() {
		return this.birthdate;
	}

	public void setBirthdate(Timestamp birthdate) {
		this.birthdate = birthdate;
	}

	public String getForename() {
		return this.forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getInfostatus() {
		return this.infostatus;
	}

	public void setInfostatus(String infostatus) {
		this.infostatus = infostatus;
	}

	public String getInsurance() {
		return this.insurance;
	}

	public void setInsurance(String insurance) {
		this.insurance = insurance;
	}

	public String getKpid() {
		return this.kpid;
	}

	public void setKpid(String kpid) {
		this.kpid = kpid;
	}

	public String getPatient() {
		return this.patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public String getPid() {
		return this.pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getSex() {
		return this.sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getWard() {
		return this.ward;
	}

	public void setWard(String ward) {
		this.ward = ward;
	}
        
	public int compareTo(MObject p) {return compareTo((Patient)p);}
	public int compareTo(Patient p) {
		if(p==null) return -1;
		else		return comparePatient(p);
	}
	private int comparePatient(Patient p) {
		if(!hasPatient())
			return p.hasPatient()?1:compareForename(p);
		else if(!p.hasPatient())
			return -1;
		else	{
			int c = patient.compareTo(p.getPatient());
			return c!=0?c:compareForename(p);
		}
	}
	private int compareForename(Patient p) {
		if(!hasForename())
			return p.hasForename()?1:compareSex(p); 
		else if(!p.hasForename())
			return -1;
		else{
			int c = forename.compareTo(p.getForename());
			return c!=0?c:compareSex(p);
		}
	}
	private int compareSex(Patient p) {
		if(!hasSex())
			return p.hasSex()?1:compareBirthdate(p);
		else if(!p.hasSex())
			return -1;
		else{
			int c = sex.compareTo(p.getSex());
			return c!=0?c:compareBirthdate(p);
		}
	}
	private int compareBirthdate(Patient p) {
		if(!hasBirthdate())
			return p.hasBirthdate()?1:0;
		else
			return !p.hasBirthdate()?-1:birthdate.compareTo(p.getBirthdate());
	}
	private boolean hasBirthdate() {return birthdate!=null;}
	private boolean hasSex() {return sex!=null;}
	private boolean hasForename() {return forename!=null;}
	private boolean hasPatient() {return patient!=null;}

	public String toString(){
		return "patient:"+id+":patient:"+patient+":forename:"+forename+":sex:"+sex+":birthdate:"+birthdate;
	}
}