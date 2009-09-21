package audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Sep 21, 2009
 * Time: 1:11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AudioIn {
  File f;
  int chunk;
  InputStream inp;
  float sampleRate;
  AudioFormat fmtUsed;

  public AudioIn(File f, int chunk) {
    this.f = f;
    this.chunk = chunk;

    try {
      AudioInputStream in = AudioSystem.getAudioInputStream(f);
      AudioFormat fmt = in.getFormat();

      sampleRate = in.getFormat().getSampleRate();
      fmtUsed = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
          sampleRate, 8, 1, 1, in.getFormat().getFrameRate(), false  );
      inp = AudioSystem.getAudioInputStream(fmtUsed, in);

    } catch (Exception e) {
      throw new RuntimeException("",e);
    }

  }

  int[] nextChunkInt(){
    int[] ret = new int[chunk];
    byte[] d = new byte[chunk];
    try {
      new DataInputStream(inp).readFully(d);
      for( int i=0; i<chunk; i++ ){
        ret[i] = d[i];
      }
      return ret;
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
  }

  double[] nextChunkDouble(){
    double[] ret = new double[chunk];
    int[] d = nextChunkInt();
    for( int i=0; i<chunk; i++ ){
      ret[i] = d[i];
    }
    return ret;
  }

  void play(double[] ch, int dupl){
    byte[] b = new byte[ch.length];
    for( int i=0; i<b.length; i++ ){
      b[i]=(byte)ch[i];
      //b[i]=(byte)(Math.random()*50);
    }

    try {
      //AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]).
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmtUsed);

      Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
      SourceDataLine line = (SourceDataLine)mixer.getLine(info);

//      SourceDataLine line = AudioSystem.getSourceDataLine(fmtUsed,
//          AudioSystem.getMixerInfo()[0]);
      line.open(fmtUsed);
      line.start();
      for( int i=0; i<dupl; i++ ){
        line.write(b, 0, b.length);
      }
      line.drain();
      line.close();
    } catch (Exception e) {
      throw new RuntimeException("",e);
    }
  }

}
