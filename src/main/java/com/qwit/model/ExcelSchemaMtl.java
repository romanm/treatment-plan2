package com.qwit.model;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.hssf.util.HSSFCellUtil;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;

import com.qwit.domain.App;
import com.qwit.domain.Diagnose;
import com.qwit.domain.Dose;
import com.qwit.domain.Drug;
import com.qwit.domain.Finding;
import com.qwit.domain.Institute;
import com.qwit.domain.Ivariable;
import com.qwit.domain.Labor;
import com.qwit.domain.Notice;
import com.qwit.domain.Patient;
import com.qwit.domain.Protocol;
import com.qwit.domain.Task;
import com.qwit.domain.Times;
import com.qwit.domain.Tree;
import com.qwit.domain.Ts;
import com.qwit.service.JXPathBean;

public class ExcelSchemaMtl{
	protected final Log log = LogFactory.getLog(getClass());
	private SchemaMtl docMtl;
	private HSSFWorkbook workbook;
	private CreationHelper createHelper;
	int rowIndex;
	int maxColumns=14;
	int currentExcelRow;
	Sheet planSheet;
	String sheetName;
	Task taskO;
	String schemaName;
	Map<String, CellStyle> styles;
	ResourceBundle bundle;
	int th1_height = 30;
	boolean	isPlan;
	Tree refTimesT;
	int dayDelay = 0;
	JXPathBean jxp = new JXPathBean();
	//TODO: check that these var are initialized in the makeAppAppTempo function
	Ts dayInfusionEndTs=null, dayInfusionBeginTs=null;
	private Row currentRow;
	int CELL_TIME = 0;
	int CELL_DRUG = 2;
	int CELL_DOSE = 7;
	int CELL_DOSE_UNIT = 8;
	int CELL_APP = 9;
	int CELL_SIGNATURE = 11;
	int CELL_TS_NR = 13;
	Tree applicationDurationT=null;
	Integer applicationDuration;
	String inputSymbol="###";
	Ts currTs;
	DecimalFormat df 	= new DecimalFormat("##");
	DecimalFormat doseF = new DecimalFormat("##.###");
	private Row previousRow;
	MessageSource messageSource;
	public ExcelSchemaMtl(MessageSource ms, SchemaMtl docMtl) 
	{
		this.docMtl=docMtl;
		workbook = new HSSFWorkbook();
		createHelper = workbook.getCreationHelper();
		rowIndex=-1;
		sheetName="plan";
		messageSource=ms;
		
		// creating a custom palette for the workbook
        HSSFPalette palette = workbook.getCustomPalette();

        //replacing lime with freebsd.org gold
        palette.setColorAtIndex(HSSFColor.BROWN.index, (byte) 240, (byte) 240, (byte) 220);
        palette.setColorAtIndex(HSSFColor.LIME.index, (byte) 255, (byte) 255, (byte) 240);
            
		styles = createStyles();
		
//		bundle = ResourceBundle.getBundle("messages", Locale.GERMAN);
		bundle = ResourceBundle.getBundle("messages");
	}
	
	public int getMaxColumns() {return maxColumns;}

	public HSSFWorkbook getWorkbook() {
		return workbook;
	}
	
	//put this into docMtl
	private String getTaskName(Ts ts) {
		Tree drugT = ts.getTimesT().getParentT().getParentT();		
		String drug = "?";
		if(docMtl.isDrugO(drugT)){
			Drug drugO = docMtl.getDrugO(drugT);
			drug = drugO.getDrug();
		}else if(docMtl.isLaborO(drugT)){
			Labor laborO = docMtl.getLaborO(drugT);
			drug=laborO.getLabor();
		}
		return drug;
	}
	
	private void setupExcelPrint()
	{
		//show/hide gridlines 
		planSheet.setPrintGridlines(false);
//		planSheet.setDisplayGridlines(false);
			
        PrintSetup ps = planSheet.getPrintSetup();
        ps.setPaperSize(ps.A4_PAPERSIZE); 
//        printSetup.setLandscape(true);
      
        //set size of the time column
        planSheet.setColumnWidth(0, 6*256);
        planSheet.setColumnWidth(1, 6*256);
//        planSheet.autoSizeColumn(8);
        
        planSheet.setMargin(Sheet.TopMargin, (double) .25);
        planSheet.setMargin(Sheet.BottomMargin, (double) .25);
        planSheet.setMargin(Sheet.LeftMargin, (double) .75);
        planSheet.setMargin(Sheet.RightMargin, (double) .25);
        
        if(getCurrentRow().getRowNum()>78)
        	planSheet.setRowBreak(78);
        
        planSheet.setColumnBreak(getMaxColumns()-1);
        workbook.setPrintArea(workbook.getSheetIndex(planSheet), 0, getMaxColumns()-1, 0, getCurrentRow().getRowNum());
        ps.setScale((short) 75);
        
	}
	
	
	public void createPlanSheet() 
	{
		currentExcelRow=0;
		planSheet = workbook.createSheet(sheetName);
		taskO = docMtl.getTaskO(docMtl.getDocT());
		
		createNames();	       
        
		makeTitle();
		makePatientHeader();
		makeChemoAppSmallTable();
		makeDayPlan();
		
		setupExcelPrint();		
		
	}
	
	
	private void makeTitle()
	{
//		Cell cell00 = planSheet.createRow(currentExcelRow).createCell(0);
		createNextRowAndCells("title", th1_height);
//		r.setHeightInPoints(30);
		schemaName = taskO.getTask();
		String outpatient = taskO.getOutpatient();
		Protocol conceptO = (Protocol) docMtl.getDocT().getDocT().getMtlO();
		String schemaTitle = schemaName +" "+ trim(outpatient) + trimBraces(conceptO.getIntention());
//		setCellValue2(0, createHelper.createRichTextString(schemaTitle));
//		cell00.setCellStyle(styles.get("title"));
		mergeCells(CELL_TIME, (maxColumns-1), schemaTitle);
		//leerzeile
		createNextRowAndCells();
//		Cell cell10 = r2.createCell(0);
//		cell10.setCellStyle(styles.get("body"));
		mergeCells(CELL_TIME, (maxColumns-1));
		
//		log.debug(conceptO.getIntention());
				
	}
	
