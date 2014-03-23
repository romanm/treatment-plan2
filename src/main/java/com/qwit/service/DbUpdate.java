package com.qwit.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

public class DbUpdate extends MtlDbEntityManager{
	protected final Log log = LogFactory.getLog(getClass());
	static SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	
	@Autowired	private SimpleJdbcTemplate simpleJdbc;
	public SimpleJdbcTemplate getSimpleJdbc() {
		return simpleJdbc;
	}

	public void setSimpleJdbc(SimpleJdbcTemplate simpleJdbc) {
		this.simpleJdbc = simpleJdbc;
	}

	String ifNotDbObj,ifYesDbObj,variableInt1;
	public String getVariableInt1() {return variableInt1;}
	public void setVariableInt1(String variableInt1) {this.variableInt1 = variableInt1;}
	public String getIfYesDbObj() {return ifYesDbObj;}
	public String getIfNotDbObj() {return ifNotDbObj;}
	public void setIfNotDbObj(String ifNotDbObj) {this.ifNotDbObj = ifNotDbObj;}
	public void setIfYesDbObj(String ifYesDbObj) {this.ifYesDbObj = ifYesDbObj;}
	
	private Timestamp dbUpdateDate;
	public void setDbUpdateDate(Timestamp dbUpdateDate) {
		this.dbUpdateDate = dbUpdateDate;
	}

	List<String> sqlL;
	private ArrayList<String> paramL;
	private int vi1;
	
	public void setDate(String date) throws ParseException {
		Date parse = df.parse(date);
		dbUpdateDate = new Timestamp(parse.getTime());
	}

	public List<String> getSqlL()			{return sqlL;}
	public void setSqlL(List<String> sqlL)	{this.sqlL = sqlL;}
	public Timestamp getDbUpdateDate()		{return dbUpdateDate;}

	@Transactional
	public void update(String updateType) {
		log.debug("--------------");

		StringBuffer sqls = new StringBuffer().append("\n-- DB UPDATE BEGIN \n");
		sqls.append("-- ").append(getDbUpdateDate()).append("\n");
		boolean isToUpdate = true;
		if(getIfYesDbObj()!=null){
			sqls.append("-- IfYesDbObj \n").append(getIfYesDbObj()+";\n");
			int size = simpleJdbc.queryForList(getIfYesDbObj()).size();
			isToUpdate = size>0;
		}
		log.debug("--------------");

		if(getIfNotDbObj()!=null){
			//			sqls.append("-- IfNotDbObj \n").append(niceSql(getIfNotDbObj()));
			sqls.append("-- IfNotDbObj \n").append(getIfNotDbObj()+";\n");
			int size = simpleJdbc.queryForList(getIfNotDbObj()).size();
			isToUpdate = size==0;
		}
		log.debug("--------------");

		if(variableInt1!=null){
			sqls.append("-- variableInt1 \n").append(getVariableInt1()+";\n");
			List<Map<String, Object>> queryForList 
			= simpleJdbc.queryForList(getVariableInt1());
			vi1=simpleJdbc.queryForInt(getVariableInt1());
		}
		log.debug("------- "+sqls);

		if(isToUpdate)
			updateDirect(sqls);
		String sql2 =
		" INSERT INTO oncows_db_version (type,version) " +
		" VALUES ('" + updateType + "','" +dbUpdateDate +"')";
		//	sqls.append(niceSql(sql2));
		sqls.append(sql2);
		log.debug("--------------"+sql2);

		simpleJdbc.update(sql2);
		sqls.append("-- DB UPDATE END \n");
		log.info(sqls);
	}
	public void updateDirect(StringBuffer sqls) {
		log.debug("----");
		for (String sql1 : getSqlL()) 
		{
			log.debug("----"+sql1);
			update1sql(sqls, sql1);
		}
	}
	public void update1sql(StringBuffer sqls, String sql1) 
	{
		sql1 = sql1.replaceAll("\\s+", " ");
		if(null!=variableInt1)
			sql1=sql1.replaceAll("variableInt1", ""+vi1);
		sqls.append(sql1+";\n");
		if(sql2sql.matcher(sql1).find()){
			if(sql1.contains("?"))	sql1=replaceParam(sql1);
			List<Map<String, Object>> dd = simpleJdbc.queryForList(sql1);
			for (Map<String, Object> row : dd) {
				for (String colName : row.keySet()) {
					log.debug(colName);
					String sql = (String) row.get(colName);
//					log.debug(sql);
					if(sql.contains("\"\"\""))
						sql=sql.replaceAll("\"\"\"", "'");
					else
						sql=sql.replaceAll("\"", "'");
					log.debug(sql);
//					StringBuffer niceSql = niceSql(sql);
//					sqls.append(niceSql);
					sqls.append(sql);
//					simpleJdbc.update(niceSql.toString());
					simpleJdbc.update(sql);
				}
			}
		}else{
//			StringBuffer niceSql = niceSql(sql1);
//			sqls.append(niceSql);
//			simpleJdbc.update(niceSql.toString());
			simpleJdbc.update(sql1);
		}
	}

	private void test2() {
		setParamL().setParam("").setParam("75").setParam("%");
		String sql1 = "INSERT INTO FORUMS (FORUM_ID, FORUM_NAME, FORUM_DESC) VALUES (?,?,?)";
		if(sql1.contains("?")){
			
		String sb = replaceParam(sql1);
		log.debug(sb);
		}
	}
	private String replaceParam(String sql1) {
		String[] sqsp = sql1.split("\\?");
		log.debug(sqsp.length);
		log.debug(paramL.size());
		StringBuffer sb = new StringBuffer(sqsp[0]);
		for (int i=1;i<sqsp.length;i++){
//			sb.append("'").append(paramL.get(i-1)).append("'").append(sqsp[i]);
			sb.append(paramL.get(i-1)).append(sqsp[i]);
		}
		return sb.toString();
	}
	public static void main(String[] args) {
		DbUpdate dbu = new DbUpdate();
		dbu.test();
//		dbu.test2();
	}
	private void test() {
		String string = "Select \n" +
				" 'insert into tree ";
		log.debug("-----"+string);
		Matcher m = sql2sql.matcher(string);
		log.debug("-----"+m);
		log.debug("-----"+m.find());
//		StringBuffer sb = niceSql(string);
//		log.debug("-----"+sb);
		string="insert history";
		log.debug("-----"+string);
		log.debug("-----"+m.find());
		string = "Select 'upDate into tree ";
		log.debug("-----"+string);
		log.debug("-----"+m.find());
	}
//	private StringBuffer niceSql(String sql) {
////		sql=sql.replaceAll("\\s+", " ");
//		StringBuffer sb = new StringBuffer();
//		Matcher m2 = sqlWs.matcher(sql);
//		while (m2.find())
//			m2.appendReplacement(sb, m2.group().toUpperCase());
//		m2.appendTail(sb);
//		sb.append(";\n");
//		return sb;
//	}
	static Pattern sql2sql = Pattern.compile("select.*(insert|update|delete)",Pattern.CASE_INSENSITIVE);
	static Pattern sqlWs = Pattern.compile("( select | insert | into | values | where | and | or )\\s"
			,Pattern.CASE_INSENSITIVE);
	public DbUpdate setParamL() {
		paramL=new ArrayList<String>();
		return this;
	}
	public DbUpdate setParam(String p) {
		paramL.add(p);
		return this;
	}
	
}
