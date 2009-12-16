package audio.cords;

import edu.princeton.cs.Complex;
import edu.princeton.cs.FFT;
import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Dec 15, 2009
 * Time: 6:53:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class Filter {
  public static void main(String[] args){
    //fftTest();

    double[] desirFreq = new double[65];
    desirFreq[8]=1;
    desirFreq[9]=1;
    double[] kern = filterKernel(desirFreq, 41);
    Utils.breakPoint();
  }

  private static void fftTest() {
    Complex[] c = new Complex[]{
        new Complex(1,0),new Complex(2,0),
        new Complex(3,0),new Complex(4,0),
        new Complex(5,0),new Complex(6,0),
        new Complex(7,0),new Complex(8,0),};
    Complex[] freq = FFT.fft(c);
    Utils.breakPoint();
  }

  private static double[] filterKernel(double[] desirFreq, int len) {
    Complex[] chCompl = new Complex[(desirFreq.length-1)*2];
    for( int j=0; j<desirFreq.length; j++ ){
      chCompl[j] = new Complex(desirFreq[j],0);
    }
    for( int j=1; j<desirFreq.length; j++ ){
      chCompl[chCompl.length-j] = new Complex(desirFreq[j],0);
    }
    Complex[] timeCompl = FFT.ifft(chCompl);

    double[] ret
    System.gc();
  }
}
