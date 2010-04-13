import collection.mutable.{Buffer, HashMap, ArrayBuffer}
import util.Sorting

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
    val data = "011010101010000111001110101110111011101110111011"

    var recent = new ArrayBuffer[String]
    var fullStr = ""
    var fullPredStr = ""
    var fullPredStrCorr = ""
    for( c <- data ){
      val predictedState = predict(recent)
      val predCh = if (predictedState.isDefined) predictedState.get.prediction else '_'
      System.out.println(""+c + predCh + " pred " + predictedState )
      fullPredStr += predCh
      fullPredStrCorr += (if(c==predCh) ' ' else c)
      
      recent.foreach((it) =>
              nextEvents.getOrElseUpdate(it, new NextState(it)).inc(c)
      )

      fullStr += c
      recent = recent.map(_+c)
      recent += ""+c
      System.out.println(recent)
    }
    //System.out.println(nextEvents)
    System.out.println(data)
    System.out.println(fullPredStr)
    System.out.println(fullPredStrCorr)
  }

  def predict(recent : Buffer[String]) : Option[NextState] = {
    //recent.flmap(s => nextEvents.get(s))
    //val pred = for(p <- recent; if p. )
    val relevantStates : Buffer[NextState] = recent.flatMap(s => nextEvents.get(s))
    val relevSorted =  Sorting.stableSort(relevantStates).toList.reverse
    System.out.println( "prediction="+ relevSorted )
    val h = relevSorted.headOption
    if( h.isDefined && h.get.probabForce>0 ) h else None
  }
}

class NextState(val event : String) extends Ordered[NextState]{
  val m = new HashMap[Char,Int]
  def inc(c:Char){
    m.update(c, m.getOrElse(c,0)+1);
    //m.put(c, m.get(c) match {case Some(v) => v+1 case _ => 1 })
  }
  def probabForce : Double = {
    if( m.size==1 ) 1
    else {
      val v = m.valuesIterable
      (v.max - v.min) / v.sum
    }
  }
  def prediction = {
    val maxV = m.valuesIterable.max
    m.filter( (t) => t._2==maxV ).head._1
  }
  def compare(that: NextState) = {
    val thisP = this.probabForce
    val thatP = that.probabForce

    if( thisP>thatP ) 1 else if( thisP<thatP ) -1 else
      if( this.event.length>that.event.length ) 1 else if ( this.event.length<that.event.length ) -1
      else 0
    //this.n - that.n
  }

  override def toString = event+" to " + m.toString + " x"+probabForce
}