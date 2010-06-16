package sound

/**
 * Created by IntelliJ IDEA.
 * User: denny
 * Date: Jun 15, 2010
 * Time: 3:46:36 PM
 *
 *
 * http://ru.wikipedia.org/wiki/%D0%A1%D0%B5%D0%BA%D1%81%D1%82%D0%B0
 * http://www.gitaristam.ru/school/frequency.htm
 */

object MusicIntervals extends Application{
  val dnote = Math.pow(2, 1/12.)

  case class Intv(i:Int, j:Int, k:Int, delta: Double){
    override def toString = i + "/" + j + "   tones="+k +"  delta="+delta
  }

  def bestMatch(i:Int, j:Int) = {
    val ratio = i.toDouble/j
    val dnotes = for( k <- 1 to 15 ) yield (k, Math.abs(Math.pow(dnote, k) - ratio ) )
    val dnotesOrd = dnotes.sortBy( x => x._2 )
    val bestK = dnotesOrd.head._1
    val bestDelta = dnotesOrd.head._2
    //dnotesOrd.filter( x => x._2 < bestDelta*2 )
    Intv(i,j,bestK, bestDelta)
  }

  val listIntv = for( i<- 1 to 13; j<- 1 until i if new Rational(i,j).n==i ) yield bestMatch(i,j)
  for( i <- listIntv.sortBy( _.delta ) ){
    println(i)
  }

}

//Results:
//2/1   tones=12  delta=8.881784197001252E-16
//4/3   tones=5  delta=0.0015065208367013305
//3/2   tones=7  delta=0.0016929231233180353 квинта консонанс
//9/8   tones=2  delta=0.0025379516906269828 большая секунда диссонанс
//9/4   tones=14  delta=0.005075903381252633
//13/11   tones=3  delta=0.00738893318453937
//5/4   tones=4  delta=0.009921049894873413 кварта консонанс ? / большая терция
//6/5   tones=3  delta=0.010792884997278707 малая терция консонанс
//10/9   tones=2  delta=0.011350937198261857
//8/5   tones=8  delta=0.012598948031800061 малая секста
//7/5   tones=6  delta=0.014213562373095456 - must be dissonance? - тритон, черт в музыке, диссонанс
//10/7   tones=6  delta=0.014357866198333236
//5/3   tones=9  delta=0.01512616384076293 большая секста
//11/7   tones=8  delta=0.01597248053962863
//9/5   tones=10  delta=0.018202563719320608 септима диссонанс
//8/7   tones=2  delta=0.020395094547769776
//12/5   tones=15  delta=0.02158576999455608
//11/10   tones=2  delta=0.02246204830937293
//7/6   tones=3  delta=0.022540448336054508
//13/12   tones=1  delta=0.02387023897403795
//9/7   tones=4  delta=0.025793235819412397
//13/9   tones=6  delta=0.030230882071349052
//13/7   tones=11  delta=0.030605768220530738
//12/11   tones=1  delta=0.03144599654979552
//7/4   tones=10  delta=0.031797436280679436
//12/7   tones=9  delta=0.03249288377828452
//11/9   tones=3  delta=0.03301510721950107
//13/10   tones=5  delta=0.034839854170034545
//13/8   tones=8  delta=0.03759894803179997
//11/8   tones=6  delta=0.03921356237309537
//11/5   tones=14  delta=0.04492409661874719
//7/3   tones=15  delta=0.04508089667211035
//13/6   tones=13  delta=0.04774047794807501
//11/6   tones=10  delta=0.05153589705265382
//5/2   tones=15  delta=0.12158576999455617
//13/5   tones=15  delta=0.22158576999455626
//8/3   tones=15  delta=0.2882524366612227
//11/4   tones=15  delta=0.37158576999455617
//3/1   tones=15  delta=0.6215857699945562
