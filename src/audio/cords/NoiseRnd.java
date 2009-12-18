package audio.cords;

import cern.jet.random.engine.MersenneTwister64;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import audio.ChunkOps;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Dec 3, 2009
* Time: 4:21:37 PM
* To change this template use File | Settings | File Templates.
*/
class NoiseRnd implements SoundIn{
  MersenneTwister64 rnd64 = new MersenneTwister64(new java.util.Date());

  public short next(){
    double s = 0;
    for( int j=0; j<12; j++ ){
        s+=rnd64.nextDouble();
    }
    return (short)(s/12*25000);
  }

  short[] next(int len){
    short[] r = new short[len];
    for( int i=0; i<len; i++ ){
      r[i] = next();
    }
    return r;
  }

  public static void main(String[] args) throws Exception{
      int sampleRate = 11025*4;
      SourceDataLine line =
              AudioSystem.getSourceDataLine(new AudioFormat(sampleRate,16,1,true,true));
      line.open();
      line.start();
      SoundIn noise = new NoiseRnd();

//    double[] desirFreq = new double[65];
//    for( int i=10; i<40; i++ ){
//      desirFreq[i]=i;
//    }

    //режем на большие куски, чтобы получить ШШШШ ?

      ShShCorrelate shsh = new ShShCorrelate(4096);
      //double[] desirFreq = shsh.freqMagnitudes(shsh.soundBufAt(257));
      //double[] desirFreq = shsh.freqMagnitudes(shsh.soundBufAt(405));
      double[] desirFreq = shsh.freqMagnitudes(shsh.soundBufAt(250/8/4));
//      int avg=15;
//      for( int i=1; i<avg; i++ ){
//        desirFreq = ChunkOps.add(desirFreq, shsh.freqMagnitudes(shsh.soundBufAt(250+i)));
//      }
//      desirFreq = ChunkOps.div(desirFreq, avg);

      noise = new Filter(noise, desirFreq, 2001, 0.01);

      for( ;; ){
          ByteArrayOutputStream ba = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(ba);
          for( int i=0; i<1000; i++ ){
               dos.writeShort(noise.next());
          }
          line.write(ba.toByteArray(), 0, ba.size());

      }
  }

}