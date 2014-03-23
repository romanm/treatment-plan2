package com.qwit.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qwit.domain.Patient;
import com.qwit.domain.Tree;
import com.qwit.domain.Url;
import com.qwit.model.ConceptMtl;
import com.qwit.model.DrugMtl;
import com.qwit.model.InstituteMtl;
import com.qwit.model.MtlXml;
import com.qwit.model.OwsSession;
import com.qwit.model.PatientMtl;
import com.qwit.model.SchemaMtl;
import com.qwit.model.UserMtl;
import com.qwit.service.DocumentService;
import com.qwit.service.ExcelService;

@Controller
public class DocumentController {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired	private DocumentService			documentService;
	@Autowired	private ExcelService			excelService;
	@Autowired	protected OwsSession owsSession;
	
//	@Autowired
//    private ApplicationContext context;
//	
	@Autowired 
	private MessageSource messageSource;

	
	@Autowired ServletContext servletContext=null;
	
	
	@RequestMapping(value = "/proxytest", method = RequestMethod.GET)
	public void getPdf(HttpServletResponse response, HttpServletRequest request)throws ClassNotFoundException
	{
		log.debug("set proxy via system.property");
		System.setProperty("http.proxyHost", "10.10.0.120");
		System.setProperty("http.proxyPort", "8080");
		log.debug("system.property: http.proxyHost = "+System.getProperty("http.proxyHost"));
		log.debug("system.property: http.proxyPort = "+System.getProperty("http.proxyPort"));
		// Next connection will be through proxy.
		URL url;
		try 
		{
			url = new URL("http://java.sun.com/");
//			InputStream ins = url.openStream();
//			log.debug("stream:"+ins.toString());
			
			String inputLine;
			String path = servletContext.getRealPath("/html");
			String fileName  = "/proxytest.html";
			File tempFile = new File(path+fileName);
			log.debug(path+fileName);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			log.debug("bufferedreader-stream:"+in.toString());
			FileOutputStream outStream = new FileOutputStream (tempFile);
			PrintStream ps = new PrintStream(outStream);
			while ((inputLine = in.readLine()) != null)
				ps.println(inputLine);
			in.close();
			outStream.close();
		} 
		catch (MalformedURLException e1) {
			log.debug("url:");
			e1.printStackTrace();
		} catch (IOException e1) {
			log.debug("stream:");
			e1.printStackTrace();
		}
	}
	
	
	@RequestMapping(value = "/tumorboard", method = RequestMethod.GET)
	public PatientMtl tumorboard(@RequestParam("id") Integer id) {
		return documentService.makeTumorboard(id);
	}
//	@RequestMapping(value = "/patient", method = RequestMethod.GET)
//	public PatientMtl patient(@RequestParam("id") Integer id) {return documentService.makePatientMtl(id);}
	@RequestMapping(value = "/patient_{givenname}_{familyname}", method = RequestMethod.GET)
	public String patientseek(@PathVariable String givenname,@PathVariable String familyname, Model model) {
		log.debug(givenname+"_"+familyname);
		Patient patientO = documentService.seekPatient(givenname,familyname);
		if(null!=patientO)
			return "redirect:patient="+patientO.getId();
		return "redirect:/";
	}
	@RequestMapping(value = "/framesetlayout={id}", method = RequestMethod.GET)
	public void framesetlayout(@PathVariable Integer id,@RequestParam("idurl") Integer idurl, Model model) {
		model.addAttribute("id", id);
		log.debug("idurl="+idurl);
		Url urlO = documentService.makeUrlO(idurl);
		log.debug("urlO="+urlO);
		model.addAttribute("urlO",urlO);
	}
	@RequestMapping(value = "/fs2patient={id}", method = RequestMethod.GET)
	public void fs2patient(@PathVariable Integer id,Model model) {
		Patient patientO = documentService.makePatientO(id);
		log.debug("patientO="+patientO);
		model.addAttribute("patientO",patientO);
	}
	@RequestMapping(value = "/patient={id}", method = RequestMethod.GET)
	public PatientMtl patientrest(@PathVariable Integer id, Model model) {
		return documentService.makePatientMtl(id);
	}
	
