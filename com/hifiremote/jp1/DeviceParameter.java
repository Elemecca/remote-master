package com.hifiremote.jp1;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

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
    if ( name.length() > 0 )
      label = new JLabel( name + ':', SwingConstants.RIGHT );
    else
      label = new JLabel();
  }
  public JLabel getLabel(){ return label; }

  public void commit(){}
  public abstract JComponent getComponent();

  private JLabel label;
}
