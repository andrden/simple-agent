package mem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 31/1/2008
 * Time: 18:48:03
 * To change this template use File | Settings | File Templates.
 */
public class ViewDepth {
  private Set<ViewDepthElem> els = new HashSet<ViewDepthElem>();
  final int maxDepth;

  public int size(){
    return els.size();
  }


  public Set<ViewDepthElem> getEls() {
    return els;
  }

  List<Set<ViewDepthElem>> elsByDepth(){
    List<Set<ViewDepthElem>> l = new ArrayList<Set<ViewDepthElem>>();
    for( ViewDepthElem sde : els ){
      while( l.size()<=sde.depth ){
        l.add(new HashSet<ViewDepthElem>());
      }
      l.get(sde.depth).add(sde);
    }
    return l;
  }

  int maxDepth(){
    int max=0;
    for( ViewDepthElem e : els ){
      if( e.depth>max ){
        max=e.depth;
      }
    }
    return max;
  }

  public boolean canUse(History h){
    if( maxDepth>=h.list.size() ){
      return false;
    }
    return true;
  }

  boolean contains(ViewDepthElem e){
    return els.contains(e);
  }

  boolean contains(ViewDepth e){
    return els.containsAll(e.els);
  }

  ViewDepth(ViewDepthElem ... elems){
    els.addAll(Arrays.asList(elems));
    maxDepth = maxDepth();
  }

  Hist getDeepSide(Hist curr){
    return curr.getAtDepth(maxDepth);
  }


  public String toString() {
    return els.toString();
  }

}
