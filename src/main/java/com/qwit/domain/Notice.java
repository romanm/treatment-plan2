package com.qwit.domain;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 * The persistent class for the notice database table.
 * 
 */
@Entity
public class Notice implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idnotice")
	private Integer id;

	private String notice,type,b;

	//bi-directional one-to-one association to TreeL
//	@OneToOne
//	@JoinColumn(name="idnotice")
//	private Tree tree;
//	public Tree getTree() {	return this.tree;}
//	public void setTree(Tree tree) {this.tree = tree;}


	public Notice() {}
	public Notice(String notice, String type) {
		setNotice(notice);
		setType(type);
	}

	public Notice(String notice, String type, Integer idNotice) {
		this.notice=notice;
		this.type=type;
		this.id=idNotice;
	}
	public String getB() {
		return this.b;
	}

	public void setB(String b) {
		this.b = b;
	}

	public String getNotice() {
		if(notice==null)notice="";
		return this.notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public String getType() {
		if(type==null)type="";
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String toString(){
		return "noticeId:"+getId()+":notice:"+notice+":type:"+type;
	}
	
	public int compareTo(MObject n) {
		return  compareTo((Notice)n);
	}
	
	public int compareTo(Notice n) {
		if(n==null)		return -1;
		else			return copmareNotice(n);
	}
		
	private int copmareNotice(Notice n) {
		if(!hasNotice())
			return n.hasNotice()?1:		compareType(n);
		else if(!n.hasNotice())
			return -1;
		else{
			int c = notice.compareTo(n.getNotice());
			//System.out.println("---c:"+c);
			return c!=0?c:				compareType(n);
		}
	}
	
	private int compareType(Notice n) {
		if(!hasType())	
			return n.hasType()?1:0;
		else if(!n.hasType())
			return -1;
		else
			return type.compareTo(n.getType());
	}
	
	public boolean hasNotice()		{return notice!=null && notice.length()>0;}	
	public boolean hasType()	{return type!=null && type.length()>0;}
	public Notice addAtt(Map<String, Object> map) {
		setNotice((String) map.get("notice"));
		setType((String) map.get("type"));
		return this;
	}
	
}