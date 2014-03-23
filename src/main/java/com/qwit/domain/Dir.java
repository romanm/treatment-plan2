package com.qwit.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Dir {
	protected final Log log = LogFactory.getLog(getClass());
	public String toString(){return "dir:"+id+":"+did+":dir:"+dir+":childNr:"+(childDs==null?"0":""+getChildDs().size());}
	
	private String dir;
	private Integer did;
	private Integer id;
	
	public String getDir() {return dir;}
	public void setDir(String dir) {this.dir = dir;}
	public Integer getDid() {return did;}
	public void setDid(Integer did) {this.did = did;}
	public Integer getId() {return id;}
	public void setId(Integer id) {	this.id = id;}
	private Map<Integer, Dir> dirDocMap;
	private Dir parentD;
	private List<Dir> childDs;

	public Dir(Integer id, Integer did, String folder) {
		this.id=id;
		this.did=did;
		this.dir=folder;
	}

	public void setDocMap(Map<Integer, Dir> dirDocMap) {
		this.dirDocMap=dirDocMap;
		dirDocMap.put(id, this);
	}

	public void setParentD(Dir parentD) {this.parentD=parentD;}
	public Dir getParentD() {return parentD;}
	
	public void addParentD(Dir parentD) {
		//log.debug(this+" "+parentD);
		setParentD(parentD);
		if(parentD.getChildDs()==null)
			parentD.setChildDs(new ArrayList<Dir>());
		if(!parentD.getChildDs().contains(this))
			parentD.getChildDs().add(this);
	}

//	public void setChildDs(ArrayList<Dir> cl) {this.childDs=cl;}
	public void setChildDs(List<Dir> cl) {this.childDs=cl;}
	public List<Dir> getChildDs() {return this.childDs;}

}
