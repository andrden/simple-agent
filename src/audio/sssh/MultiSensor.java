package audio.sssh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Feb 23, 2010
 * Time: 10:30:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class MultiSensor {
    List<Seg> segs = new ArrayList<Seg>();

    public MultiSensor() {
        Random rSeg = new Random(1);
        for( int i=0; i<140; i++ ){
          int a1 = rSeg.nextInt(65);
          int b1 = Math.max(0, Math.min(64, a1 -15 + rSeg.nextInt(30)));
            int a2 = Math.min(64, a1 + rSeg.nextInt(10));
            int b2 = Math.min(64, b1 + rSeg.nextInt(10));
            Seg seg2 = new Seg(a1, a2,b1, b2);
          segs.add(seg2);
        }
    }

    double[] values(double[] freqMagnitudes){
        double[] v = new double[segs.size()];
        for( int i=0; i<v.length; i++ ){
            v[i] = segs.get(i).comp(freqMagnitudes);
        }
        return v;
    }

    double diff(double[] a, double[] b){
        double[] e = new double[a.length];
        for( int i=0; i<a.length; i++ ){
            e[i] = Math.abs(a[i]-b[i]);
        }
        Arrays.sort(e);
        return e[e.length/5];
    }
}
