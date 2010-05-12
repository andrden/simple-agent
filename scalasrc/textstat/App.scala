package textstat

import io.Source
import collection.mutable.HashMap

/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: May 7, 2010
 * Time: 3:43:09 PM
 * To change this template use File | Settings | File Templates.
 */

object App extends Application{
  val counts = new HashMap[String,Int]

  val txt = getClass.getResourceAsStream("/textstat/Book1.html")
  val src = Source.fromInputStream(txt)
  val chars = src.toList
  for( c <- chars ){
    val key = ""+c.toLower
    counts.update(key, counts.getOrElse(key,0)+1)
  }

  val countsSorted = counts.toList.sortWith( (a,b) => a._2 > b._2 )
  countsSorted.foreach( a => println(a) )
}