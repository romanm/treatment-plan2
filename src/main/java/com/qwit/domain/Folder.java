package com.qwit.domain;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


/**
 * The persistent class for the folder database table.
 * 
 */
@Entity
public class Folder implements MObject,Serializable {
	private static final long serialVersionUID = 1L;
	public Integer getId() {return id;}
	public void setId(Integer id) { this.id=id;}

//	@GeneratedValue(strategy=GenerationType.TABLE)
	@Id
	@Column(name="idfolder")
	private Integer id;

	private Timestamp fdate;

	private String folder;

	private Integer institute;

	private String reada;

	private String type;

	private String writea;

	//bi-directional many-to-one association to Folder
	/*
    @ManyToOne
	 * */
    @ManyToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="fdid")
	private Folder parentF;
	public Folder getParentF() {return this.parentF;}
	public void setParentF(Folder folderBean) {this.parentF = folderBean;}

	//bi-directional many-to-one association to Folder
	/*
	@OneToMany(mappedBy="parentF")
	 * */
	@OneToMany(mappedBy="parentF",cascade=CascadeType.ALL)
	private List<Folder> childFs;
	public List<Folder> getChildFs() {return this.childFs;}
	public void setChildFs(List<Folder> folders) {this.childFs = folders;}

	public Folder(Integer id, String folder) {
		setId(id);
		setFolder(folder);
	}
    public Folder() {
    }

	public Timestamp getFdate() {
		return this.fdate;
	}

	public void setFdate(Timestamp fdate) {
		this.fdate = fdate;
	}

	public String getFolder() {
		return this.folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public Integer getInstitute() {
		return this.institute;
	}

	public void setInstitute(Integer institute) {
		this.institute = institute;
	}

	public String getReada() {
		return this.reada;
	}

	public void setReada(String reada) {
		this.reada = reada;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getWritea() {
		return this.writea;
	}

	public void setWritea(String writea) {
		this.writea = writea;
	}
	
	public int compareTo(MObject t) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String toString(){
		return "folder:"+getId()+":"+getFolder();
	}
}