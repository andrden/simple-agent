package worlds;

import com.pmstation.common.utils.CountingMap;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: Jan 9, 2009
 * Time: 1:40:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class Rubic2x2Model {
  final static int FSIZE =2;

  static class Face{
    Color[][] face = new Color[FSIZE][FSIZE];
    final String fname;

    Face(String fname, Color initColor) {
      this.fname = fname;
      for( int i=0; i<FSIZE; i++ ){
        for( int j=0; j<FSIZE; j++ ){
          face[i][j] = initColor;
        }
      }
    }

    void rotLeft(){
      Color[][] fc = face.clone();
      for( int i=0; i<fc.length; i++ ){
        for( int j=0; j<fc.length; j++ ){
          face[fc.length-1-j][i] = fc[i][j];
        }
      }
    }
    void rotRight(){
      Color[][] fc = face.clone();
      for( int i=0; i<fc.length; i++ ){
        for( int j=0; j<fc.length; j++ ){
          face[j][fc.length-1-i] = fc[i][j];
        }
      }
    }
    Color[] row(int idx){
      return face[idx].clone();
    }
    Color[] rowRev(int idx){
      Color[] r = row(idx);
      rev(r);
      return r;
    }
    void setRow(int idx, Color[] r){
      face[idx] = r;
    }
    void setRowRev(int idx, Color[] r){
      rev(r);
      setRow(idx, r);
    }
    Color[] col(int idx){
      Color[] cc = new Color[face.length];
      for( int i=0; i<cc.length; i++ ){
        cc[i] = face[i][idx];
      }
      return cc;
    }
    Color[] colRev(int idx){
      Color[] cc = col(idx);
      rev(cc);
      return cc;
    }
    void rev(Color[] c){
      for( int i=0, j=c.length-1; i<j; i++, j--){
        Color ci = c[i];
        c[i]=c[j];
        c[j]=ci;
      }
    }
    void setCol(int idx, Color[] c){
      for( int i=0; i<c.length; i++ ){
        face[i][idx] = c[i];
      }
    }
    void setColRev(int idx, Color[] c){
      rev(c);
      setCol(idx, c);
    }

    int score(){
      CountingMap<Color> c = new CountingMap<Color>();
      for( int i=0; i<face.length; i++ ){
        for( int j=0; j<face[i].length; j++ ){
          c.increment(face[i][j]);
        }
      }
      int sc = 0;
      for( Long v : c.values() ){
        if( v>1 ){
          sc += v*v;
        }
      }
      return sc;
    }
  }

  int totalScore(){
    return f.score()+t.score()+b.score()+l.score()+r.score()+z.score();
  }

  Face f = new Face("f",Color.WHITE); // forward, top, botton, left, right
  Face t = new Face("t",Color.YELLOW);
  Face b = new Face("b",Color.BLUE);
  Face l = new Face("l",Color.GREEN);
  Face r = new Face("r",Color.RED);
  Face z = new Face("z",Color.ORANGE); // back
/*

Faces on plane:

.t..
lfrz
.b..

*/

  String[] commands = {
      "U1","D1","L1","R1","ZR","ZL"
  };

  void execCommand(String s){
    if( s.equals("L1") || s.equals("R1") ){
      Color[][] loop = new Color[][]{
          f.row(0), r.row(0), z.row(0), l.row(0)
      };
      rotate(loop, s.equals("R1"));
      f.setRow(0, loop[0]);
      r.setRow(0, loop[1]);
      z.setRow(0, loop[2]);
      l.setRow(0, loop[3]);
      if( s.equals("R1") ){
        t.rotLeft();
      }else{
        t.rotRight();
      }
    }
    if( s.equals("U1") || s.equals("D1") ){
      Color[][] loop = new Color[][]{
          f.col(0), b.col(0), z.colRev(1), t.col(0)
      };
      rotate(loop, s.equals("D1"));
      f.setCol(0, loop[0]);
      b.setCol(0, loop[1]);
      z.setColRev(1, loop[2]);
      t.setCol(0, loop[3]);
      if( s.equals("D1") ){
        l.rotRight();
      }else{
        l.rotLeft();
      }
    }
    if( s.equals("ZR") || s.equals("ZL") ){
      Color[][] loop = new Color[][]{
          t.row(1), r.col(0), b.rowRev(0), l.colRev(1)
      };
      rotate(loop, s.equals("ZR"));
      t.setRow(1, loop[0]);
      r.setCol(0, loop[1]);
      b.setRowRev(0, loop[2]);
      l.setColRev(1, loop[3]);
      if( s.equals("ZL") ){
        f.rotLeft();
      }else{
        f.rotRight();
      }
    }
  }

  void rotate(Color[][] loop, boolean right){
    if( right ){
      Color[] c0 = loop[loop.length-1];
      for( int i=loop.length-1; i>0; i-- ){
        loop[i]=loop[i-1];
      }
      loop[0]=c0;
    }else{
      Color[] c0 = loop[0];
      for( int i=1; i<loop.length; i++ ){
        loop[i-1]=loop[i];
      }
      loop[loop.length-1]=c0;
    }
  }
}
