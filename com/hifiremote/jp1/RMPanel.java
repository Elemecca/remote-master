package com.hifiremote.jp1;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

public abstract class RMPanel
  extends JPanel
{
  public RMPanel()
  {
    super( new BorderLayout());
    setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
  }
  
  public abstract void addPropertyChangeListener( PropertyChangeListener listener );
}

