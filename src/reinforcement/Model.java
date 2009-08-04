package reinforcement;

import reinforcement.worlds.RState;

import java.util.*;

import com.pmstation.common.utils.CountingMap;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Jul 27, 2009
* Time: 4:27:30 PM
* To change this template use File | Settings | File Templates.
*/
class Model {
  Map<RState,Set<RState>> neighborStates = new HashMap<RState,Set<RState>>();

  //Map<StAct,RState> nextSt = new HashMap<StAct,RState>();
  Map<StAct,Damper> rew = new HashMap<StAct,Damper>();

  Map<StAct, CountingMap<RState>> nextStList = new HashMap<StAct,CountingMap<RState>>();
  List<StAct> allStAct = new ArrayList<StAct>();

  void update(StAct sa, RState nextSt, double rew){
    CountingMap<RState> cm = nextStList.get(sa);
    if( cm==null ){
      cm = new CountingMap<RState>();
      nextStList.put(sa, cm);
      allStAct.add(sa);
    }
    cm.increment(nextSt);

    //RState nextSt0 = this.nextSt.get(sa);
//      if( nextSt0!=null && !nextSt0.equals(nextSt) ){
//        throw new IllegalArgumentException("stochastic not yet supported");
//      }
//      Double rew0 = this.rew.get(sa);
//      if( rew0!=null && !rew0.equals(rew) ){
//        throw new IllegalArgumentException("stochastic not yet supported");
//      }

    //this.nextSt.put(sa, nextSt);

    Damper drew = this.rew.get(sa);
    if( drew==null ){
      drew = new DamperAvg();
      this.rew.put(sa, drew);
    }
    drew.add(rew);
    addNeighbor(sa.s, nextSt);
    addNeighbor(nextSt, sa.s);
  }

  Set<RState> getNeighbors(RState s){
    Set<RState> ss = neighborStates.get(s);
    if( ss==null ){
      return Collections.emptySet();
    }
    return ss;
  }

  void addNeighbor(RState s, RState nei){
    Set<RState> ss = neighborStates.get(s);
    if( ss==null ){
      ss = new HashSet<RState>();
      neighborStates.put(s, ss);
    }
    ss.add(nei);
  }

  StAct randomStAct(){
    if( allStAct.isEmpty() ){
      return null;
    }
    return allStAct.get((int)(Math.random() * allStAct.size()));
  }
//  RState nextSt(StAct sa){
//    return nextSt.get(sa);
//  }

  Map<RState,Double> nextSt(StAct sa){
    CountingMap<RState> cmap = nextStList.get(sa);
    if( cmap==null ){
      return null;
    }
    double all=cmap.syncTotalCount();
    Map<RState,Double> ret = new HashMap<RState,Double>();
    for( RState s : cmap.keySet() ){
      ret.put(s, cmap.get(s)/all);
    }
    return ret;
  }

  double rew(StAct sa){
    Damper damper = rew.get(sa);
    if( damper==null ){
      return 0;
    }
    double v = damper.value();
    return v;
  }
}
