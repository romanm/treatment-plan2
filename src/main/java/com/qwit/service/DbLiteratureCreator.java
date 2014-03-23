package com.qwit.service;

import java.util.List;

import com.qwit.domain.Literature;
import com.qwit.domain.MObject;
import com.qwit.domain.Tree;
import com.qwit.model.DrugMtl;

public class DbLiteratureCreator extends DbObjMCreator {

	public void revise(DrugMtl drugMtl){
		literature = drugMtl.getEditLiteratureO();
		log.debug(literature);
		literature.setTitle(correcturString(literature.getTitle()));
		literature.setAuthors(correcturString(literature.getAuthors()));
		log.debug(literature);
		literature = (Literature) build();
		Tree editNodeT = drugMtl.getEditNodeT();
		drugMtl.addMObject(literature, editNodeT);
//		if(!literature.getId().equals(editNodeT.getIdClass())){
//			editNodeT.setIdClass(literature.getId());
//			drugMtl.getClassM().put(literature.getId(), literature);
//		}
	}
	private String correcturString(String str) {
		String d = str
				.replaceAll("'", "’")
				.replaceAll("\\s+", " ")
				.trim();
		return d;
	}
	
	Literature literature;
	public DbLiteratureCreator (){
		setSql("SELECT d FROM Literature d WHERE" +
				" d.title= :title" +
				" AND d.authors=:authors" +
				" AND d.spring=:spring" +
				" AND d.springtype=:springtype"+
				" AND d.volume=:volume"+
				" AND d.page=:page"+
				" AND d.year=:year"+
				" AND d.url=:url"
				);
		literature = new Literature();
	}
	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(literature.getTitle())
		.setParam(literature.getAuthors())
		.setParam(literature.getSpring())
		.setParam(literature.getSpringtype())
		.setParam(literature.getPage())
		.setParam(literature.getYear().toString())
		.setParam(literature.getVolume())
		.setParam(literature.getUrl())
		;
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	@Override
	public MObject read() {
		Literature pvDb=null;
		List<Literature> dL = em.createQuery(getSql())
		.setParameter("title",		literature.getTitle())
		.setParameter("authors",	literature.getAuthors())
		.setParameter("spring",		literature.getSpring())
		.setParameter("springtype",	literature.getSpringtype())
		.setParameter("page",		literature.getPage())
		.setParameter("year",		literature.getYear())
		.setParameter("volume",		literature.getVolume())
		.setParameter("url",		literature.getUrl())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!literature.hasTitel()&&!literature.hasAuthors()&&!literature.hasSpring()&&!literature.hasSpringtype()) return true;
		return false;
	}

}
