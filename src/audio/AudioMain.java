package audio;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 10 лист 2008
 * Time: 17:47:40
 */
public class AudioMain {
  // Telephone sounds: http://www.phonebooth.us/sounds/index.htm
  

  public static void main(String[] args) throws Exception{
    new AudioMain().m();
  }

  void m() throws Exception{
    //File fwav = new File("c:/tmp/accessed.wav");
    File fwav = new File("c:/tmp/precise_dial_tone.wav");  // a sine wave at 350 hertz and a sine wave at 440 hertz
    AudioInputStream in = AudioSystem.getAudioInputStream(fwav);
    in = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, in);
    //int rate = 11025;
    int rate = (int)in.getFormat().getFrameRate();
    byte[] b = new byte[rate/10];
    in.read(b);

    for( double freq = 1; freq<rate/2; freq++ ){
      double p = power(b, rate, freq) / b.length / b.length;
      if( p>2 ){
        System.out.println(freq+" "+ p);
      }
    }
  }

  double power(byte[] b, double rate, double freq){
    double s1=0;
    double s2=0;
    for( int i=0; i<b.length; i++ ){
      double val = b[i];
      s1 += val*Math.sin(2*Math.PI*i/rate*freq);
      s2 += val*Math.cos(2*Math.PI*i/rate*freq);
    }
    return s1*s1+s2*s2;
  }
}
