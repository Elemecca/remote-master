package com.hifiremote.jp1;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class HexEditor
  extends DefaultCellEditor
{
  public HexEditor( byte[] defaultHex )
  {
    super( new JTextField());
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
    tf.setText( Protocol.hex2String(( byte[] )value ));
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
      byte[] temp = Protocol.string2hex( str );
      if ( temp.length != defaultHex.length )
      {
        String msg = "The hex command must contain exactly " + defaultHex.length + " bytes.";
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

  byte[] defaultHex = null;
}

