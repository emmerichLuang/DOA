package dOA.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import dOA.framework.controller.BaseController;
import dOA.framework.controller.JsonResult;

@Controller
public class HolidayReqController extends BaseController{
	
	private static String WORKFLOW_NAME = "holidayReq";//processDefinitionKey

	@Autowired
	private ProcessEngine processEngine;	
	
	@ResponseBody
	@RequestMapping(value = { "/webFlow/req.json" }, method = { RequestMethod.POST })	
	public void req(HttpServletRequest request, HttpServletResponse response) throws Exception{
		Map<String, String> params = super.getParams(request);
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("assigneeUser", params.get("assigneeUser"));
		variables.put("days", params.get("days"));
		variables.put("reason", params.get("reason"));
		variables.put("candidateUsers", "");
		variables.put("candidateGroups", "");		
		
		RuntimeService runtimeService = processEngine.getRuntimeService();
		//启动流程
		ProcessInstance instance = runtimeService.startProcessInstanceByKey(WORKFLOW_NAME, variables);
		
		//执行第一步的任务
		TaskService taskService = processEngine.getTaskService();
		List<Task> tList = taskService.createTaskQuery().processInstanceId(instance.getProcessInstanceId()).list();
		//应该只有一个
		if(tList==null || tList.isEmpty() || tList.size()!=1){
			throw new RuntimeException("应该只有一个task");
		}
		Task task = tList.get(0);
		//taskService.setAssignee(task.getId(), params.get("user"));
		taskService.setOwner(task.getId(), params.get("assigneeUser"));
		taskService.complete(task.getId(), variables);		
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData.put("processInstanceId", instance.getProcessInstanceId());
		resultData.put("processDefinitionId", instance.getProcessDefinitionId());		
		resultData.put("id", instance.getId());	
		
		JsonResult result =  new JsonResult(resultData, Boolean.TRUE);
		super.outJson(result, response);
	}
	
}
