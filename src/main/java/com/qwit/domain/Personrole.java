package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the personrole database table.
 * 
 */
@Entity
public class Personrole implements MObject,Serializable,Comparable<Personrole> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idpersonrole")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id=id;}

	private String personrole;

	public Personrole() {
	}

	public String toString(){
		return "personrole:"+id+":personrole:"+personrole;
	}
	public String getPersonrole() {
		return this.personrole;
	}

	public void setPersonrole(String personrole) {
		this.personrole = personrole;
	}

	public int compareTo(MObject t) {return compareTo((Personrole)t);}

	public int compareTo(Personrole a) {
		if(a==null)	return -1;
		else		return comparePersonrole(a);
	}
	private int comparePersonrole(Personrole a) {
		if(!hasPersonrole())
			return a.hasPersonrole()?1:	comparePersonrole(a);
		else if(!a.hasPersonrole())
			return -1;
		else{
			int c = personrole.compareTo(a.getPersonrole());
			return c;
		}
	}
	public boolean hasPersonrole() {return personrole!=null;}

}