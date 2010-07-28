package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class ByteEditor.
 */
public class ByteEditor extends SelectAllCellEditor
{

  /**
   * Instantiates a new byte editor.
   * 
   * @param parm
   *          the parm
   */
  public ByteEditor( Parameter parm )
  {
    this( 8, parm );
  }

  /**
   * Instantiates a new byte editor.
   * 
   * @param bits
   *          the bits
   * @param parm
   *          the parm
   */
  public ByteEditor( int bits, Parameter parm )
  {
    this( 0, ( 1 << bits ) - 1, parm );
  }

  /**
   * Instantiates a new byte editor.
   * 
   * @param min
   *          the min
   * @param max
   *          the max
   * @param parm
   *          the parm
   */
  public ByteEditor( int min, int max, Parameter parm )
  {
    super();
    setClickCountToStart( RMConstants.ClickCountToStart );
    ( ( JTextField )getComponent() ).setHorizontalAlignment( SwingConstants.CENTER );
    this.min = min;
    this.max = max;
    this.parm = parm;
  }

  /**
   * Sets the bits.
   * 
   * @param bits
   *          the new bits
   */
  public void setBits( int bits )
  {
    max = ( 1 << bits ) - 1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int,
   * int)
   */
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int col )
  {
    JTextField tf = ( JTextField )super.getTableCellEditorComponent( table, value, isSelected, row, col );

    if ( value == null )
    {
      tf.setText( "" );
    }
    else
    {
      tf.setText( Integer.toString( ( ( Integer )value ).intValue(), base ) );
    }

    return tf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  @Override
  public Object getCellEditorValue() throws NumberFormatException
  {
    Object rc = null;
    JTextField tf = ( JTextField )getComponent();
    String str = tf.getText().trim();
    if ( str != null && str.length() != 0 )
    {
      int temp = Integer.parseInt( str, base );
      if ( temp < min || temp > max )
      {
        String msg = "Value entered must be between " + min + " and " + max + '.';
        JP1Frame.showMessage( msg, tf );
        throw new NumberFormatException( msg );
      }
      else
      {
        JP1Frame.clearMessage( tf );
        rc = new Integer( temp );
      }
    }

    if ( parm != null )
    {
      parm.setValue( rc );
    }
    return rc;
  }

  /**
   * Sets the base.
   * 
   * @param base
   *          the new base
   */
  public void setBase( int base )
  {
    this.base = base;
  }

  /**
   * Gets the base.
   * 
   * @return the base
   */
  public int getBase()
  {
    return base;
  }

  /** The min. */
  private int min;

  /** The max. */
  private int max;

  /** The parm. */
  private Parameter parm = null;

  /** The base. */
  private int base = 10;
}
