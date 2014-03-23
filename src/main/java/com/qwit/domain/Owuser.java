package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;



/**
 * The persistent class for the owuser database table.
 * 
 */
@Entity
public class Owuser implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

	public String toString(){
		return "owuser:"+id+":owuser:"+owuser+":owrole:"+owrole+":owview:"+owview+":institute:"+institute;
	}
	
//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idowuser")
	private Integer id;

	private String help;

	private Integer institute;

	private String lang;

	private String owrole;

	private String owuser;

	private String owview;

	private String pword;

	private Integer tabsize;

	//bi-directional one-to-one association to TreeL

    public Owuser() {
    }

	

	

	public String getHelp() {
		return this.help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public Integer getInstitute() {
		return this.institute;
	}

	public void setInstitute(Integer institute) {
		this.institute = institute;
	}

	public String getLang() {
		return this.lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getOwrole() {
		return this.owrole;
	}

	public void setOwrole(String owrole) {
		this.owrole = owrole;
	}

	public String getOwuser() {
		return this.owuser;
	}

	public void setOwuser(String owuser) {
		this.owuser = owuser;
	}

	public String getOwview() {
		return this.owview;
	}

	public void setOwview(String owview) {
		this.owview = owview;
	}

	public String getPword() {
		return this.pword;
	}

	public void setPword(String pword) {
		this.pword = pword;
	}

	public Integer getTabsize() {
		return this.tabsize;
	}

	public void setTabsize(Integer tabsize) {
		this.tabsize = tabsize;
	}
	public int compareTo(MObject t) {
		// TODO Auto-generated method stub
		return 0;
	}

}