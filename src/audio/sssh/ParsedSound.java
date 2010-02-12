/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.sssh;

import audio.ChunkOps;
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

  ParsedSound(int chunkSize, DataInputStream di){
    this.chunkSize = chunkSize;
    buf = new short[chunkSize];

    int i=0;
    try{
      for(; /*i<250*/; i++ ){
        ChunkOps.readAll(di, buf);
        
        final double[] freqMagI = ChunkOps.freqMagnitudes(buf);
        freqMagnitudes.add(freqMagI);
        cuts.add(new Cut(i, freqMagI));
      }
    }catch(Exception e){
      //e.printStackTrace();
      System.out.println("ParsedSound stopped at block "+i);
    }

  }
}
