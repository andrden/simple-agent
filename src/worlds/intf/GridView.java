package worlds.intf;

import java.awt.*;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 21/1/2008
 * Time: 16:30:20
 * To change this template use File | Settings | File Templates.
 */
public interface GridView {
  public int getWidth();

  public int getHeight();

  public Color getColorDisplay(int x, int y);

  public String getChar(int x, int y);

  Map<String, Point> sensorLocations();
}