	public void makePatientHeader()
	{
		PatientMtl pMtl = docMtl.getPatientMtl();
		
		//Einrichtung
		Row r = createNextRow();
		Cell c = r.createCell(0);
//		Institute i = OwsSession.getOwsSession().getStationO();
		Map<String, Object> i = OwsSession.getOwsSession().getStationM();
		String iStr = "";
		if(i != null)
			iStr = trim((String)i.get("institute"))
			+ " Fax:" +trim((String)i.get("fax")) 
			+ " Tel:"+trim((String)i.get("phone"));
		c.setCellValue(t("mtl_institute")+": "+iStr);
		c.setCellStyle(styles.get("bold"));
		
		///////////////////////////////////////////////////////////////////////////////
		//Name of the patient
		///////////////////////////////////////////////////////////////////////////////
		r = createNextRow();
		c = r.createCell(0);
//		Patient p = docMtl.getPatientO(docMtl.getPatientMtl().getDocT());
		String  pStr = t("ui_secondName")+ ", "+t("ui_firstName")+", *"+t("ui_birthday");
		if(pMtl != null)
		{
			Patient p = (Patient) docMtl.getPatientMtl().getDocT().getMtlO();
			if(p != null)
			{
				DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				String date = dateFormat.format(p.getBirthdate().getTime());
				
				pStr = p.getPatient()+ ", "+p.getForename()+", "+p.getSex() + ", *"+date;
			}
			//write birthday in extra cell
//			Cell c1 = r.createCell(1);
//			c1 = r.createCell(2);
//			c1 = r.createCell(3);
//			c1.setCellValue(p.getBirthdate());
//			c1.setCellStyle(styles.get("date"));
		}
		c.setCellValue(pStr);
		c.setCellStyle(styles.get("bold"));

		///////////////////////////////////////////////////////////////////////////////
		//Diagnose of the patient
		///////////////////////////////////////////////////////////////////////////////
		r = createNextRow();
		c = r.createCell(0);
		String dStr= "?_______________";
		if(pMtl != null)
		{
			JXPathBean jxp = new JXPathBean();
			Tree dT = (Tree) jxp.getJxp(docMtl.getPatientMtl().getDocT()).getPointer("childTs[tabName='diagnose']").getValue();
			if(dT != null)
			{
				Diagnose diagnose = (Diagnose) dT.getMtlO();
				dStr = diagnose.getDiagnose();
			
	//			log.debug(dStr);
	//			log.debug(dT);
				//add notices
				for(Tree noticeT : dT.getChildTs())
				{
	//				log.debug(noticeT);
					if("notice".equals(noticeT.getTabName()))
					{
						String notice = trimHTML(((Notice) noticeT.getMtlO()).getNotice() );
						if(notice != null)
							dStr += ", " + notice;
					}
				}
			}
			
		}
		
		c.setCellValue(t("ui_diagnose")+": "+dStr);
		c.setCellStyle(styles.get("bold"));

		///////////////////////////////////////////////////////////////////////////////
		//Alter,... of the patient
		///////////////////////////////////////////////////////////////////////////////
		r = createNextRow();
		createCell(r, 0, maxColumns);
		Integer age, h, w;
		String kof = "?";

		if(pMtl != null)
		{
			if(pMtl.getAgeYear() != null)   r.getCell(1).setCellValue(pMtl.getAgeYear()); else  r.getCell(1).setCellValue("?");
			if(pMtl.getHeight() != null)   r.getCell(3).setCellValue(pMtl.getHeight());   else  r.getCell(1).setCellValue("?");
			if(pMtl.getWeight() != null)   r.getCell(5).setCellValue(pMtl.getWeight());   else  r.getCell(1).setCellValue("?");
			
			if(pMtl.getBsaFormula().equals("mosteller")) kof = "SQRT(height * weight/3600.0)";
			else if(pMtl.getBsaFormula().equals("haycock"))	kof = "(0.024265 * POWER(height, 0.3964) * POWER(weight, 0.5378))";
			else if(pMtl.getBsaFormula().equals("dubois"))	kof = "(0.007184 * POWER(height, 0.725)  * POWER(weight, 0.425))";
			else if(pMtl.getBsaFormula().equals("gehan"))     kof = "(0.0235   * POWER(height, 0.42246)* POWER(weight, 0.51456))";
			else if(pMtl.getBsaFormula().equals("boyd"))		kof = "(0.0003207 * POWER(heigh, 0.3)    * POWER(weight * 1000,0.7285 - 0.0188 * LOG10(weight * 1000)))";
			else kof = "?";
		}
		else
		{
			r.getCell(1).setCellValue("?");
			r.getCell(3).setCellValue("?");
			r.getCell(5).setCellValue("?");
		}
				
		log.debug("göße= "+t("ui_mtlHeight") + bundle.getString("ui_mtlHeight"));
		
		r.getCell(7).setCellFormula(kof); 
		r.getCell(0).setCellValue(t("ui_age"));
		r.getCell(2).setCellValue(", "+t("ui_mtlHeight"));
		r.getCell(4).setCellValue(t("ui_mtlCm")+", " + t("ui_mtlWeight"));
		r.getCell(6).setCellValue(t("ui_mtlKg")+", "+t("ui_BSA"));
		r.getCell(8).setCellValue(t("ui_squareMeter"));
		

		///////////////////////////////////////////////////////////////////////////////
		//Schemaname,... of the patient
		///////////////////////////////////////////////////////////////////////////////
		r = createNextRow();
		c = r.createCell(0);
		c.setCellValue(schemaName);
		c.setCellStyle(styles.get("bold"));

		///////////////////////////////////////////////////////////////////////////////
		//Zyklus,... of the patient
		///////////////////////////////////////////////////////////////////////////////
		r = createNextRow();
		c = r.createCell(0);
		Timestamp ts = docMtl.getOfDate().getMdate();
//		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
//		String date = dateFormat.format(ts.getTime());
	    c.setCellValue("0 "+ t("mtl_expr_value_cycle")+"; "+t("ui_begin")+" "+t("ui_day")+" 1=");
	    c = r.createCell(1);
	    c = r.createCell(2);
//	    c.setCellValue(date);*
	    Date d = new Date(ts.getTime());
	    c.setCellValue(d);
	    c.setCellStyle(styles.get("date_short"));
	    c = r.createCell(3);
	    c = r.createCell(4);
	    //repetition day
	    Integer cyclusDuration = docMtl.getSchemaC().getDuration();
	    int cd = cyclusDuration + 1;
	    c.setCellValue("Wiederholung "+t("ui_day")+ " "+ cd +" =");
	    c = r.createCell(5);
	    c = r.createCell(6);
	    Date rd = docMtl.getBeginCalendar().plusDays(cyclusDuration).toDate();
	    c.setCellValue(rd);
	    c.setCellStyle(styles.get("date_short"));
	    
	}
	
	
//	Iterator<Integer> itLast=null;
	ListIterator<Integer> itLast=null;
	ListIterator<Integer> itLast2=null;
	List daysList=null;
	private void makeChemoAppSmallTable()
	{
		Map<String, String> dddM = docMtl.getDrugDoseDaysM();
		//store the last position of the iterator, because there can be to many days and thus they shoulkd be displayed in further tables.
		
		daysList = new ArrayList( docMtl.getWeekDayS());
		itLast  = daysList.listIterator();
		itLast2 = daysList.listIterator();
		boolean doFirstTime = true;
		
		//walk through all weekDays and write several tables if the weekDays size is greater than (getMaxColumns()-2)-(CELL_TIME+6) - thats actually 6 columns
		while(itLast.hasNext())
		{
			//make header
			log.debug("- next table -");
			makeChemoAppSmallTableHeader();
//			log.debug("after header written itLast: "+ itLast.nextIndex());
			makeChemoAppSmallTableBody	(doFirstTime);
			//store the iterator start position in daysList to begin the next table from there
			itLast2 = daysList.listIterator(itLast.nextIndex());
			//do things only once - in the first table (i.e. set drug-, dose-, dose reference), so set the vars to false
			doFirstTime = false;
		}
	}

	
	/*
	 * param doFirstTime is used to format the first day differently  
	 */
	private void  makeChemoAppSmallTableHeader()
	{
		Date todayDate = Calendar.getInstance().getTime();
		DateFormat df = new SimpleDateFormat("D");
		String todayYearDay = df.format(todayDate);
		int modDayInterval = 2;
		
		// create 2 rows
		createNextRowAndCells("th1", th1_height/2);
		createNextRowAndCells("th1", th1_height/2);

		// write in the first row
		mergePrevRowCells(CELL_TIME, CELL_TIME+2, t("mtl_substance"));
		
		if(applicationDurationT != null)
		{
//			<div class="rightNormal small">
//			<c:out value="App #"/>
//			<c:choose>
//				<c:when test="${not empty sesApplicationNr and sesApplicationNr>0 }">
//					<c:out value="${sesApplicationNr}"/>
//				</c:when>
//				<c:otherwise><c:out value="*"/></c:otherwise>
//			</c:choose>
//			<a href="schema-${owsSession.schemaPart}-${docMtl.docT.id }?applicationNr=0" class="${sesApplicationNr>0?'n':'b' }">
//				<c:out value="/"/>
//				<fmt:formatNumber pattern="#" value="${applicationDuration }"/>
//			</a>
//			</div>
		}
		mergePrevRowCells(CELL_TIME+3, CELL_TIME+4, t("ui_dose"));
		mergeCells(CELL_TIME+3, CELL_TIME+4, "[mg/m²]");
		
		setCellValue	(CELL_TIME+5, t("ui_doseCalculated"), null, getPreviousRow());
		setCellValue	(CELL_TIME+5, "[mg]");

		Integer 	d = null;
		DateTime 	ddtOld=null;
		
		int i = CELL_TIME+6;
		ListIterator<Integer> it = itLast;
		while( it.hasNext())
		{
			boolean first = !it.hasPrevious();
			d= it.next();
//			log.debug(d);
			
			Pointer dayDelayP = jxp.jxp(docMtl.getPschemaT()).var("delayedDay").var("day",d)
					   .getPointer("childTs[mtlO/ivariable=$delayedDay and mtlO/ivalue=$day]/childTs[1]");

			if(dayDelayP.getValue() != null)
			{ 
				Tree ddP =     ((Tree) dayDelayP.getValue());
				dayDelay = 0 + ((Ivariable) ddP.getMtlO()).getIvalue();
			}
			
			int pd = d+dayDelay;
			DateTime ddt = docMtl.getBeginCalendar().plusDays(pd - 1);
			Date dd = ddt.toDate();
			
			Integer applicationDuration = null;
			double appNr = 0;
			if(applicationDuration!=null)
				appNr = d / applicationDuration +1;

			
			setCellValue(i, "# "+ d, "th1_align_right", getPreviousRow());
			
			if(first || ddtOld !=null &&  ddt.getMonthOfYear() != ddtOld.getMonthOfYear() || !it.hasNext())
			{
				DateFormat df2 = new SimpleDateFormat("dd.MM");
				setCellValue(i, dd, "date_dd.MM_th2");
//				log.debug("1");
			}
			else
			{
				DateFormat df3 = new SimpleDateFormat("dd");
				setCellValue(i, dd, "date_dd_th2");
//				log.debug("2");
			}

			if(dayDelayP.getValue()!=null)
			{
				Integer iv =     ((Ivariable) ((Tree) dayDelayP.getValue()).getMtlO()).getIvalue();
				setCellValue(i, iv);
//				log.debug("3");
			}
			
			ddtOld = ddt;
			
			if(i==(getMaxColumns()-2))
			{
				//save the last iterator position to start the next row from this one
				itLast = it;
//				log.debug("saved itLast"+itLast);
				break;
			}
			else
				i++;
		}//end while

//		setPrevRowCellValue	(i+1, t("uiApp")+" / ");
		setCellValue		(i+1, t("uiApp")+" / ",null,  getPreviousRow());
		setCellValue		(i+1, t("uiTempo"));
	}

	
	private void makeChemoAppSmallTableBody(boolean doFirstTime) 
	{
		for(Tree drugT : docMtl.getDocT().getChildTs())
		{
			if("drug".equals(drugT.getTabName()))
			{
				Tree doseT = jxp.getDose(drugT);
				Tree appT  = jxp.getApp(drugT);
				createNextRowAndCells();
				
				////////////////////////////////////////
				// Drug
				////////////////////////////////////////
//				String drugName= makeDrug(drugT);#
				Drug drug = (Drug) drugT.getMtlO();
				String drugName = drug.getDrug();
									
				//make references to drug cell
				if(doFirstTime) 
				{
					setCellName(drugName, getCurrentRow(), CELL_TIME);
					mergeCells(CELL_TIME, CELL_DRUG, drugName);
				}
				else
				{
					setCellFormula(CELL_TIME, drugName);
					mergeCells(CELL_TIME, CELL_DRUG);
				}
				
				////////////////////////////////////////
				// Dose
				////////////////////////////////////////
				String doseName = drugName+"_dose";
				if(doFirstTime)
				{
					setCellName(doseName, getCurrentRow(), CELL_DRUG+1);
					setCellValue(CELL_DRUG+1, makeDoseValue(doseT, false));					
				}
				else
					setCellFormula(CELL_DRUG+1, doseName);
				
			    
				Dose dose = docMtl.getDoseO(doseT);
				//unit if not standard m²
				if(dose !=null)
				{
					if(!dose.getUnit().contains("m²"))
					{
						setCellValue( CELL_DRUG+2, dose.getUnit());
						setCellAlign(CELL_DRUG+2, CellStyle.ALIGN_RIGHT);
					}
				}
				
				////////////////////////////////////////
				// Errechnete Dose
				////////////////////////////////////////
				if(docMtl.getPatientMtl()==null)
					setCellValue(CELL_DRUG+3, "###");
				else
				{
					String calcDoseName = drugName+"_calc_dose";
					if(doFirstTime)
					{
						setCellName(calcDoseName, getCurrentRow(), CELL_DRUG+3);
						setCellFormula(CELL_DRUG+3, doseName+"*KOF", true);
						log.debug("set errechnete dose: "+ doseName+"*KOF");
					}
					else
					{
						setCellFormula(CELL_DRUG+3, calcDoseName, true);
					}
					
					formatDoseFormula(CELL_DRUG+3);
					
//					evaluator.notifyUpdateCell(getCurrentRow().getCell(CELL_DRUG+3));
				}
		        
				
					
				////////////////////////////////////////
				// percent of the dose per day
				////////////////////////////////////////
				ListIterator<Integer> it = daysList.listIterator( itLast2.nextIndex());
				int cellPos = CELL_TIME+6;
				while(it.hasNext())
				{
					Integer d = it.next();
					
					Set<Ts> drugDayAppS = docMtl.getSchemaWeekPlan().getDrugDayAppS().get(drugT.getId()).get(d);
					if(drugDayAppS != null)
					{
////						log.debug("cellPos=" + cellPos + " day= " + d + "  -  it in for for: "+ it.nextIndex() + " of " + daysList.size() + "it.hasNext()=" +it.hasNext());
//						log.debug("cellPos=" + cellPos + " day= " + d + "  ts=");
//						for ( Ts ts : drugDayAppS)
//						{
//							if(ts.getBegin() != null)
//								;
//							else
//							{
//								log.debug("ts="+ts);
////								<fmt:formatDate var="yearDay" value="${ts}" pattern="D"/>
////								Integer taskTsNr = docMtl.getTsNrInTask(ts);
////								
////								Pointer doseAllModP = jxp.var("ref", doseT.getId()).jxp(docMtl.getPschemaT()).getPointer("childTs/childTs[ref=$ref]");
////								Pointer doseModP 	= jxp.var("taskTsNr").var("ttNr",taskTsNr).var("ref",doseT.getId())
////										    		 .jxp(docMtl.getPschemaT()).getPointer("childTs/childTs[ref=$ref and childTs/mtlO[ivariable=$taskTsNr]/ivalue=$ttNr]");
//
////								<c:set var="doseText">
////									<c:choose>
////										<c:when test="${empty patientMtl }"> <c:out value="###" /> </c:when>
////										<c:when test="${not empty doseModP.value.mtlO.ivalue or not empty doseAllModP.value.mtlO.ivalue}">
////											<span class="nowrap">
////											<qhtm:chemoDoseformatDoseFormula(CELL_DRUG+3);Mod ts="${ts}" view="calcDoseProc" notMeal="${true}" />
////											</span>
////										</c:when>
////										<c:when test="${(yearDay-todayYearDay) lt modDayInterval}">
////											100%
////										</c:when>
////										<c:otherwise>
////											100%
////										</c:otherwise>
////									</c:choose>
////								</c:set>
//									
////								<c:if test="${not empty applicationDuration}">
////									<spring:eval var="thisApp" expression="d / applicationDuration +1" />
////								</c:if>
////								<c:choose>
////									<c:when test="${empty schemaMtl.patientMtl}">
////										${doseText}
////									</c:when>
////									<c:when test="${sesApplicationNr==thisApp}">
////										<a href="javascript:void(0);" onclick="linkOtherDrug(${doseT.id},${ts.nr})">
////											${doseText}
////										</a>
////									</c:when>
////									<c:otherwise> ${doseText} </c:otherwise>
////								</c:choose>
////								<c:out value="${not empty ts.beyond?'…':''}"/>
//							}
//						}//for
						
						setCellValue(cellPos, 1, "percent");
						
						Pointer dayDelayP = jxp.jxp(docMtl.getPschemaT()).var("delayedDay").var("day",d)
								   .getPointer("childTs[mtlO/ivariable=$delayedDay and mtlO/ivalue=$day]/childTs[1]");
						if(dayDelayP.getValue() != null)
						{ 
							Tree ddP =     ((Tree) dayDelayP.getValue());
							dayDelay = 0 + ((Ivariable) ddP.getMtlO()).getIvalue();
						}
						int pd = d+dayDelay;
						DateTime ddt = docMtl.getBeginCalendar().plusDays(pd - 1);
						Date dd = ddt.toDate();
						DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
						
						String dailyCalcDoseName = drugName+"_daily_calc_dose_"+df.format(dd);
						setCellName(dailyCalcDoseName, getCurrentRow(), cellPos);
						log.debug("set errechnete dose: "+ dailyCalcDoseName + " in cell= " + cellPos);
						
					}//if
					
					if(cellPos>=(getMaxColumns()-2))
						break;
					else
						cellPos++;
				}//while
				log.debug("-");
			}//if
		}//for
	}
	
	
	
