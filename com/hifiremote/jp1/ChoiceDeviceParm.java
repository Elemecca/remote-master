package com.hifiremote.jp1;

import javax.swing.*;
import java.util.*;
import java.awt.event.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ChoiceDeviceParm.
 */
public class ChoiceDeviceParm
  extends DeviceParameter
{
  
  /**
   * Instantiates a new choice device parm.
   * 
   * @param name the name
   * @param defaultValue the default value
   * @param choices the choices
   */
  public ChoiceDeviceParm( String name, DefaultValue defaultValue, String[] choices )
  {
    super( name, defaultValue );
    this.choices = choices;
    if ( choices[ 0 ].equals( "" ))
      allowNull = true;
    comboBox = new JComboBox( choices );
    // JSF28may03 Questionable design decision: ChoiceDeviceParm doesn't dynamically correct default in ToolTip (compare vs. NumberDeviceParm)
    // JSF28may03 Questionable design decision: DeviceParameter always has non null defaultValue
    String helpText = "Select a value from the list.  The default value is "
     + choices[ ((Integer)getDefaultValue().value()).intValue() + 1 ] + '.';
    comboBox.setToolTipText( helpText );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DeviceParameter#getComponent()
   */
  public JComponent getComponent(){ return comboBox; }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DeviceParameter#addListener(java.util.EventListener)
   */
  public void addListener( EventListener l )
  {
    comboBox.addActionListener(( ActionListener )l );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.DeviceParameter#removeListener(java.util.EventListener)
   */
  public void removeListener( EventListener l )
  {
    comboBox.removeActionListener(( ActionListener )l );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getValue()
   */
  public Object getValue()
  {
    Object rc = null;
    int index = comboBox.getSelectedIndex();
    if ( allowNull )
    {
      if ( index != 0 )
        rc = new Integer( index - 1 );
    }
    else
      rc = new Integer( index );
    return rc;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#setValue(java.lang.Object)
   */
  public void setValue( Object val )
  {
    if ( val == null )
      comboBox.setSelectedIndex( 0 );
    else
    {
      Class<?> c = val.getClass();
      if ( c == Integer.class )
      {
        int index = (( Integer )val ).intValue();
        if ( allowNull )
          index++;
        comboBox.setSelectedIndex( index );
      }
      else if ( c == String.class )
        comboBox.setSelectedItem( val );
      else
        comboBox.setSelectedIndex( 0 );
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder buff = new StringBuilder();
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

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.Parameter#getDescription()
   */
  public String getDescription(){ return "Choice"; }

  /** The combo box. */
  private JComboBox comboBox = null;
  
  /** The choices. */
  private String[] choices = null;
  
  /** The allow null. */
  private boolean allowNull = false;
}
