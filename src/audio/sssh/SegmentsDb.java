package audio.sssh;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Feb 1, 2010
 * Time: 4:59:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class SegmentsDb {
  class Sg{
    int col;
    int val;
    int start=0;
    int end;

    Sg(int col, int val, int start) {
      this.col = col;
      this.val = val;
      this.start = start;
    }

    @Override
    public String toString() {
      return "col="+col+" v="+ val + " "+start+".."+end;
    }
  }

  Map<Integer,Sg> curr = new HashMap<Integer, Sg>();
  TreeMap<Integer, List<Sg>> all = new TreeMap<Integer, List<Sg>>();

  void add(int row, int col, int val){
    Sg s = curr.get(col);
    if( s==null ){
      s = new Sg(col, val, row);
      curr.put(col, s);
    }
    if( s.val!=val ){
      s.end=row-1;
      curr.remove(col);
      if( s.end-s.start>5 ){
        //System.out.println("col "+col+": "+s);
        addAll(s);
      }
    }
  }

  List<Sg> find(int from, int to){
    List<Sg> ret = new ArrayList<Sg>();
    for( List<Sg> li : all.subMap(from, true, to, true).values() ){
      ret.addAll(li);
    }
    return ret;
  }

  void findMatches(){
    for( Sg s : find(all.firstKey(), all.lastKey()) ){
      int delta = (s.end-s.start)/10;
      for( Sg si : find(s.start-delta, s.start+delta) ){
        if( System.identityHashCode(si)<System.identityHashCode(s)
           && si.end <= s.end+delta && si.end >= s.end-delta ){
          System.out.println(s.toString()+" "+si.toString());
        }
      }
    }
  }

  void addAll(Sg s){
    List<Sg> l = all.get(s.start);
    if( l==null ){
      l=new ArrayList<Sg>();
      all.put(s.start, l);
    }
    l.add(s);
  }
}
