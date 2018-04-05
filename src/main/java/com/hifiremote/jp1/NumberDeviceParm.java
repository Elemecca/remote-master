package com.hifiremote.jp1;

import java.awt.event.ActionListener;
import java.util.EventListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

// TODO: Auto-generated Javadoc
/**
 * The Class NumberDeviceParm.
 */
public class NumberDeviceParm extends DeviceParameter
{

  /**
   * Instantiates a new number device parm.
   * 
   * @param name
   *          the name
   * @param defaultValue
   *          the default value
   */
  public NumberDeviceParm( String name, DefaultValue defaultValue )
  {
    this( name, defaultValue, 10 );
  }

  /**
   * Instantiates a new number device parm.
   * 
   * @param name
   *          the name
   * @param defaultValue
   *          the default value
   * @param base
   *          the base
   */
  public NumberDeviceParm( String name, DefaultValue defaultValue, int base )
  {
    this( name, defaultValue, base, 8 );
  }

  /**
   * Instantiates a new number device parm.
   * 
   * @param name
   *          the name
   * @param defaultValue
   *          the default value
   * @param base
   *          the base
   * @param bits
   *          the bits
   */
  public NumberDeviceParm( String name, DefaultValue defaultValue, int base, int bits )
  {
    super( name, defaultValue );
    this.bits = bits;
    min = 0;
    max = ( 1 << bits ) - 1;
    this.base = base;
    verifier = new IntVerifier( min, max, true );
    verifier.setBase( base );
    tf = new JTextField();
    new TextPopupMenu( tf );
    tf.setInputVerifier( verifier );
    FocusSelector.selectOnFocus( tf );
    setToolTipText();
  }

  /**
   * Sets the tool tip text.
   */
  private void setToolTipText()
  {
    String numType = "";
    if ( base == 16 )
    {
      numType = "hex ";
    }
    String helpText = "Enter a " + numType + "number in the range " + min + ".." + max + ".";
    if ( defaultValue != null && defaultValue.value() != null )
    {
      helpText += "The default is " + Integer.toString( ( ( Integer )defaultValue.value() ).intValue(), base ) + '.';
    }
    tf.setToolTipText( helpText );
  }

  /**
   * Sets the bits.
   * 
   * @param bits
   *          the new bits
   */
  public void setBits( int bits )
  {
    this.bits = bits;
    max = ( 1 << bits ) - 1;
    verifier.setMax( max );
    setToolTipText();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.DeviceParameter#getComponent()
   */
  @Override
  public JComponent getComponent()
  {
    return tf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.DeviceParameter#addListener(java.util.EventListener)
   */
  @Override
  public void addListener( EventListener l )
  {
    tf.addActionListener( ( ActionListener )l );
    tf.getDocument().addDocumentListener( ( DocumentListener )l );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.DeviceParameter#removeListener(java.util.EventListener)
   */
  @Override
  public void removeListener( EventListener l )
  {
    tf.removeActionListener( ( ActionListener )l );
    tf.getDocument().removeDocumentListener( ( DocumentListener )l );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Parameter#getValue()
   */
  @Override
  public Object getValue()
  {
    String text = tf.getText();
    if ( text == null || text.length() == 0 )
    {
      return null;
    }
    Integer rc = Integer.valueOf( text, base );
    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Parameter#setValue(java.lang.Object)
   */
  @Override
  public void setValue( Object value )
  {
    if ( value == null )
    {
      tf.setText( "" );
    }
    else
    {
      String temp = null;
      Class< ? > aClass = value.getClass();
      if ( aClass == Integer.class && base != 10 )
      {
        temp = Integer.toHexString( ( ( Integer )value ).intValue() );
      }
      else
      {
        temp = value.toString();
      }
      tf.setText( temp );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder buff = new StringBuilder();
    buff.append( name );
    if ( base == 16 || bits != 8 )
    {
      buff.append( ':' );
    }
    if ( base == 16 )
    {
      buff.append( '$' );
    }
    if ( bits != 8 )
    {
      buff.append( bits );
    }
    if ( defaultValue != null )
    {
      buff.append( '=' );
      buff.append( defaultValue );
    }
    return buff.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Parameter#getDescription()
   */
  @Override
  public String getDescription()
  {
    return "Number";
  }

  /** The tf. */
  private JTextField tf = null;

  /** The bits. */
  private int bits = 8;

  /** The min. */
  private int min;

  /** The max. */
  private int max;

  /** The base. */
  private int base = 10;

  /** The verifier. */
  private IntVerifier verifier = null;
}
