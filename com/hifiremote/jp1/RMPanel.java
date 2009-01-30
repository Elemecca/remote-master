package com.hifiremote.jp1;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

// TODO: Auto-generated Javadoc
/**
 * The Class RMPanel.
 */
public abstract class RMPanel
  extends JPanel
{
  
  /**
   * Instantiates a new rM panel.
   */
  public RMPanel()
  {
    super( new BorderLayout());
    setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
  }
  
  /* (non-Javadoc)
   * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public abstract void addPropertyChangeListener( PropertyChangeListener listener );
}

