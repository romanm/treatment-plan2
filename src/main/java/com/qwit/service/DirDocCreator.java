package com.qwit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.core.context.SecurityContext;

import com.qwit.domain.Dir;
import com.qwit.model.DBTableEnum;

public class DirDocCreator extends MtlDbEntityManager {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired private SimpleJdbcTemplate simpleJdbc;
	protected String convertDir2JSON()
	{
		log.debug("-----------");
		Dir dirDocD=makeDirDocD();
		log.debug("-----------"+dirDocD);
		String str = "";
		for (Dir el : dirDocD.getChildDs())
		{
			DBTableEnum[] n = DBTableEnum.values();
			for (DBTableEnum e : n)
			{
				boolean t = e.getTableName().equals(el.getDir());
				if(e.getTableName().equals(el.getDir()))
				{
					String childs = convertFolderChildrenToJSON(el, el.getDir());
					str += "{id:'" + el.getId() + "'" + ", name:'"
							+ el.getDir() + "' ";
					if (!childs.isEmpty())
						str += ", " + childs;
					str += " },";
					break;
				}
			}
		}
		
		//remove the last comma of the last child element
		str = removeLastChar(str);
		
		String treestr = "{	'identifier':'id'," + "'label':'name'," + "'items':[";
		treestr += str;
		treestr += "]}";
		return treestr;
	}
	
	protected String convertFolderChildrenToJSON(Dir e, String parentName)
	{
		List<Dir> folderList = e.getChildDs();
		if (folderList==null||folderList.isEmpty())
			return "";
		String str = "";
		str += " children:[";
		for (Dir el : folderList)
		{
			String childs = convertFolderChildrenToJSON(el, parentName);
			str += "{id:'" + el.getId() + "'," + " name:'"
					+ el.getDir() + "', parentId:'" + el.getParentD().getId()+"', parentName:'"+parentName+"' " ;
			if (!childs.isEmpty())
				str += ", " + childs;
			str += " },";
		}
		str = removeLastChar(str);
		str += "]";
		return str;
	}
	
	protected String removeLastChar(String str)
	{
		int length = str.length() - 1;
		if (length > 0)
			return str.substring(0, length);
		else
			return str;
	}
	
	
	private Dir makeDirDocD() {
		String sql = getDirSelect();
		log.debug(sql);
		Dir dirDocD = null;
		Map<Integer,Dir> dirDocMap = new HashMap<Integer, Dir>();
		dirDocD = addDirPart(dirDocD, dirDocMap, sql);
		log.debug(dirDocD.getChildDs().size());
		log.debug(dirDocD);
		return dirDocD;
	}
	private Dir makeDirDocD_depr() {
		Integer idDoc = owsSession.getUserDocId(simpleJdbc);
		log.debug("--------------"+idDoc);
		Dir dirDocD=null;
		Map<Integer,Dir> dirDocMap = new HashMap<Integer, Dir>();
//		List<Map<String, Object>> queryForList = simpleJdbc.queryForList("select * from folder order by fdid,folder asc");
		String sql = " SELECT * FROM folder f JOIN tree t ON f.idfolder = t.id ";
		if(null==idDoc){
//			sql+=" WHERE reada='32' ";
		}
		sql+=" ORDER BY t.did, t.sort ASC";
		dirDocD = addDirPart(dirDocD, dirDocMap, sql);
		JXPathContext dirDocPC = JXPathContext.newContext(dirDocD);
		Pointer iteratePointers = dirDocPC.getPointer("childDs[dir='patient']");
		log.debug(iteratePointers);
		Dir patientDir = (Dir) iteratePointers.getValue();
		SecurityContext securityContext = owsSession.getSecurityContext();
//		if(null==securityContext)
		if(true)
			return dirDocD;
		String userName = securityContext.getAuthentication().getName();
		if(null!=userName&&!"admin".equals(userName)){
			sql=" SELECT f2.* FROM folder f1,folder f2 " +
				" WHERE f1.folder='patient' AND f2.fdid=f1.idfolder " +
				" AND (f2.reada='32' OR f2.institute in ( " +
				" SELECT idinstitute FROM owuser,tree t1,contactperson,tree t2, institute,tree t3,tree t4 " +
				" WHERE owuser='" +userName +"' " +
				" AND t1.idclass=idowuser " +
				" AND t2.id=t1.did AND t2.idclass=idcontactperson " +
				" AND t3.idclass=idinstitute AND t3.id=t4.did AND t4.idclass=idcontactperson" +
				" ))";
				log.debug(sql);
				patientDir.setChildDs(null);
				log.debug(patientDir);
				dirDocD = addDirPart(dirDocD, dirDocMap, sql);
				log.debug(patientDir);
				for(Dir d:patientDir.getChildDs()){
					sql=
					" WITH RECURSIVE foldercontent AS ( " +
					" SELECT * FROM folder WHERE idfolder = " +d.getId()+
					" UNION SELECT f.* FROM folder AS f " +
					" JOIN foldercontent AS fc ON (f.fdid = fc.idfolder) ) " +
					" SELECT * FROM foldercontent,tree WHERE idfolder=id ORDER BY did,sort";
					addDirPart2(dirDocD, dirDocMap, sql);
				}
		}
		log.debug(dirDocD);
		return dirDocD;
	}
	private void addDirPart2(Dir dirDocD, Map<Integer, Dir> dirDocMap, String sql) {
//		log.debug(sql);
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql);
		log.debug(queryForList.size());
		List<Dir> lostDir = new ArrayList<Dir>();
		for(Map<String, Object> map:queryForList){
			Integer id=(Integer) map.get("idfolder");
			Integer did=(Integer) map.get("fdid");
			Dir dir = dirDocMap.get(id);
			Dir parentD = dirDocMap.get(did);
			log.debug(dir+" - "+parentD);
//			log.debug(dir+" - "+dir.getChildDs().size()+" - "+parentD);
		}
	}
	private Dir addDirPart(Dir dirDocD, Map<Integer, Dir> dirDocMap, String sql) {
//		log.debug(sql);
		List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql);
		log.debug(queryForList.size());
		List<Dir> lostDir = new ArrayList<Dir>();
		for(Map<String, Object> map:queryForList){
			Integer id=(Integer) map.get("idfolder");
			Integer did=(Integer) map.get("fdid");
			String folder=(String) map.get("folder");
			Dir dir = new Dir(id,did,folder);
			dir.setDocMap(dirDocMap);
			Dir parentD = dirDocMap.get(did);
			if(dir.getId().equals(dir.getDid())){//root
				dirDocD=dir;
				continue;
			}
			if(parentD!=null){
				dir.addParentD(parentD);
			}else{
				lostDir.add(dir);
			}
		}
		for (Dir dir : lostDir) {
			Dir parentD = dirDocMap.get(dir.getDid());
			if(parentD!=null){
				dir.addParentD(parentD);
			}
		}
		log.debug(dirDocMap.size());
		return dirDocD;
	}
}