	@RequestMapping(value = "/schema={id}", method = RequestMethod.GET)
	public String schemaViewRest(@PathVariable Integer id, Model model) {
		return "redirect:schema-" +owsSession.getSchemaPart() +"-"+id;
	}
	
	
//	@RequestMapping(value = "/schema/printAll", method = RequestMethod.GET)
//	public SchemaMtl printAllx(@RequestParam("id") Integer id) {return printx(id);}
//	@RequestMapping(value = "/schema/print", method = RequestMethod.GET)
//	public SchemaMtl printx(@RequestParam("id") Integer id) {
////		SchemaMtl schemaMtl = schema(id);
//		SchemaMtl schemaMtl = schemarest(id,null);
//		schemaMtl.setPrintSite(true);
//		return schemaMtl;
//	}
	
	
	/**
	 * This function makes a print version out of a document you pass,   
	 * @param what (is one of: schema, concept, patient, etc... )
	 * @param view (therapieplan, chronologisch, fuchs, intensiv, etc...)
	 * @param id   (the id of the document, i.e. schemaId, conceptId, patientId - represented by docId)
	 * @return
	 */
	@RequestMapping(value = "/print-{what}-{view}-{id}", method = RequestMethod.GET)
	public ModelAndView printSchema(@PathVariable String what, @PathVariable String view, @PathVariable Integer id) 
	{
		ModelAndView modelAndView = new ModelAndView();
		
		if("schema".equals(what))
		{
			owsSession.setSchemaPart(view);
			SchemaMtl schemaMtl = schemarest(id,null);
			schemaMtl.setPrintSite(true);
			modelAndView.setViewName("print-"+what+"-*-*");
			modelAndView.addObject("schemaMtl", schemaMtl);
		}
		else if("patient".equals(what))
		{
			owsSession.setPatientPart(view);
			PatientMtl pateientMtl = documentService.makePatientMtl(id);
			modelAndView.setViewName("print-"+what+"-*-*");
			modelAndView.addObject("patientMtl",  pateientMtl);
		}
		else if("concept".equals(what))
		{
			modelAndView.setViewName("print-"+what+"-*-*");
			modelAndView.addObject("conceptMtl",  documentService.makeConceptMtl(id));
		}
//		else if("drug".equals(what))
//		{
//			owsSession.setDrugPart(view);
//			modelAndView.setViewName("print-"+what+"-*-*");
//			modelAndView.addObject("drugMtl",  documentService.makeDrugMtl(id));
//		}
//		else if("user".equals(what))
//		{
//			modelAndView.setViewName("print-"+what+"-*-*");
//			modelAndView.addObject("userMtl",  documentService.makeUserMtl(id));
//		}
//		else if("institute".equals(what))
//		{
//			modelAndView.setViewName("print-"+what+"-*-*");
//			modelAndView.addObject("instituteMtl", documentService.makeInstituteMtl(id));
//		}
		else
		{
			modelAndView.setViewName("print-error");
		}
		
		modelAndView.addObject("isPrintSite", true);
		modelAndView.addObject("viewModus", what);
		return modelAndView;
	}
	
	@RequestMapping(value = "/pdf-{what}-{view}-{id}", method = RequestMethod.GET)
	public void getPdf(@PathVariable String what,@PathVariable String view,@PathVariable Integer id, HttpServletResponse response, HttpServletRequest request)throws ClassNotFoundException
	{
		excelService.downloadDocumentAsPdf(messageSource,what, view, id, response, request);
	}

//	@RequestMapping(value = "/pdf-{id}", method = RequestMethod.GET)
//	public void getPdf(@PathVariable Integer id, HttpServletResponse response, HttpServletRequest request)throws ClassNotFoundException
//	{
////		excelService.downloadSchemaPdf_JODConverter_Version(messageSource, id, response, request);
//		excelService.downloadSchemaPdf(messageSource, id, response, request);
//	}

	@RequestMapping(value = "/xls-{id}", method = RequestMethod.GET)
	public void getXls(@PathVariable Integer id, HttpServletResponse response)throws ClassNotFoundException
	{
		SchemaMtl docMtl = documentService.makeSchemaMtl(id);
		excelService.downloadSchemaXls(messageSource, response, docMtl);
	}

