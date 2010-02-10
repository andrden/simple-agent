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
    List<double[]> freqMagnitudes;
    Map<Integer,TreeClust> nodes = new HashMap();

    public TreeClust(List<double[]> freqMagnitudes) {
        this.freqMagnitudes = freqMagnitudes;
    }

    void process(){
        System.out.println("data size="+freqMagnitudes.size());
        seg = divide();
        if( seg==null ){
            return;
        }
        for( int i=0; i<=seg.clusters.size(); i++ ){
            nodes.put(i, new TreeClust(new ArrayList()));
        }
        for( double[] freqMagI : freqMagnitudes ){
            double c1 = seg.comp(freqMagI);
            int idx = seg.clusterIdx(c1);
            nodes.get(idx).freqMagnitudes.add(freqMagI);
        }
        for( int i=0; i<seg.clusters.size(); i++ ){
            TreeClust node = nodes.get(i);
            if( node.freqMagnitudes.size()>20 ){
              node.process();
            }
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
        //ShShCorrelate.displaySeg(best);
        return best;
    }

    double testSegQuality(Seg seg1, List<double[]> freqMagnitudes){
          //    System.out.println("seg1="+seg1.toString());

          for( double[] freqMagI : freqMagnitudes ){
            double c1 = seg1.comp(freqMagI);
            seg1.points.add(c1);
          }


        final int movingAvgSize=21;
        seg1.clusterSearch(100, movingAvgSize);
        if( seg1.clusters.size()==1 && seg1.clusters.get(0).scopeRatio>0.95 ){
            return 0; // all data just grouped together
        }
        double sum=0;
        for( Seg.Clast c : seg1.clusters ){
          sum += c.quality * (1-c.scopeRatio) * c.scopeRatio;
        }
        return sum;
    }


    @Override
    public String toString() {
        return "d="+freqMagnitudes.size();
    }
}
