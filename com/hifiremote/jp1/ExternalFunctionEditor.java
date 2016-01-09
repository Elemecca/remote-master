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
    f = ( ExternalFunction )value;
    JTextField tf = ( JTextField )super.getTableCellEditorComponent( table, f.getValue(), isSelected, row, col );
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
      if ( f.get_Type() == ExternalFunction.EFCType )
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
      else if ( f.get_Type() == ExternalFunction.EFC5Type )
      {
        int temp = Integer.parseInt( str );
        if ( temp >= 0x10100 )
        {
          String msg = "Value entered must be less than " + 0x10100 + '.';
          JP1Frame.showMessage( msg, tf );
          throw new NumberFormatException( msg );
        }
        else
        {
          JP1Frame.clearMessage( tf );
          f.setEFC5( new EFC5( temp ) );
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
