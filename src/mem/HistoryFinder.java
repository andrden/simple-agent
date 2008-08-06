package mem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 13/6/2008
 * Time: 16:04:18
 */
public class HistoryFinder {

  Map<ViewDepthElem, HistoryFinder> children = new HashMap<ViewDepthElem, HistoryFinder>();

  List<Hist> foundHistList;

  public HistoryFinder() {
  }

  public HistoryFinder(List<Hist> histList) {
    this.foundHistList = histList;
  }

  public String toString() {
    return "hist=" + (foundHistList == null ? -1 : foundHistList.size())
            + " ch=" + children.size();
  }

  public List<Hist> findNext(History history, DeepState ds, ViewDepth vd) {
    if (vd.size() == 1) {
      List<Hist> found = history.find(ds);
      children.put(vd.getEls().iterator().next(), new HistoryFinder(found));
      return found;
    }

    // search by internal records only
    List<ViewDepthElem> els = new ArrayList<ViewDepthElem>(vd.getEls());
    return doFind(els, ds);
  }

  private List<Hist> doFind(List<ViewDepthElem> els, DeepState ds) {
    if (els.size() == 1) {
      List<Hist> list = findLocal(ds);
      children.put(els.get(0), new HistoryFinder(list));
      return list;
    }

    int minSize = Integer.MAX_VALUE;
    HistoryFinder hfUse = null;
    int iUse = -1;

    //System.out.println("hf---------");
    for (int i = 0; i < els.size(); i++) {
      ViewDepthElem vde = els.get(i);
      HistoryFinder hf = children.get(vde);
      int hfSize = hf.foundHistList.size();
      if (hfSize == 0) {
        return hf.foundHistList; // no need going deeper, nothing will be found
      }
      //System.out.println("hf "+hfSize+" "+vde);
      if (hfSize < minSize || (hfSize == minSize && hf.hashCode() < hfUse.hashCode() /*make search deterministic*/)) {
        minSize = hfSize;
        hfUse = hf;
        iUse = i;
      }
    }
    ViewDepthElem used = els.remove(iUse);
    //System.out.println("hf used; " +used);
    return hfUse.doFind(els, ds);
  }


  List<Hist> findLocal(DeepState d) {
    List<Hist> l = new ArrayList<Hist>();
    for (Hist h : foundHistList) {
      if (d.match(h)) {
        l.add(h);
      }
    }
    return l;
  }

}
