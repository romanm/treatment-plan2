package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;



/**
 * The persistent class for the finding database table.
 * 
 */
@Entity
public class Finding implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

	//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idfinding")
	private Integer id;
	public String toString(){return "finding:"+id+":finding:"+finding+":unit:"+unit;}
	private String finding;

	private Integer maxval;

	private Integer minval;

	private String unit;

	//bi-directional one-to-one association to TreeL
	@OneToOne
	@JoinColumn(name="idfinding")
	private Tree tree;

	public Finding() {
	}
	public Finding(String finding, String unit) {
		setFinding(finding);
		setUnit(unit);
	}

	public String getFinding() {
		return this.finding;
	}

	public void setFinding(String finding) {
		this.finding = finding;
	}

	public Integer getMaxval() {
		return this.maxval;
	}

	public void setMaxval(Integer maxval) {
		this.maxval = maxval;
	}

	public Integer getMinval() {
		return this.minval;
	}

	public void setMinval(Integer minval) {
		this.minval = minval;
	}

	public String getUnit() {
		return this.unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Tree getTree() {
		return this.tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}
	public int compareTo(MObject t) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean hasFinding()	{return finding!=null&&finding.length()>0;}
	public boolean hasUnit()	{return unit!=null&&unit.length()>0;}
	public Finding addAtt(Map<String, Object> map) {
		setFinding((String) map.get("finding"));
		setUnit((String) map.get("unit"));
		return this;
	}

}