package audio;

import edu.princeton.cs.Complex;
import edu.princeton.cs.FFT;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 21, 2009
 * Time: 12:43:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class DFT {
  double[] re;
  double[] im;
  double[] mag;
  static double twoPi = 2*Math.PI;

  double mean;
  double stdDev;

  public double[] reconstruct(){
    return reconstruct(re, im);
  }

  public static double[] reconstruct(double[] re, double[] im){
    int N = (re.length-1)*2;
    double[] ret = new double[N];

    for( int i=0; i<N; i++ ){
      for( int j=0; j<re.length; j++ ){
        double k = N/2;
        if( j==0 || j==N/2 ){
          k = N;
        }
        ret[i] += re[j]*Math.cos(i*j*twoPi/N)/k;
        if( im!=null ){
          ret[i] -= im[j]*Math.sin(i*j*twoPi/N)/k;
        }
      }
    }

    return ret;
  }

//  void meanStdDev(double[] input) {
//    mean = 0;
//    for( double v : input ){
//      mean += v;
//    }
//    mean /= input.length;
//
//    stdDev=0;
//    for( double v : input ){
//      stdDev += (mean-v)*(mean-v);
//    }
//    stdDev = Math.sqrt(stdDev/(input.length-1));
//  }

  void meanStdDevRunning(double[] input) {
    double sum=0;
    double sum2=0;
    for( double v : input ){
      sum += v;
      sum2 += v*v;
    }
    mean = sum/input.length;
    stdDev = Math.sqrt((sum2 - sum*sum/input.length)/(input.length-1));
  }

  /**
   * Forward FFT
   * @param input
   */
  public void forward(double[] input) {
    meanStdDevRunning(input);

    Complex[] chCompl = new Complex[input.length];
    for( int j=0; j<input.length; j++ ){
      chCompl[j] = new Complex(input[j],0);
    }
    Complex[] freqCompl = FFT.fft(chCompl);

    int N = input.length;
    re = new double[N/2+1];
    im = new double[N/2+1];
    for( int j=0; j<re.length; j++ ){
      re[j] = freqCompl[j].re();
      im[j] = freqCompl[j].im();
    }

    //correlationDFT(input);

    mag = new double[input.length/2+1];
    for( int j=0; j<re.length; j++ ){
      mag[j] = Math.sqrt(re[j]*re[j] + im[j]*im[j]);
    }

  }

  private void correlationDFT(double[] input) {
    int N = input.length;
    re = new double[N/2+1];
    im = new double[N/2+1];
    for( int j=0; j<re.length; j++ ){
      for( int i=0; i<N; i++ ){
        re[j] += input[i]*Math.cos(i*j*twoPi/N);
        im[j] -= input[i]*Math.sin(i*j*twoPi/N);
      }
    }
  }

  public double[] getMagnitudes() {
    return mag;
  }
}
