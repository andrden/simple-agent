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
public class Filter implements SoundIn{
  double[] kern;
  SoundIn base;
  short[] baseBuf;
  int baseBufPos=0;
  public Filter(SoundIn base, double[] desirFreq, int kernelLen, double scaleTarget){
    this.base=base;
    this.kern = filterKernel(desirFreq, kernelLen);
    kernelScale(this.kern, scaleTarget);
    baseBuf = new short[kernelLen];
  }

  public short next() {
    baseBuf[baseBufPos] = base.next();
    double sum=0;
    for( int i=0; i<kern.length; i++ ){
      sum += kern[i]*baseBuf[ (baseBufPos-i+baseBuf.length)%baseBuf.length ];
    }
    baseBufPos = (baseBufPos+1)%baseBuf.length;
    if( sum>Short.MAX_VALUE ){
      sum=Short.MAX_VALUE;
    }
    if( sum<Short.MIN_VALUE ){
      sum = Short.MIN_VALUE;
    }
    return (short)sum;
  }

  public static void main(String[] args){
    //fftTest();

    double[] desirFreq = new double[65];
    desirFreq[8]=1;
    desirFreq[9]=1;
    double[] kern = filterKernel(desirFreq, 81);
    kernelTest(kern, 128);
    Utils.breakPoint();
  }

  static void kernelScale(double[] kern, double target){
    double s=0;
    for( double d : kern ){
      s += d*d;
    }
    double k = target/Math.sqrt(s/kern.length);
    for( int i=0; i<kern.length; i++ ){
      kern[i] *= k;
    }
  }

  static void kernelTest(double[] kern, int timeLen){
    Complex[] c = new Complex[timeLen];
    for( int i=0; i<timeLen; i++ ){
      int idx=(i+(kern.length-1)/2)%timeLen;
      c[i] = new Complex(idx>=kern.length?0:kern[idx],0);
    }
    Complex[] freq = FFT.fft(c);
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
    if( len%2!=1 ){
      throw new IllegalArgumentException(""+len);
    }
    Complex[] chCompl = new Complex[(desirFreq.length-1)*2];
    for( int j=0; j<desirFreq.length; j++ ){
      chCompl[j] = new Complex(desirFreq[j],0);
    }
    for( int j=1; j<desirFreq.length; j++ ){
      chCompl[chCompl.length-j] = new Complex(desirFreq[j],0);
    }
    Complex[] timeCompl = FFT.ifft(chCompl);

    double[] ret = new double[len];
    for( int i=0; i<ret.length; i++ ){
      double hammpingWindow=0.54-0.46*Math.cos(2*Math.PI*i/(len-1));
      final int idx = (timeCompl.length + i - (len - 1) / 2) % timeCompl.length;
      ret[i] = timeCompl[idx].re() * hammpingWindow;
    }
    return ret;
  }
}
