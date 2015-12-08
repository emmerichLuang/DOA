package dOA.framework.log;


import org.apache.log4j.Logger;

/**
 * 懒得new一个logger的话用这个common的log。
 * 用这个的话log里面会少一句说明这个日志在哪个类的
 * @author E.E.
 * 2015年10月13日
 */
public class CommonLog {
	
	private static Logger log = Logger.getLogger("commonLog");   
	
	public static void info(String message, Object... args){
		log.info(format(message, args));
	}

	public static void debug(String message, Object... args){
		if(log.isDebugEnabled()){
			log.debug(format(message, args));			
		}
	}	
	
	public static void warn(String message, Object... args){
		log.warn(format(message, args));
	}	
	
	public static void warn(Throwable e, String message, Object... args){
		log.warn(format(message, args), e);
	}
	
	public static void error(Throwable e, String message, Object... args){
		log.error(format(message, args), e);
	}
	
	public static void error(String message, Object... args){
		log.error(format(message, args));
	}	
	
    private static String format(String msg, Object... args) {
        try {
            if (args != null && args.length > 0) {
                return String.format(msg, args);
            }
            return msg;
        } catch (Exception e) {
            return msg;
        }
    }
}
