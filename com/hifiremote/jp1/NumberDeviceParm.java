package com.hifiremote.jp1;

import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;

public class NumberDeviceParm
  extends DeviceParameter
{
  public NumberDeviceParm( String name, DefaultValue defaultValue )
  {
    this( name, defaultValue, 10 );
  }

  public NumberDeviceParm( String name, DefaultValue defaultValue, int base )
  {
    this( name, defaultValue, base, 8 );
  }

  public NumberDeviceParm( String name, DefaultValue defaultValue, int base, int bits )
  {
    super( name, defaultValue );
    this.bits = bits;
    min = 0;
    max = (( 1 << bits ) - 1 );
    this.base = base;
    verifier = new IntVerifier( min, max, true );
    verifier.setBase(base);
    tf = new JTextField();
    tf.setInputVerifier( verifier );
    setToolTipText();
  }
  
  private void setToolTipText()
  {
    String numType = "";
    if ( base == 16 )
      numType = "hex ";
    String helpText = "Enter a " + numType
      + "number in the range " + min + ".." + max + ".";
    if (( defaultValue != null ) && ( defaultValue.value() != null ))
      helpText += "The default is " + Integer.toString((( Integer )defaultValue.value()).intValue(), base ) + '.';
    tf.setToolTipText( helpText );
  }    

  public void setBits( int bits )
  {
    this.bits = bits;
    max = (( 1 << bits ) - 1 );
    verifier.setMax( max );
    setToolTipText();
  }

  public JComponent getComponent()
  {
    return tf;
  }

  public void addListener( EventListener l )
  {
    tf.addActionListener(( ActionListener )l );
    tf.addFocusListener(( FocusListener)l );
    tf.getDocument().addDocumentListener(( DocumentListener )l );
  }

  public void removeListener( EventListener l )
  {
    tf.removeActionListener(( ActionListener )l );
    tf.removeFocusListener(( FocusListener )l );
    tf.getDocument().removeDocumentListener(( DocumentListener )l );
  }

  public Object getValue()
  {
    String text = tf.getText();
    if (( text == null ) || ( text.length() == 0 ))
      return null;
    Integer rc = Integer.valueOf( text, base );
    return rc;
  }

  public void setValue( Object value )
  {
    if ( value == null )
      tf.setText( "" );
    else
    {
      String temp = null;
      Class aClass = value.getClass();
      if (( aClass == Integer.class ) && ( base != 10 ))
        temp = Integer.toHexString((( Integer )value ).intValue());
      else
        temp = value.toString();
      tf.setText( temp );
    }
  }

  public String toString()
  {
    StringBuilder buff = new StringBuilder();
    buff.append( name );
    if (( base == 16 ) || ( bits != 8 ))
      buff.append( ':' );
    if ( base == 16 )
      buff.append( '$' );
    if ( bits != 8 )
      buff.append( bits );
    if ( defaultValue != null )
    {
      buff.append( '=' );
      buff.append( defaultValue );
    }
    return buff.toString();
  }

  public String getDescription(){ return "Number"; }

  private JTextField tf = null;
  private int bits = 8;
  private int min;
  private int max;
  private int base = 10;
  private IntVerifier verifier = null;
}
