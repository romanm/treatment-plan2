package com.qwit.service;

import java.sql.Timestamp;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qwit.crypt.EncryptionService;
import com.qwit.domain.Contactperson;
import com.qwit.util.RegisterUserForm;

@Service("mailService")
public class MailService  extends MtlDbService{
	@Autowired private MailSender mailSender;
	@Autowired private SimpleMailMessage 	templateMessage;
	@Autowired private EncryptionService	es;
	
	private Md5PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	static protected final Log log = LogFactory.getLog(MailService.class);

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}
	public void setTemplateMessage(SimpleMailMessage templateMessage) {
		this.templateMessage = templateMessage;
	}

	//send confirmation email for the newly created account
	public void sendRegConfirmEmail(RegisterUserForm ruf)
	{
		String receiver			= ruf.getEmail(); 
		String firstName		= ruf.getFirstName(); 
		String secondName		= ruf.getSecondName();
		String userName			= ruf.getUserName();

		String confirmKey = getConfirmKey(userName);
		
		SimpleMailMessage msg, msg2;

		try
		{
			msg = new SimpleMailMessage(this.templateMessage);
			msg.setTo(receiver);
			msg.setSubject("Please confirm your new account");
			HttpServletRequest rq = owsSession.getRequest();
			int serverPort = rq.getServerPort();
			log.debug(serverPort);
			String servletPath = rq.getServletPath();
			log.debug(servletPath);
			String contextPath = rq.getContextPath();
			log.debug(contextPath);
			msg.setText(
					"Dear " + firstName +"\n\n"+
					"you registered a new account at qwit.com" +"\n\n"+
					"username:"+userName+"\n"+
					"password:***" +"\n\n" +
					", contextPath=" +contextPath+
					", servletPath="+servletPath+
					"please follow the confirmation link below to activate your account" +"\n"+
					"http://localhost:" +
					serverPort +
					"/" +
					contextPath +
					"/registerflow?a=confirmAccount&c="+confirmKey+"\n\n"+
					"Thank you for registering an acount.");		
//			log.debug("-----------------sendRegConfirmEmail:" + msg.toString());
			log.debug("send begin");
			this.mailSender.send(msg);
			log.debug("send end");
			
			//-------------send another email to roman--------------
			msg2 = new SimpleMailMessage(this.templateMessage);
			msg2.setTo(msg2.getFrom());
			msg2.setSubject("New Account Created");
			msg2.setText(
					"First name:  " + firstName +"\n"+
					"Second name: " + secondName + "\n" +
					"User name:   " + userName+"\n"+
					"Email:       " + receiver+"\n");
			this.mailSender.send(msg2);
			
		}catch(MailException ex) {
			// simply log it and go on...
			System.err.println(ex.getMessage());
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}

	@Transactional
	public String getConfirmKey(String username)
	{
		String sql ="SELECT confirmkey from confirmaccount where username=?";
		String r = (String) em.createNativeQuery(sql)
								.setParameter(1, username)
								.getSingleResult();
		return r;
	}
	
		
	@Transactional
	public String confirmAccount(String confirmKey)
	{
		//the confirmKey param cant have the length greater than 50
		if(confirmKey.length() >50)
			return "invalid";

		//select the confirmKey from db	
		String sql ="SELECT confirmkey, createdon, remindpassword from confirmaccount where confirmkey=?";
//		List<Object> cSet =  em.createNativeQuery(sql)
//								.setParameter(1, confirmKey)
//								.getResultList();
//		String c 		= (String) cSet.get(0);
//		String cdate 	= (String) cSet.get(1);
//		Boolean rpass 	= (Boolean) cSet.get(2);

		
//		Map<String,Object> rmap = simpleJdbc.queryForMap(sql, new Object[]{confirmKey});
		Map<String,Object> rmap = getConfirmAccountEntry(confirmKey);
		
		if(rmap == null)
			return "invalidConfirmKey";
		
		String c 		= (String) rmap.get("confirmkey");
		Timestamp cdate 	= (Timestamp) rmap.get("createdon");
		Boolean rpass	= (Boolean) rmap.get("remindpassword");
		
		log.debug("confirmAccount-------rpass---"+rpass);
		
		if(rpass)
			return "resetPassword";
		
		
		//if that key is present in db then return success else denied
		if(c != null && c != "" && c.equals(confirmKey))
		{
//			//seond check!
//			//check if the time (1h)for the confirmation is elapsed
//			
//			    Date now = new Date();			
//				//difference in miliseconds;
//			  	long elapsed_time = now.getTime() - cdate.getTime();			
//			    if(elapsed_time > 3600000)
//				{
//					//delete the confirmaccount entry
//		
//					return "invalid";
//				}
//			
					
			//select the username and enable user
			String userName = (String) em.createNativeQuery("SELECT username FROM confirmaccount WHERE confirmkey=?")
							  .setParameter(1, c)
							  .getSingleResult();
			
			em.createNativeQuery("UPDATE users SET  enabled='1' WHERE username=?")
			.setParameter(1, userName)
			.executeUpdate();
			return "valid";
		}
		return "invalid";
	}
	
	
	public void sendPasswordReminder(RegisterUserForm ruf)
	{
		//select the name of the person from contact table
		String firstName		= ""; 
		String secondName		= "";
		
		String receiver			= ruf.getEmail(); 
		String userName			= ruf.getUserName();			
		String confirmKey 		= getConfirmKey(userName);
		
		SimpleMailMessage msg, msg2;
		
		try
		{
			msg = new SimpleMailMessage(this.templateMessage);
			msg.setTo(receiver);
			msg.setSubject("Password Reset");
			msg.setText(
					"Dear " + firstName +"\n\n"+
					"you have requested a reset of your password" +"\n\n"+
					"please follow the link below to change your password" +"\n"+
					"http://localhost:8088/qwit7/registerflow?a=confirmAccount&c="+confirmKey+"\n\n"+
					"");		
//			log.debug("-----------------sendPasswordReminder:" + msg.toString());
			this.mailSender.send(msg);
			
			//-------------send another email to roman--------------
			msg2 = new SimpleMailMessage(this.templateMessage);
			msg2.setTo(msg2.getFrom());			
			msg2.setSubject("User Password Reset");
			msg2.setText(
					"First name:  " + firstName +"\n"+
					"Second name: " + secondName + "\n" +
					"User name:   " + userName+"\n"+					
					"Email:       " + receiver+"\n");
			this.mailSender.send(msg2);
			
		}
		catch(MailException ex) {
			// simply log it and go on...
			System.err.println(ex.getMessage());
		}
		catch(Exception e)
		{
			e.printStackTrace(System.err);
		}
		
	}
	
}
