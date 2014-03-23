package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * The persistent class for the labor database table.
 * 
 */
@Entity
public class Labor implements MObject,Serializable,Comparable<Labor> {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idlabor")
	private Integer id;

	private String labor;

	private Integer maxnormval;

	private Integer maxval;

	private Integer minnormval;

	private Integer minval;

	private String style;

	private String type;

	private String unit;

	private String url;

	//bi-directional one-to-one association to TreeL
	@OneToOne
	@JoinColumn(name="idlabor")
	private Tree tree;
	public Tree getTree() {return this.tree;}
	public void setTree(Tree tree) {this.tree = tree;}


    public Labor() {
    }

	public String getLabor() {return labor;}
	public void setLabor(String labor) {this.labor = labor;}

	public Integer getMaxnormval() {
		return this.maxnormval;
	}

	public void setMaxnormval(Integer maxnormval) {
		this.maxnormval = maxnormval;
	}

	public Integer getMaxval() {
		return this.maxval;
	}

	public void setMaxval(Integer maxval) {
		this.maxval = maxval;
	}

	public Integer getMinnormval() {
		return this.minnormval;
	}

	public void setMinnormval(Integer minnormval) {
		this.minnormval = minnormval;
	}

	public Integer getMinval() {
		return this.minval;
	}

	public void setMinval(Integer minval) {
		this.minval = minval;
	}

	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
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

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public String toString(){return "labor:"+id+":labor:"+labor+":unit:"+unit;}
	public int compareTo(MObject t) {return compareTo((Labor)t);}

	public int compareTo(Labor l) {
		if(l==null)		return -1;
		else			return copmareLabor(l);
	}
	private int copmareLabor(Labor l) {
		if(!hasLabor())
			return l.hasLabor()?1:0;
		else if(!l.hasLabor())
			return -1;
		else{
			int c = labor.compareTo(l.getLabor());
			return c!=0?c:			compareUnit(l);
		}
	}
	private int compareUnit(Labor l) {
		if(!hasUnit())
			return l.hasUnit()?1:0;
		else if(!l.hasUnit())
			return -1;
		else{
			int c = unit.compareTo(l.getUnit());
			return c;
		}
	}
	public boolean hasLabor()	{return labor!=null&&labor.length()>0;}
	public boolean hasUnit()	{return unit!=null&unit.length()>0;}
	public boolean hasType()	{return type!=null&&type.length()>0;}
	public Labor addAtt(Map<String, Object> map) {
		setLabor((String) map.get("labor"));
		setUnit((String) map.get("unit"));
		setType((String) map.get("type"));
		return this;
	}

}