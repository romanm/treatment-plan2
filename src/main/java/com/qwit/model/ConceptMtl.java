package com.qwit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.qwit.domain.Arm;
import com.qwit.domain.MObject;
import com.qwit.domain.Protocol;
import com.qwit.domain.Task;
import com.qwit.domain.Tree;
import com.qwit.service.JXPathBean;

public class ConceptMtl extends TreeManager implements Serializable{
	private static final long serialVersionUID = 1L;
	protected static final Log log = LogFactory.getLog(ConceptMtl.class);
//	protected final Log log = LogFactory.getLog(getClass());
	void runRule() {
		
	}
	private Integer cycleIdClass;
	
	public Integer getCycleIdClass() {return cycleIdClass;}

	public ConceptMtl(Tree docT) {super(docT);}

	public int cycleNr(Tree schemaT){
		int childNr=schemaT.childNr(),cycleNr=childNr;
		for (int i = childNr; i >= 0; i--) 
			if(!isClassOrVariant(schemaT, schemaT.getParentT().getChildTs().get(i)))
				cycleNr--;
		return cycleNr;
	}

	@Override
	void addMtlMap(MObject mtlC) {
		// TODO Auto-generated method stub
	}
	

	@Override
	void add1ClassObj_depr(Tree tree,MObject objM) {
		if(objM instanceof Arm)
		{
			Arm armC=(Arm) objM;
			if("cycle".equals(armC.getArm())){
				cycleIdClass=armC.getId();
			}
		}
	}

	@Override
	void addTreeClassObj_depr(MObject objM, Tree tree) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean isThisDocumentTag(Tree tree) {
		if(getClassM().get(tree.getIdClass())instanceof Task)
			return false;
		return true;
	}

	public ConceptMtl setMap2copy() {
//		super.setMap2copy();
		idClassTaskMap = new HashMap<Integer,Tree>();
		return this;
	}

	protected	Arm	editArmO;
	public		Arm	getEditArmO() {return editArmO;}
	public void openEditArm(){
		editArmO=new Arm();
		setEditNodeT();
		log.debug(getEditNodeT());
		if(getEditNodeT().getIdClass()!=null && getEditNodeT().getMtlO() instanceof Arm)
		{
			Arm editNodeArmO=(Arm) getClassM().get(getEditNodeT().getIdClass());
			log.debug(editNodeArmO);
			editArmO.setArm(editNodeArmO.getArm());
			log.debug(editArmO);
		}else{
			editArmO.setArm("");
		}
	}
	protected	Task	editSchemaO;
	public Task getEditSchemaO() {return editSchemaO;}
	public void makeNewSchema()
	{
		log.debug("-----------"+editSchemaO);
		
		log.debug("-----------");
	}
	public void openEditSchema(){
		Tree definitionT = JXPathBean.getChild(getDocT(), "definition");
		setIdt(definitionT.getId());
		editSchemaO=new Task();
		log.debug(getIdt());
		setEditNodeT();
		log.debug(getEditNodeT());
		if(getEditNodeT().getIdClass()!=null && getEditNodeT().getMtlO() instanceof Task)
		{
			Task editNodeTaskO=(Task) getClassM().get(getEditNodeT().getIdClass());
			editSchemaO.setTask(editNodeTaskO.getTask());
		}else{
			editSchemaO.setTask("");
		}
		log.debug(editSchemaO);
	}

//	private String intention = "";
	public void reviseIntention(){
		Protocol conceptO=(Protocol) getDocT().getMtlO();
		log.debug("----");
//		if(null!=conceptO.getIntentionRests())
//		intention = "";
		reviseIntention(conceptO);
//		conceptO.setIntention(intention);
	}

	public static void reviseIntention(Protocol conceptO) {
		conceptO.setIntention("");
		/*
		log.debug("----"+conceptO.getIntentionAdjuvant());
		addIntention(conceptO, conceptO.getIntentionAdjuvant());
		log.debug("----");
		addIntention(conceptO, conceptO.getIntentionLine());
		log.debug("----"+conceptO.getIntentionLine());
		log.debug("----"+conceptO.getIntentionRests());
		 */
		if(null!=conceptO.getIntentionRests())
		for(IntentionRest d:conceptO.getIntentionRests()){
			log.debug(d);
			switch (d) {
				case PEOP: addIntention1(conceptO,intentionRest[0]); break;
				case KUR: addIntention1(conceptO,intentionRest[1]); break;
				case SAL: addIntention1(conceptO,intentionRest[2]); break;
				case L1: addIntention1(conceptO,intentionRest[3]); break;
				case L2: addIntention1(conceptO,intentionRest[4]); break;
				case L3: addIntention1(conceptO,intentionRest[5]); break;
				case NEO: addIntention1(conceptO,intentionRest[6]); break;
				case ADJ: addIntention1(conceptO,intentionRest[7]); break;
				case PAL: addIntention1(conceptO,intentionRest[8]); break;
				case IND: addIntention1(conceptO,intentionRest[9]); break;
				case CONSERVATION: addIntention1(conceptO,intentionRest[10]); break;
			}
		}
		log.debug("----"+conceptO.getIntention());
	}

