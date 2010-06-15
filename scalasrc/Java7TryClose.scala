
object Java7TryClose {
  trait Closable{
    def close()
  }

  def close(cl:Closable*){
    for(i<-cl){
      println("Closing "+i)
      try{ i.close() }catch{case e:Throwable => e.printStackTrace()}
    }
    println()
    println()
  }

  def tryClose[A1 <: Closable,B](objs : A1)(f: A1=>B):B={
    try{
      f(objs)
    }finally{
      close(objs)
    }
  }

  def tryClose[A1 <: Closable,A2 <: Closable,B](o1 : A1, o2: A2)(f: (A1,A2)=>B):B={
    try{
      f(o1, o2)
    }finally{
      close(o1,o2)
    }
  }

  def tryClose[A1 <: Closable,A2 <: Closable, A3 <: Closable, B](o1 : A1, o2: A2, o3:A3)(f: (A1,A2,A3)=>B):B={
    try{
      f(o1, o2, o3)
    }finally{
      close(o1,o2,o3)
    }
  }

}