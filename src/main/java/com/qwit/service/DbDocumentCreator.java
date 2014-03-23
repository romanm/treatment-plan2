package com.qwit.service;

import java.util.List;

import com.qwit.domain.Document;
import com.qwit.domain.MObject;


public class DbDocumentCreator extends DbObjMCreator {

/**
 * Work object to seek or create.
 */
private Document doc;

/**
 * Creator of Document.
 */
public DbDocumentCreator() {
setSql(" SELECT d FROM Document d WHERE "
+ " d.document= :document AND d.contenttype= :contenttype "
+ " AND d.filename=:filename AND d.type=:type  AND d.url=:url ");
doc = new Document();
}
@Override
public void create() {
	log.info("----- 1 "+doc);
	StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
	getDbUpdate().setParamL()
	.setParam(doc.getDocument())
	.setParam(doc.getContenttype())
	.setParam(doc.getFilename())
	.setParam(doc.getType())
	.setParam(doc.getUrl())
	;
	getDbUpdate().updateDirect(sqls);
	sqls.append("-- DB UPDATE END \n");
	log.info(sqls);
}

	@Override
	public MObject read() {
		String sql2 = " SELECT * FROM Document d " +
				" WHERE d.document='" +doc.getDocument()+"'" +
				" AND d.contenttype='" +doc.getContenttype()+"'" +
				" AND d.filename='" +doc.getFilename() +"'" +
				" AND d.type='" +doc.getType() +"'" +
				" AND d.url='" +doc.getUrl() +"' ";
		List<Document> dL = 
			em.createNativeQuery(sql2, Document.class).getResultList();
//			em.createQuery(sql)
//		.setParameter("document",	doc.getDocument())
//		.setParameter("contenttype",	doc.getContenttype())
//		.setParameter("filename",	doc.getFilename())
//		.setParameter("type",		doc.getType())
//		.setParameter("url",		doc.getUrl())
//		.getResultList();
		Document pvDb=null;
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}
	@Override
	public boolean isNull() {
		if(!doc.hasContenttype()&&!doc.hasFilename()&&!doc.hasType()&&!doc.hasUrl()) return true;
		return false;
	}
	
	
	//obsolete
	public DbObjMCreator setMtlO(Document doc) {
		this.doc = doc;
		return this;
	}
}
