package dOA.framework.conf;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import dOA.framework.log.CommonLog;

/**
 * 自定义配置
 * @author E.E.
 * 2015年10月22日
 */
public class Config {
	
	private static final String PROPERTIY_FILE_PATH = "env.properties";
	
	private static Properties propertie = new Properties();
	
	private static boolean done=false;
	
	public synchronized static void init(){
		
		if(done){
			CommonLog.info("已经加载过conf.properties了。不重复加载。");
			return;
		}
		
		try {
			//stream -->buffer， properties文件读buffer而不是直接读stream，这样才不会中文乱码
			InputStream stream = Config.class.getClassLoader().getResourceAsStream(PROPERTIY_FILE_PATH);
			BufferedReader bf = new BufferedReader(new InputStreamReader(stream));
			propertie.load(bf);
			done=true;
		} catch (FileNotFoundException e) {
			CommonLog.error(e, e.getMessage());
		} catch (IOException e) {
			CommonLog.error(e, e.getMessage());
		}
	}
	
	public static String getConf(String key){
		return propertie.getProperty(key);
	}

	/**
	 * 符合前缀的参数，key-value的方式返回
	 * @param prefix
	 * @return
	 */
	public static Map<String, String> getConfMap(String prefix){
		Map<String, String> result = new HashMap<String, String>();
		for(Object obj: propertie.keySet()){
			String key = obj.toString();
			if(key.startsWith(prefix)){
				result.put(key, propertie.getProperty(key));
			}
		}
		return result;
	}	
	
	/**
	 * 把符合前缀的参数放到list中
	 * @param prefix
	 * @return
	 */
	public static List<String> getConfList(String prefix){
		List<String> result = new ArrayList<String>();
		for(Object obj: propertie.keySet()){
			if(obj.toString().startsWith(prefix)){
				result.add(propertie.getProperty(obj.toString()));
			}
		}
		return result;
	}	
	
	
}
