package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;



/**
 * The persistent class for the contactperson database table.
 * 
 */
@Entity
public class Contactperson implements MObject,Serializable,Comparable<Contactperson>  {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}
	public String toString(){
		return "contactperson:"+id+":contactperson:"+contactperson+":forename:"+forename+":title:"+title;
	}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idcontactperson")
	private Integer id;

	private String contactperson;

	private String email;

	private String fax;

	private String forename;

	private String mobil;

	private String note;

	private String phone;

	private String title;


    public Contactperson() {
    }

	public String getContactperson() {
		return this.contactperson;
	}

	public void setContactperson(String contactperson) {
		this.contactperson = contactperson;
	}

	public String getEmail() {
		if(email==null)
			email="";
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFax() {
		if(fax==null)
			fax="";
		return this.fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getForename() {
		if(forename==null) 
			forename="";
		return this.forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getMobil() {
		if(mobil==null)
			mobil="";
		return this.mobil;
	}

	public void setMobil(String mobil) {
		this.mobil = mobil;
	}

	public String getNote() {
		if(note==null)
			note="";
		return this.note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getPhone() {
		if(phone==null)
			phone="";
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTitle() {
		if(title==null)
			title="";
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public int compareTo(MObject t) {return compareTo((Contactperson)t);}
	public int compareTo(Contactperson c) {
		if(c==null)return 1;
		else return compareContactperson(c);
	}
	
	private int compareContactperson(Contactperson c) {
		if(!hasContactperson())
			return c.hasContactperson()?1:compareForename(c);
		else if(!c.hasContactperson())
			return 1;
		else{
			int ct = contactperson.compareTo(c.getContactperson());
			return ct!=0?ct:compareForename(c);
		}
	}
	
	private int compareForename(Contactperson c) {
		if(!hasForename())
			return c.hasForename()?1:0;
		else 
			return !c.hasForename()?-1:forename.compareTo(c.getForename());
	}
	
	public boolean hasForename()		{return forename!=null&&forename.length()>0;}
	
	public boolean hasContactperson()	{return contactperson!=null&&contactperson.length()>0;}
	
	
}