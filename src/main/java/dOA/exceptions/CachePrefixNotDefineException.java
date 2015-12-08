package dOA.exceptions;

/**
 * 用缓存， 然而缓存前缀没有定义
 * @author E.E.
 * 2015年10月22日
 */
public class CachePrefixNotDefineException extends RuntimeException {

	public CachePrefixNotDefineException(String msg){
		super(msg);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
