package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

public class FlagDeviceParm
  extends DeviceParameter
{
  public FlagDeviceParm( String name, DefaultValue defaultValue )
  {
    super( "", defaultValue );
    checkBox = new JCheckBox( name );
    setValue( defaultValue.value());
  }

  public Object getValue()
  {
    if ( checkBox.isSelected())
      return Boolean.TRUE;
    else
      return Boolean.FALSE;
  }

  public void setValue( Object value )
  {
    boolean flag = false;
    if ( value != null )
    {
      if ( value.getClass() == Integer.class )
        flag = ((( Integer )value ).intValue() != 0 );
      else if ( value.getClass() == Boolean.class )
        flag = (( Boolean )value ).booleanValue();
    }
    checkBox.setSelected( flag );
  }

  public JComponent getComponent(){ return checkBox; }

  public void addListener( EventListener l )
  {
    checkBox.addItemListener(( ItemListener )l );
  }

  public void removeListener( EventListener l )
  {
    checkBox.removeItemListener(( ItemListener )l );
  }

  private JCheckBox checkBox = null;
}
