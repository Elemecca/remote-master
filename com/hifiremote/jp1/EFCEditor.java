package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class EFCEditor
  extends DefaultCellEditor
{
  public EFCEditor( int digits )
  {
    super( new JTextField());
    this.digits = digits;
    setClickCountToStart( 1 );
    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.CENTER );
  }

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
    tf.selectAll();

    return tf;
  }

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

  public void setDigits( int digits )
  {
    this.digits = digits;
  }

  private int digits = 3;
}