	@RequestMapping(value = "/patient-{patientPart}-{id}", method = RequestMethod.GET)
	public PatientMtl schemaViewRest(@PathVariable String patientPart,@PathVariable Integer id) {
		log.debug(patientPart);
		owsSession.setPatientPart(patientPart);
		log.debug(owsSession.getPatientPart());
		return documentService.makePatientMtl(id);
	}
	@RequestMapping(value = "/schema-plan-{id}-drugNotice={drugNotice}")
	public String schemaPart_drugNotice(@PathVariable Integer id, @PathVariable String drugNotice) {
		boolean isDrugNotice = "on".equals(drugNotice);
		if(null!=drugNotice&&drugNotice.length()>0&&isDrugNotice!=owsSession.isDrugNotice()){
			owsSession.setDrugNotice(isDrugNotice);
		}
		return "redirect:schema-plan-"+id;
	}
	@RequestMapping(value = "/schema-plan-{id}-withInfo={withInfo}")
	public String schemaPart_withInfo(@PathVariable Integer id, @PathVariable String withInfo) {
		log.debug(withInfo);
		boolean isWithInfo = "on".equals(withInfo);
		log.debug(isWithInfo);
		if(null!=withInfo&&withInfo.length()>0&&isWithInfo!=owsSession.isWithInfo())
			owsSession.setWithInfo(isWithInfo);
		return "redirect:schema-plan-"+id;
	}
	@RequestMapping(value = "/schema-{schemaPart}-{id}", method = RequestMethod.GET)
	public SchemaMtl schemaViewRest(@PathVariable String schemaPart,@PathVariable Integer id, Model model) {
		owsSession.setSchemaPart(schemaPart);
		return documentService.makeSchemaMtl(id);
	}
	
	@RequestMapping("/setStation")
	public String setStation(@RequestParam("id") Integer idDoc, @RequestParam("idStation") Integer idStation){
//		owsSession.setStation(idStation);
		documentService.setStation(idStation);
		String docType = documentService.getDocUrlType(idDoc);
		String url = docType +"-"+owsSession.getSchemaPart()+"-"+idDoc;
		if("patient".equals(docType))
			return "redirect:"+docType +"="+idDoc;
		else
			return "redirect:" + docType +"-"+owsSession.getSchemaPart()+"-"+idDoc;
	}
//	@RequestMapping(value = "/schema={id}", method = RequestMethod.GET)
	public SchemaMtl schemarest(@PathVariable Integer id, Model model) {
		return documentService.makeSchemaMtl(id);
	}
//	@RequestMapping(value = "/schema", method = RequestMethod.GET)
//	public SchemaMtl schema(@RequestParam("id") Integer id) {return documentService.makeSchemaMtl(id);}
	

	@RequestMapping(value = "/concept={id}", method = RequestMethod.GET)
	public ConceptMtl conceptrest(@PathVariable Integer id, Model model) {return documentService.makeConceptMtl(id);}
//	@RequestMapping(value = "/concept", method = RequestMethod.GET)
//	public ConceptMtl concept(@RequestParam("id") Integer id) {return documentService.makeConceptMtl(id);}
	@RequestMapping(value = "/user", method = RequestMethod.GET)
	public UserMtl user() {return documentService.makeUserMtl();}
	@RequestMapping(value = "/user={id}", method = RequestMethod.GET)
		
