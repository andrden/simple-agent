package com.pmstation.common.utils;

import java.util.*;

/**
 *
 * User: adenysenko
 * Date: Jan 11, 2007
 * Time: 10:20:24 AM
 *
 */
public class CountingMap<T> extends HashMap<T,Long> {
  public CountingMap(Map<? extends T, ? extends Long> m) {
    super(m);
  }
  public CountingMap(){
    super();
  }

  public void increment(T key, long delta){
        Long v = get(key);
        if( v==null ){
            put(key, delta);
        }else{
            put(key, v+delta);
        }
    }

    public void increment(T key){
      increment(key, 1L);
    }

    public synchronized void syncIncrement(T key) {
      increment(key);
    }

    public synchronized void syncIncrement(T key, long delta) {
      increment(key, delta);
    }

    public void decrement(T key){
        Long v = get(key);
        if( v==null ){
            throw new NoSuchElementException("key="+key);
        }else if( v.longValue()==1 ){
            remove(key);
        }else{
            put(key, v - 1);
        }
    }
    public synchronized void syncDecrement(T key){
        decrement(key);
    }
    public synchronized boolean syncIncrementIfLess(T key, long max){
        Long v = get(key);
        long newVal;
        if( v==null ){
            newVal = 1;
        }else{
            newVal = v+1;
        }
        if( newVal<=max ){
            put(key, newVal);
            return true;
        }else{
            return false;
        }
    }

  public synchronized List<T> syncKeys() {
    List<T> res = new ArrayList<T>();
    for (T o : keySet()) {
      res.add(o);
    }
    return res;
  }

  public synchronized long syncTotalCount() {
    long ret=0;
    for (Long v : values()) {
      ret += v;
    }
    return ret;
  }

   public synchronized Long syncGet(T key) {
    return get(key);
  }

  public synchronized Map<T,Long> syncGetWithMinHit(int min) {
    Map<T,Long> res = new HashMap<T,Long>();
    for (T o : keySet()) {
      long v = get(o);
      if( v>= min ){
        res.put(o, v);
      }
    }
    return res;
  }

  public void addFrom(CountingMap<T> other){
    synchronized(other){
      for(Map.Entry<T,Long> me : other.entrySet()){
        increment(me.getKey(), me.getValue());
      }
    }
  }

  public synchronized List<T> syncKeysSortedDesc(){
    return keysSortedDesc();
  }

  public List<T> keysSortedDesc(){
    return keysSortedDesc(this);
  }

  public static <T> List<T> keysSortedDesc(final Map<T,Long> map){
    List<T> res = new ArrayList<T>(map.keySet());
    Collections.sort(res, new Comparator<T>(){
      public int compare(T o1, T o2) {
        return map.get(o2).compareTo(map.get(o1));
      }
    });
    return res;
  }

  public Map<T,Long> mapSortedDesc(){
    Map<T,Long> r = new LinkedHashMap<T,Long>();
    for( T k : keysSortedDesc(this) ){
      r.put(k, get(k));
    }
    return r;
  }

  public synchronized long syncGetValOr0(T key){
    return getValOr0(key);
  }

  public long getValOr0(T key){
    Long v = get(key);
    if( v==null ) return 0;
    return v.longValue();
  }

  public synchronized CountingMap<T> syncSnapshot(boolean reset){
    CountingMap<T> res = new CountingMap<T>(this);
    if( reset ) clear();
    return res;
  }

  public void purgeIfLess(long min){
    for( Iterator<Map.Entry<T,Long>> i = entrySet().iterator(); i.hasNext(); ){
      Map.Entry<T,Long> me = i.next();
      if( me.getValue()<min ){
        i.remove();
      }
    }
  }

}
