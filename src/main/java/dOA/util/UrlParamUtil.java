package dOA.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import dOA.framework.log.CommonLog;


/**
 * 默认编码、解码，而且增加了try catch
 * @author E.E.
 * 2015年10月20日
 */
public class UrlParamUtil {
	
	private static final String encode = "UTF-8";
	
	/**
	 * 编码
	 * @param msg
	 * @return
	 */
	public static String encode(String msg){
		String result = null;
		try {
			result = URLEncoder.encode(msg, encode);
		} catch (UnsupportedEncodingException e) {
			CommonLog.error(e, e.getMessage());
		}
		return result;
	}
	
	/**
	 * 解码
	 * @param msg
	 * @return
	 */
	public static String decode(String msg){
		String result = null;
		try {
			result = URLDecoder.decode(msg, encode);
		} catch (UnsupportedEncodingException e) {
			CommonLog.error(e, e.getMessage());
		}
		return result;
	}
}
