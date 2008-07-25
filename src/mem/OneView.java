package mem;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 25/7/2008
 * Time: 17:46:10
 */
public class OneView implements Serializable {
  Map<String,Object> view = new HashMap<String,Object>();

  public Object get(String key){
    return view.get(key);
  }

  public OneView pt(String key, Object val){
    view.put(key, val);
    return this;
  }

  public String toString() {
    return view.toString();
  }

  public Map<String, Object> getViewAll(){
    Map<String, Object> ret = new HashMap<String, Object>(view);
    return ret;
  }

}
