/**
 * Created by IntelliJ IDEA.
 * User: user
 * Date: May 14, 2010
 * Time: 4:27:42 PM
 * To change this template use File | Settings | File Templates.
 */

object QubicRoot extends Application {
  import Math.abs
  val tolerance = 0.0001

  def isCloseEnough(x: Double, y: Double) = abs((x - y) / x) < tolerance

  def fixedPoint(f: Double => Double)(firstGuess: Double) = {
    def iterate(guess: Double): Double = {
      val next = f(guess)
      println(next)
      if (isCloseEnough(guess, next)) next
      else iterate(next)
    }
    iterate(firstGuess)
  }

  def averageDamp(f: Double => Double)(x: Double) = (x + f(x)) / 2

  def sqrt(x: Double) = fixedPoint(averageDamp(y => x/y))(1.0)

  def root3(x: Double) = fixedPoint(averageDamp(y => x/y/y))(1.0)

  println( root3(8) )
  println( root3(27) )

}