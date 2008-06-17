package mem;

import com.pmstation.common.utils.CountingMap;

import java.util.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 29/1/2008
 * Time: 16:05:33
 * To change this template use File | Settings | File Templates.
 */
public class History implements Serializable {
  List<Hist> list = new ArrayList<Hist>();
  public Hist last;
  long nextOrder=0;

  Map<String,Object> lastResult = new HashMap<String,Object>();

  public int getResult(Hist h){
    if( last==h ){
      return (Integer)lastResult.get(Hist.RES_KEY);
    }
    return h.getResultFromNext();
  }

  public Hist createNextHist(){
    Map<String,Object> nextViewAll = getNextViewAll(last);
    int res = (Integer)nextViewAll.get(Hist.RES_KEY);
    Hist hnext = new Hist(last, nextViewAll, null);
    hnext.setResult(res);
    return hnext;
  }

  public Map<String,Object> getNextViewAll(Hist h){
    if( last==h ){
      return new HashMap<String,Object>(lastResult);
    }
    return h.next.getViewAll();
  }

  public void add(Hist h){
    h.order = ++nextOrder;
    list.add(h);
    if( last!=null ){
      last.next=h;
      h.prev=last;
      h.setResult((Integer)lastResult.get(Hist.RES_KEY));
      //System.out.println("clear in add");
      lastResult.clear();
    }
    last=h;
  }

  public void setLastResult(int result, Map<String,Object> view){
    //System.out.println("clear in setLast");
    lastResult.clear();
    lastResult.putAll(view);
    lastResult.put(Hist.RES_KEY, new Integer(result));
  }

  public List<Hist> find(DeepState d){
    List<Hist> l = new ArrayList<Hist>();
    for( Hist h : list ){
      if( d.match(h) ){
        l.add(h);
      }
    }
    return l;
  }

  public boolean exists(DeepState d){
    for( Hist h : list ){
      if( d.match(h) ){
        return true;
      }
    }
    return false;
  }

  // {RF=27, LN=1, LL=2, RN=2, NR=2, LR=10, RL=12, NN=3, NF=2, RR=48, FR=29, FN=1, FL=6, FF=17, LF=7}

  // recent:
  // {RR=18, FR=11, RF=11, FF=10, LF=5, FL=5, RL=4, LR=3, LL=2}

  // LR RL LLLL RRRR - removed:
  // {FL=121, LF=115, LL=109, RF=83, FR=77, RR=63, FF=43, LN=10, RN=5, NR=9, RL=11, LR=13, NN=3, NF=4, FN=3, NL=6}
//  public CountingMap<String> groupCommands(){
//    CountingMap<String> c = new CountingMap<String>();
//    for( Hist h : list ){
//      if( h.next!=null ){
//        c.increment(h.getCommand() + h.next.getCommand());
//      }
//    }
//    return c;
//  }

  public CountingMap<List> groupCommands(int level){
    CountingMap<List> c = new CountingMap<List>();
    for( Hist h : list ){
      List l = new ArrayList();
      for( int i=0; i<=level; i++){
        Hist hi = h.next(i);
        if( hi==null ){
          break;
        }
        l.add(hi.getCommand());
      }
      if( l.size()!=level+1 ){
        continue;
      }
      c.increment(l);
    }
    return c;
  }

  public List<Hist> findFaFb(){
    Map<StateDepthElem, Object> data = new HashMap<StateDepthElem, Object>();
    data.put(new StateDepthElem("C0"), "Fb");
    data.put(new StateDepthElem("C1"), "Fa");
    return find(new DeepState(data));
  }

  public void print(int depth){
    Hist h = last;
    for( int i=0; i<depth && h!=null; i++ ){
      System.out.println(h);
      h=h.prev;
    }
  }

  private static final long serialVersionUID = -5995303277916958873L;
}
