package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class EFCEditor.
 */
public class EFCEditor
  extends SelectAllCellEditor
{
  
  /**
   * Instantiates a new eFC editor.
   * 
   * @param digits the digits
   */
  public EFCEditor( int digits )
  {
    super();
    this.digits = digits;
    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.CENTER );
  }

  /* (non-Javadoc)
   * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
   */
  public Component getTableCellEditorComponent( JTable table, Object value,
                                                boolean isSelected, int row,
                                                int col )
  {
    JTextField tf =
      ( JTextField )super.getTableCellEditorComponent( table, value,
                                                       isSelected, row, col );
    if ( value == null )
      tf.setText( "" );
    else
      tf.setText( value.toString());

    return tf;
  }

  /* (non-Javadoc)
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  public Object getCellEditorValue()
    throws NumberFormatException
  {
    Object rc = null;
    JTextField tf = ( JTextField )getComponent();
    String str = tf.getText().trim();
    if (( str != null ) && ( str.length() != 0 ))
    {
      if ( digits == 3 )
        rc = new EFC( str );
      else
        rc = new EFC5( str );
    }

    return rc;
  }

  /**
   * Sets the digits.
   * 
   * @param digits the new digits
   */
  public void setDigits( int digits )
  {
    this.digits = digits;
  }

  /** The digits. */
  private int digits = 3;
}

