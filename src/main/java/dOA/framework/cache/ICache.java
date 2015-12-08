package dOA.framework.cache;


public interface ICache {
	
	public static final String DEFAULT_PREFIX = "gobal_";
	
	public String get(String prefix, String key) throws Exception;
	
	public void set(String prefix, String key, String value);
	
	public void del(String prefix, String key);
	
	/**
	 * singleBbs.core.init.InitAction 中初始化缓存
	 */
	public void initCache();
}
