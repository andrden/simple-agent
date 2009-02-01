package audio;

import com.pmstation.common.utils.MinMaxFinder;

import javax.sound.sampled.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.*;

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

  void wavWrite() throws Exception{
    File fwav = new File("c:/tmp/precise_dial_tone.wav");  // a sine wave at 350 hertz and a sine wave at 440 hertz
    AudioInputStream in = AudioSystem.getAudioInputStream(fwav);
    in = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, in);
    //int rate = 11025;
    int rate = (int)in.getFormat().getFrameRate();
    byte[] b = new byte[rate*3];

    Map<Long,Double> freqs = new HashMap<Long,Double>();
    Map<Long,Double> phase = new HashMap<Long,Double>();
    //freqs.put(350l,1.);
    //freqs.put(440l,1.);

    for( long l=350; l<365; l++ ){
      freqs.put(l, Math.exp(-(l-350)*3) );
      freqs.put(l*2, Math.exp(-(l-350)*4) );
      freqs.put(l*13/10, Math.exp(-(l-350)*4) );
      freqs.put(l*11/10, 4*Math.exp(-(l-350)*2) );
      //phase.put(l, Math.PI*Math.random());
    }

//    for( long l=550; l<570; l++ ){
//      freqs.put(l, 1.*(l-549)/20);
//      phase.put(l, Math.PI*Math.random());
//    }

    double max=0;
    for( Double v : freqs.values() ){
      max+=v;
    }

    for( int i=0; i<b.length; i++ ){
      double v = 0;
      for( Long f : freqs.keySet() ){
        v += freqs.get(f)*Math.sin(2*Math.PI*i/rate*f /*+ phase.get(f)*/);
      }
      b[i] = (byte)(v/max*50);
    }

    AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(b), in.getFormat(), b.length);
    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File("c:/tmp/generated.wav"));
  }

  void m() throws Exception{
    //wavWrite();
    //wavTransform();
    wavVisualize();
  }

  private void wavVisualize() throws Exception {
    RateBytes rateBytes = new RateBytes().invoke();
    byte[] b = rateBytes.getB();
    MinMaxFinder mm = new MinMaxFinder();
    for( byte bi : b ){
      mm.add(bi, "");
    }
    byte min = (byte)mm.getMinVal();
    byte max = (byte)mm.getMaxVal();
    int w = b.length;
    if( w>4000 ){
      w=4000;
    }

    BufferedImage im = new BufferedImage(w, max-min+1, BufferedImage.TYPE_INT_RGB);
    for( int i=0; i<w; i++ ){
      im.setRGB(i, b[i]-min, Color.GREEN.getRGB());
    }
    ImageIO.write(im, "png",new File("d:/tmp/sound1.png"));
  }

  private void wavTransform() throws UnsupportedAudioFileException, IOException {
    RateBytes rateBytes = new RateBytes().invoke();
    int rate = rateBytes.getRate();
    byte[] b = rateBytes.getB();


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
    for( int i=0; i</*770*/b.length; i++ ){
      double val = b[i];
      s1 += val*Math.sin(2*Math.PI*i/rate*freq);
      s2 += val*Math.cos(2*Math.PI*i/rate*freq);
    }
    return s1*s1+s2*s2;
  }

  private class RateBytes {
    private int rate;
    private byte[] b;

    public int getRate() {
      return rate;
    }

    public byte[] getB() {
      return b;
    }

    public RateBytes invoke() throws UnsupportedAudioFileException, IOException {
      //File fwav = new File("c:/tmp/accessed.wav");
      File fwav = new File("c:/tmp/precise_dial_tone.wav");  // a sine wave at 350 hertz and a sine wave at 440 hertz
      AudioInputStream in = AudioSystem.getAudioInputStream(fwav);
      in = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, in);
      //int rate = 11025;
      rate = (int) in.getFormat().getFrameRate();
      //b = new byte[rate / 10];
      b = new byte[rate / 2];
      in.read(b);
      return this;
    }
  }
}
