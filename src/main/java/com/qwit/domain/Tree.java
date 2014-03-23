package com.qwit.domain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The persistent class for the tree database table.
 */
@Entity
//@SequenceGenerator(name="DBID",sequenceName="dbid")
public class Tree implements Serializable {

	@Transient private boolean importChild;
	public boolean isImportChild() {return importChild;}
	public void setImportChild(boolean importChild) {this.importChild = importChild;}

	@Transient private MObject mtlO;
	/**
	 * Get entity object with medical date (MeTaL object) for this mtl-node.
	 * @return MeTaL object for this node.
	 */
	public MObject getMtlO() {return mtlO;}
	public void setMtlO(MObject mtlO) {
		this.mtlO = mtlO;
		if(null==mtlO)
			setIdClass(null);
		else
			setIdClass(mtlO.getId());
	}

/**
 * 
 */
private static final long serialVersionUID = 1L;

//	@SequenceGenerator(name="TREE_ID_GENERATOR", sequenceName="dbid")
//	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TREE_ID_GENERATOR")
//@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="DBID")
	@Id
	private Integer id;

	@Column(name="idclass")
	private Integer idClass;	
	public Integer getIdClass() {return idClass;}
	public void setIdClass(Integer idclass) {this.idClass = idclass;}
	
	@Column(name="sort")
	private Long sort;

	@Column(name="tab_name")
	private String tabName;

	private String ttype;

	//bi-directional many-to-one association to Tree
	@ManyToOne(cascade=CascadeType.ALL)
//	@ManyToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
	@JoinColumn(name="did")
	private Tree parentT;
	public Tree getParentT() { return this.parentT; } 
	public void setParentT(Tree tree1) { 
		this.parentT = tree1;
		if(!parentT.hasChild())
			parentT.setChildTs(new ArrayList<Tree>());
		if(!parentT.getChildTs().contains(this))
			parentT.getChildTs().add(this);
	}

	
	//	@OneToOne(cascade={CascadeType.MERGE, CascadeType.PERSIST})
	@OneToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="id")
	private History history;
	public History getHistory() {return history;}
	public void setHistory(History history) {this.history = history;}

	//bi-directional many-to-one association to Tree
	@OneToMany(mappedBy="parentT",cascade=CascadeType.ALL)
	@OrderBy("sort")
	private List<Tree> childTs;
	public List<Tree> getChildTs() {return this.childTs; } 
	public void setChildTs(List<Tree> trees1) {this.childTs = trees1; }
	public boolean	hasChild() {return getChildTs()!=null&&getChildTs().size()>0;}
	

	//bi-directional many-to-one association to Tree
	@ManyToOne
	@JoinColumn(name="iddoc")
	private Tree docT;
	public Tree getDocT() { return this.docT; } 
	public void setDocT(Tree docT) { this.docT = docT; }

	//bi-directional many-to-one association to Tree
	@OneToMany(mappedBy="docT")
	private List<Tree> docTs;
	public List<Tree> getDocNodes() { return this.docTs; }
	public void setDocNodes(List<Tree> trees) { this.docTs = trees; }

	/*
	//bi-directional many-to-one association to Tree
	@ManyToOne
	@JoinColumn(name="ref")
	private Tree refT;
	//bi-directional many-to-one association to Tree
	@OneToMany(mappedBy="refT")
//	@LazyCollection(LazyCollectionOption.FALSE)
	private Set<Tree> refTs;

	public Tree getRefT() { return this.refT; } 
	public void setRefT(Tree tree4) { this.refT = tree4; }
	
	public Set<Tree> getRefTs() { return this.refTs; } 
	public void setRefTs(Set<Tree> trees4) { this.refTs = trees4; }
	 * */

	private Integer ref;
	public Integer	getRef()			{return ref;}
	public void		setRef(Integer ref)	{this.ref = ref;}
	public boolean	hasRef()			{return ref!=null&&ref>0;}
	
	
	public Tree() { }

	public Integer getId() { return this.id; } 
	public void setId(Integer id) {
		this.id = id; 
		if(getHistory()!=null)
			getHistory().setTree(this);
	}

	public Long getSort() { return this.sort; } 
	public void setSort(int sort) { setSort((long)sort); }
	public void setSort(Long sort) { this.sort = sort; }

	public String getTabName() { return this.tabName; } 
	public void setTabName(String tabName) { this.tabName = tabName; }

	public String getTtype() { return this.ttype; } 
	public void setTtype(String ttype) { this.ttype = ttype; }

	public String toString(){
		return "tree:id:"+id+":tabName:" +tabName+":idclass:"+idClass
		+":did:"+(parentT==null?0:parentT.getId())
		+":ref:"+ref+":sort:"+sort
		+":iddoc:"+(docT==null?0:docT.getId())
		+"\n mtlO="+getMtlO()
		+(null==getHistory()?"":"\n"+getHistory())
		//+":hashCode:"+this.hashCode()
		;
	}
