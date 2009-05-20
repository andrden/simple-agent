package contin.alg;

import contin.Command;

import java.util.*;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: May 18, 2009
 * Time: 7:11:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContinAlg {
  List<String> commands;
  List<Hist> history = new ArrayList<Hist>();

  public ContinAlg(List<String> commands) {
    this.commands = commands;
  }

  List<Hist> closestHist(final Hist h){
      List<Hist> r = new ArrayList<Hist>(history);
      Collections.sort(r, new Comparator<Hist>(){
          public int compare(Hist o1, Hist o2) {
              return new Double(h.distance(o1)).compareTo(h.distance(o2));
          }
      });
      return r;
  }

  public Command suggestCmd(){
    if(!history.isEmpty()){
        Hist hlast = history.get(history.size() - 1);
        List<Hist> clos = closestHist(hlast);
        Set<Command> currBadCommands = new HashSet<Command>();
        for( Hist h : clos ){
            Hist n = h.next;
            if(n==null){
                continue;
            }
            if( n.deltaSenses<0 ){
                currBadCommands.add(n.cmd);
            }
            if( n.deltaSenses>0 && !currBadCommands.contains(n.cmd) ){
                return n.cmd;
            }
        }
        //Utils.breakPoint();
    }
      //return new Command(Utils.rnd(commands), 1);
    return new Command(Utils.rnd(commands), 2*Math.random()-1);
  }

  public void cmdExecuted(Command cmd, Map<String, Object> newView,
      Map<String, Double> newSenses){
      Hist h = new Hist(cmd, newView, newSenses);
      if(!history.isEmpty()){
          Hist hlast = history.get(history.size() - 1);
          hlast.next=h;
          h.deltaSenses=h.sumSenses-hlast.sumSenses;
      }
      history.add(h);
//    simplest generic adaptive way: find closest positive event in history
//    and execute the same command as old one!
  }

  static class Hist{
      Command cmd;
      Map<String, Object> newView;
      Map<String, Double> newSenses;
      double sumSenses;
      double deltaSenses;
      Hist next;


      public String toString() {
          return cmd+" "+newSenses;
      }

      public Hist(Command cmd, Map<String, Object> newView, Map<String, Double> newSenses) {
          this.cmd = cmd;
          this.newView = newView;
          this.newSenses = newSenses;
          for( double v : newSenses.values() ){
              sumSenses += v;
          }
      }

      double distance(Hist h){
          double r=0;
          for( String s : newSenses.keySet() ){
              r += Math.abs(newSenses.get(s)-h.newSenses.get(s));
          }
          return r;
      }
  }
}
