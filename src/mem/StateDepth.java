package mem;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 31/1/2008
 * Time: 18:48:03
 * To change this template use File | Settings | File Templates.
 */
public class StateDepth {
  private Set<StateDepthElem> els = new HashSet<StateDepthElem>();
  final int maxDepth;

  public static final StateDepth[] TRACKABLES = new StateDepth[]{
    new StateDepth("C0"),
    new StateDepth("V0", "C0"),

    new StateDepth("C0", "C1"),
    new StateDepth("C0", "C1", "V1"),
    new StateDepth("C0", "V0","V1"),

    new StateDepth("C0","C1","C2","C3"),
    new StateDepth("C0", "V0","V1","V2"),
  };

  List<Set<StateDepthElem>> elsByDepth(){
    List<Set<StateDepthElem>> l = new ArrayList<Set<StateDepthElem>>();
    for( StateDepthElem sde : els ){
      while( l.size()<=sde.depth ){
        l.add(new HashSet<StateDepthElem>());
      }
      l.get(sde.depth).add(sde);
    }
    return l;
  }

  int maxDepth(){
    int max=0;
    for( StateDepthElem e : els ){
      if( e.depth>max ){
        max=e.depth;
      }
    }
    return max;
  }

  public boolean canUse(History h){
    if( maxDepth>h.list.size() ){
      return false;
    }
    return true;
  }

  public boolean contains(String elemAsString){
    return contains(new StateDepthElem(elemAsString));
  }

  boolean contains(StateDepthElem e){
    return els.contains(e);
  }

  StateDepth(String ... elemsAsStrings){
    for( String s : elemsAsStrings ){
      els.add(new StateDepthElem(s));
    }
    maxDepth = maxDepth();
  }

  StateDepth(StateDepthElem ... elems){
    els.addAll(Arrays.asList(elems));
    maxDepth = maxDepth();
  }

  public boolean noop(Hist h){
    Hist deepSide = getDeepSide(h);
    if( deepSide==null ){
      return false;
    }
    return h.next!=null && deepSide.viewMatch( h.next, false );
  }

  Hist getDeepSide(Hist curr){
    return curr.getAtDepth(maxDepth);
  }


  public String toString() {
    return els.toString();
  }


//  public Set<StateDepthElem> getEls() {
//    return els;
//  }
}
