package com.hifiremote.jp1;

import javax.swing.*;

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

  private JCheckBox checkBox = null;
}
