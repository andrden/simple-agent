package utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 04.07.2008
 * Time: 21:58:40
 */
public class RptCounter<T> {
  int bufSize;
  int minTrack;

  Map<T, AtomicLong> rpt = new HashMap<T, AtomicLong>();
  LinkedHashMap<T, Long> buf = new LinkedHashMap<T, Long>() {
    protected boolean removeEldestEntry(Map.Entry<T, Long> eldest) {
      return size() > bufSize;
    }
  };

  public RptCounter(int bufSize, int minTrack) {
    this.bufSize = bufSize;
    this.minTrack = minTrack;
  }

  public synchronized Map<T, AtomicLong> getRpt() {
    return new HashMap<T, AtomicLong>(rpt);
  }

  public synchronized void add(T key) {
    AtomicLong a = rpt.get(key);
    if (a != null) {
      a.incrementAndGet();
      return;
    }
    Long v = buf.remove(key);
    if (v == null) {
      v = 1L;
    } else {
      v = v + 1L;
    }
    if (v >= minTrack) {
      rpt.put(key, new AtomicLong(v));
    } else {
      buf.put(key, v);
    }
  }

  public static void main(String[] args) {
    //RptCounter c = new RptCounter(,)
  }
}
