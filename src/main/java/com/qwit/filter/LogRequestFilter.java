package com.qwit.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class LogRequestFilter implements Filter {

	private FilterConfig filterConfig;
	private final static String FILTER_APPLIED = "requestlog_filter_applied";
	private static final Logger log = Logger.getLogger("com.qwit.filelogger");  

	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain nextFilter) 
	throws IOException, ServletException {

//		toLog(req,"req");
		toLog2(req,"req");
		nextFilter.doFilter(req, res);

//		log.warn(excludeFromLog(req));

		// log.warn(req.getAttribute(FILTER_APPLIED)); 
		// Ensure that filter is only applied once per request and resources are excluded
//		toLog(req,"res");

	}

	private void toLog(ServletRequest req, String be) {
		if(!excludeFromLog(req) && req.getAttribute(FILTER_APPLIED) == null)
		{
			req.setAttribute(FILTER_APPLIED, Boolean.TRUE);
			toLog2(req, be);
		}
	}

	private void toLog2(ServletRequest req, String be) {
		if(!excludeFromLog(req))
		{
			HttpServletRequest request = (HttpServletRequest)req;
			String url = request.getRequestURL().toString();
			String query = request.getQueryString();
			log.warn("---" +be +"-- " + url +"?"+ query);
		}
	}

	public FilterConfig getFilterConfig() {return filterConfig;}
	public void destroy() {}
	@Override
	public void init(FilterConfig arg0) throws ServletException {}
	private boolean excludeFromLog(ServletRequest req) {
		if(req instanceof HttpServletRequest)
		{
			HttpServletRequest hreq = (HttpServletRequest) req;
			//            return (hreq.getRequestURI().endsWith(".css") ||
			//                    hreq.getRequestURI().endsWith(".js"));
			return hreq.getRequestURL().toString().contains("resources") ? true : false; 
		}
		return false;
	}


}
