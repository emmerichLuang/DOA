package dOA.framework.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import dOA.exceptions.CachePrefixNotDefineException;
import dOA.exceptions.MCInitException;
import dOA.framework.conf.Config;
import net.spy.memcached.MemcachedClient;

/**
 * mc实现的版本
 * 可以获取mcClient从而更多玩法
 * @author E.E.
 * 2015年11月5日
 */
@Component
public class MemcachedImpl implements ICache{

	private static final String CACHE_PREFIX_CONF = "cachePrefixMapper.xml";
	
	//clientName--mcNodes
	private static Map<String, MemcachedClient> CLIENTS = new HashMap<String, MemcachedClient>();
	
	// prefix--mc clientName
	private static Map<String, CacheEntityConf> PREFIX_MAPPER = new HashMap<String, CacheEntityConf>();
	
	
	//
	private static Logger mcLog = Logger.getLogger("memcachedLog");   
	
	private static class CacheEntityConf{
		Integer expTime;	//单位秒，超过30天就成为时间戳了
		String clientName;
		
		public CacheEntityConf(String clientName, Integer expTime){
			this.expTime = expTime;
			this.clientName = clientName;
		}
	}
	
	/**
	 * 不是接口的方法。可以很随意的获取某个client，做mc才有的动作。cas什么的
	 * @return
	 */
	public Map<String, MemcachedClient> getClients(){
		return CLIENTS;
	}
	
	/**
	 * 1、根据配置， 初始化mcclient
	 * 2、读取prefix映射
	 * @throws IOException 
	 */
	public void initCache() {
		//1、初始化mc client们；
		Map<String, String> confMap = Config.getConfMap("cache.memcached.");
		for(String confKey: confMap.keySet()){
			String clientKey = confKey.replaceFirst("cache.memcached.", "");	
			List<InetSocketAddress> nodes = str2Conf(confMap.get(confKey));
			try {
				MemcachedClient c = new MemcachedClient(nodes);
				CLIENTS.put(clientKey, c);
			} catch (IOException e) {
				throw new MCInitException("mc init exception! clientName:"+clientKey);
			}
		}
		
		//2、初始化prefix的对应哪个client的mapper
		try {
			SAXReader reader = new SAXReader();  
			InputStream stream = Config.class.getClassLoader().getResourceAsStream(CACHE_PREFIX_CONF);
			Document document = reader.read(stream);
			List<?> states = document.selectNodes("/prefixs/prefix");  
		    Iterator<?> it = states.iterator();  
		    while (it.hasNext()) {  
		        Element el = (Element) it.next();
		        String name = el.attributeValue("name");
		        String client = el.attributeValue("client");
		        String expTime = el.attributeValue("expTime");
		        
		        if(!CLIENTS.containsKey(client)){
		        	throw new MCInitException(String.format("mc init exception! in file %s. clientName not exists:%s", 
		        			CACHE_PREFIX_CONF, client));
		        }
		        
		        PREFIX_MAPPER.put(name, new CacheEntityConf(client, Integer.parseInt(expTime)));
		    }			
			
		} catch (DocumentException e) {
			throw new MCInitException("mc init exception! while init prefix mapper."+e.getMessage());
		}  
	}

	
	/**
	 * aliyun.9game.cn:11211,aliyun.9game.cn:11211 逗号切割的多个client对象，转换为对象。
	 * @param clientKey
	 * @return
	 */
	private List<InetSocketAddress> str2Conf(String clientKey){
		List<InetSocketAddress> clientList = new ArrayList<InetSocketAddress>();
		String[] oneClient = clientKey.split(",");
		
		for(String clientStr:oneClient){
			String[] arr = clientStr.split(":");	//aliyun.9game.cn:11211 一个
			InetSocketAddress inetSocketAddress = new InetSocketAddress(arr[0], Integer.parseInt(arr[1]));
			clientList.add(inetSocketAddress);
		}
		return clientList;
	}
	
	
	public String get(String prefix, String key) {
		String client = PREFIX_MAPPER.get(prefix).clientName;
		if(StringUtils.isEmpty(client)){
			throw new CachePrefixNotDefineException("prefix not define! prefix:"+prefix);
		}
		MemcachedClient mcClient = CLIENTS.get(client);
		Object obj = mcClient.get(key);

		mcLog.info(String.format("get prefix:%s key:%s", prefix, key));
		
		return obj==null?null:obj.toString();
	}

	public void set(String prefix, String key, String value) {
		String client = PREFIX_MAPPER.get(prefix).clientName;
		if(StringUtils.isEmpty(client)){
			throw new CachePrefixNotDefineException("prefix not define! prefix:"+prefix);
		}
		MemcachedClient mcClient = CLIENTS.get(client);
		mcClient.set(key, PREFIX_MAPPER.get(prefix).expTime, value);
		mcLog.info(String.format("set prefix:%s key:%s", prefix, key));
	}

	public void del(String prefix, String key) {
		String client = PREFIX_MAPPER.get(prefix).clientName;
		if(StringUtils.isEmpty(client)){
			throw new CachePrefixNotDefineException("prefix not define! prefix:"+prefix);
		}
		MemcachedClient mcClient = CLIENTS.get(client);
		mcClient.delete(key);
		mcLog.info(String.format("del prefix:%s key:%s", prefix, key));
	}
}
