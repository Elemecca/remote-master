package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class EFCEditor
  extends DefaultCellEditor
{
  public EFCEditor()
  {
    super( new JTextField());
//    setClickCountToStart( 1 );
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
      tf.setText((( EFC )value ).toString());
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
      rc = new EFC( str );

    return rc;
  }
}

