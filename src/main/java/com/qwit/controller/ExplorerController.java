package com.qwit.controller;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.core.support.RabbitGatewaySupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qwit.amqp.MtlDocRequest;
import com.qwit.config.client.AmqpConfigClient;
import com.qwit.model.ExplorerMtl;
import com.qwit.model.OwsSession;
import com.qwit.service.ExplorerService;
import com.qwit.service.TumorboardDate;
import com.qwit.util.FormUtil;
import com.qwit.util.RegisterUserForm;

@Controller
public class ExplorerController {
	protected final Log log = LogFactory.getLog(getClass());
	@Autowired private ExplorerService explorerService;
	
	@Autowired private MailSender mailSender;
	@Autowired private SimpleMailMessage 	templateMessage;
	
	//test mail
	@RequestMapping(value = "/mailtest", method = RequestMethod.GET)
	public void sendRegConfirmEmail(RegisterUserForm ruf)
	{
		String receiver			= "sk07sk@googlemail.com"; 
		String firstName		= ruf.getFirstName(); 
		String secondName		= ruf.getSecondName();
		String userName			= ruf.getUserName();

		String confirmKey = "111111111-999999999";
		
		SimpleMailMessage msg;

		try
		{
			msg = new SimpleMailMessage();
			
			msg.setFrom("admin@imsel.imise.uni-leipzig.de");
			msg.setTo(receiver);
			msg.setSubject("Please confirm your new account");
			msg.setText(
			"Dear " + firstName +"\n\n"+
			"you registered a new account at qwit.com" +"\n\n"+
			"username:"+userName+"\n"+
			"password:***" +"\n\n" +
			"please follow the confirmation link below to activate your account" +"\n"+
			"imsel.imise.uni-leipzig.de/registerflow?a=confirmAccount&c="+confirmKey+"\n\n"+
			"Thank you for registering an acount.");		
//			log.debug("-----------------sendRegConfirmEmail:" + msg.toString());
			log.debug("send begin");
			this.mailSender.send(msg);
			log.debug("send end");
					
		}catch(MailException ex) {
			// simply log it and go on...
			System.err.println(ex.getMessage());
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}

	

	//test exceoption resolver
	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public ExplorerMtl explorer() throws Exception
	{
		throw new QException("this is a QWIT-7 test exception");
	}
	
	@RequestMapping(value = "/video-{name}", method = RequestMethod.GET)
	public String video(@PathVariable String name, ModelAndView model) {
		return "redirect:explorer?id=&search=";
	}
	@RequestMapping(value = "/explorerpost={id}", method = RequestMethod.POST)
	public ExplorerMtl schemaseek(@PathVariable Integer id, ModelAndView model) {
		String search = owsSession.getRequest().getParameter("search");
		log.debug("search="+search);
		return makeExplorerMtl(id,model);
	}
	@RequestMapping(value = "/explorer={id}", method = RequestMethod.GET)
	public ExplorerMtl schemarest(@PathVariable Integer id, ModelAndView model) {
		return makeExplorerMtl(id,model);
	}
//	, @RequestParam("search") String search 
//	@RequestMapping(value = "/explorer", method = RequestMethod.GET)
	@RequestMapping(value = "/explorer")
	public ExplorerMtl explorer(@RequestParam("id") Integer idFolder, ModelAndView model){
		return makeExplorerMtl(idFolder,model);
	}

	private ExplorerMtl makeExplorerMtl(Integer idFolder, ModelAndView model) {
		String characterEncoding = owsSession.getRequest().getCharacterEncoding();
		log.debug("characterEncoding = "+characterEncoding);
		setIdFolder(idFolder);
		log.debug("----------- idFolder="+idFolder);
		setSessionModus();
		setSessionSubFolder();
		log.debug("----------- idFolder="+idFolder);
		String search = owsSession.getRequest().getParameter("search");
		log.debug("search = "+search);
		return explorerService.makeExplorerMtl(idFolder, search);
	}

	/**
	 * Read from DB and make JSON object of user folders.
	 * @return FormUtil Object with storeT Daten in json format for diji.Tree widget.
	 */
	@RequestMapping(value = "/explorerDataStore", method = RequestMethod.GET)
	public FormUtil explorerDataStore(){
		FormUtil formUtil = new FormUtil();
		log.debug("------");
		String t = explorerService.getCompleteFolderTreeJSON();
		log.debug("------");
		formUtil.setFolderTreeJSON(t);
		return formUtil;
	}
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public FormUtil home(){
		log.debug("---home---");
		explorerService.setSessionStationM();
		log.debug("---home---"+owsSession.getStationM());
		FormUtil formUtil = new FormUtil();
		return formUtil;
	}
	@RequestMapping(value = "/tumorboardhome", method = RequestMethod.GET)
	public FormUtil tbh(){
		Calendar instance = tumorboardDate.getInstance();
		owsSession.setTumorboardDate(instance);
		FormUtil formUtil = new FormUtil();
		return formUtil;
	}
	@Autowired	@Qualifier("tumorboardDate")	TumorboardDate	tumorboardDate;

	//////////////////////////////////////////////////////////////////////////
	//
	//	Session Section
	//
	//////////////////////////////////////////////////////////////////////////
	@Autowired	protected OwsSession owsSession;
	
	private void setSessionModus() {
		String modus = owsSession.getRequest().getParameter("modus");
		if("definition".equals(modus)
		||"patient".equals(modus)
		)
			owsSession.setModus(modus);
		
	}
	private void setSessionSubFolder() {
		String subDir = owsSession.getRequest().getParameter("subdir");
		if("on".equals(subDir))			owsSession.setSubDir(true);
		else if("off".equals(subDir))	owsSession.setSubDir(false);
	}
	
	private void setIdFolder(Integer idFolder)
	{
		owsSession.setIdFolder(idFolder);
	}
	
	private Integer getIdFolder()
	{
		return owsSession.getIdFolder();
	}
	
	
	//////////////////////////////////////////////////////////////////////////
	//
	//	ExceptionHandler Section
	//
	//////////////////////////////////////////////////////////////////////////
//	@ExceptionHandler(QException.class)
//	public ModelAndView handleIOException(QException ex, HttpServletRequest request) 
//	{
//		ModelAndView m = new ModelAndView("explorer");
//		
//		Integer idFolder = 9938;
//		Integer id = getIdFolder();
//		setIdFolder(null);
//		
//		if(id != null)
//			idFolder = id;
//		
//		log.debug("------id--- " + id);
//		ExplorerMtl e = explorer(idFolder);
//		m.addObject("explorerMtl", e);
//		log.debug("------handle exception ----------");
//		return m;
//	}

	@ExceptionHandler(QException.class)
	public String handleException(QException ex, HttpServletRequest request) {
		
		Integer idFolder = 9932;
		Integer id = getIdFolder();
		
		if(id != null)
			idFolder = id;
		return "redirect:explorer?id="+idFolder+"&search=";
	}

	
	
	
	//////////////////////////////////////////////////////////////////////////
	//
	//	Test Section
	//
	//////////////////////////////////////////////////////////////////////////
	/*
	@Autowired
	public ExplorerController(ExplorerService explorerService) {
		this.explorerService = explorerService;
	}
	 * */
	
//	@Autowired @Qualifier("mtlDServiceGateway") RabbitGatewaySupport gateway;
	@RequestMapping(value = "/startclient", method = RequestMethod.GET)
	public void amqpClientSend()	{
		//old test
		ApplicationContext contextClient = null ;
		if(null==contextClient)
			contextClient = new AnnotationConfigApplicationContext(AmqpConfigClient.class);
		
//		OisDocServiceGateway docGw = context.getBean(OisDocServiceGateway.class);
		RabbitGatewaySupport docGw = contextClient.getBean(RabbitGatewaySupport.class);
		log.debug(docGw);
		MtlDocRequest docRq = new MtlDocRequest();
		docRq.setIdDoc(123);
		log.debug(docRq);
		log.debug("--------------");
//		docGw.send(docRq);
		Object convertSendAndReceive = docGw.getRabbitTemplate().convertSendAndReceive(docRq);
		log.debug("--------------"+convertSendAndReceive);
//		Object receiveAndConvert = docGw.getRabbitTemplate().receiveAndConvert();
//		RabbitTemplate rtp = context.getBean(RabbitTemplate.class);
//		MtlDocResponce docRs = (MtlDocResponce) rtp.convertSendAndReceive(docRq);
//		log.debug(docRs);
//		docGw.send(docRq);
		log.debug("Sent mtlDocRequest");
		
		//test 2
//		MtlDocRequest docRq = new MtlDocRequest();
		docRq.setIdDoc(123);
		log.debug(docRq);
		
		// TODO it is good import 
//		Object cSAR = gateway.getRabbitTemplate().convertSendAndReceive(docRq);
//		log.debug("--------------"+cSAR);
		
	}
	
}
