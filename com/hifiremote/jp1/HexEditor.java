package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class HexEditor
  extends DefaultCellEditor
{
  public HexEditor( Hex defaultHex )
  {
    super( new JTextField());
    setClickCountToStart( 1 );
    this.defaultHex = defaultHex;
    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.CENTER );
  }

  public Component getTableCellEditorComponent( JTable table, Object value,
                                                boolean isSelected, int row,
                                                int col )
  {
    JTextField tf =
      ( JTextField )super.getTableCellEditorComponent( table, value,
                                                       isSelected, row, col );
    tf.setText((( Hex )value ).toString());
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
      Hex temp = new Hex( str );
      if ( temp.length() != defaultHex.length() )
      {
        String msg = "The hex command must contain exactly " + defaultHex.length() + " bytes.";
        KeyMapMaster.showMessage( msg );
        throw new NumberFormatException( msg );
      }
      else
      {
        KeyMapMaster.clearMessage();
        rc = temp;
      }
    }

    return rc;
  }

  Hex defaultHex = null;
}

