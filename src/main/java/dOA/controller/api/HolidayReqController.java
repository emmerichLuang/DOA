package dOA.controller.api;

import java.io.IOException;
import java.util.ArrayList;
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
import dOA.util.JacksonUtil;

@Controller
public class HolidayReqController extends BaseController{
	
	private static String WORKFLOW_NAME = "holidayReq";//processDefinitionKey

	@Autowired
	private ProcessEngine processEngine;	
	
	@ResponseBody
	@RequestMapping(value = { "/webFlow/holidayReq/req.json" }, method = { RequestMethod.POST })	
	public void req(HttpServletRequest request, HttpServletResponse response) throws Exception{
		Map<String, String> params = super.getParams(request);
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("assigneeUser", params.get("assigneeUser"));
		variables.put("days", params.get("days"));
		variables.put("reason", params.get("reason"));
		variables.put("candidateUsers", "");
		variables.put("candidateGroups", "");		
		
		RuntimeService runtimeService = processEngine.getRuntimeService();

		//设置ACT_HI_PROCINST表的start_user_id 和在xml中的startevent中写activiti:initiator一样
		processEngine.getIdentityService().setAuthenticatedUserId(params.get("assigneeUser"));	
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
		taskService.setOwner(task.getId(), params.get("assigneeUser"));
		taskService.complete(task.getId(), variables);		
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData.put("processInstanceId", instance.getProcessInstanceId());
		resultData.put("processDefinitionId", instance.getProcessDefinitionId());		
		resultData.put("id", instance.getId());	
		
		JsonResult result =  new JsonResult(resultData, Boolean.TRUE);
		super.outJson(result, response);
	}
	
	
	/**
	 * 某人todo的东西
	 * 
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@ResponseBody
	@RequestMapping(value = { "/webflow/holidayReq/peopleToDo.json"}, method = { RequestMethod.POST })
	public void peopleToDo(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, String> params = super.getParams(request);
		TaskService taskService = processEngine.getTaskService();
		List<Task> tasks = taskService.createTaskQuery().taskAssignee(params.get("people")).list();
		
		List<Map> resultList = new ArrayList();
		for(Task tt:tasks){
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", tt.getId());
			m.put("taskDefine", tt.getTaskDefinitionKey());		//任务定义	
			m.put("desc", tt.getDescription());		
			resultList.add(m);
		}
		
		JsonResult result =  new JsonResult(resultList, Boolean.TRUE);
		response.getWriter().write(JacksonUtil.catchedEncode(result));
	}		
	
	
	/**
	 * 主管处理
	 * TODO: 流程图中没有做网关。那么下面的代码就是人肉处理驳回了。
	 * @param request
	 * @return
	 * @throws IOException 
	 */
	@ResponseBody
	@RequestMapping(value = { "/webflow/holidayReq/leaderAudit.json"}, method = { RequestMethod.POST })
	public void leaderAudit(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, String> params = super.getParams(request);
		TaskService taskService = processEngine.getTaskService();
		Task t = taskService.createTaskQuery().taskId(params.get("taskId")).singleResult();

		Map<String, Object> variables = new HashMap<String, Object>();
		
		taskService.complete(t.getId(), variables);
		
		JsonResult result =  new JsonResult(null, Boolean.TRUE);
		response.getWriter().write(JacksonUtil.catchedEncode(result));
	}	
}
