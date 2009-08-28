package audio;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
* User: adenysenko
* Date: Aug 28, 2009
* Time: 2:33:25 PM
* To change this template use File | Settings | File Templates.
*/
class RateBytes {
  private int rate;
  private byte[] b;

  public int getRate() {
    return rate;
  }

  public byte[] getB() {
    return b;
  }

  public RateBytes invoke() throws UnsupportedAudioFileException, IOException {

    //File fwav = new File("D:\\proj\\4shared\\bin\\video\\windows\\obama.wav");
    File fwav = new File("c:/tmp/accessed.wav");
    //File fwav = new File("c:/tmp/precise_dial_tone.wav");  // a sine wave at 350 hertz and a sine wave at 440 hertz
    //File fwav = new File("C:\\proj\\cr6\\sounds\\quatre.wav");
    AudioInputStream in = AudioSystem.getAudioInputStream(fwav);
    //in = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, in);
    AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      in.getFormat().getSampleRate(), 8, 1, 1, in.getFormat().getFrameRate(), false  );
    in = AudioSystem.getAudioInputStream(fmt, in);
    //int rate = 11025;
    rate = (int) in.getFormat().getFrameRate();
    //b = new byte[rate / 10];
    b = new byte[rate / 2];
    in.read(b);
    return this;
  }
}
