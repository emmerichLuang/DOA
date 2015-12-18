package dOA.framework.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dOA.util.JacksonUtil;

public abstract class BaseController {

	protected boolean checkRequireParam(Map<String, String> params, HttpServletResponse response, String... requiredParamName) throws Exception{
		for(String paramName:requiredParamName){
			if(!params.containsKey(paramName)){
				JsonResult result = new JsonResult(null, false, "需要参数："+paramName);
				outJson(result, response);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param obj
	 * @param response
	 * @throws Exception 
	 */
	protected void outJson(JsonResult result, HttpServletResponse response) throws Exception{
		response.setCharacterEncoding("UTF-8");  
	    response.setContentType("application/json; charset=utf-8");  
	    String jsonStr = JacksonUtil.catchedEncode(result);
	    response.getWriter().write(jsonStr);
	    response.getWriter().flush();
	    response.getWriter().close();
	}
	
	/**
	 * 获取参数
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Map<String, String> getParams(HttpServletRequest request){
		// 参数Map
	    Map<String, String[]> properties = request.getParameterMap();
	    // 返回值Map
	    Map<String, String> returnMap = new HashMap<String, String>();
	    Iterator<Entry<String, String[]>> entries = properties.entrySet().iterator();
	    Map.Entry entry;
	    String name = "";
	    String value = "";
	    while (entries.hasNext()) {
	        entry = (Map.Entry) entries.next();
	        name = (String) entry.getKey();
	        Object valueObj = entry.getValue();
	        if(null == valueObj){
	            value = "";
	        }else if(valueObj instanceof String[]){
	            String[] values = (String[])valueObj;
	            for(int i=0;i<values.length;i++){
	                value = values[i] + ",";
	            }
	            value = value.substring(0, value.length()-1);
	        }else{
	            value = valueObj.toString();
	        }
	        returnMap.put(name, value);
	    }
	    return returnMap;
	}
}
