package audio.cords;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Dec 18, 2009
 * Time: 6:25:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class ArraySoundIn implements SoundIn{
  short[] data;
  int i=-1;

  public ArraySoundIn(short[] data) {
    this.data = data;
  }

  public short[] getData() {
    return data;
  }

  public short next() {
    i++;
    if( i>=data.length ){
      return 0;
    }
    return data[i];
  }
}
