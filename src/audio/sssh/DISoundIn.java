/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package audio.sssh;

import audio.cords.SoundIn;
import java.io.DataInput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author дом
 */
public class DISoundIn implements SoundIn{
    DataInput di;

    public DISoundIn(DataInput di) {
        this.di = di;
    }

    public short next() {
        try {
            return di.readShort();
        } catch (IOException ex) {
            throw new RuntimeException("",ex);
        }
    }


}