	private boolean formatDoseFormula(int cellNr)
	{
		return formatFormulaOperatorGt(cellNr, 100.0F, "integer", "float_2_digits_after_comma");
	}
	
	/*
	 * format the formula of the current row in the cell with index cellNr
	 * OperatorGt = Operantor Greater
	 */
	private boolean formatFormulaOperatorGt(int cellNr, float condValue, String style1, String style2)
	{
		Row r = getCurrentRow();
		if(cellNr >r.getLastCellNum())
			return false;
		
		HSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        double  cellValue = evaluator.evaluate( r.getCell(cellNr) ).getNumberValue() ;
        
		if(cellValue > condValue)
			setCellStyle(cellNr, style1);
		else
			setCellStyle(cellNr, style2);
		
		return true;
	}
	
	
	private void setCellName(String cellname, Row row, Integer col) 
	{
		Name   name;
        String sName="'"+sheetName+"'!";
        name = workbook.createName();
        name.setNameName(cellname);
		CellReference cr = new CellReference(row.getRowNum(), col);
//		log.debug(cr.formatAsString());
	    name.setRefersToFormula(sName+cr.formatAsString());
	}
	
	private String convert2ExcelChar(Integer col) {
		
		return null;
	}
	/**
	 * 
	 */
	private void makeDayPlan()
	{

		Row r = createNextRowAndCells("th1", th1_height);
		mergeCells(r, CELL_TIME,  1, t("ui_time"));
		mergeCells(r, CELL_DRUG, 6, t("tag_task_tumortherapy"));
		mergeCells(r, CELL_DOSE, 8, t("ui_dose_app"));
		mergeCells(r, CELL_APP, 10, t("uiApp")+" / "+t("uiTempo"));
		mergeCells(r, CELL_SIGNATURE, 12, t("uiSignature"));
		setCellValue(CELL_TS_NR, "#");
	
		//vom Roman variante
//		currentExcelRow=5;
//		Ts previousTs=null;
//		for (Ts ts : docMtl.getPlan()) {
//			if(null==previousTs||ts.getCday()>previousTs.getCday())
//				addDayTitleRow(ts);
//			Row row = planSheet.createRow(currentExcelRow);
//			Cell time1Cell = row.createCell(0);
//			time1Cell.setCellValue(
//					createHelper.createRichTextString("__:__"));
//			Cell drugNameCell = row.createCell(2);
//			String drug = getTaskName(ts);
//			drugNameCell.setCellValue(
//					createHelper.createRichTextString(drug));
//			drugNameCell.setCellStyle(styles.get("normal_text"));
//			currentExcelRow++;
//			previousTs=ts;
//		} 
		
		//variante vom Roman geschrieben vom Simon
//		r = createNextRowAndCells("th2", 0);
//		r.getCell(0).setCellValue(t("ui_day"));			//TODO: output

//		Ts previousTs=null;
//		for (Ts ts : docMtl.getPlan()) 
//		{
//			if(null==previousTs||ts.getCday()>previousTs.getCday())
//					addDayTitleRow2(ts);
//			
//			r = createNextRowAndCells();
//			
//			r.getCell(0).setCellValue("__:__");t(
//			
//			String drug = getTaskName(ts);
//			Cell drugNameCell = r.getCell(2);
//			drugNameCell.setCellValue(drug);
//			drugNameCell.setCellStyle(styles.get("normal_text"));
//			previousTs=ts;
//		}
		
		Ts 		previousTs	= null;
		Integer previousDay = -111;
		Integer firstSameDay= null;
		Integer toDay 		= null;
		Integer appBeginDay = 1;
		Integer appEndDay   = taskO.getDuration();
		Integer dayInfusion = 0;
				currTs		= null;
		Ivariable ivariable = null;
		
		applicationDurationT = docMtl.getApplicationDurationT(jxp.jxp(docMtl.getDocT()));
		if(applicationDurationT != null)
			ivariable = ((Ivariable)applicationDurationT.getMtlO());		
		applicationDuration  = (ivariable!=null) ? ivariable.getIvalue():0;
		
		for (Ts ts : docMtl.getPlan()) 
		{
			if(applicationDurationT == null || (ts.getCday() >=appBeginDay && appEndDay>=ts.getCday()))
			{
				currTs=ts;

				if(ts.getBegin()!=null)
					continue;
				else if( OwsSession.getOwsSession().isSameDayTogether() && toDay!= null && ts.getCday()<toDay && ts.getCday()>firstSameDay)
					continue;
				else
				{
					if(ts.getCday()>previousDay)
					{
						if("hour".equals(OwsSession.getOwsSession().getSchemaPart()) && docMtl.getHourPlan().getIntensivDayDrugM().get(ts.getCday())!=null)
						{
							createNextRowAndCells();
							setCellValue(CELL_TIME, "Intensiv day "+ts.getCday());
						}
						else
						{
							boolean first = true;
//							log.debug("test");
//							log.debug(docMtl.getRqmMS());
//							log.debug(previousDay);
							if(docMtl.getRqmMS().get(previousDay) != null)
							for (Ts ts2 : docMtl.getRqmMS().get(previousDay))
							{
								createNextRowAndCells();
							
								if(first)
									setCellValue(CELL_TIME, "Bedarf");
								else
									setCellValue(CELL_TIME, "");
										
								makeDayPlanDayDose(ts2);
								
								Tree drugT = ts2.getTimesT().getParentT().getParentT();
								String ddne="dummy notice";
	//							<c:set var="ddne">
	//								<mtl:ne forT="${schemaMtl.drugDose[drugT]}" noNr="true"/>
	//								<spring:eval var="dayS" expression="jxp.childs(drugT,'day')" />
	//								<c:forEach items="${dayS}" var="dayP">
	//									<c:set var="dayT" value="${dayP.value}"/>
	//									<c:if test="${fn:length(schemaMtl.ddNE[dayT])>0}">
	//										<mtl:ne forT="${dayT}" noRqm="true" noNr="true"/>
	//									</c:if>
	//								</c:forEach>
	//							</c:set>
								
								if(ddne != null )
								{
									createNextRowAndCells();
									mergeCells(CELL_TIME, CELL_TIME+4, ""+ ddne);
								}
								first=false;
							}
						}
						makeDayInfusion(dayInfusion);
					
					
						//create table head row of style type th2
						dayInfusion=0;
						createNextRowAndCells("th2", 0);
						Integer currWeek = (ts.getCday()-1)/7;
						Integer previousWeek = (previousDay-1)/7;
						if(currWeek>previousWeek)
						{
							setCellValue(CELL_TS_NR, "w"+(currWeek+1));
							setCellAlign(CELL_TS_NR, CellStyle.ALIGN_RIGHT);
						}
						
						Pointer dayDelayP = jxp.jxp(docMtl.getPschemaT()).var("delayedDay").var("day",ts.getCday())
									.getPointer("childTs[mtlO/ivariable=$delayedDay and mtlO/ivalue=$day]/childTs[1]");
						Ivariable dayDelayO = null;
						if(dayDelayP.getValue() != null)
						{
							dayDelayO = (Ivariable) ((Tree)dayDelayP.getValue()).getMtlO();
						}
						makePlanDate(ts, dayDelayO);
						
						if(toDay==null || ts.getCday()>toDay)
						{
							firstSameDay = ts.getCday();
							toDay = docMtl.getSameDayM().get(ts.getCday());
						}
						
						if(dayDelayP.getValue()!=null)
						{
							//TODO: output
	//					<sup class="red">+
	//						${dayDelayP.value.mtlO.ivalue }
	//					</sup>
							
						}
						previousDay = ts.getCday();
					}
					
					
					////
					if(docMtl.getDayRqm().get(ts.getTimesT().getParentT()) != null)
						;
					else if(ts.getBegin()!=null)
						;
					else if("hour".equals(OwsSession.getOwsSession().getSchemaPart()) && docMtl.getHourPlan().getIntensivDayDrugM().get(ts.getCday())!=null)
						;
					else
					{
						//<!-- Eingabe beschreibung -->
						makeDayPlanDay(ts);
						//calc how much infusion in a day (sum all ml-dosis)
						Tree drugT = ts.getTimesT().getParentT().getParentT();
						Iterator<Pointer> dDrugDoseMlIP = jxp.var("ml").jxpip(drugT, "childTs[mtlO/unit=$ml]|childTs/childTs[mtlO/unit=$ml]");
						while(dDrugDoseMlIP.hasNext())
						{
							 Pointer dDrugDoseMlP = (Pointer) dDrugDoseMlIP.next();
							 Dose dose = (Dose)((Tree)dDrugDoseMlP.getValue()).getMtlO();
							 dayInfusion = (int) (dayInfusion + dose.getValue());
						}
					}
					
				}			
				
			}
		}
	}
	
