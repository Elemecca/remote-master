package com.hifiremote.jp1;

import java.text.ParseException;
import javax.swing.JComponent;
import javax.swing.JTextField;

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
    verifier = new IntVerifier( min, max, true );
    tf = new JTextField();
    String helpText = "Enter a number in the range " + min + ".." + max + ".";
    if ( defaultValue != null )
      helpText += "  The default is " + defaultValue + ".";
    tf.setToolTipText( helpText );
    tf.setInputVerifier( verifier );
  }

  public NumberDeviceParm setBase( int base )
  {
    this.base = base;
    verifier.setBase( base );
    String numType = "";
    if ( base == 16 )
      numType = " hex ";
    String helpText = "Enter a " + numType +  
                      "number in the range " + 
                      Integer.toString( min, base ) + 
                      ".." +
                      Integer.toString( max, base ) + '.';
    if ( getDefaultValue() != null );
      helpText += "  The defaultValue is " + Integer.toString((( Integer )getDefaultValue()).intValue(), base ) + '.';
    tf.setToolTipText( helpText );
    return this;
  }

  public JComponent getComponent()
  {
    return tf;
  }

  public Object getValue()
  {
    String text = tf.getText();
    if (( text == null ) || ( text.length() == 0 ))
      return null;
    return Integer.valueOf( tf.getText(), base );
  }

  public void setValue( Object value )
  {
    if ( value == null )
      tf.setText( "" );
    else
    {
      String temp = null;
      if ( base == 10 )
        temp = value.toString();
      else
        temp = Integer.toHexString((( Integer )value ).intValue());
      tf.setText( temp );
    }
  }

  private JTextField tf = null;
  private int min;
  private int max;
  private int base = 10;
  private IntVerifier verifier = null;
}
