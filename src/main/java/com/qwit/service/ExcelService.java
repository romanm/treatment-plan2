package com.qwit.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.qwit.model.ExcelSchemaMtl;
import com.qwit.model.SchemaMtl;

@Service("excelService")
public class ExcelService 
{
	@Autowired ServletContext servletContext=null;
	

	protected final Log log = LogFactory.getLog(getClass());
//	public String makeSchema(SchemaMtl docMtl){
//		ExcelSchemaMtl excelSchemaMtl = new ExcelSchemaMtl(docMtl);
//		excelSchemaMtl.createPlanSheet();
//		String fileName = "schema-" +docMtl.getDocT().getId() +".xls";
//		saveFile(excelSchemaMtl.getWorkbook(), fileName);
//		return fileName;
//	}
//	
//	private void saveFile(Workbook wb, String fileName) {
////		String path="file:///home/roman/Documents/workspace-sts-2.7.1.RELEASE/qwit8/target/qwit8/xls/";
////		String path="/home/roman/Documents/workspace-sts-2.7.1.RELEASE/qwit8/target/qwit8/xls/";
//		String path="/home/klebeck/Documents/workspace-sts-2.7.2.RELEASE/qwit8/target/qwit8/xls/";
//		FileOutputStream fileOut=null;
//		try {
////			fileOut = new FileOutputStream(fileName);
//			fileOut = new FileOutputStream(path+fileName);
//			wb.write(fileOut);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}finally{
//			if(null!=fileOut){
//				try {
//					fileOut.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//	}

