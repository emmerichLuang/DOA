package dOA.framework.cache;

import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 会剔除越界部分的内容
 * @author E.E.
 * 2015年11月5日
 */
@Component
public class LruCache implements ICache{

	private static Integer MAX_SIZE = 99999;
	/**
	 * may evict an entry before this limit is exceeded
	 * 超过最大长度就剔除
	 */
	private static Cache<String, String> c = CacheBuilder.newBuilder().maximumSize(MAX_SIZE).build();
	
	public String get(String prefix, String key) throws Exception{
		return c.getIfPresent(key);
	}
	
	public void set(String prefix, String key, String value){
		c.put(key, value);
	}

	public void del(String prefix, String key) {
		c.invalidate(key);
	}

	public void initCache() {
		
	}
}
