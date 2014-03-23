package com.qwit.domain;

import java.io.Serializable;
import javax.persistence.*;

import com.qwit.model.IntentionRest;


import java.sql.Timestamp;
import java.util.Set;


/**
 * The persistent class for the protocol database table.
 * 
 */
@Entity
public class Protocol implements MObject,Serializable,Comparable<Protocol> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="idprotocol")
	private Integer id;

	public Integer getId() {return id;}
	public void setId(Integer id) {this.id = id;}

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
	@Transient private String intentionAdjuvant,intentionLine;
	@Transient private Set<IntentionRest> intentionRests;
	public String getIntentionAdjuvant() {return intentionAdjuvant;}
	public String getIntentionLine() {return intentionLine;}
	public Set<IntentionRest> getIntentionRests() {return intentionRests;}
	public void setIntentionAdjuvant(String intentionAdjuvant) {this.intentionAdjuvant = intentionAdjuvant;}
	public void setIntentionLine(String intentionLine) {this.intentionLine = intentionLine;}
	public void setIntentionRests(Set<IntentionRest> intentionRest) {
		this.intentionRests = intentionRest;
	}

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

	public Protocol() {
		setInfostatus("work");
		setProtocolvar("");
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
		if(null==intention)intention="";
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
	public void setProtocolvar(String protocolvar) { this.protocolvar = protocolvar; }

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
	public int compareTo(MObject p) {return compareTo((Protocol)p);	}
	public int compareTo(Protocol p) {
		if(p==null)
			return -1;
		else
			return compareProtocol(p);
	}
	private int compareProtocol(Protocol p) {
		if(!hasProtocol())
			return p.hasProtocol()?1:0;
		else if(!p.hasProtocol())
			return -1;
		else
			return protocol.compareTo(p.getProtocol());
	}
	public boolean hasProtocol() {return protocol!=null;}

	public String toString(){
		return "protocol:"+getId()
		+":protocol:"+getProtocol()
		+":protocolvar:"+getProtocolvar()
		+":protocoltype:"+getProtocoltype()
		+":phase:"+getPhase()
		+":prospective:"+getProspective()
		+":randomized:"+getRandomized()
		+":expansion:"+getExpansion()
		+":indication:"+getIndication()
		+":infostatus:"+getInfostatus()
		+":intention:"+getIntention()
		+":status:"+getStatus()
		+":studynumber:"+getStudynumber()
		+":longname:"+getLongname()
		;
	}

	@Transient private Integer duration;
	public Integer getDuration() {return duration;}
	public void setDuration(Integer duration) {this.duration = duration;}

	@Transient private String schemaName;
	public String getSchemaName() {return schemaName;}
	public void setSchemaName(String schemaName) {this.schemaName = schemaName;}

}
