package worlds;

import worlds.intf.WorldGridView;

import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.awt.*;

import utils.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jan 9, 2009
 * Time: 5:14:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class Rubic2x2World extends Rubic2x2Model
    implements WorldGridView, Serializable {
  int lastCmdRes=0;
  Face[][] faceLocations = {
      {null,t,null,null},
      {l,f,r,z},
      {null,b,null,null},
  };

  public Collection<String> targetSensors() {
    return Collections.singleton("$");
  }

  public int availableResults() {
    return 0;
  }
  public boolean commandWrong(String cmd) {
    return false;
  }

  public List<String> commands() {
    return Arrays.asList(commands);
  }

  public Map<String, Object> view() {
    Map<String, Color> m = new HashMap<String, Color>();
    putView(m, f);
    putView(m, t);
    putView(m, b);
    putView(m, l);
    putView(m, r);
    putView(m, z);

    Map<String,Object> ms = new HashMap<String,Object>();
    for( String k : m.keySet() ){
      ms.put(k, Utils.color2name(m.get(k)));
    }
    ms.put("$",lastCmdRes);

    return ms;
  }

  void putView(Map<String, Color> to, Face f){
    for( int i=0; i<f.face.length; i++ ){
      for( int j=0; j<f.face.length; j++ ){
        to.put(f.fname+i+j, f.face[i][j]);
      }
    }
  }

  public void command(String cmd) {
    int sc0 = totalScore();
    execCommand(cmd);
    int sc1 = totalScore();
    lastCmdRes = sc1-sc0;
  }

  public int getWidth() {
    return FSIZE *faceLocations[0].length;
  }

  public int getHeight() {
    return FSIZE *faceLocations.length;
  }

  public Color getColorDisplay(int x, int y) {
    Face ff=faceLocations[y/2][x/2];
    if( ff==null ){
      return Color.BLACK;
    }
    return ff.face[y%2][x%2];
  }

  public String getChar(int x, int y) {
    return null;
  }

  public Map<String, Point> sensorLocations() {
    return null;
  }
}
