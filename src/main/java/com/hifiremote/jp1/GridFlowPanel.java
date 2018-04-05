package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;

// TODO: Auto-generated Javadoc
/**
 * The Class GridFlowPanel.
 */
public class GridFlowPanel
  extends JPanel
  implements Scrollable
{
  
  /**
   * Instantiates a new grid flow panel.
   */
  GridFlowPanel()
  {
    super( new GridFlowLayout());
  }

  /* (non-Javadoc)
   * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
   */
  public Dimension getPreferredScrollableViewportSize() 
  {
    return getPreferredSize();
  }

  /* (non-Javadoc)
   * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
   */
  public int getScrollableUnitIncrement( Rectangle visibleRect,
                                         int orientation,
                                         int direction )
  {
    return 1;
  }

  /* (non-Javadoc)
   * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
   */
  public int getScrollableBlockIncrement( Rectangle visibleRect,
                                         int orientation,
                                         int direction) 
  {
    return 10;
  }

  /* (non-Javadoc)
   * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
   */
  public boolean getScrollableTracksViewportWidth() 
  {
      return true;
  }

  /* (non-Javadoc)
   * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
   */
  public boolean getScrollableTracksViewportHeight() 
  {
      return false;
  }
}
