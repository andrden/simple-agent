package scala.p1

import scala.collection._

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: Mar 3, 2010
 * Time: 4:10:40 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.{Date, Locale}
import java.text.DateFormat
import java.text.DateFormat._
import collection.immutable.HashMap

object A {
  def main(args : Array[String]):Unit={
//    val now = new Date
//    val df = getDateInstance(LONG, Locale.FRANCE)
//    println(df format now)
    val l = List(1,2,3)
    val m = Map() ++ (l map (o => (o,o*o)))
    println(m+ " "+m.getClass)
    val m1 = (new mutable.HashMap[Int,Int] /: l){ case (h,i) => h.put(i, i*i); h }
    println(m1)

   // val CSB = new C{ type T=StringBuilder }
   // println( CSB.getClass+" "+CSB.newInst )
  }
 /*
  abstract class C{
    type T <: AnyRef
    def newInst = {
      //val c = classOf[T]
      T.newInstance
    }
  }
  */
}
