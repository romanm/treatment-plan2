package com.qwit.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;



/**
 * The persistent class for the task database table.
 * 
 */
@Entity
@Table(name="task")
public class Schema implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idtask")
	private Integer id;
	private Integer duration, startnum;
	private String task, taskvar, cyclename, type;

	private Integer maxcycle;
	private Integer version;

	private String infostatus;

	private String intention;

	private String longname;


	private String outpatient;

	private String reference;

	//bi-directional one-to-one association to TreeL
	@OneToOne
	@JoinColumn(name="idtask")
	private Tree schemaT;
	public Tree getSchemaT() {return schemaT;}
	public void setSchemaT(Tree tree) {this.schemaT = tree;}

	public Schema() {}

	public String getCyclename() {
		return this.cyclename;
	}

	public void setCyclename(String cyclename) {
		this.cyclename = cyclename;
	}

	public Integer getDuration() {
		return this.duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getInfostatus() {
		return this.infostatus;
	}

	public void setInfostatus(String infostatus) {
		this.infostatus = infostatus;
	}

	public String getIntention() {
		return this.intention;
	}

	public void setIntention(String intention) {
		this.intention = intention;
	}

	public String getLongname() {
		return this.longname;
	}

	public void setLongname(String longname) {
		this.longname = longname;
	}

	public Integer getMaxcycle() {
		return this.maxcycle;
	}

	public void setMaxcycle(Integer maxcycle) {
		this.maxcycle = maxcycle;
	}

	public String getOutpatient() {
		return this.outpatient;
	}

	public void setOutpatient(String outpatient) {
		this.outpatient = outpatient;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Integer getStartnum() {
		return this.startnum;
	}

	public void setStartnum(Integer startnum) {
		this.startnum = startnum;
	}

	public String getTask() {
		return this.task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String getTaskvar() {
		return this.taskvar;
	}

	public void setTaskvar(String taskvar) {
		this.taskvar = taskvar;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getVersion() {
		return this.version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	public int compareTo(MObject t) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}