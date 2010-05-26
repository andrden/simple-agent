package image.edge

import java.awt.image.BufferedImage

/**
 * Created by IntelliJ IDEA.
 * User: denny
 * Date: May 26, 2010
 * Time: 4:37:54 PM
 * To change this template use File | Settings | File Templates.
 */

object GaussianBlur{
  def blur(img: BufferedImage, size: Int, iterations: Int) : BufferedImage = {
    if( iterations==0 )
      img
    else
      blur(blurSingle(img, size), size, iterations-1)
  }

  private def blurSingle(img: BufferedImage, size: Int) = {
    val imgNew = new BufferedImage(img.getWidth, img.getHeight, img.getType)
    blurX(img, size, imgNew)

    val imgNew2 = new BufferedImage(img.getWidth, img.getHeight, img.getType)
    blurY(imgNew, size, imgNew2)
    imgNew2
  }

  private def blurX(img: BufferedImage, size: Int, result: BufferedImage){
    for( y <- 0 until img.getHeight ){
      for( x <- 0 until img.getWidth ){
        val avg = new ColorAvg
        for( xi <- (x-size) to (x+size) ; if xi>=0 && xi < img.getWidth ){
          avg.update(img.getRGB(xi, y))
        }
        result.setRGB(x, y, avg.toRgb)
      }
    }
  }

  private def blurY(img: BufferedImage, size: Int, result: BufferedImage){
    for( y <- 0 until img.getHeight ){
      for( x <- 0 until img.getWidth ){
        val avg = new ColorAvg
        for( yi <- (y-size) to (y+size) ; if yi>=0 && yi < img.getHeight ){
          avg.update(img.getRGB(x, yi))
        }
        result.setRGB(x, y, avg.toRgb)
      }
    }
  }

}