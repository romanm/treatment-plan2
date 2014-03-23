package com.qwit.service;

import java.util.List;

import com.qwit.domain.App;
import com.qwit.domain.MObject;

public class DbAppCreator extends DbObjMCreator {

	private App app;

	@Override
	public void create() {
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		log.debug(app);
		log.debug(app.getAppapp());
		log.debug(app.getUnit());
		if(isNull())
			return;
		getDbUpdate().setParamL().setParam(app.getAppapp().toString()).setParam(app.getUnit());
		getDbUpdate().updateDirect(sqls);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}

	public DbAppCreator() {
		setSql("SELECT d FROM App d WHERE d.appapp = :appapp AND d.unit=:unit");
	}
	@Override
	public MObject read() {
		App appDb=null;
		List dL = em.createQuery(getSql())
		.setParameter("appapp",	app.getAppapp())
		.setParameter("unit",	app.getUnit())
		.getResultList();
//		log.debug(dL);
		if(dL.size()>0)
		{
			appDb=(App) dL.get(0);
		}
//		log.debug(appDb);
		return appDb;
	}

	@Override
	public boolean isNull() {
		if(!app.hasAppapp()&&!app.hasUnit())return true;
		return false;
	}

	public DbObjMCreator setMtlO(App mtlO) {
		this.app=mtlO;
//		log.debug(app);
		return this;
	}

}
