package com.qwit.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.domain.Drug;
import com.qwit.domain.Folder;
import com.qwit.domain.Protocol;
import com.qwit.domain.Tree;
import com.qwit.model.DBTableEnum;
import com.qwit.model.DrugMtl;
import com.qwit.model.ExplorerMtl;
import com.qwit.util.FlowObjCreator;
import com.qwit.util.FormUtil;

@Service("explorerService")
@Repository
public class ExplorerService extends MtlDbService {

    protected final Log log = LogFactory.getLog(getClass());
    @Autowired
    protected SimpleJdbcTemplate simpleJdbc;
    @Autowired
    DirDocCreator dirDocCreator;

    public String getCompleteFolderTreeJSON() {
        log.debug("------------getCompleteFolderTreeJSON--------");
        String json = dirDocCreator.convertDir2JSON();
        return json;
//		Folder folder = getRootDocFolder();
//		return convertFolderTree2JSON(folder);
    }

    public void moveTreeNode(ExplorerMtl docMtl, Integer idFolder, String upDown) {
        log.debug(upDown);
        log.debug(idFolder);
        Tree idcT = em.find(Tree.class, idFolder);
        log.debug(idcT);
        docMtl.moveNode(idcT, upDown);
    }

    private Folder getPatientFolder(Folder f) {
        if ("patient".equals(f.getFolder())) {
            return f;
        }
        return getPatientFolder(f.getParentF());
    }

    private boolean isPatientFolder(Folder f) {
        boolean isPatient = "patient".equals(f.getFolder());
        if (isPatient) {
            return true;
        }
        if ("folder".equals(f.getFolder())) {
            return false;
        }
        return isPatientFolder(f.getParentF());
    }

    @Transactional(readOnly = true)
    public ExplorerMtl makeExplorerMtl(Integer idFolder, String search) {
        if (null != search) {
            search = search.toLowerCase();
        }
        //initialize oesSessionContainer
        //log.debug("///////" + OwsSessionContainer.getRequest().getParameter("id"));
        owsSession.getCopyNodeId();
//		public ExplorerMtl makeExplorerMtl(String tableName, Integer idFolder, String search) {
        Folder f = (Folder) em.find(Folder.class, idFolder);
        String tableType = f.getFolder();
        List<Folder> breadcrumb = new ArrayList<Folder>();
        Folder f1 = f;
        //BUG: wenn kein Verzeichnis ausgewählt im explorer -> kommt ein Fehler auf
        //tableName="protocol";
        if ("folder".equals(f1.getFolder())) {
            tableType = f1.getFolder();
        } else {
            while (!"folder".equals(f1.getParentF().getFolder())) {
                f1 = f1.getParentF();
                tableType = f1.getFolder();
                breadcrumb.add(f1);
            }
        }
        ExplorerMtl explorerMtl = new ExplorerMtl(tableType);
        explorerMtl.setAccessRight(getRight(f.getId()));
//		ExplorerMtl explorerMtl = new ExplorerMtl(tableName);
        if (f == null && !isValidFolderName(tableType)) {
            return null;
        }
        //set the breadcrumb
        List<Folder> breadcrumb2 = new ArrayList<Folder>();
        for (ListIterator<Folder> i = breadcrumb.listIterator(breadcrumb.size()); i.hasPrevious();) {
            breadcrumb2.add(i.previous());
        }
        //breadcrumb2.remove(0);
        breadcrumb2.add((Folder) em.find(Folder.class, idFolder));
        explorerMtl.setTreeBreadcrumb(breadcrumb2);
        setFolder(explorerMtl, idFolder);
        List<Map<String, Object>> folderContentFromDBJDBC = getFolderContentFromDB_JDBC(tableType, idFolder, search);
        if (isPatientFolder(f) && !"patient".equals(f.getFolder())) {
            Folder patientFolder = getPatientFolder(f);
            String sqlFolderRecursive = DrugMtl.getSqlFolderRecursive(patientFolder.getId());
            sqlFolderRecursive += " WHERE NOT " + f.getId() + " IN (fdid,idfolder)";
            log.debug(sqlFolderRecursive);
            log.debug("----------TODO------patient from other station-----" + patientFolder);
//			String inInstitute = inInstitute();
//			if(inInstitute.length()>0){
//				log.debug(inInstitute);
//				String sqlInstitutepatientFolder = getSqlInstitutepatientFolder(inInstitute);
            String sql = " SELECT * FROM Patient patientO, Tree patientT " + " WHERE "
                    + " ( lower(patientO.patient) LIKE '" + search + "%' "
                    + " OR lower(patientO.forename) LIKE '" + search + "%')"
                    + " AND patientT.id=patientO.idPatient "
                    + " AND patientT.did IN ( " + sqlFolderRecursive + " ) "
                    + //				" AND patientT.did IN ( " + sqlInstitutepatientFolder + " )";
                    " ";
            log.debug(sql);
            List<Map<String, Object>> queryForList = simpleJdbc.queryForList(sql);
            log.debug(queryForList.size());
            explorerMtl.setPatientMoveList(queryForList);
//			}
            log.debug("----------TODO------patient from other station-----");
        }
        if ("drug".equals(tableType)) {
//			explorerService.readDrugs(e);
            readDrugs(folderContentFromDBJDBC, explorerMtl);
            return explorerMtl;
        }
        Folder fd = getRootDocFolder();
        explorerMtl.setFolderDoc(fd);
        explorerMtl.setFolderContent(folderContentFromDBJDBC);

        if ("protocol".equals(tableType)) {
            setStatusMap(folderContentFromDBJDBC, explorerMtl);
        }

        String addDrug = owsSession.getRequest().getParameter("adddrug");
        if (null != addDrug) {
            FormUtil formUtil = new FormUtil();
            formUtil.setJsonDrugs(getJsonDrugs());
            explorerMtl.setFormUtil(formUtil);
        }
        return explorerMtl;
    }

