package com.hifiremote.jp1;

import javax.swing.*;
import java.util.*;
import java.awt.event.*;

public class ChoiceDeviceParm
  extends DeviceParameter
{
  public ChoiceDeviceParm( String name, DefaultValue defaultValue, String[] choices )
  {
    super( name, defaultValue );
    this.choices = choices;
    comboBox = new JComboBox( choices );
    // JSF28may03 Questionable design decision: ChoiceDeviceParm doesn't dynamically correct default in ToolTip (compare vs. NumberDeviceParm)
    // JSF28may03 Questionable design decision: DeviceParameter always has non null defaultValue
    String helpText = "Select a value from the list.  The default value is "
     + choices[ ((Integer)getDefaultValue().value()).intValue() + 1 ] + '.';
    comboBox.setToolTipText( helpText );
  }

  public JComponent getComponent(){ return comboBox; }

  public void addListener( EventListener l )
  {
    comboBox.addActionListener(( ActionListener )l );
  }

  public void removeListener( EventListener l )
  {
    comboBox.removeActionListener(( ActionListener )l );
  }

  public Object getValue()
  {
    Object rc = null;
    int index = comboBox.getSelectedIndex();
    if ( index != 0 )
      rc = new Integer( index - 1 );
    return rc;
  }

  public void setValue( Object val )
  {
    int index = 0;
    if ( val != null )
    {
      Class c = val.getClass();
      if ( c == Integer.class )
       comboBox.setSelectedIndex((( Integer )val ).intValue() + 1 );
      else if ( c == String.class )
        comboBox.setSelectedItem( val );
    }
    else
      comboBox.setSelectedIndex( index );
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer();
    buff.append( name );
    buff.append( ':' );
    for ( int i = 1; i < choices.length; i ++ )
    {
      if ( i > 0 )
        buff.append( '|' );
      buff.append( choices[ i ]);
    }
    if ( defaultValue != null )
    {
      buff.append( '=' );
      buff.append( defaultValue );
    }
    return buff.toString();
  }

  public String getDescription(){ return "Choice"; }

  private JComboBox comboBox = null;
  private String[] choices = null;
}