	private int makeDayPlanDay(Ts ts) {
		Ts processTs=ts;

		Tree drugT = ts.getTimesT().getParentT().getParentT();
		Row r;
		
		if(drugT.getTabName().equals("labor"))
		{
			String s = ((Labor) drugT.getMtlO()).getLabor();
			String s2 = t("finding_"+s);
			//TODO: what is this? output into wright cell 
			createNextRowAndCells();
			setCellValue(CELL_TIME+1, s2);
		}
		else if(drugT.getTabName().equals("finding"))
		{
			String s = ((Finding) drugT.getMtlO()).getFinding();
			String s2 = t("finding_"+s);
			//TODO: what is this? output into wright cell 
			createNextRowAndCells();
			setCellValue(CELL_TIME+1, s2);
		}
		else if(drugT.getTabName().equals("drug"))	
		{
			createNextRowAndCells();

			makeTsDay(ts);
			makeDayPlanDayDose(ts);
			setCellValue(CELL_SIGNATURE, "...........");
			setCellValue(CELL_TS_NR, ts.getNr());
				
			//go through the drugs that contained in parent drug (go through all solvents)
//			log.debug("before children drugs");
			for(Tree ddrugT : drugT.getChildTs())
			{
				boolean ruleCancel = docMtl.ruleCancel(ddrugT,ts,jxp);

				if(ruleCancel)
					;
				else if(ddrugT.getTabName().equals("drug"))
				{
//					log.debug("ddrugT.getTabName()==\"drug\"");
					createNextRowAndCells();
					
					Tree doseT = jxp.getDose(ddrugT);
					Dose dose = (Dose) doseT.getMtlO();
					
					setCellValue(CELL_DRUG, "+" + makeDrug(ddrugT));
					
					String unit = dose.getUnit();
					if(unit.contains("/"))
					{	
						setCellValue(CELL_DOSE, makeDoseValue(doseT, false));
						setCellValue(CELL_DOSE_UNIT, unit);
						setCellAlign(CELL_DOSE_UNIT, CellStyle.ALIGN_RIGHT);
					}
					else
					{
						setCellValue(CELL_DOSE, makeDoseValue(doseT, true));
						setCellValue(CELL_DOSE_UNIT, makeDoseUnit(doseT, true));
						setCellAlign(CELL_DOSE_UNIT, CellStyle.ALIGN_RIGHT);
					}
					makeDddNEnr(ddrugT);
						
				}
			}
			
			if(OwsSession.getOwsSession().isDrugNotice())
			{
				Iterator<Pointer> neForPS = jxp.var("notice").var("expr").jxpip(drugT,
						"childTs[childTs/tabName=$notice or childTs/tabName=$expr]"+
						"|childTs/childTs[childTs/tabName=$notice or childTs/tabName=$expr]");
							
				if(neForPS.hasNext())
				{
					//TODO:add output
//					<tr><td></td><td colspan="5">
					while(neForPS.hasNext())
					{
						 Pointer neForP = (Pointer) neForPS.next();
						 makeNE((Tree) neForP.getValue());
					}
//					</td></tr>
				}
			}
		}
		return 0;
	}

