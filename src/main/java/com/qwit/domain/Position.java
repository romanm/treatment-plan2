package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the position database table.
 * 
 */
@Entity
public class Position implements MObject,Serializable,Comparable<Position> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idposition")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}


	private String foot;

	private String position;

	private String task;

    public Position() {
    }


	public String getFoot() {
		return this.foot;
	}

	public void setFoot(String foot) {
		this.foot = foot;
	}

	public String getPosition() {
		return this.position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getTask() {
		return this.task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public int compareTo(MObject t) {return compareTo((Position)t);}
	public int compareTo(Position d) {
		if(d==null)		return -1;
		else			return copmarePosition(d);
	}
	private int copmarePosition(Position d) {
		if(!hasPosition())
			return d.hasPosition()?1:	compareFoot(d);
		else if(!d.hasPosition())
			return -1;
		else{
			int c = position.compareTo(d.getPosition());
			return c!=0?c:			compareFoot(d);
		}
	}
	private int compareFoot(Position d) {
		if(!hasFoot())
			return d.hasFoot()?1:0;
		else if(!d.hasFoot())
			return -1;
		else{
			int c = foot.compareTo(d.getFoot());
			return c;
		}
	}
	private boolean hasPosition()	{return position!=null&&position.length()>0;}
	private boolean hasFoot()	{return foot!=null;}

}