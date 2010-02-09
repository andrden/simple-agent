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
            nodes.get(i).process();
        }
    }

    Seg divide(){
        for(int i=0; i<100; i++){
          Random r = new Random();
          Seg seg1 = new Seg(r.nextInt(65),r.nextInt(65),r.nextInt(65),r.nextInt(65));

          if( ShShCorrelate.testSegQuality(seg1, freqMagnitudes) ){
              return seg1;
          }
        }
        return null;
    }

    @Override
    public String toString() {
        return "d="+freqMagnitudes.size();
    }
}
