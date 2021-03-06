package audio;

import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;

import edu.princeton.cs.FFT;
import edu.princeton.cs.Complex;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 21, 2009
 * Time: 12:28:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class FFTTest {
  public static void main(String[] args){
    //new FFTTest().doit();
    //new FFTTest().doit2();
    new FFTTest().doit3();
  }

  private void doit3(){
//D:\proj\4shared\bin\video\windows>
// ffmpeg -i rec0920-142804.mp3 -ac 1 -acodec pcm_u8  aou.wav    

    File fwav = new File("D:\\proj\\4shared\\bin\\video\\windows\\aou.wav");
    //int chunk = 1024;
    int chunk = 1024*2;
    AudioIn ai = new AudioIn(fwav, chunk);
    List<DFT> dfts = new ArrayList<DFT>();
//    for( int i=0; i<20; i++ ){
//      double[] ch = ai.nextChunkDouble();
//    }

    ChunkOps chAvg = new ChunkOps();
    for( int i=0; i<50/*280*/; i++ ){
      double[] ch = ai.nextChunkDouble();
      ch = ChunkOps.hammingWindow(ch);

      DFT dft = new DFT();
      dft.meanStdDevRunning(ch);
      dft.forward(ch);
      

      chAvg.add(dft.mag);

      Graph g = new Graph();
      double[] magMod = dft.mag.clone();
      Arrays.fill(magMod, 0, 20/*up to 100hz*/, 0d);
      //g.show(magMod);
      //g.show(ch);

      System.out.printf("i=%d stddev %f  sum2=%f \n",  i, dft.stdDev, ChunkOps.sumSquared(ch));
      //ch = DFT.reconstruct(dft.mag,  null);
      //ai.play(ch, 5);
      dfts.add(dft);
    }

    Graph g = new Graph();
    double[] magShow = chAvg.avg().clone();
    g.show( ChunkOps.toDecibels(magShow) );
    Arrays.fill(magShow, 0, 20/*up to 100hz*/, 0d);
    g.show( ChunkOps.toDecibels(magShow) );
    //g.show(ChunkOps.first(chAvg.avg(),256));

//    for( int i=0; i<chunk/2+1; i++ ){
//      for( DFT d : dfts ){
//        System.out.printf("%4.0f ",d.mag[i]);
//      }
//      System.out.println(" freq="+(i*ai.sampleRate/chunk));
//    }

//    double[] rec = dft.reconstruct();
//    for( int i=0; i<dft.mag.length; i++ ){
//      if( dft.mag[i]>100 ){
//        System.out.println(i+" "+dft.mag[i]+ " freq="+(i*8000/chunk));
//      }
//    }
    Utils.breakPoint();
  }



  private void doit2(){
    File fwav = new File("c:/tmp/precise_dial_tone.wav"); // 350 Hz and 440 Hz
    int chunk = 8192;
    AudioIn ai = new AudioIn(fwav, chunk);
    double[] ch = ai.nextChunkDouble();
    DFT dft = new DFT();
    dft.forward(ch);
    double[] rec = dft.reconstruct();
    for( int i=0; i<dft.mag.length; i++ ){
      if( dft.mag[i]>100 ){
        System.out.println(i+" "+dft.mag[i]+ " freq="+(i*8000/chunk));
      }
    }
    Utils.breakPoint();
  }

  private void doit() {
    //KJFFT fft = new KJFFT(8);
    float[] sam = new float[]{0,1,0,0,0,0,0,5};
    //offset0(sam);
    //float[] ff = fft.calculate(sam);

    double[] dsam = new double[sam.length];
    for( int i=0; i<sam.length; i++ ){
      dsam[i]=sam[i];
    }
    DFT dft = new DFT();
    dft.forward(dsam);
    double[] rec = dft.reconstruct();
    Utils.breakPoint();
  }

  void offset0(float[] d){
    float s = 0;
    for( int i=0; i<d.length; i++ ){
      s += d[i];
    }
    s /= d.length;
    for( int i=0; i<d.length; i++ ){
      d[i] -= s;
    }
  }

//  float[] ffRestore(KJFFT fft){
//    fft.getXre()
//    for( int i=0; i<)
//  }
}