//	public void copyDoc(CopyDocMtl copyDocM, MtlDbService dbService) {
//		copy(copyDocM.getDocT(),copyDocM,this,dbService);
//	}
//	private void copy(Tree originalT, CopyDocMtl copyDocM, Tree newDocT, MtlDbService dbService) {
//		if(copyDocM.getNotCopyS().contains(originalT))return;
//		setId(dbService.nextDbid());
//		setDocT(newDocT);
////		newDocT.getDocNodes().add(this);
//		copyAtt(originalT);
//		copyDocM.getTree().put(originalT.getId(), originalT);
//		copyDocM.getNewTree().put(originalT.getId(), this);
//		System.out.println(originalT+"\n to "+this);
//		copyDocM.addTreePaar(this);
//		dbService.getEntityManager().persist(this);
//		
//		if(originalT.getRef()!=null)
//			copyDocM.addRef(originalT);
//		if(originalT.getChildTs()!=null){
//			childTs=new ArrayList<Tree>();
//			for (Tree ocT : originalT.getChildTs()) {
//				Tree newT = new Tree();
//				newT.setParentT(this);
//				childTs.add(newT);
//				newT.copy(ocT, copyDocM, newDocT, dbService);
//			}
//		}
//	}
	public void copyAtt(Tree originalT) {
		setTabName(originalT.getTabName());
		setIdClass(originalT.getIdClass());
		setSort(originalT.getSort());
	}

//	@Transient private Set<TsTask> tses;
	transient private Set<TsTask> tses;
	public Set<TsTask> getTses() {return tses;}
	public boolean hasTses(){return tses!=null;}
	public void setTses() {tses=new ConcurrentSkipListSet<TsTask>();}
	public void addTs(TsTask ts) {
		if(!hasTses())	setTses();
		getTses().add(ts);
	}
	public void addTs(TsTask ts, Map<Long,TsTask> plan2TsS) {
		while(plan2TsS.containsKey(ts.getTime()))
			ts.setTime(ts.getTime()+1);
//		log.debug("timesId="+ts.getTimesT().getId()+" "+ts.getTimesT().getParentT().getParentT().getMtlO()+"\n"+ts);
		plan2TsS.put(ts.getTime(), ts);
		addTs(ts);
	}
	@Transient
	protected final Log log = LogFactory.getLog(getClass());

//	private Dose doseO;
//	public void putDose(MObject mtlO) {doseO=(Dose) mtlO;}
//	
//	private Drug drugO;
//	public void putDrug(MObject mtlO) {drugO=(Drug) mtlO;}
	/*
	protected int childNr(Tree tree) {
		int childNr = tree.getParentT().getChildTs().indexOf(tree);
		return childNr;
	}
	 */
	public int childNr() {
		int childNr = getParentT().getChildTs().indexOf(this);
		return childNr;
	}
}
