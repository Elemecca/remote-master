package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

public class GridFlowPanel
  extends JPanel
  implements Scrollable
{
  GridFlowPanel()
  {
    super( new GridFlowLayout());
  }

  public Dimension getPreferredScrollableViewportSize() 
  {
    return getPreferredSize();
  }

  public int getScrollableUnitIncrement( Rectangle visibleRect,
                                         int orientation,
                                         int direction )
  {
    return 1;
  }

  public int getScrollableBlockIncrement( Rectangle visibleRect,
                                         int orientation,
                                         int direction) 
  {
    int rc = 10;
    
    return 10;
  }

  public boolean getScrollableTracksViewportWidth() 
  {
      return true;
  }

  public boolean getScrollableTracksViewportHeight() 
  {
      return false;
  }
}
