package com.pmstation.common.utils;

import java.util.StringTokenizer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 *
 * User: adenysenko
 * Date: Jan 23, 2007
 * Time: 3:00:46 PM
 *
 */
public class PrivateFieldGetter {

  public static Object evalStatic(Class c, String path) throws Exception{
    return eval(c, null, path);
  }

  public static void set(Object inst, String fieldName, Object newValue) throws Exception{
    Field field = findField(inst.getClass(), fieldName);
    field.set(inst, newValue);
  }

  public static void set(Class clz, String fieldName, Object newValue) throws Exception{
    Field field = findField(clz, fieldName);
    field.set(null, newValue);
  }

  public static Object eval(Object inst, String path) throws Exception{
    return eval(inst.getClass(), inst, path);
  }

  public static Object evalNoEx(Object inst, String path){
    try {
      return eval(inst.getClass(), inst, path);
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
  }

  public static Object callSimple(Object inst, String name, Object ... args){
    if( inst==null ){
      throw new NullPointerException("inst==null for calling "+name);
    }
    Class[] parms = new Class[args.length];
    for( int i=0; i<args.length; i++ ){
      if( args[i]==null ){
        throw new RuntimeException("arg is null, i="+i);
      }
      parms[i] = args[i].getClass();
    }
    try {
      Method method = inst.getClass().getMethod(name, parms);
      method.setAccessible(true);
      return method.invoke(inst, args);
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
  }

  private static Object eval(Class c, Object inst, String path) throws Exception{
    StringTokenizer st = new StringTokenizer(path,".");
    while(st.hasMoreTokens()){
      String fieldName = st.nextToken();
      Field field = findField(c, fieldName);
      inst = field.get(inst);
      if( inst!=null ){
        c = inst.getClass();
      }
    }
    return inst;
  }

  private static Field findField(Class c, String fieldName) throws NoSuchFieldException {
    while(true){
      try{
        Field field = c.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
      }catch(NoSuchFieldException e){
        if( c==Object.class ){
          throw e;
        }
        c = c.getSuperclass();
      }
    }
  }

  public static void main(String[] args) throws Exception{
    String abc = "abc";
    System.out.println("a "+abc.length());
    PrivateFieldGetter.set(abc,"count",123);
    System.out.println("b "+abc.length());

    System.out.println( evalStatic(Class.forName("java.nio.Bits"), "reservedMemory") );

    ByteBuffer.allocateDirect(100000000);

    System.out.println( evalStatic(Class.forName("java.nio.Bits"), "reservedMemory") );
    //Object v = eval(new PrivateFields(),"x.value");
    //Object v = eval("aa","count");
    //v=v;
  }
}
