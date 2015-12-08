package dOA.cache;

import org.springframework.stereotype.Service;

/**
 * 缓存的外观，入口
 * @author E.E.
 * 2015年10月22日
 */
@Service("cacheFacade")
public class CacheFacade implements ICache{
	
	//不要注解注入。改为XML配置的注入
	private ICache cache;

	public String get(String prefix, String key) throws Exception {
		return cache.get(prefix,key);
	}

	public void set(String prefix, String key, String value) {
		cache.set(prefix,key, value);
	}

	public void del(String prefix, String key) {
		cache.del(prefix,key);
	}

	public ICache getCache() {
		return cache;
	}

	public void setCache(ICache cache) {
		this.cache = cache;
	}

	public void initCache() {
		
	}
	
}
