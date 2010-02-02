package audio.sssh;

import com.pmstation.common.utils.MinMaxFinder;

import java.util.*;

import utils.Utils;

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
    Bucket bucket;

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
  class Bucket{
    List<Sg> list = new ArrayList<Sg>();

    void add(Sg s){
      for( Sg si : list ){
        if( s==si ){
          return;
        }
      }
      list.add(s);
      s.bucket=this;
    }

    Entity asEntity(){
      Entity e = new Entity();
      for( Sg s : list ){
        e.colVals.put(s.col, s.val);
      }
      return e;
    }

    @Override
    public String toString() {
      MinMaxFinder a = new MinMaxFinder();
      MinMaxFinder b = new MinMaxFinder();
      for( Sg s : list ){
        a.add(s.start,"");
        b.add(s.end,"");
      }
      return (int)a.getMinVal()+".."+(int)a.getMaxVal()+" to "
          +(int)b.getMinVal()+".."+(int)b.getMaxVal()
          +  " size="+list.size();
    }
  }
  class Entity{
    Map<Integer,Integer> colVals = new HashMap<Integer,Integer>();

    @Override
    public String toString() {
      return colVals.toString();
    }

    boolean contains(Entity e){
      return colVals.entrySet().containsAll(e.colVals.entrySet());
    }
  }

  Map<Integer,Sg> curr = new HashMap<Integer, Sg>();
  TreeMap<Integer, List<Sg>> all = new TreeMap<Integer, List<Sg>>();
  List<Bucket> buckets = new ArrayList<Bucket>();
  Map<String,Entity> entities = new HashMap<String,Entity>();

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
        if( si!=s
           && si.end <= s.end+delta && si.end >= s.end-delta ){
          if( s.bucket!=null && si.bucket!=null && s.bucket!=si.bucket ){
            //tils.breakPoint();
            buckets.remove(si.bucket);
            for( Sg sj : si.bucket.list ){
              sj.bucket=null;
              s.bucket.add(sj);
            }
          }
          Bucket b = s.bucket==null ? si.bucket : s.bucket;
          if( b==null ){
            b = new Bucket();
            buckets.add(b);
          }
          b.add(s);
          b.add(si);
          //System.out.println(s.toString()+" "+si.toString());
        }
      }
    }

    for( Bucket b : buckets ){
      Entity e = b.asEntity();
      boolean rpt=false;
      if( entities.containsKey(e.toString()) ){
        rpt=true;
      }else{
        entities.put(e.toString(), e);
      }
      System.out.println(b+" "+(rpt?" *":"")+ " "+e);
    }

    for( Entity e : entities.values() ){
      List<Bucket> blist = new ArrayList<Bucket>();
      for(Bucket b : buckets ){
        if( b.asEntity().contains(e) ){
          blist.add(b);
        }
      }
      if( blist.size()>1 ){
        System.out.println(e+" count="+blist.size());
        for( Bucket b : blist ){
          System.out.println("    "+b);
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