	public void downloadSchemaXls(MessageSource ms,  HttpServletResponse response, SchemaMtl docMtl) {
		ExcelSchemaMtl excelSchemaMtl = new ExcelSchemaMtl(ms, docMtl);
		excelSchemaMtl.createPlanSheet();
		String fileName = "schema-" +docMtl.getDocT().getId() +".xls";
		
		// 6. Set the response properties
		response.setHeader("Content-Disposition", "inline; filename=" + fileName);
		// Make sure to set the correct content type
		response.setContentType("application/vnd.ms-excel");
		 
		//7. Write to the output stream
		log.debug("Writing report to the stream");
		try 
		{
			// Retrieve the output stream
			ServletOutputStream outputStream = response.getOutputStream();
			// Write to the output stream
			excelSchemaMtl.getWorkbook().write(outputStream);
			// Flush the stream
			outputStream.flush();
		} 
		catch (Exception e) 
		{
			log.error("Unable to write report to the output stream");
		}
	}

//	public void downloadSchemaPdf_old(MessageSource messageSource,	Integer docId, HttpServletResponse response, HttpServletRequest request) 
//	{
//			
//		log.debug(request.getRequestURI());
//		log.debug(request.getContextPath());
//		log.debug(request.getLocalName());
//		log.debug(request.getPathInfo());
//		log.debug(request.getPathTranslated());
//		log.debug(request.getRequestURL());
//		String uriBase = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
////		String uriBase = request.getContextPath();
//		String uri = "/schema-plan-"+docId;
//		log.debug(uriBase+uri);
//		
//		try 
//		{
//			//write html file from url
//			URL url = new URL(uriBase+uri);	
//			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//			String inputLine;
//			String path="/home/klebeck/Documents/workspace-sts-2.8.1.RELEASE/qwit8/target/qwit8/html/";
//			String fileName = "schema-" +docId +".html";
//			File file = new File(path+fileName);
//			
//			FileOutputStream f	ServletOutputStream outputStream = response.getOutputStream();out = new FileOutputStream (file);
//			PrintStream ps = new PrintStream(fout);
//			
//			
//			while ((inputLine = in.readLine()) != null)
//				ps.println(inputLine);
//		  	
//			in.close();
//			
//			
//			///////////////////////////////////////////////////////////////////////////////
//			//convert html 2 pdf
//			///////////////////////////////////////////////////////////////////////////////
//			
//			String fileName2 = fileName + ".pdf";
////			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
//			response.setHeader("Content-Disposition", "inline; filename=" + fileName2);
//			response.setContentType("application/pdf");
//			
//			//------------------------
//			InputStream inputStream = url.openStream();
//			
//			//soffice -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard
//			// connect to an OpenOffice.org instance running on port 8100
//			SocketOpenOfficeConnection connection = new SocketOpenOfficeConnection(8100);
//			connection.connect();
//
//			DocumentFormat inputDF = new DocumentFormat("HTML", DocumentFamily.TEXT, "text/html", "");
//			inputDF.setExportFilter(DocumentFamily.TEXT, "HTML 	(StarWriter)");
//			
//			DocumentFormat outputDF = new DocumentFormat("Portable	Document Format", DocumentFamily.TEXT, "application/pdf", "pdf");
//			outputDF.setExportFilter(DocumentFamily.TEXT, "writer_pdf_Export");
//			
//			DocumentConverter dc = new OpenOfficeDocumentConverter(connection);
//			
//			log.debug(inputDF);
//			log.debug(dc);
//			
//			ServletOutputStream outputStream = response.getOutputStream();
////			dc.convert(inputStream, inputDF, outputStream, outputDF);
//			
//			//////////////////////////////////////////////////////////////////////////////////////
//			//test
//			//////////////////////////////////////////////////////////////////////////////////////
//			File  inputF  = new File("/home/klebeck/Documents/workspace-sts-2.8.1.RELEASE/qwit8/target/qwit8/html/"+fileName);
//			File  outputF = new File("/home/klebeck/Documents/workspace-sts-2.8.1.RELEASE/qwit8/target/qwit8/html/test.pdf");
////			File outputF = File.createTempFile("temporary", ".pdf");
//			
//			dc.convert(inputF, outputF);
//			outputF.deleteOnExit();
//			 
//			FileInputStream input = new FileInputStream(outputF);
//			BufferedInputStream buf = new BufferedInputStream(input);
//			int readBytes = 0;
//			while ((readBytes = buf.read()) != -1)
//				outputStream.write(readBytes);
//
//		      
//			// close the connection
//			connection.disconnect();
//			
//			outputStream.flush();
//			
//		}
//		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	
	//working
	public void downloadSchemaPdf_JODConverter_Version(MessageSource messageSource,	Integer docId, HttpServletResponse response, HttpServletRequest request) 
	{
		String uriBase = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		String uri = "/schema/print?id="+docId;
		String page = uriBase + uri;
		
		try 
		{
			///////////////////////////////////////////////////////////////////////////////
			// write html file from url to a temp file
			///////////////////////////////////////////////////////////////////////////////
			String inputLine;
//			String path="/home/klebeck/Documents/workspace-sts-2.8.1.RELEASE/qwit8/target/qwit8/html/";
			String path = servletContext.getRealPath("/html");
			String fileName   = "schema-" +docId;
			String fileSuffix = ".html";
			File tempFile = new File(path+fileName+fileSuffix);
//			File tempFile = File.createTempFile(fileName+"-", fileSuffix);
			URL url = new URL(page);	
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			FileOutputStream outStream = new FileOutputStream (tempFile);
			PrintStream ps = new PrintStream(outStream);
			while ((inputLine = in.readLine()) != null)
				ps.println(inputLine);
			in.close();
			outStream.close();
			
			///////////////////////////////////////////////////////////////////////////////
			//convert html 2 pdf
			///////////////////////////////////////////////////////////////////////////////
			response.setHeader("Content-Disposition", "inline; filename=" + fileName+".pdf");
			response.setContentType("application/pdf");
			
			String fileSuffix2 = ".pdf";
			//temporary html file to be converted
			
			File  inputFile  = tempFile.getCanonicalFile();
			//test
//			File inputFile = new File("/home/klebeck/Documents/workspace-sts-2.8.1.RELEASE/qwit8/target/qwit8/html/schema-1267235.html");
			
			//temporary pdf file wich will be passed to response.outputstream
			File  outputFile = File.createTempFile(fileName+"-", fileSuffix2);
			/*
			 * 
			OfficeManager officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
		    officeManager.start();
		    OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
		    converter.convert(inputFile, outputFile);
		    officeManager.stop();
			 */
		    
		    //delete all the temp files
//		    tempFile.deleteOnExit();
//		    inputFile.deleteOnExit();
//		    outputFile.deleteOnExit();
		    
			///////////////////////////////////////////////////////////////////////////////
			//pass the converted pdf file to response outputstream
			///////////////////////////////////////////////////////////////////////////////
			ServletOutputStream outputStream = response.getOutputStream();
			FileInputStream input = new FileInputStream(outputFile);
			BufferedInputStream buf = new BufferedInputStream(input);
			int readBytes = 0;
			while ((readBytes = buf.read()) != -1)
				outputStream.write(readBytes);
			outputStream.flush();
			
			//delete all the temp files
			tempFile.delete();
			inputFile.delete();
			outputFile.delete();
			
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void downloadDocumentAsPdf(MessageSource messageSource,	String what, String view, Integer docId, HttpServletResponse response, HttpServletRequest request)
	{
		String uriBase = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		String uri = "/print-"+what+"-"+view+"-"+docId;
		String page = uriBase + uri;
//		String path = "/home/klebeck/Documents/workspace-sts-2.8.1.RELEASE/qwit8/target/qwit8/html/";
		String path = servletContext.getRealPath("/html");
		String fileName   = what+"-" +docId+".pdf";
		String pdfFile = path+fileName;
		
		Runtime r = Runtime.getRuntime();
		Process p = null;
		    // String cmd[] = { "/bin/sh", "ls", "-l" };
		String cmd[] = { "wkhtmltopdf", page, pdfFile};
		
		try 
		{
			p = r.exec(cmd);
			int i = p.waitFor();
//			log.debug(i);
			
			writePdfToResponeStream(response, path, fileName);
		} 
		catch (Exception e) 
		{
		    e.printStackTrace();
		}
	}
	
	public void writePdfToResponeStream( HttpServletResponse response,String path, String filename)
	{
		try 
		{
			response.setHeader("Content-Disposition", "inline; filename=" + filename);
			response.setContentType("application/pdf");
			File  pdfFile = new File(path+filename);
			ServletOutputStream outputStream = response.getOutputStream();
			FileInputStream input = new FileInputStream(pdfFile);
			BufferedInputStream buf = new BufferedInputStream(input);
			int readBytes = 0;
			while ((readBytes = buf.read()) != -1)
			outputStream.write(readBytes);
			outputStream.flush();
			pdfFile.deleteOnExit();
		}
		catch(Exception e)
		{
			 e.printStackTrace();
		}
	}
}
