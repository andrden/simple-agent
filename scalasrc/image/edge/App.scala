package image.edge

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import Math.{abs,sin,cos}
import java.io.File
import javax.swing.JFrame
import java.awt.{Canvas, Color}

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


object App extends Application{
  //val imgFile = "/image/edge/green_apple_blur.jpg" 
  //val imgFile =  "/image/edge/green_apple.jpg"
  val imgFile =  "/image/edge/apple-leafs.jpg"
  //val imgFile =  "/image/edge/apple-leafs_blur.jpg"

  val imgStream = getClass.getResourceAsStream(imgFile)
  val img : BufferedImage = ImageIO.read(imgStream)
  val imgNew = new BufferedImage(img.getWidth, img.getHeight, img.getType)
  imgNew.getGraphics.drawImage(img,0,0,null)
  println("width %d heigth %d".format(img.getWidth, img.getHeight))


  ImageIO.write(GaussianBlur.blur(img, 10, 5), "jpeg", new File("/tmp/apple-leafs_blurG.jpeg"))
  System exit 0

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
      //println("lookAroundAside"+deg)
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

  val size=30 //40 //8

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
      //println("%s %d".format(p, diff))
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

  def borderWalker(cutPointA:XY, cutPointB:XY)(f: (XY)=>Boolean){
    val tr = new StepColorTrack
    stepLine(cutPointA, cutPointB, size)(tr.stepFunc)
    val maxDiff0 = maxDiffPoint(tr.maxDiffPP._1, tr.maxDiffPP._2)
    if( !f(maxDiff0) ) return

    val trRound = new StepColorTrack
    lookAround(maxDiff0, size)(trRound.stepFunc)
    val maxDiff1 = maxDiffPoint(trRound.maxDiffPP._1, trRound.maxDiffPP._2)

    var p0 = maxDiff0
    var p1 = maxDiff1
    while( f(p1) ){
      val pnew = nextBorderPoint(p0,p1)
      p0 = p1
      p1 = pnew
    }
  }

  def paintBlur(){
    for( x <- 0 until img.getWidth ){
      println("blur x="+x)
      for( y <- 0 until img.getHeight ){
        imgNew.setRGB(x+img.getWidth, y, colorAvg(XY(x,y), size))
      }
    }
  }

  def randXY = XY( (Math.random * img.getWidth).intValue, (Math.random * img.getHeight).intValue )

  def newFollowSegment(canvas: Canvas) = {
    val lineBeg = randXY // XY(0,100)
    val lineEnd = randXY // XY(399,200)

    canvas.getGraphics.drawLine( lineBeg.x, lineBeg.y, lineEnd.x, lineEnd.y )

    var pointsFound : Int = 0
    def nextPoint(p:XY):Boolean = {
      println(p)
      paintDisk(p, size/2, Color.BLUE)

      val ovalSz = 8
      val g = canvas.getGraphics
      g.setColor(Color.BLUE)
      g.drawOval(p.x-ovalSz/2,p.y-ovalSz/2, ovalSz,ovalSz)
      Thread sleep 80

      pointsFound += 1
      pointsFound < 380
    }

    borderWalker(lineBeg, lineEnd)(nextPoint)
  }

  val jframe = new JFrame("image.edge")
  val canvas = new Canvas
  jframe.add(canvas)
  jframe.setSize(400,400)
  jframe setVisible true

  Thread sleep 2000
  canvas.getGraphics.drawImage(img,0,0,null)

  var ok=false
  while(!ok){
    try{
      newFollowSegment(canvas)
      ok=true;
    }catch {
      case e: Exception => println(e.toString + " will continue") 
    }
  }

  ImageIO.write(imgNew, "jpeg",new File("/tmp/apple2.jpeg"))
}