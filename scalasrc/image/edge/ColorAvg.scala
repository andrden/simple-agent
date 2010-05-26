package image.edge

/**
 * Created by IntelliJ IDEA.
 * User: denny
 * Date: May 26, 2010
 * Time: 4:40:52 PM
 * To change this template use File | Settings | File Templates.
 */

class ColorAvg{
  var count=0;
  var r=0;
  var g=0;
  var b=0;
  def update(rgb: Int){
    count += 1
    r += ((rgb >> 16) & 0xFF)
    g += ((rgb >> 8) & 0xFF)
    b += ((rgb >> 0) & 0xFF)
  }
  def toRgb = ((r/count)<<16) + ((g/count)<<8) + (b/count)
}
