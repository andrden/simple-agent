package audio.sssh;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Feb 11, 2010
 * Time: 4:12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Cut {
    int id;
    short[] wav;
    double[] freq;

    public Cut(int id, short[] wav, double[] freq) {
        this.id = id;
        this.wav = wav;
        this.freq = freq;
    }

    static DataInputStream soundFile2() throws FileNotFoundException {
      // "C:\\proj\\cr6\\sounds/onetwothree.voice" says the following:
      // "рас, рас, рас, два, три, четыре, пять ... в веб-камере мирофон ещё есть..
      // . правда не понял где он там ..."


      DataInputStream di = new DataInputStream(new FileInputStream(
          //"C:\\proj\\cr6\\sounds/onetwothree.voice"
         // "C:\\proj\\cr6\\sounds/shshss.voice"

         // "C:\\Projects\\simple-agent\\sounds/shshss.voice"
           "/opt/project/simple-agent/sounds/shshss2.voice"
      ));
      return di;
    }

    static DataInputStream soundFile123() throws FileNotFoundException {
      // "C:\\proj\\cr6\\sounds/onetwothree.voice" says the following:
      // "рас, рас, рас, два, три, четыре, пять ... в веб-камере мирофон ещё есть..
      // . правда не понял где он там ..."


      DataInputStream di = new DataInputStream(new FileInputStream(
          //"C:\\proj\\cr6\\sounds/onetwothree.voice"
         // "C:\\proj\\cr6\\sounds/shshss.voice"

          "C:\\Projects\\simple-agent\\sounds/onetwothree.voice"
          // "/opt/project/simple-agent/sounds/shshss.voice"
      ));
        return di;
    }

    static DataInputStream soundFile() throws FileNotFoundException {
     
      DataInputStream di = new DataInputStream(new FileInputStream(
          //"C:\\proj\\cr6\\sounds/onetwothree.voice"
         // "C:\\proj\\cr6\\sounds/shshss.voice"

          "C:\\Projects\\simple-agent\\sounds/shshss.voice"
          // "/opt/project/simple-agent/sounds/shshss.voice"
      ));



/*
Mapping of sounds/shshss.voice:
200-300 shsh
 380-480 ssss
565-655 shsh
 745-840 ssss
930-1030 shsh
 1140-1260 ssss
1400-1415 ch!
...chch
...chch
...shsh
...ssss
...chch
 ~=2500 ss
*/

      return di;
    }

}
