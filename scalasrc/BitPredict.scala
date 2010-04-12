import collection.mutable.{Buffer, HashMap, ArrayBuffer}

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
  val nextEvents = new HashMap[String, NextState]
  def main(args: Array[String]){
    val data = "0110101010100001110011101011"

    var recent = new ArrayBuffer[String]
    var fullStr = ""
    for( c <- data ){
      predict(recent)
      
      recent.foreach((it) =>
              nextEvents.getOrElseUpdate(it, new NextState(it)).inc(c)
      )

      fullStr += c
      recent = recent.map(_+c)
      recent += ""+c
      System.out.println(recent)
    }
    System.out.println(nextEvents)
  }

  def predict(recent : Buffer[String]){
    //recent.flmap(s => nextEvents.get(s))
    //val pred = for(p <- recent; if p. )
    System.out.println( "prediction="+ recent.flatMap(s => nextEvents.get(s)) )
  }
}

class NextState(event : String){
  val m = new HashMap[Char,Int]
  def inc(c:Char){
    m.update(c, m.getOrElse(c,0)+1);
    //m.put(c, m.get(c) match {case Some(v) => v+1 case _ => 1 })
  }

  override def toString = event+" to " + m toString
}