    @Transactional(readOnly = true)
    public void setStatusMap(List<Map<String, Object>> folderContentFromDBJDBC, ExplorerMtl explorerMtl) {
        explorerMtl.setPrTaSt(folderContentFromDBJDBC);
        /*
         //		String inPvid = "";
         Integer protocolId = 0, protocolCnt=0;
         for (Map<String, Object> map : folderContentFromDBJDBC) {
         Integer 
         idprotocol=(Integer) map.get("idprotocol");
			
         if(idprotocol != null)
         {
         if(!protocolId.equals(idprotocol) ){
         protocolId=idprotocol;
         protocolCnt++;
         }
         //				Integer 
         //				pvid=(Integer) map.get("pvTaskid");
         //				if(pvid!=null)	inPvid+=","+pvid;
         //				pvid=(Integer) map.get("pvPrid");
         //				if(pvid!=null)	inPvid+=","+pvid;
         }
         }
         e.setProtocolCnt(protocolCnt);
         */
        explorerMtl.setDocStateMap(new HashMap<Integer, Map<String, Object>>());
        if (explorerMtl.getInPvid().length() > 0) {
            String inPvid = explorerMtl.getInPvid().substring(1);
            String sql =
                    " SELECT * FROM tree,pvariable,history,owuser "
                    + " WHERE id IN (" + inPvid + ") AND idclass=idpvariable AND id=idhistory "
                    + " AND idowuser=idauthor "
                    + " ORDER BY mdate ASC";
//			" ORDER BY mdate DESC";
            log.debug(sql);
            List<Map<String, Object>> res = simpleJdbc.queryForList(sql);
            explorerMtl.setDocStatus(res);
            /*
             for (Map<String, Object> map : res) 
             e.getDocStateMap().put((Integer) map.get("id"), map);
             */
        }
    }

    public void readDrugs(List<Map<String, Object>> folderContentFromDBJDBC, ExplorerMtl e) {
        List<Drug> drugs = new ArrayList<Drug>();
        Map<Integer, Drug> genericMap = new HashMap<Integer, Drug>();
        Map<Integer, List<Drug>> genericMapList = new HashMap<Integer, List<Drug>>();
        int n = 0;
        for (Map<String, Object> map : folderContentFromDBJDBC) {
            Integer id = (Integer) map.get("iddrug");
            Integer idgeneric = (Integer) map.get("idgeneric");
            Drug drug = new Drug();
            drug.setId(id);
            drug.setDrug((String) map.get("drug"));
            if (id.equals(idgeneric)) {
                drug.setGeneric(drug);
                genericMap.put(id, drug);
                drugs.add(drug);
                if (genericMapList.containsKey(id)) {
                    for (Drug drug3 : genericMapList.get(id)) {
                        drug.addTrade(drug3);
                    }
                }
            } else if (genericMap.containsKey(idgeneric)) {
                n++;
                Drug drug2 = genericMap.get(idgeneric);
                drug2.addTrade(drug);
            } else {
                n++;
                if (!genericMapList.containsKey(idgeneric)) {
                    ArrayList<Drug> trades = new ArrayList<Drug>();
                    genericMapList.put(idgeneric, trades);
                }
                genericMapList.get(idgeneric).add(drug);
            }
        }
        e.setDrugs(drugs);
        e.setDrugsSize(n);
    }

