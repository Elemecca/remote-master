package com.hifiremote.jp1;

import javax.swing.*;
import java.util.*;

public abstract class DeviceParameter
  extends Parameter
{
  public DeviceParameter( String name )
  {
    this( name, null );
  }

  public DeviceParameter( String name, DefaultValue defaultValue )
  {
    super( name, defaultValue );
    name = getDisplayName();
    if ( name.length() > 0 )
      label = new JLabel( name + ':', SwingConstants.RIGHT );
    else
      label = new JLabel();
  }
  public JLabel getLabel(){ return label; }

  public void commit(){}
  public abstract JComponent getComponent();
  public abstract void addListener( EventListener l );
  public abstract void removeListener( EventListener l );

  private JLabel label;
}
