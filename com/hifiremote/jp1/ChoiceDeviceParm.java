package com.hifiremote.jp1;

import javax.swing.JComponent;
import javax.swing.JComboBox;

public class ChoiceDeviceParm
  extends DeviceParameter
{
  public ChoiceDeviceParm( String name, Integer defaultValue, String[] choices )
  {
    super( name, defaultValue );
    comboBox = new JComboBox( choices );
    String helpText = "Select a value from the list.";
    if ( defaultValue != null )
      helpText += "  The default value is " + choices[ defaultValue.intValue() + 1 ] + '.';
    comboBox.setToolTipText( helpText );
  }

  public JComponent getComponent(){ return comboBox; }
  public Integer getValue()
  {
    Integer rc = null;
    int index = comboBox.getSelectedIndex();
    if ( index != 0 )
      rc = new Integer( index - 1 );
    return rc;
  }
  public void setValue( Integer val )
  {
    int index = 0;
    if ( val != null )
      index = val.intValue() + 1;

    comboBox.setSelectedIndex( index );
  }

  private JComboBox comboBox = null;
}
