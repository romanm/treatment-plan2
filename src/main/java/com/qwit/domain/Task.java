package com.qwit.domain;


import java.io.Serializable;
import java.util.Map;

import javax.persistence.*;



/**
 * The persistent class for the task database table.
 * 
 */
@Entity
public class Task implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idtask")
	private Integer id;

	private String cyclename;

	private Integer duration;

	private String infostatus;

	private String intention;

	private String longname;

	private Integer maxcycle;

	private String outpatient;

	private String reference;

	private Integer startnum;

	private String task;

	private String taskvar;

	private String type;

	private Integer version;

    public Task() {
    }

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
		if(taskvar==null)taskvar="";
		return this.taskvar;
	}

	public void setTaskvar(String taskvar) {
		this.taskvar = taskvar;
	}

	public String getType() {
		if(type==null)
			type="";
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

	public String toString(){
		return "task:"+getId()
		+":task:"+getTask()
		+":taskvar:"+taskvar
		+":outpatient:"+outpatient
		+":type:"+getType()
		+":duration:"+getDuration()
		+":Cyclename:"+getCyclename();
	}
	public boolean hasTask() {return task!=null&&task.length()!=0;}
	public Task addAtt(Map<String, Object> map) {
		setTask((String) map.get("task"));
		setType((String) map.get("type"));
		setTaskvar((String) map.get("taskvar"));
		return this;
	}
}