	//TODO: hier weiter machen / konvertieren
	private void makeTsDay(Ts ts) 
	{
		//from editTime.tagx
		
		Tree timesT = ts.getTimesT();
				
		boolean	isPlan =(ts!=null && (OwsSession.getOwsSession().getSchemaPart().equals("plan") || OwsSession.getOwsSession().getSchemaPart().equals("all")));
		
		String title = "";
		int myNr;
		if(isPlan)
		{
			myNr = ts.getNr();
		}
		else
			myNr = docMtl.getTimesNr().get(timesT);
		
		if(timesT.getRef() != null)
		{
				refTimesT = docMtl.getTree().get(timesT.getRef());
				Integer refNr = null;
				if(isPlan)
				{
					if(refTimesT == docMtl.getDocT())
					{
						refNr=0;
					}
					else
					{
						refNr = docMtl.getTimesTs(refTimesT,1).getNr();
					}
				}
				else
				{
					refNr = docMtl.getTimesNr().get(refTimesT);					
				}
				title = "("+myNr+")";
				Times times = ((Times)timesT.getMtlO());
				if(times.getRelunit().equals("H") || times.getRelunit().equals("M"))
				{
					title += times.getRelvalue(); 
					title += t("ui_times_"+times.getRelunit()); 
					
				}
				title += t("tag_times_apporder_"+times.getApporder());
				title += "("+refNr+")";
		}
		
//		<span title="${title}">
		if(timesT == null)
			;
		else if(docMtl.getEditTimesC() != null && (docMtl.getIdt() == timesT.getId() || docMtl.getIdt()==ts.getNr()))
		{
			makeTimes(timesT, ts);
			makeTimesHidden(timesT);
		}
		else if(ts != null)
		{
			makeTimes(timesT, ts);
		}
		else 
		{
			makeTimes(timesT, ts);
		}
//		</span>
		
	}
	
	
	//not needed thus it makes formular and hidden vars for data transfer
	private void makeTimesHidden(Tree timesT) {
		// TODO Auto-generated method stub
		
	}		

	
	private void makeTimes(Tree timesT, Ts ts) 
	{
		// where to write the time, in which cell?
		Times times = ((Times)timesT.getMtlO());
		String distance = "", hhmm="";
		
		//hack
		isPlan=true;
		
//		log.debug("makeTimes");
		
		if(times == null )
			;
		else if(times.getRelunit().equals("M") && times.getRelvalue() >= 60)
		{
			int restMin = times.getRelvalue()%60;
			
			distance  = "" + df.format((times.getRelvalue()-restMin)/60);
			distance += "h" +  df.format(restMin) + t("ui_times_M");
//			log.debug("1");
		}
		else if( times.getRelunit().equals("H") || times.getRelunit().equals("M"))
		{
			distance =	""+times.getRelvalue() + t("ui_times_"+times.getRelunit());
//			log.debug("2");
		}
		
//		log.debug("distance");
		
		String hhmmP ="__:__";
//		if(noHhmm)
//		{
//			hhmmP="";
//			
//		}

		if(times != null && times.getAbs() == null && timesT.getRef()== null  && timesT.getMtlO() != null)
		{
			setCellValue(CELL_TIME, "Error");
//			log.debug("3=Error");
		}
		else if(times != null &&  times.getAbs().indexOf("=") > 0)
		{
			String s = times.getAbs();
			s.replace("=", "-");
			setCellValue(CELL_TIME, s);
//			log.debug("4 = " + s);
		}
		else if(isPlan)
		{
//			log.debug("isPlan = true");
			Times timesO = ((Times)ts.getTimesT().getMtlO());
			if(ts.getTimesT().getRef()==null && timesO !=null && timesO.getAbs()!=null)
			{
				DateFormat dateFormat = new SimpleDateFormat("kk:mm");
				setCellValue(CELL_TIME+1, dateFormat.format(ts.getTime()));
			}
			else
			{
				setCellValue(CELL_TIME, hhmmP+"+");
				setCellValue(CELL_TIME+1, ts.getHhmm());
//				log.debug("6 = "+hhmmP+ts.getHhmm());
			}
		}
		else if(timesT.getRef() != null)
		{
			hhmm = docMtl.getTree().get(timesT.getRef()).getTabName().equals("task") ? "Beginn":"";
			refTimesT = docMtl.getTree().get(timesT.getRef());
			Integer rn = docMtl.getTimesNr().get(refTimesT);
			Integer n = docMtl.getTimesNr().get(timesT);
			if(times.getApporder().equals("0"))
				hhmm +="."+rn;
			else if(times.getApporder().equals("2"))
				hhmm +=""+rn+">.";
			else if(times.getApporder().equals("3"))
				hhmm +="."+n+">";
			else if(times.getApporder().equals("1"))
				hhmm +="&lt;"+rn+".";
			
			hhmm += (distance!=null ?"|":"")+distance+"|"+(times.getApporder().equals("3") ? rn:n);
			setCellValue(CELL_TIME, hhmmP+"("+hhmm+")");
//			log.debug("7 = "+ hhmm);
		}

		hhmm = "__:__";
//		if(noHhmm)
//		{
//			hhmm="";
//			
//		}
		
//		log.debug("isPlan = "+ isPlan);
//		log.debug(timesT.getIdClass()==null); 
		if(isPlan)
			;
		else if(times != null && times.getAbs().indexOf("=")>0)
		{
//			log.debug("8");
		}
		else if(timesT.getIdClass()==null )
		{			
			setCellValue(CELL_TIME, hhmm);
//			log.debug("9 = " + hhmm);
		}
		else
		{
			String s = times.getAbs();
			s.replace("00, ", "00,  ");
			setCellValue(CELL_TIME, s);
//			log.debug("10 = " + s);
		}
			
	}
	
	private String  makeDoseUnit(Tree doseT, boolean calculated) 
	{
		Dose dose = (Dose) doseT.getMtlO();
		String out="";
		if(doseT.getIdClass()==null || dose== null || dose.getUnit()==null)
		{
			out=inputSymbol;
		}
		else if(calculated && dose!= null && dose.getUnit().indexOf("/")>0)
		{
			out=dose.getUnit().substring(0, dose.getUnit().indexOf("/"));
		}
		else
		{
			out= (doseT.getIdClass() != null)? dose.getUnit():"";
		}
//	
		return out + t("mtl_dose_pro_"+dose.getPro());
	}
	
	private void makeDddNEnr(Tree ddrugT) {
		// TODO Auto-generated method stub
		
	}
	
