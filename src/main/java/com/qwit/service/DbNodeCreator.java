package com.qwit.service;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.MObject;
import com.qwit.domain.Tree;
import com.qwit.model.TreeManager;

public class DbNodeCreator extends MtlDbEntityManager {

	protected final Log log = LogFactory.getLog(getClass());

	List<DbNodeCreator> childsL;
	public List<DbNodeCreator> getChildsL(){return childsL;}
	public void setChildsL(List<DbNodeCreator> childsL) {this.childsL = childsL;}

	MObject objM;
	public MObject getObjM()			{return objM;}
	public void setObjM(MObject objM)	{this.objM = objM;}

	String findSelect;
	public void setFindSelect(String findSelect) {this.findSelect = findSelect;}

	public Tree find(String param1) {
		List resultList = em.createNativeQuery(findSelect, Tree.class)
		.setParameter("param1", param1)
		.getResultList();
		return get(resultList);
	}

	public Tree find() {
		Tree npT = find1();
		if(npT==null){
			getDbUpdate().update("createObj");
			npT=find1();
		}
		return npT;
	}
	private Tree find1() {
		String sql = getDbUpdate().getIfNotDbObj();
		List resultList = em.createNativeQuery(sql, Tree.class).getResultList();
		return get(resultList);
	}
	private Tree get(List resultList) {
		Tree npT=null;
		if(resultList.size()>0)
			npT = (Tree) resultList.get(0);
		return npT;
	}

	String tabName;
	public void setTabName(String tabName) {this.tabName = tabName;}
	public DbNodeCreator setTagName(String tabName) {
		setTabName(tabName);
		return this;
	}
	DbNodeCreator firstCreator;

	public Tree addChild() {
		long s = Calendar.getInstance().getTimeInMillis();
		Tree t1 = create(parentT,s, docT,treeManager,this);
		return t1;
	}
	
	private void createChilds(Tree parentT, Tree docT, TreeManager treeManager){
		long sort = 1;
		for(DbNodeCreator dnc:childsL)
			dnc.create(parentT,sort++,docT,treeManager,firstCreator);
	}

	private Tree create(Tree parentT, long sort, Tree docT, TreeManager treeManager, DbNodeCreator _1Creator){
		this.firstCreator=_1Creator;
		Tree t = addChild(parentT,tabName, sort, docT, treeManager);
		treeManager.nextCurrentId();
		if(childsL!=null)
			createChilds(t,docT,treeManager);
		return t;
	}
	public Tree addChild(Tree parentT, String tabName, long sort, Tree docT, TreeManager treeManager) {
		Tree t = add1Child(parentT, tabName, sort, docT);
//		if(parentT.getChildTs()==null)
//			parentT.setChildTs(new ArrayList<Tree>());
//		parentT.getChildTs().add(t);
		treeManager.getTree().put(t.getId(), t);
		return t;
	}
	public Tree add1Child(Tree parentT, String tabName, long sort, Tree docT) {
		Tree t = new Tree();
		t.setTabName(tabName);
		t.setId(nextDbid());
		t.setParentT(parentT);
		if(null!=docT)
			t.setDocT(docT);
		t.setSort(sort);
		setAtt(t);
		return t;
	}
	private void setAtt(Tree t) {
		try {
			if(ref!=null)		setRef(t);
			if(idclass!=null)	setIdClass(t);
			if(setTree!=null)	setTree(t);
		}
		catch (IllegalArgumentException e)	{e.printStackTrace();}
		catch (SecurityException e)			{e.printStackTrace();}
		catch (IllegalAccessException e)	{e.printStackTrace();}
		catch (InvocationTargetException e)	{e.printStackTrace();}
		catch (NoSuchMethodException e)		{e.printStackTrace();}
	}
	private void setTree(Tree t) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		getClass().getMethod(setTree,Tree.class).invoke(firstCreator,t);
	}
	private void setIdClass(Tree t) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Integer idcl = (Integer) getClass().getMethod(idclass).invoke(firstCreator);
		MObject mObject = firstCreator.getTreeManager().getClassM().get(idcl);
		if(mObject!=null){
			t.setMtlO(mObject);
		}else{
			t.setIdClass(idcl);
		}
	}
	private void setRef(Tree t) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Integer r = (Integer) getClass().getMethod(ref).invoke(firstCreator);
		t.setRef(r);
	}

	public DbNodeCreator setTreeManager(TreeManager tm) {
		this.treeManager=tm;
		docT=tm.getDocT();
//		parentT=tm.getParentT();
		parentT=docT;
		log.debug(parentT);
		return this;
	}

	String setTree;
	private Tree tree1,tree2,tree3;
	public void setSetTree(String setTree) {this.setTree = setTree;}
	public Tree getTree1() {return tree1;}
	public Tree getTree2() {return tree2;}
	public Tree getTree3() {return tree3;}
	public void setTree1(Tree tree1) {this.tree1 = tree1;}
	public void setTree2(Tree tree2) {this.tree2 = tree2;}
	public void setTree3(Tree tree3) {this.tree3 = tree3;}

	private Tree parentT,docT;
	public Tree getParentT() {return parentT;}
	public DbNodeCreator setParentT(Tree parentT) {
		log.debug("parentT = "+parentT);
		this.parentT=parentT;
		return this;
	}
	public DbNodeCreator setDocT(Tree docT) {
		this.docT = docT;
		return this;
	}
	protected TreeManager treeManager;
	public TreeManager getTreeManager() {return treeManager;}

	Integer ref1,ref2,ref3;
	public Integer getRef3() {return ref3;}
	public Integer getRef1() {return ref1;}
	public Integer getRef2() {return ref2;}
	public DbNodeCreator setRef1(Integer ref1) {this.ref1 = ref1;return this;}
	public DbNodeCreator setRef2(Integer ref2) {this.ref2 = ref2;return this;}
	public DbNodeCreator setRef3(Integer ref3) {this.ref3 = ref3;return this;}

	Integer idclass1,idclass2,idclass3,idclass4,idclass5,idclass6;
	public Integer getIdclass1() {return idclass1;}
	public Integer getIdclass2() {return idclass2;}
	public Integer getIdclass3() {return idclass3;}
	public Integer getIdclass4() {return idclass4;}
	public Integer getIdclass5() {return idclass5;}
	public Integer getIdclass6() {return idclass6;}
	public DbNodeCreator setIdclass1(Integer idclass1) {this.idclass1 = idclass1;return this;}
	public DbNodeCreator setIdclass2(Integer idclass2) {this.idclass2 = idclass2;return this;}
	public DbNodeCreator setIdclass3(Integer idclass3) {this.idclass3 = idclass3;return this;}
	public DbNodeCreator setIdclass4(Integer idclass4) {this.idclass4 = idclass4;return this;}
	public DbNodeCreator setIdclass5(Integer idclass5) {this.idclass5 = idclass5;return this;}
	public DbNodeCreator setIdclass6(Integer idclass6) {this.idclass6 = idclass6;return this;}

	String idclass, ref;
	public void setIdclass(String idclass) {this.idclass = idclass;}
	public void setRef(String ref) {this.ref = ref;}

}
