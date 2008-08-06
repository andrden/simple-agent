package mem;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class Hist extends OneView<Hist> {
  public static final String CMD_KEY = "!";
  public static final String RES_KEY = "$";
  public static final String NOOP_KEY = "noop";


  //public Hist next;
  //public Hist prev;
  long order;

  public Hist() {

  }

  public int getResultFromNext() {
    Integer i = (Integer) next.view.get(RES_KEY);
    if (i == null) {
      return 0;
    }
    return i.intValue();
  }


  public String toString() {
    return "#" + order + " " + view;
  }

  public static Integer getResult(Map<String, Object> fullView) {
    return (Integer) fullView.get(Hist.RES_KEY);
  }

  public Hist(Hist prevHistForProxy, Map<String, Object> view, String command) {
    if (view != null) {
      setView(view);
    }
    if (command != null) {
      setCommand(command);
    }
    prev = prevHistForProxy;
  }

  public void setView(Map<String, Object> view) {
    this.view = new HashMap<String, Object>(view);
    setResult(0);
  }

  public void setCommand(String command) {
    view.put(CMD_KEY, command);
  }

  public void setResult(int result) {
    view.put(RES_KEY, new Integer(result));
  }


  public Map<String, Object> getViewAllWithoutCmd() {
    Map<String, Object> ret = new HashMap<String, Object>(view);
    ret.remove(Hist.CMD_KEY);
    return ret;
  }

  public Map<String, Object> getViewOnly() {
    Map<String, Object> ret = new HashMap<String, Object>(view);
    ret.remove(CMD_KEY);
    ret.remove(RES_KEY);
    return ret;
  }

  public boolean viewMatch(Map<String, Object> v, boolean withCmd) {
    for (String k : v.keySet()) {
      if (RES_KEY.equals(k)) {
        continue;
      }
      if (!withCmd && CMD_KEY.equals(k)) {
        continue;
      }
      if (!view.containsKey(k)) {
        return false;
      }
      if (!view.get(k).equals(v.get(k))) {
        return false;
      }
    }
    return true;
  }

  public boolean viewMatch(Hist h, boolean withCmd) {
    Map<String, Object> v = h.view;
    return viewMatch(v, withCmd);
  }

  public String getCommand() {
    return (String) view.get(CMD_KEY);
  }

  Hist next(int level) {
    Hist ret = this;
    for (int i = 0; i < level; i++) {
      if (ret.next == null) {
        return null;
      }
      ret = ret.next;
    }
    return ret;
  }

  public Hist getAtDepth(int d) {
    Hist h = this;
    for (int i = 0; i < d; i++) {
      h = h.prev;
      if (h == null) {
        return h;
      }
    }
    return h;
  }

  private static final long serialVersionUID = 3105379650975415123L;

}
