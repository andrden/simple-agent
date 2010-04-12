package scala.p1

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
object A {
  def main(args : Array[String]):Unit={
    val now = new Date
    val df = getDateInstance(LONG, Locale.FRANCE)
    println(df format now)
  }
}
