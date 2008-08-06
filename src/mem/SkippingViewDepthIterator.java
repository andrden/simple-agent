package mem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: adenysenko
 * Date: 2/5/2008
 * Time: 17:52:48
 */
public class SkippingViewDepthIterator implements ViewDepthIterator {
  ViewDepthIterator it;
  List<ViewDepth> skips = new ArrayList<ViewDepth>();

  public SkippingViewDepthIterator(ViewDepthIterator it) {
    this.it = it;
  }

  public ViewDepth next() {
    for (; ;) {
      ViewDepth d = it.next();
      if (d == null) {
        return null;
      }
      if (!shouldSkip(d)) {
        return d;
      }
      // continue search for new useful pattern
    }
  }

  public void addSkipPattern(ViewDepth d) {
    skips.add(d);
  }

  boolean shouldSkip(ViewDepth d) {
    for (ViewDepth skip : skips) {
      if (d.contains(skip)) {
        return true;
      }
    }
    return false;
  }
}
