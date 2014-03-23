package com.qwit.service;

import java.util.List;

import com.qwit.domain.MObject;
import com.qwit.domain.Notice;

public class DbNoticeCreator extends DbObjMCreator {

	private Notice notice;

	@Override
	public void create() {
		log.info("----- 1");
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		getDbUpdate().setParamL()
		.setParam(notice.getNotice().toString()).setParam(notice.getType()).setParam(notice.getB());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	public DbNoticeCreator() {
		setSql("SELECT d FROM Notice d WHERE d.notice = :notice AND d.type=:type");
//		dose=new Notice();
	}
	@Override
	public MObject read() {
		String tn = notice.getNotice().trim();
		notice.setNotice(tn);
		Notice pvDb=null;
		List<Notice> dL = em.createQuery(getSql())
		.setParameter("notice",	notice.getNotice())
		.setParameter("type",	notice.getType())
		.getResultList();
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}

	public DbObjMCreator setMtlO(Notice notice) {
		this.notice=notice;
		return this;
	}

	@Override
	public boolean isNull() {
		if(!notice.hasNotice()&&!notice.hasType())return true;
		return false;
	}

}
