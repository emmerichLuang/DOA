package dOA.exceptions;

/**
 * 这个DDL对象没有映射
 * @author E.E.
 * 2015年10月22日
 */
public class EntityNoMappingException extends RuntimeException {

	public EntityNoMappingException(String msg){
		super(msg);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
