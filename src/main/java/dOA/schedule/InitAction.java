package dOA.schedule;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 
 * @author liangrl
 * @date   2015年12月7日
 *
 */
public class InitAction implements ApplicationListener<ContextRefreshedEvent>{

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// root applicationContext没有parent，保证是统一的context
		ApplicationContext context = event.getApplicationContext();		
		
		if (context.getParent() == null) {	//避免springMVC初始化两次
			
		}		
	}

}
