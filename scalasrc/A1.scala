package scala.p1

import scala.collection._

/**
 * Created by IntelliJ IDEA.
 * User: denny
 * Date: Jun 7, 2010
 * Time: 12:50:13 PM
 * To change this template use File | Settings | File Templates.
 */

object A1{
  def code{
//    val now = new Date
//    val df = getDateInstance(LONG, Locale.FRANCE)
//    println(df format now)
    val l = List(1,2,3)
    val m = Map() ++ (l map (o => (o,o*o)))
    println(m+ " "+m.getClass)
    val m1 = (new mutable.HashMap[Int,Int] /: l){ case (h,i) => h.put(i, i*i); h }
    println(m1)

  }
}