    @Transactional(readOnly = true)
    public void setFolder(ExplorerMtl explorerMtl, Integer idFolder) {
        Folder f = (Folder) em.find(Folder.class, idFolder);
        explorerMtl.setFolderO(f);
    }

    public void setFolderName(FlowObjCreator foc, int idf) {
        Folder folderO = getEntityManager().find(Folder.class, idf);
        foc.setFolder(folderO.getFolder());
    }
    String sqlUpdateFolder = "UPDATE folder SET folder=? WHERE idfolder=?";

    public void renameFolder(FlowObjCreator foc) {
        String folder = foc.getFolder();
        folder = folder.trim();
        simpleJdbc.update(sqlUpdateFolder, folder, foc.getIdf());
    }

    protected boolean isValidFolderName(String tableName) {
        if (tableName == null) {
            return false;
        }

        //security!
        //iterate through our table enumeration 
        //forbid execution for unknown tableNames
        DBTableEnum[] n = DBTableEnum.values();
        for (DBTableEnum e : n) {
            if (e.getTableName().equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    private List<Map<String, Object>> protocolList(Integer idFolder, String search) {
        String nofilter = owsSession.getRequest().getParameter("nofilter");
        if (null != nofilter) {
            owsSession.setIntentions(null);
            owsSession.initDrugSet();
        } else {
            String editDrugCdrug = owsSession.getRequest().getParameter("editDrugC.drug");
            if (null != editDrugCdrug && editDrugCdrug.length() > 0) {
                String drugSql = "SELECT d FROM Drug d where d.drug=:drug";
                List resultList = em.createQuery(drugSql).setParameter("drug", editDrugCdrug).getResultList();
                if (resultList.size() > 0) {
                    Drug d = (Drug) resultList.get(0);
                    owsSession.getDrugset().add(d);
//					owsSession.setInDrug(d.getId());
                }
            }
            String removeDrugS = owsSession.getRequest().getParameter("removeDrug");
            if (null != removeDrugS && removeDrugS.length() > 0) {
                int removeDrugI = Integer.parseInt(removeDrugS);
                Drug removeDrug = null;
                for (Drug drug : owsSession.getDrugset()) {
                    if (drug.getId().equals(removeDrugI)) {
                        removeDrug = drug;
                    }
                }
                if (null != removeDrug) {
                    owsSession.getDrugset().remove(removeDrug);
                }
            }
        }
        /*
         String sql="SELECT p.protocol,p.intention AS printention,p.idprotocol,t.*,t4.id AS pvTaskid ,t3.id AS pvPrid " +
         " FROM protocol p,tree t1 LEFT JOIN tree t3 ON t3.ref=t1.idclass " +
         ", task t, tree t2 LEFT JOIN tree t4  ON t4.ref=t2.idclass " +
         " WHERE t1.did IN (x1) AND t1.id=p.idprotocol AND t2.iddoc=t1.id AND t2.id=t.idtask x2" +
         " ORDER BY p.protocol,t.task,t3.id ,t4.id";
         */
        String sql =
                "\n SELECT idfolder,folder, tf.sort AS fsort, p.* FROM folder,tree tf,("
                + "\n SELECT t1.did AS pdid, p.protocol, p.protocolvar, p.intention AS printention, p.idprotocol, h2.mdate "
                + " AS md,t.task,t.idtask,t2.sort,t4.id AS pvTaskid ,t3.id AS pvPrid , t4.id AS t4id"
                + "\n FROM protocol p, "
                + " tree t1 LEFT JOIN tree t3 ON t3.ref=t1.idclass , "
                + " task t LEFT JOIN  history h2 ON t.idtask=h2.idhistory , "
                + " tree t2 LEFT JOIN tree t4 ON t4.ref=t2.idclass "
                + "\n WHERE t1.did IN (x1) AND t1.id=p.idprotocol AND t2.iddoc=t1.id AND t2.id=t.idtask x2 "
                + //		"\n ORDER BY p.protocol,t.task,t3.id ,t4.id " +
                ") p "
                + " WHERE tf.id=idfolder and tf.id=p.pdid "
                + "\n ORDER BY fsort, p.protocol, p.protocolvar, p.sort, p.pvPrid, p.t4id "
                + //		"\n ORDER BY fsort, p.protocol, p.protocolvar, p.task, p.pvPrid, p.t4id " +
                "";
        if (owsSession.getInDrug().length() > 2) {
            sql = sql.replace("x2", "x2 "
                    + " AND t2.id IN ( SELECT t1.id FROM tree t1, tree t2 "
                    + " WHERE t1.id=t2.iddoc AND t2.idclass in (" + owsSession.getInDrug() + ") )");
            //log.debug(sql);
        }
        String sql2 = "" + idFolder;
        if (owsSession.isSubDir()) {
            sql2 = DrugMtl.getSqlFolderRecursive(idFolder);
        }
        sql = sql.replace("x1", sql2);
        String[] intentions = owsSession.getRequest().getParameterValues("intention");
        String dd = Arrays.toString(owsSession.getIntentions());
        if (null == intentions && null != owsSession.getIntentions()) {
            intentions = owsSession.getIntentions();
        }
        if (null != nofilter) {
            intentions = null;
        }
        String x2 = "";
        if (null != intentions && !Arrays.toString(intentions).contains("all")) {
//		if(null!=intentions){
            x2 = " AND (";
            for (int i = 0; i < intentions.length; i++) {
                if (i > 0) {
                    x2 += " OR ";
                }
                x2 += " p.intention LIKE '%" + intentions[i] + "%'";
            }
            x2 += " ) ";
//			sql=sql.replace("x2", x2);
            owsSession.setIntentions(intentions);
        }
        String seek = owsSession.getRequest().getParameter("seek");
        owsSession.setSchemaSeek(seek);
        if (null != seek && seek.length() > 0) {
            x2 += " AND ( lower(p.protocol) LIKE '%"
                    + seek + "%' OR lower(t.task) LIKE '%"
                    + seek + "%' ) ";
        }
        sql = sql.replace("x2", x2);
        log.debug(sql);
        if (null != intentions && Arrays.toString(intentions).contains("all")) {
            owsSession.setIntentions(null);
        }
        log.debug(sql);
        List<Map<String, Object>> res = simpleJdbc.queryForList(sql);
        return res;
    }

    /**
     * Read from DB Folders for all or for authentified user.
     *
     * @param tableName
     * @param idFolder
     * @param search
     * @return
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFolderContentFromDB_JDBC(String tableName, Integer idFolder, String search) {
        if ("protocol".equals(tableName)) {
            return protocolList(idFolder, search);
        }
        if (idFolder < 0) {
            return null;
        }

        String sql;
        if (owsSession.isSubDir() == true) {
            sql = "WITH RECURSIVE foldercontent AS ( "
                    + "SELECT * FROM folder WHERE idfolder = ? "
                    + "UNION ALL "
                    + "SELECT f.* FROM folder AS f "
                    + "JOIN foldercontent AS fc ON (f.fdid = fc.idfolder) "
                    + ") "
                    + "SELECT p.* FROM " + tableName + " p, tree t, history h"
                    + " WHERE p.id" + tableName + " = t.id AND t.id=h.idhistory AND t.did IN "
                    + "( SELECT idfolder FROM foldercontent )";
        } else {
            sql = "SELECT p.* FROM " + tableName + " p, tree t, history h"
                    + " WHERE p.id" + tableName + " = t.id AND t.id=h.idhistory AND t.did IN "
                    + "( ? )";
        }

        log.debug("----------" + idFolder);
        log.debug("----------" + sql);
        if ("patient".equals(tableName)) {
            SecurityContext securityContext = owsSession.getSecurityContext();
            if (null != securityContext) {
                String userName = securityContext.getAuthentication().getName();
                if (null != userName && !"admin".equals(userName)) {
                    String sql1 = getReadWriteFolder(userName);
                    sql += " AND t.did IN ("
                            + "SELECT f.idfolder FROM (" + sql1
                            + ") f )";
                    log.debug("----------" + search);
                    log.debug(sql);
                    sql = addSqlSeek(tableName, search, sql);
                    sql = addOrderingAppendix(sql, tableName);
                    log.debug(sql);
                    List<Map<String, Object>> res = simpleJdbc.queryForList(sql, idFolder);
                    return res;
                }
            }
        }
//		sql=getSqltoTable(tableName);

        sql = addSqlSeek(tableName, search, sql);
        sql = addOrderingAppendix(sql, tableName);
        sql = sql.concat(";");

        log.debug(sql + "/" + idFolder);


        List<Map<String, Object>> res = simpleJdbc.queryForList(sql, idFolder);
//		List<Map<String, Object>>	res = simpleJdbc.queryForList(sql,idFolder,idFolder,idFolder,idFolder,idFolder,idFolder,idFolder,idFolder);
        log.debug("--------------getFolderContentFromDB_JDBC-----------size: " + res.size());

        return res;
    }

    /**
     * Append the ORDER BY statement for the given tableName to a sql statement
     *
     * @param sqlString an opened sql statment without an ORDER BY statement
     * @param tableName tableName, which can be: patient, diagnose, finding,
     * labor, drug, schema, task
     * @return the sql statement with an appended ORDER BY statement (only with
     * the above-mentioned tableName)
     */
    private String addOrderingAppendix(String sqlString, final String tableName) {
        String sqlAppendix = "";

        if (tableName.equals("patient")) {
            sqlAppendix = " ORDER BY patient, forename";
        } else if (tableName.equals("diagnose")) {
            sqlAppendix = " ORDER BY diagnose";
        } else if (tableName.equals("finding")) {
            sqlAppendix = " ORDER BY finding";
        } else if (tableName.equals("labor")) {
            sqlAppendix = " ORDER BY labor";
        } else if (tableName.equals("drug")) {
            sqlAppendix = " ORDER BY drug";
        } else if (tableName.equals("schema")) {
            sqlAppendix = " ORDER BY schema";
        } else if (tableName.equals("task")) {
            sqlAppendix = " ORDER BY task";
        }

        return sqlString.concat(sqlAppendix);
    }

    private String addSqlSeek(String tableName, String search, String sql) {
        log.debug("----------" + search);
        if (null != search && search != "") {
            if ("patient".equals(tableName)) {
                sql += " AND ( lower(p." + tableName + ") LIKE '" + search + "%'"
                        + " OR lower(p.forename) LIKE '" + search + "%'"
                        + ")";
            } else {
                sql += " AND  p." + tableName + " LIKE '" + search + "%'";
            }
        }
        return sql;
    }

    private String getSqltoTable(String tableName) {
        return "SELECT p.* FROM " + tableName + " p, tree t, history h"
                + " WHERE p.id" + tableName + " = t.id AND t.id=h.idhistory AND t.did IN ( "
                + " select f1.idfolder from folder f1 where f1.idfolder=? "
                + " union all select f2.idfolder from folder f2,folder f1 "
                + " where f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + " union all select f3.idfolder from folder f3,folder f2,folder f1"
                + " where f3.fdid=f2.idfolder and f3.idfolder!=f3.fdid and f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + " union all select f4.idfolder from folder f4,folder f3,folder f2,folder f1"
                + " where f4.fdid=f3.idfolder and f4.idfolder!=f4.fdid and f3.fdid=f2.idfolder and f3.idfolder!=f3.fdid and f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + " union all select f5.idfolder from folder f5,folder f4,folder f3,folder f2,folder f1"
                + " where f5.fdid=f4.idfolder and f5.idfolder!=f5.fdid and  f4.fdid=f3.idfolder and f4.idfolder!=f4.fdid and f3.fdid=f2.idfolder and f3.idfolder!=f3.fdid and f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + " union all select f6.idfolder from folder f6,folder f5,folder f4,folder f3,folder f2,folder f1"
                + " where f6.fdid=f5.idfolder and f6.idfolder!=f6.fdid and f5.fdid=f4.idfolder and f5.idfolder!=f5.fdid and f4.fdid=f3.idfolder and f4.idfolder!=f4.fdid and f3.fdid=f2.idfolder and f3.idfolder!=f3.fdid and f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + " union all select f7.idfolder from folder f7,folder f6,folder f5,folder f4,folder f3,folder f2,folder f1"
                + " where f7.fdid=f6.idfolder and f7.idfolder!=f7.fdid and f6.fdid=f5.idfolder and f6.idfolder!=f6.fdid and f5.fdid=f4.idfolder and f5.idfolder!=f5.fdid and f4.fdid=f3.idfolder and f4.idfolder!=f4.fdid and f3.fdid=f2.idfolder and f3.idfolder!=f3.fdid and f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + " union all select f8.idfolder from folder f8,folder f7,folder f6,folder f5,folder f4,folder f3,folder f2,folder f1"
                + " where f8.fdid=f7.idfolder and f8.idfolder!=f8.fdid and f7.fdid=f6.idfolder and f7.idfolder!=f7.fdid and f6.fdid=f5.idfolder and f6.idfolder!=f6.fdid and f5.fdid=f4.idfolder and f5.idfolder!=f5.fdid and f4.fdid=f3.idfolder and f4.idfolder!=f4.fdid and f3.fdid=f2.idfolder and f3.idfolder!=f3.fdid and f2.fdid=f1.idfolder and f2.idfolder!=f2.fdid and f1.idfolder=?"
                + ")";
    }

    public void cut(ExplorerMtl explorerMtl) {
        log.debug("-----------idt=" + explorerMtl.getIdt());
        log.debug("-----------idc=" + explorerMtl.getIdc());
        if (explorerMtl.getIdc() != null) {
            owsSession.setCutNodeId(explorerMtl.getIdc());
        }
    }

    public void paste(ExplorerMtl eMtl) {
        if (null != owsSession.getCutNodeId()) {
            String tableName = eMtl.getFolderType();
            Integer id = eMtl.getFolderO().getId();
            String sql = "UPDATE tree SET did=? WHERE id=?";
            log.debug("======paste cut: " + sql + " idc=" + owsSession.getCutNodeId());
            simpleJdbc.update(sql, id, owsSession.getCutNodeId());
            owsSession.setCutNodeId(null);
        } else if (null != owsSession.getCopyNodeId()) {
            log.debug("======paste copy: idc=" + owsSession.getCopyNodeId());
            FlowObjCreator foc = new FlowObjCreator();
            foc.setIdimport(owsSession.getCopyNodeId());
            log.debug("======paste copy: foc=" + eMtl.getFolderO().getId());
            foc.setIdf(eMtl.getFolderO().getId());
//			importProtocol(foc);
            copyProtocol(foc);
        }
    }

    public void paste_old(ExplorerMtl eMtl) {
        Integer idc = owsSession.getCopyNodeId();
        log.debug(idc);

        if (idc == null) {
            return;
        } else {
            String tableName = eMtl.getFolderType();
            Integer id = eMtl.getFolderO().getId();
            String sql = "UPDATE tree SET did=? WHERE id=?";
            log.debug("======paste: " + sql + " idc=" + idc);
            simpleJdbc.update(sql, id, idc);
        }
    }

    /**
     * Legt neue Verzeichnis an.
     *
     * @param eMtl
     * @param idf
     * @param folder
     */
    @Transactional
    public void newFolder(ExplorerMtl eMtl, Integer idf, String folder) {
//	public void newFolder(ExplorerMtl eMtl, FlowObjCreator foc){
//		Integer idf = foc.getIdf();
        Integer userId = owsSession.getUserId(simpleJdbc);
        String userName = owsSession.getSecurityContext().getAuthentication().getName();
        newFolder(idf, folder, userId, userName);
    }

    public void deleteEditNode(ExplorerMtl docMtl) {
        owsSession.getUserId(simpleJdbc);
        Tree delT = em.find(Tree.class, docMtl.getIdc());
        log.debug("-----------" + delT);
        if ("task".equals(delT.getTabName())) {
            boolean lastDefinitionSchema = lastDefinitionSchema(delT);
            if (lastDefinitionSchema) {
                return;
            }
            trashSchemaDefinition(docMtl, delT);
        } else if ("drug".equals(delT.getTabName())) {
            move2trashFolder(delT);
        } else if ("protocol".equals(delT.getTabName())) {
            /* 2012.01.05
             int idTrash = simpleJdbc.queryForInt(
             " SELECT f1.idfolder FROM folder f1,folder f0 " +
             " WHERE f0.idfolder=f1.fdid AND f1.folder='trash' AND f0.folder='protocol' ");
             Tree trashFolderT = em.find(Tree.class, idTrash);
             */
            Protocol protocolO = em.find(Protocol.class, delT.getId());
            protocolO.setProtocolvar(protocolO.getProtocolvar() + " :in trash " + protocolO.getId());
            log.debug(protocolO);
            move2trashFolder(delT);
            /* 2012.01.05
             move2trash_depr(trashFolderT, delT, delT.getParentT().getId(), "folder");
             */
            /*
             String tabName = "folder";
             delT.setParentT(parentT);
             Tree placeOldT = treeCreator.addChild(delT,tabName, 0, delT, docMtl);
             placeOldT.setIdClass(placeOldId);
             docMtl.addHistory(placeOldT);
             em.merge(delT);
             * */
        }
    }
}
