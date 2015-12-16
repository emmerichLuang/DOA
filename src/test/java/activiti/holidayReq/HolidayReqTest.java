package activiti.holidayReq;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class HolidayReqTest{
	
	/**
	 * deploy
	 * @param args
	 */
	public static void main(String[] args) {
		String[] locations = {"spring/applicationContext.xml"};
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(locations);
		
		ProcessEngine processEngine = (ProcessEngine)context.getBean("processEngine");
		try {
			Deployment deployment = processEngine.getRepositoryService().createDeployment().name("请假申请")
					.addClasspathResource("diagrams/holidayReq.bpmn20.xml").deploy();     
			String deploymentId = deployment.getId();
			System.out.println(deploymentId);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}	
}
