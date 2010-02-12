/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.sssh;

import audio.ChunkOps;
import audio.cords.SoundIn;

import java.io.DataInput;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author дом
 */
public class ParsedSound {
  int chunkSize;
  short[] buf;
  List<double[]> freqMagnitudes = new ArrayList();
  List<Cut> cuts = new ArrayList();

    ParsedSound(int chunkSize, DataInput di){
        this(chunkSize, new DISoundIn(di));
    }

  ParsedSound(int chunkSize, SoundIn di){
    this.chunkSize = chunkSize;
    buf = new short[chunkSize];

    int i=0;
    try{
      for(; /*i<250*/; i++ ){
        final double[] freqMagI = nextFreqMags(di);
        freqMagnitudes.add(freqMagI);
        cuts.add(new Cut(i, freqMagI));
      }
    }catch(Exception e){
      //e.printStackTrace();
      System.out.println("ParsedSound stopped at block "+i);
    }

  }

  double[] nextFreqMags(SoundIn di){
      ChunkOps.readAll(di, buf);
      final double[] freqMagI = ChunkOps.freqMagnitudes(buf);
      return freqMagI;
  }

  List<double[]> rangeFreqMag(int start, int end){
      List<double[]> ret = new ArrayList();
      for( int i=start; i<end; i++ ){
          ret.add(cuts.get(i).freq);
      }
      return ret;
  }
}
