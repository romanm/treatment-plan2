package com.qwit.util;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StringParser{
	protected final Log log = LogFactory.getLog(getClass());
	private final  static Pattern LINK_PATTERN = Pattern.compile("([^<\\s]*)<a[^>]*>.*</a>([^<\\s]*)");

	public boolean consistsLinksOnly(String notice)
	{
		//log.debug("....consistsLinksOnly..." + notice);
		return LINK_PATTERN.matcher(notice).matches();
	}
}