	private static void addIntention(Protocol conceptO, String addIntention) {
		if(null!=addIntention&&addIntention.length()>0)
			addIntention1(conceptO,addIntention);
	}
	private static void addIntention1(Protocol conceptO,String addIntention) {
//		Protocol conceptO=(Protocol) getDocT().getMtlO();
		String intention = conceptO.getIntention();
		intention += intention.length()>0?",":"";
		intention += addIntention;
		log.debug(intention);
		conceptO.setIntention(intention);
	}
	public Protocol getConceptC()		{return (Protocol) getDocT().getMtlO();}
	public Protocol getNewProtocol()	{return getConceptC();}
	private static String[]
//			intentionRest		= {"peop","kur","sal","1line","2line","3line"
		intentionRest		= {
		"peop","kur","sal","l1","l2","l3"
		,"neo","adj","pal","ind","conservation"},
//		intentionRest		= {"peop","kur","sal"},
		intentionAdjuvant	= {"neo","adj","pal"},
		intentionLine		= {"1line","2line","3line"};
	private Set<IntentionRest> intentionRests;

	public void openEditConcept(){
		Protocol protocolO = (Protocol) getDocT().getMtlO();
		String intention = protocolO.getIntention();
//		String intention = protocolO.getIntention().toLowerCase();
		log.debug("------------"+intention);
		/*
		for(String s:intentionAdjuvant){
			log.debug("s="+s+" "+intention.contains(s));
			if(intention.contains(s)){
				protocolO.setIntentionAdjuvant(s);
				log.debug("------------"+protocolO.getIntentionAdjuvant());
			}
		}
		log.debug("------------"+protocolO.getIntentionAdjuvant());
		for(String s:intentionLine)
			if(intention.contains(s))
				protocolO.setIntentionLine(s);
		 */
		
		protocolO.setIntentionRests(new HashSet<IntentionRest>());
		log.debug("------------"+protocolO.getIntentionRests());
		for (int i = 0; i < intentionRest.length; i++) {
			log.debug("------------"+intentionRest[i]+" "+intention.contains(intentionRest[i]));
			if(intention.contains(intentionRest[i])){
				IntentionRest intentionRest2 = IntentionRest.values()[i];
				log.debug("------------"+intentionRest2);
				protocolO.getIntentionRests().add(intentionRest2);
			}
		}
		log.debug("------------"+protocolO.getIntentionRests());
	}

	@Override
	public void paste() {
		// TODO Auto-generated method stub
	}

	public static boolean isArmO(Tree t)	{return null!=t && t.getMtlO() instanceof Arm;}
	public static Arm	getArmO(Tree t)		{return (Arm) t.getMtlO();}

	private Map<Integer, SchemaMtl> defSchemaMtlMap;
	public Map<Integer, SchemaMtl> getDefSchemaMtlMap() {return defSchemaMtlMap;}
	public void setDefSchemaMtl(SchemaMtl schemaMtl) {
		if(null==defSchemaMtlMap)
			defSchemaMtlMap = new HashMap<Integer, SchemaMtl>();
		defSchemaMtlMap.put(schemaMtl.getDocT().getId(), schemaMtl);
	}

	private List<Tree> originalSchemaL;
	private Map<Integer,List<Tree>> variantSchemaML;
	public Map<Integer, List<Tree>> getVariantSchemaML() {
		if(null==variantSchemaML)
		{
			createOriginalVariantSchemaL();
		}
		return variantSchemaML;
	}

	public List<Tree> getOriginalSchemaL() {
		if(null==originalSchemaL)
		{
			createOriginalVariantSchemaL();
		}
		return originalSchemaL;
	}

	private void createOriginalVariantSchemaL() {
		originalSchemaL=new ArrayList<Tree>();
		variantSchemaML=new HashMap<Integer, List<Tree>>();
		Tree originalSchemaT;
		for(Tree defSchemaT:definitionSchemaL){
			Task defSchemaO = getTaskO(defSchemaT);
			Integer originalId=null;
			try {
				originalId = Integer.parseInt(defSchemaO.getCyclename());
			} catch (NumberFormatException e) {
			}
			if(null==originalId||defSchemaO.getCyclename().equals(defSchemaT.getId().toString())){
				//Original
				originalSchemaT = defSchemaT;
			}else{
				//Variant
				originalSchemaT = getTree().get(originalId);
				if(!variantSchemaML.containsKey(originalId))
					variantSchemaML.put(originalId, new ArrayList<Tree>());
				variantSchemaML.get(originalId).add(defSchemaT);
			}
			if(!originalSchemaL.contains(originalSchemaT))
			{
				originalSchemaL.add(originalSchemaT);
			}
		}
	}
	private List<Tree> definitionSchemaL;
	public List<Tree> getDefinitionSchemaL() {return definitionSchemaL;}
	public void addDefintitionSchemaT(Tree schemaT) {
		if(null==definitionSchemaL)
			definitionSchemaL=new ArrayList<Tree>();
		definitionSchemaL.add(schemaT);
	}

	public Tree previousPatientSchema(Tree tree) {
		if(null!=getPatientMtl()){
			int childNr = tree.getParentT().getChildTs().indexOf(tree);
			for (int i = childNr; i > 0; i--) {
				Tree tree2 = tree.getParentT().getChildTs().get(i);
				Tree tree3 = getPatientMtl().getPschemaRefM().get(tree2.getId());
				if(null!=tree3)
					return tree3;
			}
		}
		return null;
	}
}
