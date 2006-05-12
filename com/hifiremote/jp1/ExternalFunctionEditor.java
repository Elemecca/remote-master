package com.hifiremote.jp1;

import java.awt.Component;
import javax.swing.*;
import java.text.*;
import javax.swing.table.*;

public class ExternalFunctionEditor
  extends DefaultCellEditor
{
  public ExternalFunctionEditor()
  {
    super( new JTextField());
//    setClickCountToStart( 1 );
    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.LEFT );
    this.min = 0;
    this.max = 255;
  }

  public Component getTableCellEditorComponent( JTable table, Object value,
                                                boolean isSelected, int row,
                                                int col )
  {
    JTextField tf =
      ( JTextField )super.getTableCellEditorComponent( table, value,
                                                       isSelected, row, col );

    f = ( ExternalFunction )value;
    tf.setText( f.toString());
    tf.selectAll();

    return tf;
  }

  public Object getCellEditorValue()
    throws NumberFormatException
  {
    Object rc = f;
    JTextField tf = ( JTextField )getComponent();
    String str = tf.getText().trim();
    if (( str != null ) && ( str.length() != 0 ))
    {
      if ( f.getType() == ExternalFunction.EFCType )
      {
        short temp = Short.parseShort( str );
        if (( temp < min ) || ( temp > max ))
        {
          String msg = "Value entered must be between " + min + " and " + max + '.';
          KeyMapMaster.showMessage( msg );
          throw new NumberFormatException( msg );
        }
        else
        {
          KeyMapMaster.clearMessage();
          f.setEFC( new EFC( temp ));
        }
      }
      else
        f.setHex( new Hex( str ));
    }
    else
      f.setHex( null );

    KeyMapMaster.clearMessage();
    return rc;
  }

  private int min;
  private int max;
  private ExternalFunction f;
}
