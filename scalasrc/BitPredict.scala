import collection.mutable.{HashMap, ArrayBuffer}

/**
nextEvents.getOrElseUpdate(it, new NextState).inc(c)

replaces Java's

NextState s = nextEvents.get(it);
if( s==null ){
  s = new NextState();
  nextEvent.put(it, s);
}
s.inc(c);

 */

object BitPredict {
  def main(args: Array[String]){
    val data = "0110101010100001110011101011"
    val nextEvents = new HashMap[String, NextState]

    var recent = new ArrayBuffer[String]
    var fullStr = ""
    for( c <- data.elements ){
      recent.foreach((it) =>
              nextEvents.getOrElseUpdate(it, new NextState).inc(c)
      )

      fullStr += c
      recent = recent.map((it) => it+c)
      recent += ""+c
      System.out.println(recent)
    }
    System.out.println(nextEvents)
  }
}

class NextState{
  val m = new HashMap[Char,Int]
  def inc(c:Char){
    m.update(c, m.getOrElse(c,0)+1);
    //m.put(c, m.get(c) match {case Some(v) => v+1 case _ => 1 })
  }

  override def toString = m toString
}