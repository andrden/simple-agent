package sound

class Rational(numerator: Int, denominator: Int) {

  require(denominator != 0)

  private val gcd = greatestCommonDivisor(numerator.abs,
    denominator.abs)
  val n = numerator / gcd
  val d = denominator / gcd

  def this(n: Int) = this(n, 1)

  private def greatestCommonDivisor(a: Int, b: Int): Int =
  if (b == 0) a else greatestCommonDivisor(b, a % b)

  def + (that: Rational): Rational =
  new Rational(n * that.d + d * that.n, d * that.d)

  def - (that: Rational): Rational =
  new Rational(n * that.d - d * that.n, d * that.d)

  def * (that: Rational): Rational =
  new Rational(n * that.n, d * that.d)

  def / (that: Rational): Rational =
  new Rational(n * that.d, d * that.n)

  override def toString = n + "/" + d
}