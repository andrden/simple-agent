package image.edge

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import Math.{abs,sin,cos}
import java.io.File

/**

 */

case class XY(x:Int, y:Int){
  def dx(p:XY) = p.x-x
  def dy(p:XY) = p.y-y
  def dist(p:XY) = Math.sqrt(dx(p)*dx(p) + dy(p)*dy(p))
  def +(p:XY) = XY(x+p.x, y+p.y)
  def shift(dx:Double,dy:Double) = XY(x+dx.toInt, y+dy.toInt)
  def shiftRadial(step:Int, radians: Double) = shift(step*cos(radians),step*sin(radians))
  def middle(p:XY) = XY((x+p.x)/2, (y+p.y)/2)
  def angle(p:XY) = Math.atan2( p.y-y, p.x-x )
}

class ColorAvg{
  var count=0;
  var r=0;
  var g=0;
  var b=0;
  def update(rgb: Int){
    count += 1
    r += ((rgb >> 16) & 0xFF)
    g += ((rgb >> 8) & 0xFF)
    b += ((rgb >> 0) & 0xFF)
  }
  def toRgb = ((r/count)<<16) + ((g/count)<<8) + (b/count)
}

object App extends Application{
  val imgStream = getClass.getResourceAsStream("/image/edge/green_apple.jpg")
  val img : BufferedImage = ImageIO.read(imgStream)
  val imgNew = new BufferedImage(img.getWidth, img.getHeight, img.getType)
  imgNew.getGraphics.drawImage(img,0,0,null)
  println("width %d heigth %d".format(img.getWidth, img.getHeight))

  def stepLine(beg:XY, end:XY, step:Int)(f:(XY)=>Unit) : Unit = {
     val stepX = (step * beg.dx(end) / beg.dist(end)).toInt
     val stepY = (step * beg.dy(end) / beg.dist(end)).toInt
     val next = beg + XY(stepX, stepY)
     f(beg)
     if( beg.dist(end)>=step ) stepLine(next,end,step)(f)
     else f(end)
  }

  def difColor(rgb1 : Int, rgb2 : Int) : Int = {
    val color1 = new Color(rgb1)
    val color2 = new Color(rgb2)
    return abs(color1.getRed-color2.getRed)+ abs(color1.getGreen-color2.getGreen)  +
      abs(color1.getBlue-color2.getBlue)
  }

  def colorAvg(p:XY, size:Int):Int ={
    val r = size/2
    val avg = new ColorAvg
    for(x <- (p.x - r) to (p.x + r) ){
      // println (x)
      for(y <- (p.y - r) to (p.y + r) ){
        if( (p.x-x)*(p.x-x) + (p.y-y)*(p.y-y) < r*r ){
          if( x>=0 && y>=0 && x<img.getWidth && y<img.getHeight ){
            avg.update(img.getRGB(x,y))
          }
        }
      }
    }
    avg.toRgb
  }

  def lookAround(p:XY, size:Int)(f:(XY)=>Unit){
    for( deg <- 0.until(360, 20) ){
      //println(deg)
      val pdeg = p.shiftRadial(size, deg*Math.Pi/180)
      f(pdeg)
    }
  }

  def lookAroundAside(p:XY, size:Int, pusher: XY)(f:(XY)=>Unit){
    val ang = p.angle(pusher)*180/Math.Pi
    for( deg <- (ang+60).until(ang+360-60, 20) ){
      println("lookAroundAside"+deg)
      val pdeg = p.shiftRadial(size, deg*Math.Pi/180)
      //paintDisk(pdeg, size/2, Color.RED)
      f(pdeg)
    }
  }

  def paintDisk(p:XY, size: Int, color: Color) = {
    val g = imgNew.getGraphics
    g.setColor(color)
    g.drawOval(p.x-size/2,p.y-size/2, size,size)
    //g.setColor(color)
    //g.fillOval(p.x-size/2,p.y-size/2, size,size)
  }

  val size=8

  def difColor(a:XY, b:XY) : Int ={
     val rgba = colorAvg(a,size)
     val rgbb = colorAvg(b,size)
     difColor(rgba, rgbb)
  }

  class StepColorTrack{
    var prev=0
    var prevP:XY=null
    var maxDiff=0
    var maxDiffPP : (XY,XY) = null
    def stepFunc(p:XY){
      val rgb = colorAvg(p,size)
      //paintDisk(p, size/2, new Color(rgb))
      val diff = difColor(rgb, prev)
      if( prevP!=null && diff>maxDiff ){
        maxDiff=diff;
        maxDiffPP=(prevP,p)
      }
      prev = rgb
      prevP=p
      println("%s %d".format(p, diff))
    }
  }

  def maxDiffPoint(a:XY, b:XY) : XY = {
    val mid = a.middle(b)
    if( a==mid || b==mid ) return mid
    val da = difColor(a,mid)
    val db = difColor(mid,b)
    if( da>db ) maxDiffPoint(a,mid) else maxDiffPoint(mid,b)
  }

  def nextBorderPoint(p0 : XY, p1: XY) : XY = {
    val trRound = new StepColorTrack
    lookAroundAside(p1, size, p0)(trRound.stepFunc)
    val maxDiff = maxDiffPoint(trRound.maxDiffPP._1, trRound.maxDiffPP._2)
    maxDiff
  }

//  val y1=100
//  val y2=200
//  var rgbPrev=0
//  for( x <- 0 to 399 ){
//    val y = y1 + x*(y2-y1)/400
//    val rgb : Int = img.getRGB(x,y)
//    val diff = difColor(rgb, rgbPrev)
//    println("%d %d  %d".format(x,y, diff))
//    rgbPrev=rgb
//  }


//  var avgPrev=0;
//  def stepFunc(p:XY){
//    val rgb = colorAvg(p,size)
//    val diff = difColor(rgb, avgPrev)
//    avgPrev = rgb
//    println("%s %d".format(p, diff))
//  }
  val tr = new StepColorTrack
  stepLine(XY(0,100),XY(399,200),size)(tr.stepFunc)
  val maxDiff0 = maxDiffPoint(tr.maxDiffPP._1, tr.maxDiffPP._2)
  println(tr.maxDiffPP+ "  "+maxDiff0) // (XY(77,117),XY(84,118))  XY(81,117)
  println("------------------")
  val trRound = new StepColorTrack
  lookAround(maxDiff0, size)(trRound.stepFunc)
  val maxDiff1 = maxDiffPoint(trRound.maxDiffPP._1, trRound.maxDiffPP._2)
  println(trRound.maxDiffPP+ "  "+maxDiff1)

  paintDisk(maxDiff0, size/2, Color.BLUE)
  paintDisk(maxDiff1, size/2, Color.BLUE)

  var p0 = maxDiff0
  var p1 = maxDiff1
  for( i <- 1 to 85 ){
    val pnew = nextBorderPoint(p0,p1)
    paintDisk(pnew, size/2, Color.BLUE)
    p0 = p1
    p1 = pnew
  }

  //val trRound2 = new StepColorTrack
  //lookAroundAside(maxDiff1, size, maxDiff0)(trRound2.stepFunc)

  ImageIO.write(imgNew, "jpeg",new File("/tmp/apple2.jpeg"))
}