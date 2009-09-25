package audio;

import kjdss.KJFFT;
import utils.Utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

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
    
    for( int i=0; i<50; i++ ){
      double[] ch = ai.nextChunkDouble();


      DFT dft = new DFT();
      dft.meanStdDevRunning(ch);
      dft.forward(ch);

      Graph g = new Graph();
      //g.show(dft.mag);
      g.show(ch);

      System.out.printf("stddev %f \n",  dft.stdDev);
      //ai.play(ch, 20);
      dfts.add(dft);
    }
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