	private Object makeDoseValue(Tree doseT, boolean calculated) 
	{
		Dose dose = (Dose) doseT.getMtlO();
		String out = null;
		if(docMtl.getPatientMtl() == null && calculated && docMtl.getCalcDoseR().get(doseT) !=null)
		{
			String d = docMtl.getCalcDoseR().get(doseT);
			log.debug("makeDoseValue - rowNr= "+getCurrentRow().getRowNum() + "-makedosevalue - calculated= "+ calculated + " dose="+d);
			return (Integer) Integer.parseInt(d);
		}
		else if(docMtl.getPatientMtl()==null && calculated && dose != null && dose.getUnit().indexOf("/")>0)
		{
			return "?____";
		}
		else if(docMtl.getPatientMtl()!=null && calculated)
		{
			String d = docMtl.getCalcDoseR().get(doseT);
			log.debug("makeDoseValue - rowNr= "+getCurrentRow().getRowNum() + "-makedosevalue - calculated= "+ calculated + " dose="+d);
			return (Integer) Integer.parseInt(d);
			
		}
		else if(dose == null)
		{
			out = "###";
		}
		else
		{
			String d = doseF.format(dose.getValue());
			log.debug("makeDoseValue - rowNr= "+getCurrentRow().getRowNum() + "-makedosevalue - calculated= "+ calculated + " dose="+d);
			return (Integer) Integer.parseInt(d);
		}
		
		return null;
	}
	
	private String  makeDrug(Tree drugT) 
	{
		
		return makeName(drugT);
//		<c:if test="${formUtil.mode=='confirmDrug' and schemaMtl.editNodeT.id==drugT.id}">
//			<span style="color: red;">/${schemaMtl.editTradeC.drug}?</span>
//		</c:if>
//		<c:if test="${formUtil.mode=='newTrade' and schemaMtl.editNodeT.id==drugT.id}">
//			<span style="color: red;">/${schemaMtl.editTradeC.drug}?</span>
//		</c:if>
//		<c:if test="${formUtil.mode=='newDrug' and schemaMtl.editNodeT.id==drugT.id}">
//			<span style="color: red;">/${schemaMtl.editDrugC.drug}?</span>
//		</c:if>
//		<c:if test="${withDose}">
//			<spring:eval var="doseT" expression="jxp.getDose(drugT)" />
//			<c:if test="${fn:indexOf(doseT.mtlO.unit,'/')>0}">
//				<c:set var="unit"><mtl:doseUnit doseT="${doseT}"/></c:set>
//				<c:set var="doseValue"><mtl:doseValue doseT="${doseT}"/></c:set>
//				<span class="small">(${fn:trim(doseValue)} ${fn:trim(unit)})</span>
//			</c:if>
//		</c:if>
//	
		
	}
	
	private String makeName(Tree drugT) 
	{
		Drug drug = (Drug) drugT.getMtlO();
		if(drug == null)
			drug = (Drug) docMtl.getPatientMtl().getClassM().get(drugT.getIdClass());
		
		if("finding".equals(drugT.getTabName()))
		{
			return ((Finding) drugT.getMtlO()).getFinding();
		}
		else if("labor".equals(drugT.getTabName()))
		{
			return ((Labor) drugT.getMtlO()).getLabor();
			
		}
		else if("drug".equals(drugT.getTabName()))
		{
			String out = "";
			String drugName = makeDrugGenericName(drug);

			if(drugT.getParentT().getId()==docMtl.getDocT().getId())
			{
				out = drugName;
			}
			else
			{
				out = drugName;
			}
			
			Drug trade = docMtl.getTrade().get(drug.getId());
			if(trade != null)
			{
				out +=  " ("+ trade.getDrug().toUpperCase()+") ";
			}
			return out;
		}
		else if("notice".equals(drugT.getTabName()))
		{
			Notice n = (Notice) drugT.getMtlO();
			return n.getNotice().substring(0, 10)+"["+drugT.getId()+"]";
		}
		else
		{
			return drug.getDrug();
		}
	}
	
	private String makeDrugGenericName(Drug drug) {
		String s = drug.getGeneric().getDrug();
		if(drug != drug.getGeneric())
		{
			s +=  " ("+ drug.getDrug().toUpperCase() +")";
			
		}
		return s;
	}
	
	
	private void makeNE(Tree value) 
	{
	}
	
	private void makeDayInfusion(Integer dayInfusion) 
	{
		if(dayInfusion > 0)
		{
//			//dayInfusionBeginTs ist immer null?
//			//was ist dayInfusionEndTs ?
//			if(dayInfusionEndTs!=null)
//			{
//				Row r = createNextRowAndCells();
////				r.getCell(0).setCellValue("Intensiv day "+ts.getCday());
//				mergeCells(r, 0, 1, t("ui_dayInfusion") + " = ");
//				mergeCells(r, 2, 3, ""+dayInfusion + " ml");
//				
//				double minInf = (dayInfusionEndTs.getTsBeyondEnd().getTime()-dayInfusionBeginTs.getTime())/1000/60;
//				double hInf   = minInf/60;
//				minInf = minInf-hInf*60;
//				
//				String s="";
//				if(hInf>0)
//					s += hInf+" h";	
//				if(minInf>0)
//					s += minInf+" min";
//				r.getCell(4).setCellValue(s);
//				
//				mergeCells(r, 5, 6, "Σ#"+(dayInfusionBeginTs.getNr() -dayInfusionEndTs.getNr()) );
//			}
		}
	}
	
	
	private void makePlanDate(Ts ts, Ivariable dayDelayO)
	{
		JXPathBean jxp = new JXPathBean();
		Integer lastSameDay = docMtl.getSameDayM().get(ts.getCday());
		setCellValue(CELL_TIME,   t("ui_day"));
		setCellValue(CELL_TIME+1, ""+ts.getCday());
		Ts tsDate=ts;
		if(dayDelayO!= null)
		{
			
			Integer pd = ts.getCday()+dayDelayO.getIvalue();
			tsDate = (Ts) docMtl.getBeginCalendar().plusDays(pd - 1).toDate();
		}
		
//		<fmt:formatDate value="${tsDate}" dateStyle="short"/>
		setCellValue(CELL_DRUG, new Date(tsDate.getTime()), "date_short");
		
		if(lastSameDay != null)
		{
			setCellValue(CELL_DRUG+1, " - bis Tag ");
			Date lastSameDayD = jxp.dateAddDay(ts,lastSameDay-ts.getCday());
			
			setCellValue(CELL_DRUG+2, ""+lastSameDay);
			DateFormat dateFormat = new SimpleDateFormat("EE");
			
			dateFormat.format(lastSameDayD);
//			<fmt:formatDate value="${lastSameDayD}" dateStyle="short"/>
		}
	}
	
	private void makeDayPlanDayDose(Ts  ts) 
	{
		Tree drugT = ts.getTimesT().getParentT().getParentT();
		Tree doseT = jxp.getDose(drugT);
		Dose dose = ((Dose) doseT.getMtlO());
		
		//drug
//		setCellValue2(CELL_DRUG, makeDrug(drugT));
		Drug drug = (Drug) drugT.getMtlO();
		String drugName = drug.getDrug();
		//set the reference only if the drug is a chemotherapie drug
		if(drugT.getParentT().getId()==docMtl.getDocT().getId())
			setCellFormula(CELL_DRUG, drugName);
		else
			setCellValue(CELL_DRUG, makeDrug(drugT));
		
		if(dose.getUnit().contains("/"))
		{
			String title = makeDoseTitle(ts);
			//TODO:output
//			<c:out value=" " />
			String malDefDose = makeMalDefDoseValue(ts, true);
			
			//TODO:output
			//"("+malDefDose.trim()+")";
//			<span class="small" title="${title}">(${fn:trim(malDefDose)})</span>
			
		}
		makeDddNEnr(drugT);
		makeTabletGabe(drugT);
		
		makeChemoDoseMod(drugT, ts, drugName, dose);
		
		setCellValue(CELL_DOSE_UNIT, makeDoseUnit(doseT, ts, true));
		setCellAlign(CELL_DOSE_UNIT, CellStyle.ALIGN_RIGHT);
		setCellValue(CELL_APP, makeDoseApp(doseT));
		
		makeEditDrugAppApp(drugT);
	}
	
	
	
