package predict.singletarget;

/**
*/
public class BoolCount {
  long cTrue;
  long cFalse;
  void inc(boolean flag){
    if( flag ){
      cTrue++;
    }else{
      cFalse++;
    }
  }

  @Override
  public String toString() {
    return "T"+cTrue + " F" + cFalse;
  }
}
