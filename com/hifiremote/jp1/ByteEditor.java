package com.hifiremote.jp1;

import java.awt.Component;
import javax.swing.*;
import java.text.*;
import javax.swing.table.*;

public class ByteEditor
  extends DefaultCellEditor
{
  public ByteEditor()
  {
    this( 8 );
  }

  public ByteEditor( int bits )
  {
    this( 0, ( 1 << bits ) - 1);
  }

  public ByteEditor( int min, int max )
  {
    super( new JTextField());
    setClickCountToStart( 1 );
    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.CENTER );
    this.min = min;
    this.max = max;
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
//    tf.setText( Integer.toString( Translate.byte2int((( Integer )value ).byteValue())));
      tf.setText( (( Integer )value ).toString());

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
      int temp = Integer.parseInt( str );
      if (( temp < min ) || ( temp > max ))
      {
        String msg = "Value entered must be between " + min + " and " + max + '.';
        KeyMapMaster.showMessage( msg );
        throw new NumberFormatException( msg );
      }
      else
      {
        KeyMapMaster.clearMessage();
        rc = new Integer( temp );
      }
    }

    return rc;
  }

  private int min;
  private int max;
}
