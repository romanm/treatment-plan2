package com.qwit.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * The persistent class for the drug database table.
 * 
 */
@Entity
public class Drug implements MObject,Serializable,Comparable<Drug> {
	
	@Transient private Folder folder;	
	public Folder getFolder() {return folder;}
	public void setFolder(Folder folder) {this.folder = folder;}
	
	//	public class Drug implements MClass,Serializable,Cloneable {
	private static final long serialVersionUID = 1L;

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="iddrug")
	private Integer id;

	//bi-directional many-to-one association to Drug
	@ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="idgeneric")
	private Drug generic;
	//bi-directional many-to-one association to Drug
	@OneToMany(mappedBy="generic")
	private List<Drug> trade;
	public List<Drug> getTrades() {return this.trade;}
	public void setTrades(List<Drug> drugs) {this.trade = drugs;}
	
	
//	//bi-directional one-to-one association to TreeL
//	@OneToOne
//	@JoinColumn(name="iddrug")
//	private Tree tree;
//	public Tree getTree() {return this.tree;}
//	public void setTree(Tree tree) {this.tree = tree;}


	public Drug() {}

	private String drug;
	public String getDrug() {return this.drug;}
	public void setDrug(String drug) {this.drug = drug;}

	private String type;
	public String getType() {return this.type;}
	public void setType(String type) {this.type = type;}

	public Drug getGeneric() {
		return this.generic;
	}

	public void setGeneric(Drug drugBean) {
		this.generic = drugBean;
	}
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}
	public String toString(){
		return getDrug()+"/"+getGeneric().getDrug()
		+"\t drug:"+getId()+":idgeneric:"
		+(getGeneric()!=null?getGeneric().getId().toString():"null")
		+(getFolder()!=null?getFolder():"")
		;
	}
	public int compareTo(MObject t) {return compareTo((Drug)t);}
	public int compareTo(Drug d) {
		if(d==null)		return -1;
		else			return copmareDrug(d);
	}
	private int copmareDrug(Drug d) {
		if(!hasDrug())
			return d.hasDrug()?1:0;
		else if(!d.hasDrug())
			return -1;
		else{
			int c = drug.compareTo(d.getDrug());
			return c;
		}
	}

	public boolean hasDrug()	{return drug!=null&&drug.length()>0;}

//	private boolean hasId()		{return id!=null;}
	public boolean hasGeneric() {return generic!=null;}
	public void addTrade(Drug drug) {
		if(trade==null)
			trade=new ArrayList<Drug>();
		trade.add(drug);
		drug.setGeneric(this);
	}

}