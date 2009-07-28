package reinforcement;

import reinforcement.worlds.RState;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Jul 27, 2009
* Time: 4:27:30 PM
* To change this template use File | Settings | File Templates.
*/
class Model {
  Map<RState,Set<RState>> neighborStates = new HashMap<RState,Set<RState>>();

  Map<StAct,RState> nextSt = new HashMap<StAct,RState>();
  Map<StAct,Damper> rew = new HashMap<StAct,Damper>();
  void update(StAct sa, RState nextSt, double rew){
    RState nextSt0 = this.nextSt.get(sa);
//      if( nextSt0!=null && !nextSt0.equals(nextSt) ){
//        throw new IllegalArgumentException("stochastic not yet supported");
//      }
//      Double rew0 = this.rew.get(sa);
//      if( rew0!=null && !rew0.equals(rew) ){
//        throw new IllegalArgumentException("stochastic not yet supported");
//      }

    this.nextSt.put(sa, nextSt);

    Damper drew = this.rew.get(sa);
    if( drew==null ){
      drew = new Damper();
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
    if( nextSt.isEmpty() ){
      return null;
    }
    List<StAct> allStAct = new ArrayList<StAct>(nextSt.keySet());
    return allStAct.get((int)(Math.random() * allStAct.size()));
  }
  RState nextSt(StAct sa){
    return nextSt.get(sa);
  }
  double rew(StAct sa){
    Damper damper = rew.get(sa);
    double v = damper.value();
    return v;
  }
}
