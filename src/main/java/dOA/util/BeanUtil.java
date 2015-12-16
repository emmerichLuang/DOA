package dOA.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.beans.BeanCopier;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1、利用apache common bean
 * 反映机制实现，性能不高
 * 2、CGLib，动态代理实现，性能好
 * 
 * 
 * @author linwn@ucweb.com
 * @createDate 2014-6-29
 * 
 */
public class BeanUtil {

    static Logger logger = LoggerFactory.getLogger(BeanUtil.class);
    
    private static final ConcurrentHashMap<String,BeanCopier> 
                                        cglibBeanCopierMap = new ConcurrentHashMap<String,BeanCopier>();
    
    /**
     * 采用CGLib动态代理实现，性能较好，推荐使用
     * @param dest目标对象
     * @param orig原始对象
     */
    public static void copy(Object dest,Object orig){
        if(dest == null || orig == null){
            logger.warn("BeanUtil copy object err:dest=[%s],orig=[%s]", dest,orig);
            return;
        }
        //这个动态代理创建比较耗性能,缓存在map中。
        String key = String.format("%s_%s", dest.getClass().getName(),orig.getClass().getName());
        BeanCopier copier = null;
        if(!cglibBeanCopierMap.containsKey(key)){
            copier = BeanCopier.create(orig.getClass(), dest.getClass(), false);
            cglibBeanCopierMap.put(key, copier);
        }else{
            copier = cglibBeanCopierMap.get(key);
        }
        //没有使用转换器,避免使用属性类型不同,名字相同，类型不同不会复制
        copier.copy(orig, dest, null);
    }
    
    /**
     * 不同类型，进行属性copy，PropertyUtils实现，性能一般
     * @deprecated
     * @param dest
     * @param orig
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static void copyProps(Object dest , Object orig) {
        if (dest == null) {  
            throw new IllegalArgumentException("No destination bean specified");  
        }  
        if (orig == null) {  
            throw new IllegalArgumentException("No origin bean specified");  
        }  
        Class origClass = orig.getClass();  
        if (origClass == String.class || origClass.isPrimitive()) {  
            dest = orig;  
        }  
        if(orig.getClass().isArray()){  
            Object[] destArr = (Object[]) dest;  
            Object[] origArr = (Object[]) orig;  
            Class elemenClass = destArr.getClass().getComponentType();  
            for(int i=0;i<origArr.length;i++){  
                if(destArr[i]==null){  
                    try {
                        destArr[i] = elemenClass.newInstance();
                    } catch (Exception e) {
                        logger.error( "bean copy err", e);
                    } 
                }  
                copyProps(destArr[i], origArr[i]);  
            }  
        }
      
        PropertyDescriptor[] origDescriptors = PropertyUtils.getPropertyDescriptors(orig); 
        for (int i = 0; i < origDescriptors.length; i++) {  
            String name = origDescriptors[i].getName();  
            
            if ("class".equals(name)) {  
                continue;  
            }  
            Object value = null;  
            if (PropertyUtils.isReadable(orig, name)  
                    && PropertyUtils.isWriteable(dest, name)) {  
                try {  
                    value = PropertyUtils.getSimpleProperty(orig, name);  
                    PropertyUtils.setSimpleProperty(dest, name, value); 
                } catch (IllegalArgumentException e) {  
                    // 类型不同  
                    try {  
                        PropertyDescriptor targetDescriptor = PropertyUtils  
                                .getPropertyDescriptor(dest, name);  
                        Object new_value = targetDescriptor.getPropertyType()  
                                .newInstance();  
                        copyProps(new_value, value);  
                        PropertyUtils.setSimpleProperty(dest, name, new_value);  
                    }catch(Exception e1){  
                        logger.error("bean copy err", e1);
                    }
                } catch (Exception e) {  
                    logger.error("bean copy err", e);
                } 
            }  
        }  
    }
    
    /**
     * 同一类型
     * 对象copy
     * @param t
     * @return
     */
    public static <T> T deepCopyBean(T t){
        if(t == null){
            return null;
        }
        try{
            //collection 类型
            if( t instanceof Collection){
                return (T)deepCopyCollection((Collection)t);
            }
            //数组类型
            if(t.getClass().isArray()){
                return (T)deepCopyArray(t);
            }
            if(t instanceof Map){
                return (T)deepCopyMap((Map)t);
            }
            if(t instanceof Date){
                return (T)new Date(((Date)t).getTime());
            }
            if(t instanceof Timestamp){
                return (T)new Timestamp(((Timestamp)t).getTime());
            }
            if(t.getClass().isPrimitive() || t instanceof String
                    || t instanceof Boolean || Number.class.isAssignableFrom(t.getClass())){
                return t;
            }
            if(t instanceof StringBuilder){
                return (T)new StringBuilder(t.toString());
            }
            if(t instanceof StringBuffer){
                return (T)new StringBuffer(t.toString());
            }
            Object dest = t.getClass().newInstance();
            BeanUtilsBean bean =BeanUtilsBean.getInstance();
            PropertyDescriptor[] origDestors = bean.getPropertyUtils().getPropertyDescriptors(t);
            for(int i = 0 ; i < origDestors.length ; i++){
                String name = origDestors[i].getName();
                if("class".equals(name)){
                    continue ;
                }
                if(bean.getPropertyUtils().isReadable(t, name) 
                        &&bean.getPropertyUtils().isWriteable(t, name)){
                    try{
                        Object value = deepCopyBean(bean.getPropertyUtils().getSimpleProperty(t, name));
                        bean.getPropertyUtils().setSimpleProperty(dest, name, value);
                    }catch(NoSuchMethodException e){
                        logger.error("bean no such method:"+t.getClass().getName(), e);
                    }
                    
                }
            }
            return (T)dest;
        }catch(Exception e){
            logger.error("bean copy err,msg: "+e.getMessage(), e);
            return null;
        }
        
    }
    
    /**
     * 深度拷贝Collection类
     * @param c
     * @return
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private static Collection deepCopyCollection(Collection c) throws InstantiationException, IllegalAccessException{
        Collection dest = c.getClass().newInstance();
        for(Object o : c){
            dest.add(deepCopyBean(o));
        }
        return dest;
    }
    
    /**
     * 深度拷贝数组
     * @param array
     * @return
     */
    private static Object deepCopyArray(Object array){
        int length = Array.getLength(array);
        Object dest = Array.newInstance(array.getClass().getComponentType(), length);
        if(length == 0){
            return dest;
        }
        for(int i = 0 ; i < length; i++){
            Array.set(dest, i, deepCopyBean(Array.get(array, i)));
        }
        return dest;
    }
    
    /**
     * 
     * @param m
     * @return
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    private static Map deepCopyMap(Map m) throws InstantiationException, IllegalAccessException{
        Map dest = m.getClass().newInstance();
        for(Object o : m.entrySet()){
            Entry e = (Entry)o;
            dest.put(deepCopyBean(e.getKey()), deepCopyBean(e.getValue()));
        }
        return dest;
    }
    
    
    
}
