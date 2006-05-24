package com.hifiremote.jp1;

import java.awt.Component;
import javax.swing.*;
import java.text.*;
import javax.swing.table.*;

public class ByteEditor
  extends DefaultCellEditor
{
  public ByteEditor( Parameter parm )
  {
    this( 8, parm );
  }

  public ByteEditor( int bits, Parameter parm )
  {
    this( 0, ( 1 << bits ) - 1, parm );
  }

  public ByteEditor( int min, int max, Parameter parm )
  {
    super( new JTextField());
    setClickCountToStart( 1 );
    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.CENTER );
    this.min = min;
    this.max = max;
    this.parm = parm;
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
      tf.setText( Integer.toString((( Integer )value ).intValue(), base ));

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
      int temp = Integer.parseInt( str, base );
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

    if ( parm != null )
      parm.setValue( rc );
    return rc;
  }

  public void setBase( int base )
  {
    this.base = base;
  }

  public int getBase(){ return base; }

  private int min;
  private int max;
  private Parameter parm = null;
  private int base = 10;
}
