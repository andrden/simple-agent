package logic;

import mem.*;

import java.util.*;
import java.io.Serializable;

import com.pmstation.common.utils.CountingMap;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 18/4/2008
 * Time: 18:19:41
 */
public class Attempts implements Serializable {
  Alg alg;

  Causes causes;
  History history;

  public Attempts(Alg alg) {
    this.alg = alg;
    causes=alg.causes;
    history=alg.history;
  }
  void log(String s){
    alg.log(s);
  }

  CmdSet findNotEverTried(ViewDepthIterator vdi, List<String> cs, Map<String, Object> view){
    HistoryFinder hf = new HistoryFinder();
    for(;;){
      ViewDepth vd = vdi.next();
      if( vd==null ){
        break;
      }
      if( !vd.canUse(history) ){
        return new CmdSet(cs.get(0));
        //continue;
      }
      for( String c : cs ){
        DeepState ds = DeepState.lookBehind(vd, new Hist(history.last, view, c)).get(0);
        ds = ds.expandGroupCommands(alg.cmdGroups);
        //if( !history.exists(ds) ){
        if( hf.findNext(history, ds, vd).isEmpty() ){
          String s = "random - not ever tried " + c + " in " + ds;
          //log(s);
          CmdSet cset = new CmdSet(c);
          cset.setFoundFrom(s);
          return cset;
        }
      }
    }
    return null;
  }

  Set<ViewDepthElem> viewOnly(Set<ViewDepthElem> s){
    Set<ViewDepthElem> res = new HashSet<ViewDepthElem>();
    for( ViewDepthElem e : s ){
      if( e.isView() ){
        res.add(e);
      }
    }
    return res;
  }

  CmdSet randomWithIntent(List<String> cs, Map<String, Object> view) {
    if( cs.size()==1 ){
      return new CmdSet(cs.get(0));
    }

    cs = new ArrayList<String>(cs);
    Collections.shuffle(cs); // no prejudice to commands

    Set<ViewDepthElem> usedCauseElemts = causes.usedCauseElems();
    usedCauseElemts = viewOnly(usedCauseElemts);
    if( !usedCauseElemts.isEmpty() ){
      // first try already usable elems - they are the first candidates to cover new situation
      CmdSet cset1 = findNotEverTried( ViewDepthGenerator.createWithViewElelms(usedCauseElemts), cs, view);
      if( cset1!=null ){
        return cset1;
      }
    }

    // try situations not ever tried before
    // includes trying all different commands
    ViewDepthIterator vdi = new ViewDepthGenerator(view.keySet());
    CmdSet cset1 = findNotEverTried(vdi, cs, view);
    if( cset1!=null ){
      return cset1;
    }

    CountingMap<List> histCmds2 = history.groupCommands(1);
    for( String s : cs ){
      for( String sPrev : cs ){
        if( !histCmds2.containsKey(Arrays.asList(sPrev, s)) ){
          String msg = "not ever tried cmd pair: " + sPrev + " " + s;
          //log(msg);
          CmdSet cset = new CmdSet(sPrev, s);
          cset.setFoundFrom(msg);
          return cset;
        }
      }
    }


    // random to verify causes
    if( Math.random()<0.7 ){ // always leave some attempts as truly random
      for( Cause c : causes.validCauses() ){
        //....
      }
    }

    String nextCmd;
    nextCmd = cs.get((int)(Math.random()*cs.size()));
    String msg = "plain random";
    log(msg);
    CmdSet cset = new CmdSet(nextCmd);
    cset.setFoundFrom(msg);
    return cset;
  }

}
