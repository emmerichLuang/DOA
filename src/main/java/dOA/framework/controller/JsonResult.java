package dOA.framework.controller;

import java.util.Map;

/**
 * 
 * @author E.E.
 * 2015年10月8日
 */
public class JsonResult {
	
	public JsonResult(Object result, Boolean success, String msg) {
		super();
		this.id = System.currentTimeMillis();
		this.result = result;
		this.success = success;
		this.msg = msg;
	}	
	
	public JsonResult(Object result, Boolean success) {
		super();
		this.id = System.currentTimeMillis();
		this.result = result;
		this.success = success;
	}		
	
	public JsonResult(Object result, Boolean success, Map<?, ?> extendInfo) {
		super();
		this.id = System.currentTimeMillis();
		this.result = result;
		this.success = success;
		this.extendInfo = extendInfo;
	}	
	
	public JsonResult(Long id, Object result, Boolean success, Map<?, ?> extendInfo, String msg) {
		super();
		this.id = id;
		this.result = result;
		this.success = success;
		this.extendInfo = extendInfo;
		this.msg = msg;
	}

	private Long id;	//如果有就和请求的id一样。
	
	private Object result;
	
	private Boolean success;
	
	private Map<?, ?> extendInfo;

	private String msg;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Object getExtendInfo() {
		return extendInfo;
	}

	public void setExtendInfo(Map<?, ?> extendInfo) {
		this.extendInfo = extendInfo;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
