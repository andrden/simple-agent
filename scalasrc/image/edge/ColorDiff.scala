package image.edge

/**
 * Created by IntelliJ IDEA.
 * User: denny
 * Date: May 28, 2010
 * Time: 5:02:02 PM
 * To change this template use File | Settings | File Templates.
 */

class ColorDiff{
  class Axis{
    var sum, min, max : Int = 0
    var first = true
    def +=(v: Int){
      sum += v
      if( first ){
        min = v
        max = v
      }else{
        min = min.min(v)
        max = max.max(v)
      }
      first=false
    }
    def /(c:Int) = sum/c

    def diff = max-min
  }

  var count=0;
  var r,g,b = new Axis

  def update(rgb: Int){
    count += 1
    r += ((rgb >> 16) & 0xFF)
    g += ((rgb >> 8) & 0xFF)
    b += ((rgb >> 0) & 0xFF)
  }

  def toRgb = ((r/count)<<16) + ((g/count)<<8) + (b/count)

  def maxDiff = r.diff.max(g.diff).max(b.diff)
}