	private String  makeEditDrugAppApp(Tree drugT) {
		
			Tree appT = jxp.getApp(drugT);
		
			if(appT != null)
			{
//				<c:out value=" " />
				return makeEditAppApp(appT);
				
			}
			return "";
	}
	
	
	private String  makeEditAppApp(Tree appT) {
		if(appT != null)
		{
			return makeAppAppTempo(appT);
		}
		return "";
		
	}
	
	
	private String makeAppAppTempo(Tree appT) 
	{
		String out="";
		Pointer appP;
		Tree modAppT = null;
		if(currTs!=null && docMtl.getPatientMtl()!=null && !appT.getDocT().getTabName().equals("patient"))
		{
			appP = jxp.var("refTasks", docMtl.getDocT().getId()).var("refTask",appT.getParentT().getId())
					  .var("tTsNr",docMtl.getTaskTsNr(currTs)).var("taskTsNr").var("app").jxp(docMtl.getPatientMtl().getDocT())
		              .getPointer("childTs[ref=$refTasks]/childTs[ref=$refTask]/childTs[childTs/mtlO[ivariable=$taskTsNr]/ivalue=$tTsNr]/childTs[tabName=$app]");
			modAppT = (Tree) appP.getValue();
		}
		
		if(modAppT != null)
		{
			makeAppAppTempo(modAppT);
			
		}
		else
		{
			App app = (App) appT.getMtlO();
			if(app.getAppapp()>0)
			{
				Integer min = app.getAppapp();
				if(min>60 && "min".equals(app.getUnit()))
				{
					out  += "" + df.format((min - min % 60) / 60);
					min=min % 60;
				}
				
				out  += "" + df.format(min);
				
				if(dayInfusionBeginTs != null)
				if(dayInfusionBeginTs.getCday() != currTs.getCday())
				{
					dayInfusionBeginTs=currTs;
				}
				dayInfusionEndTs=currTs;
				
			}
			else if(app.getUnit() != null)
				;
			else
			{
				out = "Dauer?";
			}
			
			if(app.getAppapp() >0)
				return out + app.getUnit();
			
		}
		return out;
	}
	
		
	private String makeDoseApp(Tree doseT)
	{
		Dose dose = (Dose) doseT.getMtlO(); 
		if(doseT.getIdClass()==null || dose.getApp() == null)
			return inputSymbol;
		else
			return dose.getApp();
	}
	
	private String  makeDoseUnit(Tree doseT, Ts ts, boolean calculated) 
	{
		Dose dose = (Dose) doseT.getMtlO(); 
		String out = "";
		if(doseT.getIdClass()==null || dose == null || dose.getUnit() == null )
		{
			out = inputSymbol;
		}
		else if(calculated && dose.getUnit().indexOf("/")>0)
		{
			out =  dose.getUnit().substring(0, dose.getUnit().indexOf("/"));
		}
		else
		{
			out =  (doseT.getIdClass()!=null) ?  dose.getUnit():"";
			
		}
		return out + t("mtl_dose_pro_"+dose.getPro());
	}
	
	private void makeChemo1DoseMod(Tree doseT) {
		
		 
	}
	
	
	private void makeChemoDoseMod(Tree drugT, Ts ts, String drugName, Dose dose) 
	{
		Float doseValue = dose.getValue();
		//only for chemodrugs do the references to the formulas of their doses
		if(drugT.getParentT().getId()==docMtl.getDocT().getId())
		//	if(docMtl.isChemo(drugT))
//		if(docMtl.isChemoDose(drugT) && docMtl.getPatientMtl()!= null)
		{
			Integer d = ts.getCday();
			Set<Ts> drugDayAppS = docMtl.getSchemaWeekPlan().getDrugDayAppS().get(drugT.getId()).get(d);
			if(drugDayAppS != null)
			{
		//		setCellValue(CELL_DOSE, 1, "percent");
				
				Pointer dayDelayP = jxp.jxp(docMtl.getPschemaT()).var("delayedDay").var("day",d)
						   .getPointer("childTs[mtlO/ivariable=$delayedDay and mtlO/ivalue=$day]/childTs[1]");
				if(dayDelayP.getValue() != null)
				{ 
					Tree ddP =     ((Tree) dayDelayP.getValue());
					dayDelay = 0 + ((Ivariable) ddP.getMtlO()).getIvalue();
				}
				int pd = d+dayDelay;
				DateTime ddt = docMtl.getBeginCalendar().plusDays(pd - 1);
				Date dd = ddt.toDate();
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
				
				String dailyCalcDoseName = drugName+"_daily_calc_dose_"+df.format(dd);
				String calcDoseName = drugName+"_calc_dose";
				setCellFormula(CELL_DOSE, dailyCalcDoseName+"*"+calcDoseName);
				formatDoseFormula(CELL_DOSE);
				log.debug("makeChemoDoseMod - rowNr= "+getCurrentRow().getRowNum() + "dose="+dose + " - 1 - formula ");
			}
			else
			{
				log.debug("makeChemoDoseMod - rowNr= "+getCurrentRow().getRowNum() + "dose="+dose + " - 2 ");
				setCellValue(CELL_DOSE, doseValue);
			}
		}
		else
		{
			if(ts.getTimesT() == null)
				return;
			Times times = (Times)ts.getTimesT().getMtlO();
//			log.debug("-------------------------------------------------");
//			log.debug(times);
//			log.debug(times.getAbs());
			if(times == null)
				return;
			
			boolean isMealDose = times.getAbs().contains("=");
			
			if(isMealDose)
			{
					String abs = times.getAbs();
					int malN = "day"!=dose.getPro() ? 1 : abs.replace("=","").replace("0", "").length();
					float mealDoseValue = doseValue / malN;
					String out = abs.replace("=","-").replace("1", df.format(mealDoseValue));
					setCellValue(CELL_DOSE, out);
					log.debug("makeChemoDoseMod - rowNr= "+getCurrentRow().getRowNum() + "dose="+dose + " - 3 ");
			}
			else
			{
				setCellValue(CELL_DOSE, doseValue.intValue());
				log.debug("makeChemoDoseMod - rowNr= "+getCurrentRow().getRowNum() + "dose="+dose + " - 4 ");
			}
		}
		
		setCellAlign(CELL_DOSE, CellStyle.ALIGN_RIGHT);
	}
	
	
	private String makeMalDefDoseValue(Ts ts, boolean b) {
		// TODO Auto-generated method stub
		return null;
	}
	private String makeDoseTitle(Ts ts) {
		// TODO Auto-generated method stub
		return null;
	}
	private void makeTabletGabe(Tree drugT) {
		// TODO Auto-generated method stub
		
	}
	private void addDayTitleRow2(Ts ts) 
	{
		Row r = createNextRowAndCells();
		Cell cell = r.getCell(0);
		cell.setCellValue(ts.getCday());
		cell = r.createCell(1);
		cell.setCellValue(t("ui_day"));
		cell = r.createCell(2)	;
		
	}
	

	
	
