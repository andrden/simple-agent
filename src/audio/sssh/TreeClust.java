package audio.sssh;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Feb 9, 2010
 * Time: 6:21:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class TreeClust {
    Seg seg;
    List<Cut> freqMagnitudes;
    Map<Integer,TreeClust> nodes = new HashMap();

    public TreeClust(List<Cut> freqMagnitudes) {
        this.freqMagnitudes = freqMagnitudes;
    }

    void printFoundGroups(String nesting){
        for( TreeClust n : nodes.values() ){
            if( n.nodes.size()==0 && n.freqMagnitudes.size()>20 ){
                System.out.println(nesting+seg+" grp-sz="+n.freqMagnitudes.size()+" "+n.cutIntervals());
            }
            n.printFoundGroups(nesting+" ");
        }
    }

    String cutIntervals(){
        StringBuilder res = new StringBuilder();
        NavigableSet<Integer> s = new TreeSet();
        for( Cut c : freqMagnitudes ){
            s.add(c.id);
        }
        while(!s.isEmpty()){
            int a = s.first();
            int b=a;
            while(s.contains(b+1)){
                b++;
            }
            res.append(" "+a+"-"+b);
            s = s.tailSet(b+1, true);
        }
        return res.toString();
    }

    void processOneStep(Seg s){
        seg = s;
        clusterIt(seg, freqMagnitudes);
        separate();
        printFoundGroups("");
    }

    void process(){
        System.out.println("data size="+freqMagnitudes.size());
        seg = divide();
        if( seg.clusters.size()<2 ){
            return;
        }
        separate();
        for( int i=0; i<seg.clusters.size(); i++ ){
            TreeClust node = nodes.get(i);
            if( node.freqMagnitudes.size()>20 ){
              node.process();
            }
        }
    }

    private void separate() {
        for( int i=0; i<=seg.clusters.size(); i++ ){
            nodes.put(i, new TreeClust(new ArrayList()));
        }
        for( Cut freqMagI : freqMagnitudes ){
            double c1 = seg.comp(freqMagI.freq);
            int idx = seg.clusterIdx(c1);
            nodes.get(idx).freqMagnitudes.add(freqMagI);
        }
    }

    Seg divide(){
        List<Seg> trySegs = new ArrayList();
        final Map<Seg,Double> q = new HashMap();

        for(int i=0; i<100; i++){
          Random r = new Random();
          Seg seg1 = new Seg(r.nextInt(65),r.nextInt(65),r.nextInt(65),r.nextInt(65));
          trySegs.add(seg1);
          q.put(seg1, testSegQuality(seg1, freqMagnitudes));
        }

        Collections.sort( trySegs, new Comparator<Seg>() {
            public int compare(Seg o1, Seg o2) {
                return q.get(o2).compareTo(q.get(o1));
            }
        });

        Seg best = trySegs.get(0);
        System.out.println("best="+best+" q="+q.get(best));
        //ShShCorrelate.displaySeg(best);
        return best;
    }

    double testSegQuality(Seg seg1, List<Cut> freqMagnitudes){
          //    System.out.println("seg1="+seg1.toString());

        clusterIt(seg1, freqMagnitudes);
        if( seg1.clusters.size()==1 && seg1.clusters.get(0).scopeRatio>0.95 ){
            return 0; // all data just grouped together
        }
        double sum=0;
        for( Seg.Clast c : seg1.clusters ){
          sum += c.quality * (1-c.scopeRatio) * c.scopeRatio;
        }
        return sum;
    }

    private void clusterIt(Seg seg1, List<Cut> freqMagnitudes) {
        for( Cut freqMagI : freqMagnitudes ){
          double c1 = seg1.comp(freqMagI.freq);
          seg1.points.add(c1);
        }


        final int movingAvgSize=21;
        seg1.clusterSearch(100, movingAvgSize);
    }


    @Override
    public String toString() {
        return "d="+freqMagnitudes.size();
    }
}
