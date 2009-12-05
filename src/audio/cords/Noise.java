package audio.cords;

import cern.jet.random.engine.MersenneTwister64;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Dec 3, 2009
* Time: 4:21:37 PM
* To change this template use File | Settings | File Templates.
*/
class Noise implements SoundIn{
    short[] buf = new short[11025*4*15/7];
    int globalIdx=0;
    int idx=-1;
    Noise(){
        //Random rnd = new Random(System.nanoTime());
      MersenneTwister64 rnd64 = new MersenneTwister64(new java.util.Date());
        for( int i=0; i<buf.length; i++ ){
//              double s = 0;
//              for( int j=0; j<12; j++ ){
//                  s+=Math.random();
//              }
//              buf[i] = (short)(s/12*25000);

          //buf[i] = (short)(rnd.nextGaussian()*25000);

            double s = 0;
            for( int j=0; j<12; j++ ){
                s+=rnd64.nextDouble();
            }
            buf[i] = (short)(s/12*25000);
        }
    }
    public short next(){
       idx = (idx+1)%buf.length;
       globalIdx++;
       double dsin=0;
       if( globalIdx/(11025*4*4) % 2 == 0 ){
           dsin = 150*Math.sin(2*Math.PI*globalIdx*350/(11025*4));
       }
       return (short)(buf[idx] + dsin);
    }
}