	private  Map<String, CellStyle> createStyles()
	{
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();

        CellStyle style;
        String fontNmae="Verdana";
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short)18);
        titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
        titleFont.setFontName(fontNmae);
		style = workbook.createCellStyle();
		style.setFont(titleFont);
		style.setBorderBottom(CellStyle.BORDER_MEDIUM);
		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		//    style.setFillBackgroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		styles.put("title", style);
		
		style = workbook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.LIME.index);
		//    style.setFillBackgroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		styles.put("body", style);
		
		style = workbook.createCellStyle();
		style.setBorderRight(CellStyle.BORDER_MEDIUM);
		style.setRightBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setBorderBottom(CellStyle.BORDER_MEDIUM);
		style.setBottomBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setBorderLeft(CellStyle.BORDER_MEDIUM);
		style.setLeftBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setBorderTop(CellStyle.BORDER_MEDIUM);
		style.setTopBorderColor(IndexedColors.GREY_40_PERCENT.getIndex());
		style.setFillForegroundColor(HSSFColor.LIME.index);
		//    style.setFillBackgroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		styles.put("patient_header", style);
		
		style = workbook.createCellStyle();
		styles.put("normal_text", style);
		
		style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName(fontNmae);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		styles.put("bold", style); 
		
		style = workbook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		style.setFont(font);		
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setBorderBottom(CellStyle.BORDER_THICK);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("th1", style);

		style = workbook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		style.setFont(font);		
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		styles.put("th2", style);
		
		style = workbook.createCellStyle();
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		style.setFont(font);		
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		style.setBorderBottom(CellStyle.BORDER_THICK);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		styles.put("th1_align_right", style);
		
		/////////////////////////////////////////////
		// Date styles
		/////////////////////////////////////////////
		style = workbook.createCellStyle();
		style.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy"));
		styles.put("date", style);
		
		style = workbook.createCellStyle();
		style.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yy"));
		styles.put("date_short", style);   
		
		style = workbook.createCellStyle();
		style.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM"));
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		style.setFont(font);		
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		styles.put("date_dd.MM_th2", style);
		
		style = workbook.createCellStyle();
		style.setDataFormat(createHelper.createDataFormat().getFormat("dd"));
		style.setFillForegroundColor(HSSFColor.BROWN.index);
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);        
		style.setFont(font);				
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		styles.put("date_dd_th2", style);

		style = workbook.createCellStyle();
		style.setDataFormat(workbook.createDataFormat().getBuiltinFormat("0%"));
		styles.put("percent", style);
		
		style = workbook.createCellStyle();
		style.setDataFormat(workbook.createDataFormat().getBuiltinFormat("0"));
		styles.put("integer", style);

		style = workbook.createCellStyle();
		style.setDataFormat((short)2);
		styles.put("float_2_digits_after_comma", style);
		
	    return styles;
	}

	  public void createNames()
	  {
	        Name   name;
	        String sName="'"+sheetName+"'!";
	        
	        name = workbook.createName();
	        name.setNameName("height");
	        name.setRefersToFormula(sName+"$D$6");

	        name = workbook.createName();
	        name.setNameName("weight");
	        name.setRefersToFormula(sName+"$F$6");
	        
	        name = workbook.createName();
	        name.setNameName("KOF");
	        name.setRefersToFormula(sName+"$H$6");
	    }
	
	private void addDayTitleRow(Ts ts) {
		Row row = planSheet.createRow(currentExcelRow);
		Cell cell = row.createCell(0);
		cell.setCellValue(ts.getCday());
		cell = row.createCell(1);
		cell.setCellValue(
				createHelper.createRichTextString("Tag"));
		currentExcelRow++;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	//
	//     SOKRATHES-POI API
	//
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	//shortening for the bundle.getString - gets the string from the message.properties file
	private String t(String s)
	{
//		log.debug(messageSource);
//		log.debug(Locale.getDefault());
		//TODO: in future, change Locale.getDefault() to some language i.e.  Local.ENGLISH accordingli to the user's setup
		return messageSource.getMessage(s, null, Locale.getDefault());
		
//		String val = bundle.getString(s); 
//		try
//		{
//			return new String(val.getBytes("ISO-8859-1"), "UTF-8");
//		}
//		catch(final UnsupportedEncodingException e) 
//		{  
//            throw new RuntimeException("Encoding not supported", e);  
//        }  
//		return bundle.getString(s); 
	}
	
	private void setCellAlign(int cellNr, short align)
	{
			HSSFCellUtil.setAlignment((HSSFCell) getCurrentRow().getCell(cellNr), workbook, align);
	}
	
	private void setCellStyle(int cellNr, String style)
	{
		Row r = getCurrentRow();
		if(r.getLastCellNum()<=cellNr)
			  r.createCell(cellNr);
		r.getCell(cellNr).setCellStyle(styles.get(style));
		//log.debug("rowNr= "+ r.getRowNum() + " cellNr= "+cellNr + " style= " + r.getCell(cellNr).getCellStyle().getDataFormatString());
	}

	
	/*
	 * third param is boolean - if true locks the cell
	 */
	private Cell setCellFormula(int cellNr, String value, Object... params)
	{
		int l = params.length;
	    Row r = getCurrentRow();
	    
	    //if we pass a row then use that row instead of the current
	    if(l>2 && params[1] !=null && Row.class.isInstance(params[1]))
		{
	    	r = (Row)  params[1];
//	    	log.debug("param[1] is given " + r);
		}
	    
		if(r.getLastCellNum()<=cellNr)
		{
			r.createCell(cellNr);	
//			log.debug(cellNr + " " + value);
		}
		
		Cell c = r.getCell(cellNr);
		c.setCellFormula(value);

		if(l>0 && params[0] !=null && Boolean.class.isInstance(params[0]))
		{
			Boolean	lockCell = (Boolean)  params[0];
//	    	log.debug(cellNr + "v=" + lockCell+" params0="+params[0]);
			c.getCellStyle().setLocked(lockCell);
		}
		
		return c;
	}
	
	
	//obsolete
//	private Row setPrevRowCellValue(int cellNr, Object... params) 
//	{
//		//clone params and add the previous row to the parameter list
//		Object[] olist = Arrays.copyOf(params, params.length+1);
//		olist[olist.length-1] = getPreviousRow();		
//		return setCellValue(cellNr, olist);
//	}

	
	/*lock
	 * cellNr = cell number (the cell will be created if not created yet)
	 * params => first param (params[0]) is either String - text to put into the cell
	 * or a Date, the second param (params[1]) is a String that specifies the style for the Date
	 */
	private Row  setCellValue( int cellNr, Object... params) 
	{
		int l = params.length;
//		String out="";
//		if(l>0)
//			out = params[0].toString();
//		if(l>1)
//			out += params[1].toString();
//		if(l>2)
//			out += params[2].toString();
//		log.debug("cellNr: "+cellNr + "  " + out);
		
//		log.debug(params);
//		log.debug(l);
	    
	    Row r = getCurrentRow();
	    
	    //if we pass a row then use that row instead of the current
	    if(l>2 && params[2] !=null && Row.class.isInstance(params[2]))
		{
	    	r = (Row)  params[2];
//	    	log.debug("param[2] is given " + r);
		}
	    
	    //if we write to a column bigger than maxColumn then create that cell first
	    if(r.getLastCellNum()<=cellNr)
		{
			r.createCell(cellNr);	
//			log.debug(cellNr);
		}
	    
	    Cell c = r.getCell(cellNr);
	    
	    if(l>0 && params[0] != null && String.class.isInstance(params[0]))
	    {
	    	String 	value = (String) params[0];
//	    	log.debug(cellNr + "v=" + value+" params0="+params[0]);
	    	c.setCellValue(value);
	    	c.setCellType(Cell.CELL_TYPE_STRING);
	    }
	    if(l>0 && params[0] !=null && Integer.class.isInstance(params[0]))
	    {
	    	Integer	value = (Integer)  params[0];
//	    	log.debug(cellNr + "v=" + value+" params0="+params[0]);
	    	c.setCellValue(value);
	    	c.setCellType(Cell.CELL_TYPE_NUMERIC);
	    }
	    if(l>0 && params[0] !=null && Date.class.isInstance(params[0]))
	    {
	    	Date	value = (Date)  params[0];
//	    	log.debug(cellNr + "v=" + value+" params0="+params[0]);
	    	c.setCellValue(value);
	    }
	    
	    if(l>1 && params[1] != null && String.class.isInstance(params[1]))
	    {
	    	String 	style = (String) params[1];
//	    	log.debug("row: "+r+" cellNr: " + cellNr + "s=" + style+" params0="+params[1]);
	    	c.setCellStyle(styles.get(style));
	    }
	
		return r;
	}
	
	
	private Row mergePrevRowCells(int firstCol, int lastCol, String str )
	{
		return mergeCells(getPreviousRow(), firstCol, lastCol, str);
	}

	private Row mergeCells(int firstCol, int lastCol)
	{
		return mergeCells(getCurrentRow(), firstCol, lastCol);
	}
	
	private Row mergeCells(int firstCol, int lastCol, String str )
	{
		return mergeCells(getCurrentRow(), firstCol, lastCol, str);
	}
	
	private Row mergeCells(Row r, int firstCol, int lastCol, String str )
	{
		mergeCells(r, firstCol, lastCol).getCell(firstCol).setCellValue(str);
		return r;
	}
	private Row mergeCells(Row r, int firstCol, int lastCol )
	{
		
		planSheet.addMergedRegion(new CellRangeAddress(r.getRowNum(), r.getRowNum(), firstCol, lastCol ));
		return r;
	}
	
	private int mergeCells(int firstRow, int lastRow, int firstCol, int lastCol )
	{
		//merge the cells
		return planSheet.addMergedRegion(new CellRangeAddress(
				firstRow, //first row (0-based)
				lastRow, //last row  (0-based)
				firstCol, //first column (0-based)
				lastCol  //last column  (0-based)
				));
	}
	
	
	private Row createNextRowAndCells(String style, int height)
	{
		Row r = createNextRowAndCells();
		if(height!=0)
			r.setHeightInPoints(height);
		
//		int end = r.getLastCellNum();
//		int start= r.getFirstCellNum();
		int end = maxColumns;
		int start= 0;
		for(int i=start; i<end; i++)
		{
			Cell c = r.getCell(i);
			c.setCellStyle(styles.get(style));
		}
		return r;
	}
	
	public Row getCurrentRow() {
		return currentRow;
	}
	
	public Row getPreviousRow()
	{
		return previousRow;
	}
	
	private Row createNextRowAndCells()
	{
		return createCell(createNextRow(), 0, maxColumns);
	}
	
	
	//creates a row and merges all cells of this row till maxColumn
	private Row createNextLine()
	{
		currentRow = createNextRow();
		mergeCells(currentRow.getRowNum(), currentRow.getRowNum(), 0, maxColumns);
		return currentRow;
	}
	
	private Row createNextRow()
	{
		previousRow = currentRow;
		currentRow = planSheet.createRow(++rowIndex);
		return currentRow;
	}
	
	private Row createCell(Row r, int start, int end)
	{
		for(int i=start; i<end; i++)
			r.createCell(i);
		return r;
	}
	
	public String trim(String str)
	{		
		return str==null ? "":str;
	}
	
	public Object trim2(Object str)
	{		
		return str==null ? "?":""+str;
	}
	
	public String trimBraces(String str)
	{
		return str==null ? "" : "("+str+")";
	}
	
	public String trimHTML(String s)
	{
		return s.replaceAll("\\<.*?>","").replaceAll("\\&.*?\\;", "");
	}
}
