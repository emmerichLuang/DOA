package dOA.util;


import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import dOA.framework.log.CommonLog;

public class JacksonUtil{
    private static ObjectMapper objectMapper = new ObjectMapper();
    static{
    	objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    	//设置输入时忽略JSON字符串中存在而Java对象实际没有的属性 
    	objectMapper.setDeserializationConfig(objectMapper.getDeserializationConfig().without(                  
    		       DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES));
    }
    
	/**
	 * 使用对象进行json反序列化。
	 * @param json
	 * @param pojoClass
	 * @return
	 * @throws Exception
	 */
	public static Object decode(String json, Class<?> pojoClass) throws Exception{		
		return objectMapper.readValue(json, pojoClass);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> decode2Map(String json){
		Map<String, Object> result = new HashMap<String, Object>();
		result = (Map<String, Object>) catchDecode(json, result.getClass());
		
		return result;
	}

	public static Object catchDecode(String json, Class<?> pojoClass){		
		try {
			return decode(json, pojoClass);
		} catch (Exception e) {
			CommonLog.error(e, "error json:"+json);
		}
		return null;
	}
	
	public static String catchedEncode(Object o){
		try {
			return encode(o);
		} catch (Exception e) {
			CommonLog.error(e, e.getMessage());
		}
		return null;
	}
	
	/**
	 * 将对象序列化。
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public static String encode(Object o) throws Exception{
		try{
			return objectMapper.writeValueAsString(o);
		}catch(Exception e){
			throw e;
		}
	}
}
