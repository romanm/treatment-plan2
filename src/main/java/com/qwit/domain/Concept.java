package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;


import java.sql.Timestamp;


/**
 * The persistent class for the protocol database table.
 * 
 */
@Entity
@Table(name="protocol")
public class Concept implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idprotocol")
	private Integer id;

	private Timestamp amendment;

	private Integer amendmentnr;

	private String center;

	private String diagnosis;

	private String dkrnumber;

	private Timestamp enddate;

	private String expansion;

	private String indication;

	private Timestamp infodate;

	private String infostatus;

	private String intention;

	private String isrctn;

	private String longname;

	private String phase;

	private String primarytarget;

	private String prospective;

	private String protocol;

	private String protocoltype;

	private String protocolvar;

	private String randomized;

	private String release;

	private String secondarytarget;

	private Timestamp stand;

	private String starttext;

	private Timestamp starttime;

	private String status;

	private String studynumber;

	private Integer version;

    public Concept() {
    }

	public Timestamp getAmendment() {
		return this.amendment;
	}

	public void setAmendment(Timestamp amendment) {
		this.amendment = amendment;
	}

	public Integer getAmendmentnr() {
		return this.amendmentnr;
	}

	public void setAmendmentnr(Integer amendmentnr) {
		this.amendmentnr = amendmentnr;
	}

	public String getCenter() {
		return this.center;
	}

	public void setCenter(String center) {
		this.center = center;
	}

	public String getDiagnosis() {
		return this.diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}

	public String getDkrnumber() {
		return this.dkrnumber;
	}

	public void setDkrnumber(String dkrnumber) {
		this.dkrnumber = dkrnumber;
	}

	public Timestamp getEnddate() {
		return this.enddate;
	}

	public void setEnddate(Timestamp enddate) {
		this.enddate = enddate;
	}

	public String getExpansion() {
		return this.expansion;
	}

	public void setExpansion(String expansion) {
		this.expansion = expansion;
	}

	public String getIndication() {
		return this.indication;
	}

	public void setIndication(String indication) {
		this.indication = indication;
	}

	public Timestamp getInfodate() {
		return this.infodate;
	}

	public void setInfodate(Timestamp infodate) {
		this.infodate = infodate;
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

	public String getIsrctn() {
		return this.isrctn;
	}

	public void setIsrctn(String isrctn) {
		this.isrctn = isrctn;
	}

	public String getLongname() {
		return this.longname;
	}

	public void setLongname(String longname) {
		this.longname = longname;
	}

	public String getPhase() {
		return this.phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getPrimarytarget() {
		return this.primarytarget;
	}

	public void setPrimarytarget(String primarytarget) {
		this.primarytarget = primarytarget;
	}

	public String getProspective() {
		return this.prospective;
	}

	public void setProspective(String prospective) {
		this.prospective = prospective;
	}

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getProtocoltype() {
		return this.protocoltype;
	}

	public void setProtocoltype(String protocoltype) {
		this.protocoltype = protocoltype;
	}

	public String getProtocolvar() {
		return this.protocolvar;
	}

	public void setProtocolvar(String protocolvar) {
		this.protocolvar = protocolvar;
	}

	public String getRandomized() {
		return this.randomized;
	}

	public void setRandomized(String randomized) {
		this.randomized = randomized;
	}

	public String getRelease() {
		return this.release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public String getSecondarytarget() {
		return this.secondarytarget;
	}

	public void setSecondarytarget(String secondarytarget) {
		this.secondarytarget = secondarytarget;
	}

	public Timestamp getStand() {
		return this.stand;
	}

	public void setStand(Timestamp stand) {
		this.stand = stand;
	}

	public String getStarttext() {
		return this.starttext;
	}

	public void setStarttext(String starttext) {
		this.starttext = starttext;
	}

	public Timestamp getStarttime() {
		return this.starttime;
	}

	public void setStarttime(Timestamp starttime) {
		this.starttime = starttime;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStudynumber() {
		return this.studynumber;
	}

	public void setStudynumber(String studynumber) {
		this.studynumber = studynumber;
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