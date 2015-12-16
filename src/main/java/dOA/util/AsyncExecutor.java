package dOA.util;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dOA.framework.log.CommonLog;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 也是拷贝过来的， 异步执行器。
 * 通过字节码加强的方式， 对非static的方法做一次代理，把方法异步执行。
 * @author E.E.
 * 2015年10月26日
 */
public class AsyncExecutor implements MethodInterceptor{
	
	/**
	 * 把增强之后的代理对象缓存起来，避免重复创建代理对象，影响性能
	 */
	private Map<String, Object> proxyCache = new ConcurrentHashMap<String, Object>();
	
	/**
	 * 线程池
	 */
	private ExecutorService pool = null;
	
	private static volatile  AsyncExecutor instance = null;
	
	private static final AtomicInteger threadNum = new AtomicInteger();
	
	private AsyncExecutor() {
		init();
	}
	
	private void init() {
	  //增加线程特征，线程名,方便在堆栈中定位问题等。
        pool = new ScheduledThreadPoolExecutor(10, new ThreadFactory(){
            public Thread newThread(Runnable r){
                int curIndex = threadNum.getAndIncrement();
                if(curIndex > 999){
                    threadNum.set(0);
                }
                return new Thread(r,"asyncExecutor-thread-" + curIndex);
            }
        });
	    //JVM关闭时添加钩子
	    addShutdownHook();
	}
	
	/**
	 * 获取异步执行器实例
	 * @return
	 */
	public static AsyncExecutor getInstance() {
		if(instance == null) {
			synchronized (AsyncExecutor.class) {
				if(instance == null) {
					instance = new AsyncExecutor();
				}			
			}			
		}
		return instance;
	}
	
	/**
	 * 返回指定业务service类的代理对象
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> clazz) {
		return (T)this.enhance(clazz);
	}
	
	/**
	 * 根据类对象，返回增强之后的代理对象
	 * @param clazz
	 * @return
	 */
	private  Object enhance(Class<?> clazz) {
		if(proxyCache.get(clazz.getName()) != null) {
			CommonLog.debug("缓存中存在%s的增强对象", clazz.getName());
			return proxyCache.get(clazz.getName());
		}
		Enhancer enhancer = new Enhancer();
		enhancer.setCallback(this);
		enhancer.setSuperclass(clazz);
		Object o = enhancer.create();
		proxyCache.put(clazz.getName(), o);
		return o;
	}

	public Object intercept(final Object obj, final Method method, final Object[] args,
			final MethodProxy methodProxy) throws Throwable {
		CommonLog.debug("当前主线程所属线程组线程组(%s),线程id(%s)，线程名称(%s)", 
						Thread.currentThread().getThreadGroup().getName(),
						Thread.currentThread().getId(),
						Thread.currentThread().getName());
		pool.submit(new Callable<Object>() {
			public Object call() throws Exception {	
				CommonLog.debug("启动子线程执行异步操作，线程组(%s),线程id(%s)，线程名称(%s),执行的方法(%s)", 
						Thread.currentThread().getThreadGroup().getName(),
						Thread.currentThread().getId(),
						Thread.currentThread().getName(),
						obj.getClass().getName()+"."+method.getName());
				Object o = null;
				try {
					o = methodProxy.invokeSuper(obj, args);
				} catch (Throwable e) {
					CommonLog.error(e,e.getMessage());
				}
				return o;
			}
			
		});
		return null;
	}
	
	//JVM关闭时的钩子程序
    private  void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run() {
            	CommonLog.warn("JVM shutdown，begining hook ...AsyncExecutor task ");
                pool.shutdown();
                try {
                    if(!pool.awaitTermination(60, TimeUnit.SECONDS)){
                        pool.shutdownNow();
                        if(!pool.awaitTermination(60, TimeUnit.SECONDS)){
                        	CommonLog.error("Pool did not terminate");
                        }
                    }
                } catch (InterruptedException e) {
                	CommonLog.error(e,"Pool terminate err");
                    // (Re-)Cancel if current thread also interrupted
                    pool.shutdownNow();
                    // Preserve interrupt status
                    Thread.currentThread().interrupt();
                }
            }
            
        });
    }
    
}
