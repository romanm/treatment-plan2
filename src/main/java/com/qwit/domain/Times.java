package com.qwit.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The persistent class for the times database table.
 * 
 */
@Entity
public class Times implements MObject,Serializable,Comparable<Times> {
	private static final long serialVersionUID = 1L;
	@Transient protected final Log log = LogFactory.getLog(getClass());
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

	@Id
	@Column(name="idtimes")
	private Integer id;

	private String abs;
	
	/**
	 *
	<xsl:choose>
		<xsl:when test="@apporder=0">..></xsl:when>		Nach Ende
		<xsl:when test="@apporder=1">&lt;..</xsl:when>	Vor Beginn
		<xsl:when test="@apporder=3">.&lt;.</xsl:when>	Endet vor Beginn
		---
		<xsl:when test="@apporder=2">.>.</xsl:when>		Nach Beginn
	</xsl:choose>
	 */
	private String apporder;
	/**
	 * H M S
	 */
	private String relunit;
	/**
	 * Value for relunit
	 */
	private Integer relvalue;
	private Integer visual;

	//bi-directional one-to-one association to TreeL
//	@OneToOne
//	@JoinColumn(name="idtimes")
//	private Tree tree;
//	public Tree getTree() {		return this.tree;	}
//	public void setTree(Tree tree) {		this.tree = tree;	}

    public Times() {}

	public String getAbs() {
		if(this.abs==null)abs="";
		return this.abs;
	}
	public void setAbs(String abs) {this.abs = abs;}

	public long calcTs(Ts ts) {
		int i = hms()*directionRelvalue();
//		int calcTs = i;
		long timeInMillis = ts.getTime();
		Timestamp tsEnd = new Timestamp(timeInMillis);
//		log.debug(tsEnd+"/"+hms()+"/"+directionRelvalue()+"/"+apporder);

		if(apporder==null)
			timeInMillis = ts.getTsBeyondEnd().getTime()+i;
//		else if(apporder.equals("1")||apporder.equals("2")||apporder.equals("3"))
		else if(apporder.equals("1")||apporder.equals("2"))
			timeInMillis += i;
		else if(apporder.equals("0"))
			timeInMillis = ts.getTsBeyondEnd().getTime()+i;
		else
			timeInMillis=timeInMillis+1;
		
		tsEnd = new Timestamp(timeInMillis);
//		log.debug(tsEnd+"/"+hms()+"/"+directionRelvalue()+"/"+apporder);
		
		return timeInMillis;
	}
	public String getApporder() {
		if(null==apporder)
			apporder="";
		return this.apporder;
	}
	public void setApporder(String apporder) {this.apporder = apporder;}
	public int hms() {
		int hms = 1000;
		if(relunit==null)				hms = 1000;
		else if(relunit.equals("H"))	hms = 60*60*1000;
		else if(relunit.equals("M"))	hms = 60*1000;
		else if(relunit.equals("S"))	hms = 1000;
		return hms;
	}
	public String getRelunit() {
		if(null==relunit)
			relunit="";
		return this.relunit;
	}
	public void setRelunit(String relunit) {this.relunit = relunit;}

	public Integer getDistance() {return directionRelvalue()*hms();}
	public Integer directionRelvalue() {
//		if(abs!=null)return 0;
		if(hasAbs())return 0;
		if(apporder==null&&relvalue==null)return 0;
		boolean before = (apporder!=null && (apporder.equals("1")||apporder.equals("3")));
		if(before)return 0-relvalue;
		return relvalue;
	}
	public Integer getRelvalue() {
		if(relvalue==null) relvalue=0;
		return this.relvalue;
	}
	public void setRelvalue(Integer relvalue) {this.relvalue = relvalue;}

	public Integer getVisual() {
		if(visual==null) visual=0;
		return this.visual;
	}
	public void setVisual(Integer visual) {this.visual = visual;}
	
	public String toString(){
		return "times:"+getId()+":abs:"+getAbs()
		+":apporder:"+getApporder()
		+":relunit:"+getRelunit()
		+":relvalue:"+getRelvalue()
		+":visual:"+getVisual()
		;
	}
	public int compareTo(MObject t)	{return compareTo((Times)t);}
	public int compareTo(Times t){
		if(t==null)		return -1;
		if(hasAbs())	return !t.hasAbs()?-1:abs.compareTo(t.getAbs());
		else			return t.hasAbs()?1:compareApporder(t);
	}
	private int compareApporder(Times t) {
		if(hasApporder())
			if(!t.hasApporder())
				return -1;
			else{
				int c=apporder.compareTo(t.getApporder());
				return c!=0?c:compareUnit(t);
			}
		else	return t.hasApporder()?1:compareUnit(t);
	}
	private int compareUnit(Times t) {
		if(hasUnit())
			if(!t.hasUnit())
				return -1;
			else{
				int c = relunit.compareTo(t.getRelunit());
				return c==0?compareRelvalue(t):c;
			}
		else	return t.hasUnit()?1:compareRelvalue(t);
	}
	private int compareRelvalue(Times t) {
		if(hasRelvalue())	
			if(!t.hasRelvalue())
				return -1;
			else{
				int c = relvalue.compareTo(t.getRelvalue());
				return c==0?compareVisual(t):c;
			}
		else	return t.hasRelvalue()?1:compareVisual(t);
	}
	private int compareVisual(Times t) {
		if(hasVisual())	return	!t.hasVisual()?-1:visual.compareTo(t.getVisual());
		else			return	t.hasVisual()?1:0;//0==all 0
	}
	private boolean hasUnit()		{return relunit!=null&&relunit.length()>0;}
	private boolean hasVisual()		{return visual!=null;}
	public boolean hasRelvalue()	{return relvalue!=null;}
	public boolean hasApporder()	{return apporder!=null&&apporder.length()>0;}
	public boolean hasAbs()			{return abs!=null&&abs.length()>0;}
	public boolean getMealtimes()	{return hasAbs() && abs.contains("=");}
	public boolean allNull(){
		return !hasAbs()&&!hasApporder()&&!hasUnit()&&!hasUnit()&&!hasRelvalue()&&!hasVisual();
	}
	public Times addAtt(Map<String, Object> map) {
		String abs = (String) map.get("abs");
		if(abs!=null&&abs.length()>0){
//		if(hasAbs()){ in kein fall hier hasAbs() verwenden
			setAbs(abs);
		}else{
			setRelunit((String)		map.get("relunit"));
			setApporder((String)	map.get("apporder"));
			setVisual((Integer)		map.get("visual"));
			setRelvalue((Integer)	map.get("relvalue"));
		}
		return this;
	}
	
}
