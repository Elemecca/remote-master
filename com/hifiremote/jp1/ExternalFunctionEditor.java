package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class ExternalFunctionEditor.
 */
public class ExternalFunctionEditor extends SelectAllCellEditor
{

  /**
   * Instantiates a new external function editor.
   */
  public ExternalFunctionEditor()
  {
    super();
    ( ( JTextField )getComponent() ).setHorizontalAlignment( SwingConstants.LEFT );
    this.min = 0;
    this.max = 255;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int,
   * int)
   */
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int col )
  {
    JTextField tf = ( JTextField )super.getTableCellEditorComponent( table, value, isSelected, row, col );

    f = ( ExternalFunction )value;
    if ( f.getType() == ExternalFunction.EFCType )
    {
      EFC efc = f.getEFC();
      if ( efc != null )
        tf.setText( efc.toString() );
      else
        tf.setText( "" );
    }
    else
    {
      Hex hex = f.getHex();
      if ( hex != null )
      {
        tf.setText( hex.toString() );
      }
      else
        tf.setText( "" );
    }

    return tf;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  public Object getCellEditorValue() throws NumberFormatException
  {
    Object rc = f;
    JTextField tf = ( JTextField )getComponent();
    String str = tf.getText().trim();
    if ( ( str != null ) && ( str.length() != 0 ) )
    {
      if ( f.getType() == ExternalFunction.EFCType )
      {
        short temp = Short.parseShort( str );
        if ( ( temp < min ) || ( temp > max ) )
        {
          String msg = "Value entered must be between " + min + " and " + max + '.';
          JP1Frame.showMessage( msg, tf );
          throw new NumberFormatException( msg );
        }
        else
        {
          JP1Frame.clearMessage( tf );
          f.setEFC( new EFC( temp ) );
        }
      }
      else
        f.setHex( new Hex( str ) );
    }
    else
      f.setHex( null );

    JP1Frame.clearMessage( tf );
    return rc;
  }

  /** The min. */
  private int min;

  /** The max. */
  private int max;

  /** The f. */
  private ExternalFunction f;
}
