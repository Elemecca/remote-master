package com.hifiremote.jp1;

import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

public class NumberDeviceParm
  extends DeviceParameter
{
  public NumberDeviceParm( String name, Integer defaultValue )
  {
    this( name, defaultValue, 8 );
  }

  public NumberDeviceParm( String name, Integer defaultValue, int bits )
  {
    this( name, defaultValue, 0, (( 1 << bits ) - 1 ));
  }

  public NumberDeviceParm( String name, Integer defaultValue, int min, int max )
  {
    super( name, defaultValue );
    this.min = min;
    this.max = max;
    IntOrNullFormatter formatter = new IntOrNullFormatter( min, max );
    formatter.setAllowsInvalid( false );
    tf = new JFormattedTextField( formatter );
    String helpText = "Enter a number in the range " + min + ".." + max + ".";
    if ( defaultValue != null )
      helpText += "  The default is " + defaultValue + ".";
    tf.setToolTipText( helpText );
    tf.setFocusLostBehavior( JFormattedTextField.COMMIT_OR_REVERT );
  }

  public JComponent getComponent()
  {
    return tf;
  }

  public Object getValue()
  {
    return ( Integer )tf.getValue();
  }

  public void setValue( Object value )
  {
    tf.setValue( value );
  }

  public void commit()
  {
    try
    {
      if ( tf.isEditValid())
        tf.commitEdit();
    }
    catch ( ParseException e )
    {
    }
  }

  private JFormattedTextField tf = null;
  private int min;
  private int max;
}
