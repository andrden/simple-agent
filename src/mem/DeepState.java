package mem;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 31/1/2008
 * Time: 18:48:23
 * To change this template use File | Settings | File Templates.
 */
public class DeepState implements Serializable {
  private Map<ViewDepthElem, Object> data = new HashMap<ViewDepthElem, Object>();

  public boolean containsKeys(Collection<ViewDepthElem> keys) {
    return data.keySet().containsAll(keys);
  }

  private DeepState() {
  }

  public boolean hasElemsOtherThan(ViewDepthElem v) {
    for (ViewDepthElem vi : data.keySet()) {
      if (!vi.equals(v)) {
        return true;
      }
    }
    return false;
  }

  Set<ViewDepthElem> keySet() {
    return data.keySet();
  }

  public DeepState(Map<StateDepthElem, Object> gdata) {
    for (StateDepthElem e : gdata.keySet()) {
      if (e.viewOrCmd) {
        Map<String, Object> view = (Map<String, Object>) gdata.get(e);
        for (String s : view.keySet()) {
          data.put(new ViewDepthElem(e.depth, s), view.get(s));
        }
      } else {
        data.put(new ViewDepthElem(e.depth, Hist.CMD_KEY), gdata.get(e));
      }
    }
  }

  public boolean equalsDS(DeepState other) {
    return isSubsetOf(other) && data.size() == other.data.size();
  }

  public DeepState expandGroupCommands(Map<String, List<String>> cmdGroups) {
    List<Map<String, Object>> split = splitByDepth();
    for (int i = split.size() - 1; i >= 0; i--) {
      Map<String, Object> layer = split.get(i);
      for (String sde : layer.keySet()) {
        if (sde.equals(Hist.CMD_KEY)) {
          List<String> group = cmdGroups.get((String) layer.get(sde));
          if (group != null) {
            for (int j = 0; j < group.size(); j++) {
              if (j > 0) {
                split.add(i, new HashMap<String, Object>());
              }
              split.get(i).put(Hist.CMD_KEY, group.get(j));
            }
          }
        }
      }
    }


    DeepState m = new DeepState();
    for (int i = 0; i < split.size(); i++) {
      for (String e : split.get(i).keySet()) {
        m.data.put(new ViewDepthElem(i, e), split.get(i).get(e));
      }
    }
    return m;
  }

  public Map<String, Object> getElemsAtDepth(int depth) {
    List<Map<String, Object>> ls = splitByDepth();
    if (ls.size() <= depth) {
      return null;
    }
    return ls.get(depth);
  }

  public boolean hasElemsAtDepth(int depth) {
    List<Map<String, Object>> ls = splitByDepth();
    if (ls.size() <= depth) {
      return false;
    }
    return ls.get(depth).size() > 0;
  }

  List<Map<String, Object>> splitByDepth() {
    List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
    for (ViewDepthElem sde : data.keySet()) {
      while (sde.depth >= ret.size()) {
        ret.add(new HashMap<String, Object>());
      }
      ret.get(sde.depth).put(sde.key, data.get(sde));
    }
    return ret;
  }

  boolean isSubsetOf(DeepState other) {
    for (ViewDepthElem e : data.keySet()) {
      Object o = other.data.get(e);
      if (o == null) {
        return false;
      }
      if (!data.get(e).equals(o)) {
        return false;
      }
    }
    return true;
  }

  DeepState intersect(DeepState other) {
    Map<ViewDepthElem, Object> common = new HashMap<ViewDepthElem, Object>();
    for (ViewDepthElem e : data.keySet()) {
      if (other.data.containsKey(e)) {
        if (data.get(e).equals(other.data.get(e))) {
          common.put(e, data.get(e));
        }
      }
    }
    if (common.isEmpty()) {
      return null;
    }
    DeepState ds = new DeepState();
    ds.data = common;
    return ds;
  }

  DeepState cloneMe() {
    DeepState ds = new DeepState();
    ds.data.putAll(data);
    return ds;
  }

  public static List<DeepState> lookBehind(StateDepth depth, Hist h) {
    return lookBehind(new DeepState(new HashMap<StateDepthElem, Object>()), depth, h, 0);
  }

  public static List<DeepState> lookBehind(ViewDepth depth, Hist h) {
    return lookBehind(new DeepState(new HashMap<StateDepthElem, Object>()), depth, h, 0);
  }

  static boolean hasCmdRef(Set<StateDepthElem> s) {
    for (StateDepthElem e : s) {
      if (!e.viewOrCmd) {
        return true;
      }
    }
    return false;
  }

  public static List<DeepState> lookBehind(DeepState d, StateDepth depth, Hist h, int level) {
    // filling 'DeepState d' with relevant info
    List<Set<StateDepthElem>> elsByDepth = depth.elsByDepth();
    if (level >= elsByDepth.size()) {
      return Arrays.asList(d);
    }
    Set<StateDepthElem> s = elsByDepth.get(level);
//    if( level==0 && hasCmdRef(s) ){
//
//    }
//    List<DeepState> variants = new ArrayList<DeepState>();

    for (StateDepthElem e : s) {
      if (e.viewOrCmd) {
        Map<String, Object> m = h.getViewOnly();
        for (String k : m.keySet()) {
          d.data.put(new ViewDepthElem(e.depth, k), m.get(k));
        }
      } else {
        d.data.put(new ViewDepthElem(e.depth, Hist.CMD_KEY), h.getCommand());
      }
    }
    return lookBehind(d, depth, h.getAtDepth(1), level + 1);
  }

  public static List<DeepState> lookBehind(DeepState d, ViewDepth depth, Hist h, int level) {
    // filling 'DeepState d' with relevant info
    List<Set<ViewDepthElem>> elsByDepth = depth.elsByDepth();
    if (level >= elsByDepth.size()) {
      return Arrays.asList(d);
    }
    Set<ViewDepthElem> s = elsByDepth.get(level);
//    if( level==0 && hasCmdRef(s) ){
//
//    }
//    List<DeepState> variants = new ArrayList<DeepState>();

    for (ViewDepthElem e : s) {
      d.data.put(e, h.get(e.key));
    }
    Hist hDeeper = h.getAtDepth(1);
    if (hDeeper == null) {
      return Collections.emptyList();
    }
    return lookBehind(d, depth, hDeeper, level + 1);
  }

  public String toString() {
    return data.toString();
  }

  public DeepState intersect(Hist h) {
    Map<ViewDepthElem, Object> mres = new HashMap<ViewDepthElem, Object>();
    for (ViewDepthElem e : data.keySet()) {
      Hist hold = h.getAtDepth(e.depth);
      if (hold != null) {
        Object v = hold.get(e.key);
        if (v != null && v.equals(data.get(e))) {
          mres.put(e, v);
        }
      }
    }
    if (mres.isEmpty()) {
      return null;
    }
    DeepState dsRes = new DeepState();
    dsRes.data = mres;
    return dsRes;
  }

  boolean match(Hist h) {
    for (ViewDepthElem e : data.keySet()) {
      Hist hold = h.getAtDepth(e.depth);
      if (hold == null) {
        return false;
      }

      if (e.key.equals(Hist.CMD_KEY) && hold.getCommand() == null) {
        // testing temporary hist - no command means any command will do
        if (data.size() == 1) {
          return false; // depends only upon command, nosense
        }
      } else {
        Map<String, Object> view = new HashMap<String, Object>();
        view.put(e.key, data.get(e));
        if (!hold.viewMatch(view, true)) {
          return false;
        }
      }
    }

    return true;
  }
}
