package com.qwit.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qwit.domain.MObject;
import com.qwit.domain.Times;

public class DbTimesCreator extends DbObjMCreator {

	private Times times;

	@Override
	public void create() {
		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		if(times.hasAbs()){
			getDbUpdate().setParamL().setParam(times.getAbs());
			String sql = getDbUpdate().getSqlL().get(0);
			getDbUpdate().update1sql(sqls, sql);
		}else{
			getDbUpdate().setParamL().setParam(times.getRelunit()).setParam(times.getApporder())
			.setParam(times.getVisual().toString()).setParam(times.getRelvalue().toString());
			String sql=getDbUpdate().getSqlL().get(1);
			getDbUpdate().update1sql(sqls, sql);
		}
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}
	@Override
	public MObject read() {
//		times.setAbs(times.getAbs().replaceAll(" ", ""));
		if(times.hasAbs())
			times.setAbs(absNN(times.getAbs()).replaceAll(" ", ""));
		Times pvDb=null;
		log.debug(times);
		List<Times> dL = timesL(times);
		if(dL.size()>0)
		{
			pvDb=dL.get(0);
		}
		return pvDb;
	}

	public static void main(String[] args) {
		DbTimesCreator d = new DbTimesCreator();
		String s = "08:00,09:00,00:00";
		String sb = d.absNN(s);
		System.out.println(s+" - "+sb);
	}
	private String absNN(String abs) {
		StringBuffer sb = new StringBuffer();
		Matcher m2 = absNN.matcher(abs);
		while (m2.find()) m2.appendReplacement(sb, m2.group(1)+":");
		m2.appendTail(sb);
		return sb.toString();
	}
	static Pattern absNN = Pattern.compile("0(\\d):");
	public List<Times> timesL(Times times) {
		List tL;
		String sql;
		if(times.hasAbs()){
			sql = "SELECT t FROM Times t WHERE t.abs = :abs ";
			tL = em.createQuery(sql)
			.setParameter("abs", times.getAbs())
			.getResultList();
		}else{
			sql = "SELECT t FROM Times t WHERE "
				+" t.relunit = :relunit "
				+" AND t.apporder = :apporder "
				+" AND t.visual = :visual "
				+" AND t.relvalue = :relvalue "
				;
			tL = em.createQuery(sql)
			.setParameter("relunit", times.getRelunit())
			.setParameter("apporder", times.getApporder())
			.setParameter("visual", times.getVisual())
			.setParameter("relvalue", times.getRelvalue())
			.getResultList();
		}
		log.debug(sql+" "+tL.size());
		return tL;
	}
	public List<Times> timesL_depr(Times times) {
		List tL;
		if(times.hasAbs()){
			String sql = "SELECT t FROM Times t WHERE t.abs = :abs ";
//					+" AND t.apporder is null "
//					+" AND t.visual is null "
//					+" AND t.relunit is null "
//					+" AND t.relvalue is null ";
			tL = em.createQuery(sql
					)
					.setParameter("abs", times.getAbs())
					.getResultList();
		}else{
			String sql = "SELECT t FROM Times t WHERE "
				+" t.relunit = :relunit "
				+" AND t.apporder = :apporder "
				+" AND t.visual = :visual "
				+" AND t.relvalue = :relvalue "
//				+" AND t.abs is null "
				;
			tL = em.createQuery(sql)
			.setParameter("relunit", times.getRelunit())
			.setParameter("apporder", times.getApporder())
			.setParameter("visual", times.getVisual())
			.setParameter("relvalue", times.getRelvalue())
			.getResultList();
		}
		return tL;
	}

	public DbObjMCreator setMtlO(Times times) {
		this.times=times;
		return this;
	}
	@Override
	public boolean isNull() {
		if(isRelNull()&&!times.hasAbs())return true;
		return false;
	}
	private boolean isRelNull() {
		if(!times.hasApporder())return true;
		return false;
	}
}
