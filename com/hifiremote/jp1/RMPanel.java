package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

// TODO: Auto-generated Javadoc
/**
 * The Class RMPanel.
 */
public abstract class RMPanel extends JPanel
{

  /**
   * Instantiates a new rM panel.
   */
  public RMPanel()
  {
    super( new BorderLayout() );
  }
  
  public abstract void set( RemoteConfiguration remoteConfig );

  protected void refresh()
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public abstract void addPropertyChangeListener( PropertyChangeListener listener );
}