	public UserMtl user (@PathVariable Integer id, Model model) {
		return documentService.makeUserMtl(id);
	}
	@RequestMapping("/removeAdminNode")
	public String removeAdminNode(
			@RequestParam("iddoc") Integer idDoc
			,@RequestParam("idfolder") Integer idfolder){
		documentService.setNodeRoleField(idfolder, "admin", false);
		return "redirect:user="+idDoc;
	}
	@RequestMapping("/setRWNodeRole")
	public String setRWNodeRole(@RequestParam("iddoc") Integer idDoc) {
		documentService.setRWNodeRole(idDoc);
		return "redirect:user="+idDoc;
	}
	@RequestMapping("/setAdminStation")
	public String setAdminStation(
			@RequestParam("iddoc") Integer idDoc
			){
		documentService.setAdminStation(idDoc);
		return "redirect:user="+idDoc;
	}
	@RequestMapping("/changeId")
	public String changeId(
			@RequestParam("iddoc") Integer idDoc
			,@RequestParam("changeid") Integer changeid
			,@RequestParam("checkid") Integer checkid
			) {
		
		return "redirect:user="+idDoc;
	}
	@RequestMapping("/changeNextId")
	public String changeNextId(
			@RequestParam("iddoc") Integer idDoc
			,@RequestParam("checkid") Integer checkid
			) {
		log.debug(checkid);
		int updateNr = documentService.changeId(checkid);
		log.debug(updateNr);
		if(0==updateNr)
		{
			if("patient".equals(owsSession.getTargetT().getTabName()))
			{
				updateNr = documentService.changePatientId(checkid);
			}
		}
		return "redirect:user="+idDoc;
	}

	@RequestMapping("/checkid")
	public String checkid(
			@RequestParam("iddoc") Integer idDoc
			,@RequestParam("checkid") Integer checkid) {
		log.debug(checkid);
		Tree treeById = documentService.checkTreeById(checkid);
		owsSession.setTargetT(treeById);
		owsSession.setTargetIdClass(checkid);
		log.debug(treeById);
		return "redirect:user="+idDoc;
	}

	@RequestMapping("/setAdminNode")
	public String setAdminNode(
			@RequestParam("iddoc") Integer idDoc
			,@RequestParam("idfolder") Integer idfolder) {
		log.debug(idDoc);
		log.debug(idfolder);
		documentService.setNodeRoleField(idfolder, "admin", true);
		return "redirect:user="+idDoc;
	}

	@RequestMapping(value = "/institute={id}", method = RequestMethod.GET)
	public InstituteMtl instituterest(@PathVariable Integer id, Model model) {
		InstituteMtl makeInstituteMtl = documentService.makeInstituteMtl(id);
		return makeInstituteMtl;
	}
	@RequestMapping(value="/setBsaFormul", method=RequestMethod.POST)
	public String setBsaFormul(
			@RequestParam("iddoc") Integer idDoc
			){
		documentService.setBsaFormul(idDoc);
		return "redirect:institute="+idDoc;
	}
	@RequestMapping(value = "/institute", method = RequestMethod.GET)
	public InstituteMtl institute() {
//		public InstituteMtl institute(@RequestParam("id") Integer id) {
		InstituteMtl makeInstituteMtl = documentService.makeInstituteMtl();
		return makeInstituteMtl;
	}
//	public UserMtl user2() {
//		ows-SessionContainer.getUserId(simpleJdbc);
//		String type = ows-SessionContainer.getRequest().getParameter("type");
//		log.debug(type);
//		if(null!=type)
//			ows-SessionContainer.getOwsSession().setUserNoticeType(type);
//		return documentService.makeUserMtl();
//	}
//	@Autowired	protected SimpleJdbcTemplate simpleJdbc;
//	@RequestMapping(value = "/xml", method = RequestMethod.GET)
//	public MtlXml xml(@RequestParam("id") Integer id) {return documentService.makeDoc2xml(id);}
	@RequestMapping(value = "/xml-{id}", method = RequestMethod.GET)
	public MtlXml xmlrest2(@PathVariable Integer id, Model model) {return documentService.makeDoc2xml(id);}
	@RequestMapping(value = "/xml={id}", method = RequestMethod.GET)
	public MtlXml xmlrest1(@PathVariable Integer id, Model model) {return documentService.makeDoc2xml(id);}
//	@RequestMapping(value = "/drug")
	@RequestMapping(value = "/drug", method = RequestMethod.GET)
	public DrugMtl drug(@RequestParam("id") Integer id) {
		log.info("load drug "+id);
		return documentService.makeDrugMtl(id);
	}
	@RequestMapping(value = "/drug={id}", method = RequestMethod.GET)
	public DrugMtl drugrest(@PathVariable Integer id, Model model) {
		log.info("load rest drug "+id);
		return documentService.makeDrugMtl(id);
	}

}
