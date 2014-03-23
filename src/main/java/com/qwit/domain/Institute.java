package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the institute database table.
 * 
 */
@Entity
public class Institute implements MObject,Serializable,Comparable<Institute> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idinstitute")
	private Integer id;
	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}

	private String bsaformula;
	private String city;
	private String country;
	private String daysintensive;
	@Column(name="dose_round_percent")
	private Integer doseRoundPercent;
	private String evening;
	private String fax;
	private String institute;
	private String morning;
	private String newtherapydayhour;
	private String night;
	private String noon;
	private String oldtimescalc;
	private String phone;
	@Column(name="pl_task_show")
	private String plTaskShow;
	private String printpaper;
	private String shortname;
	private String street;
	private String styletask;
	@Column(name="task_cycle_template")
	private String taskCycleTemplate;
	@Column(name="task_ed_dtt")
	private String taskEdDtt;
	private String taskstart;
	@Column(name="taskstart_hide")
	private String taskstartHide;

	private String zip;

    public Institute() {
    }

	public String getBsaformula() {
		return this.bsaformula;
	}

	public void setBsaformula(String bsaformula) {
		this.bsaformula = bsaformula;
	}

	public String getCity() {
		return this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return this.country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDaysintensive() {
		return this.daysintensive;
	}

	public void setDaysintensive(String daysintensive) {
		this.daysintensive = daysintensive;
	}

	public Integer getDoseRoundPercent() {
		return this.doseRoundPercent;
	}

	public void setDoseRoundPercent(Integer doseRoundPercent) {
		this.doseRoundPercent = doseRoundPercent;
	}

	public String getEvening() {
		return this.evening;
	}

	public void setEvening(String evening) {
		this.evening = evening;
	}

	public String getFax() {
		return this.fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getInstitute() {return this.institute;}
	public void setInstitute(String institute) {this.institute = institute;}

	public String getMorning() {
		return this.morning;
	}

	public void setMorning(String morning) {
		this.morning = morning;
	}

	public String getNewtherapydayhour() {
		return this.newtherapydayhour;
	}

	public void setNewtherapydayhour(String newtherapydayhour) {
		this.newtherapydayhour = newtherapydayhour;
	}

	public String getNight() {
		return this.night;
	}

	public void setNight(String night) {
		this.night = night;
	}

	public String getNoon() {
		return this.noon;
	}

	public void setNoon(String noon) {
		this.noon = noon;
	}

	public String getOldtimescalc() {
		return this.oldtimescalc;
	}

	public void setOldtimescalc(String oldtimescalc) {
		this.oldtimescalc = oldtimescalc;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPlTaskShow() {
		return this.plTaskShow;
	}

	public void setPlTaskShow(String plTaskShow) {
		this.plTaskShow = plTaskShow;
	}

	public String getPrintpaper() {
		return this.printpaper;
	}

	public void setPrintpaper(String printpaper) {
		this.printpaper = printpaper;
	}

	public String getShortname() {
		return this.shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getStreet() {
		return this.street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStyletask() {
		return this.styletask;
	}

	public void setStyletask(String styletask) {
		this.styletask = styletask;
	}

	public String getTaskCycleTemplate() {
		return this.taskCycleTemplate;
	}

	public void setTaskCycleTemplate(String taskCycleTemplate) {
		this.taskCycleTemplate = taskCycleTemplate;
	}

	public String getTaskEdDtt() {
		return this.taskEdDtt;
	}

	public void setTaskEdDtt(String taskEdDtt) {
		this.taskEdDtt = taskEdDtt;
	}

	public String getTaskstart() {
		return this.taskstart;
	}

	public void setTaskstart(String taskstart) {
		this.taskstart = taskstart;
	}

	public String getTaskstartHide() {
		return this.taskstartHide;
	}

	public void setTaskstartHide(String taskstartHide) {
		this.taskstartHide = taskstartHide;
	}

	public String getZip() {
		return this.zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}
	public int compareTo(MObject t) {
		return compareTo((Institute)t);
	}
	public int compareTo(Institute l) {
		if(!hasInstitute())
			return l.hasInstitute()?1:0;
		else if(!l.hasInstitute())
			return -1;
		else{
			int c = institute.compareTo(l.getInstitute());
			return c;
		}
	}
	public boolean hasInstitute()	{return institute!=null;}
	public String toString(){
		return "institute:"+getId()+":institute:"+institute;
	}
}

