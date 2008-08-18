package mem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:46:10
 */
public class OneView<T extends OneView> implements Serializable {
  Map<String, Object> view = new HashMap<String, Object>();
  public T next;
  public T prev;

  public boolean isEmpty(){
    return view.isEmpty();
  }

  public Object get(String key) {
    return view.get(key);
  }

  public OneView cloneBranch(){
    OneView v = new OneView();
    v.view = new HashMap<String, Object>(view);
    v.prev = prev;
    return v;
  }

  public void mergeByAddNew(OneView other) {
    if (other == null) {
      return;
    }
    Set<String> keys = other.view.keySet();
    for (String s : keys) {
      if (!view.containsKey(s)) {
        view.put(s, other.view.get(s));
      }
    }
  }

  public OneView pt(String key, Object val) {
    view.put(key, val);
    return this;
  }

  public String toString() {
    return view.toString();
  }

  public Map<String, Object> getViewAll() {
    Map<String, Object> ret = new HashMap<String, Object>(view);
    return ret;
  }

}
