package worlds;

import worlds.intf.WorldGridView;

import java.awt.*;
import java.util.*;

/**
 * Rules to learn from this world:
 *
 * (b - black, w - white, y - yellow - food
 * Main rules:
 * 1) b F -> noop
 * 2) y F  -> +
 * 3) N -> noop
 *
 * Optional rules:
 * 4) L then R -> noop
 */
public class Feed0 implements WorldGridView {
  int availableResults=0;

  String[] FIELD_INIT = {
          "bbbbbbb bbb",
          "b yy  b byb",
          "by by bbb b",
  };

  enum Dir{
    left(-1,0,"<"),
    up(0,-1,"^"),
    right(1,0,">"),
    down(0,1,"V");

    int dx;
    int dy;
    String ch;
    Dir(int dx, int dy, String ch){
      this.dx=dx;
      this.dy=dy;
      this.ch=ch;
    }
  }

  int width=FIELD_INIT[0].length();
  int height=FIELD_INIT.length;
  Color[][] cdata = new Color[width][height];
  int x0 =2;
  int y0 =2;
  int dirIdx=0;
  int result=0;
  String prevCommand=null;

  Feed0.Dir getDir(){
    return Feed0.Dir.values()[dirIdx];
  }

  Point getFwd(){
    return new Point(x0+getDir().dx, y0+getDir().dy);
  }

  public Feed0(){
    for( int i=0; i<width; i++ ){
      for( int j=0; j<height; j++ ){
        Color c;
//        if( i==0 || i==width-1 || j==0 || j==height-1 ){
//          c=Color.BLACK;
//        }else{
//          c=Color.WHITE;
//        }
        char ch = FIELD_INIT[j].charAt(i);
        switch(ch){
          case 'b':
            c = Color.BLACK;
            break;
          case ' ':
            c = Color.WHITE;
            break;
          case 'y':
            c = Color.YELLOW;
            break;
          default:
            throw new RuntimeException("char="+ch);
        }
        cdata[i][j]=c;
      }
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public Color getColorDisplay(int x, int y){
    if( x==x0 && y==y0){
      return Color.BLUE;
    }
    return cdata[x][y];
  }

  public Color getColor(Point p){
    return getColorDisplay(p.x, p.y);
  }

  public String getChar(int x, int y) {
    if( x==x0 && y==y0 ){
      return getDir().ch;
    }
    return null;
  }


  public java.util.List<String> commands() {
    return Arrays.asList("L","R","N","j");
  }


  public int availableResults() {
    return availableResults;
  }

  public boolean commandWrong(String cmd) {
    return false;
  }

  public void command(String cmd) {
    availableResults++;
    if( cmd.equals("j") ){
          result=-1;
    }
    if( cmd.equals("L") ){
          result=-1;
    }

    prevCommand = cmd;
  }


  public Map<String, Object> view() {
    return Collections.singletonMap("f", (Object)getColor(getFwd()));
  }


  public int result(boolean reset) {
    int r=result;
    if( reset ){
      result=0;
    }
    return r;
  }
}
