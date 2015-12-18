package dOA.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.task.Task;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import dOA.framework.controller.BaseController;
import dOA.framework.controller.JsonResult;
import dOA.util.BeanUtil;

@Controller
public class CommonWebFlowController extends BaseController {

	@Autowired
	private ProcessEngine processEngine;

	/**
	 * 根据processDefinitionId， 输出这个流程的定义
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = { "/webFlow/getProcessDefinition.json" }, method = { RequestMethod.POST })
	public void getProcessDefinition(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> params = super.getParams(request);

		/*if(!super.checkRequireParam(params, response, "processDefinitionId")){
			return ;
		}*/
		
		String processDefinitionId = params.get("processDefinitionId");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
				.getDeployedProcessDefinition(processDefinitionId);

		List<ActivityImpl> activitiList = def.getActivities();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (ActivityImpl activiti : activitiList) {
			Map<String, Object> temp = activiti.getProperties();
			temp.put("activitiId",activiti.getId());
			list.add(temp);
		}

		JsonResult result = new JsonResult(list, Boolean.TRUE);
		super.outJson(result, response);
	}

	/**
	 * 得知当前正在执行的是哪一个任务
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = { "/webFlow/getInfoByInstanceId.json" }, method = { RequestMethod.POST })
	public void getInfoByInstanceId(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> params = super.getParams(request);

		String instanceId = params.get("instanceId");

		RepositoryService repositoryService = processEngine.getRepositoryService();
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		
		//获得当前的taskId-- 历史表
		List<Task> taskList = taskService.createTaskQuery().processInstanceId(instanceId).list();
		if(taskList==null||taskList.size()==0){
			JsonResult result = new JsonResult(null, Boolean.FALSE, "流程已结束或者流程不存在");
			super.outJson(result, response);
			return;
		}if(taskList.size()!=1){
			JsonResult result = new JsonResult(null, Boolean.FALSE, "task不止一个");
			super.outJson(result, response);
			return;
		}
		Task task = taskList.get(0);
		String excId = task.getExecutionId();  //通过历史表，找到运行时表的ID
		ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(excId).singleResult();  
		String activitiId = execution.getActivityId();
		//System.err.println("activitiId:"+activitiId);
		ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(task.getProcessDefinitionId());
		ProcessDefinitionEntity def = (ProcessDefinitionEntity)processDefinition ;

		List<ActivityImpl> activitiList = def.getActivities();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (ActivityImpl activiti : activitiList) {
			Map<String, Object> temp = new HashMap<String, Object>();
			temp.putAll(activiti.getProperties());
			temp.put("activitiId",activiti.getId());			
			if(activiti.getId().equals(activitiId)){	//记录当前在执行的任务
				temp.put("current", "==============================current==============================");
			}

			//子流程有循环引用   type = subProcess
			if(temp.get("type")!=null && "subProcess".equals(temp.get("type"))){
				System.out.println("有子流程");
				temp.remove("initial");	//避免无限循环
			}
					
			list.add(temp);
		}

		JsonResult result = new JsonResult(list, Boolean.TRUE);
		super.outJson(result, response);
	}	
}
