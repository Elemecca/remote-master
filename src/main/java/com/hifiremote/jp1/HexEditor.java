package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;

// TODO: Auto-generated Javadoc
/**
 * The Class HexEditor.
 */
public class HexEditor
  extends SelectAllCellEditor
{
  
  /**
   * Instantiates a new hex editor.
   */
  public HexEditor()
  {
    this( null );
  }

  /**
   * Instantiates a new hex editor.
   * 
   * @param defaultHex the default hex
   */
  public HexEditor( Hex defaultHex )
  {
    super();
    this.defaultHex = defaultHex;
//    (( JTextField )getComponent()).setHorizontalAlignment( SwingConstants.CENTER );
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
      tf.setText((( Hex )value ).toString());

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
      Hex temp = new Hex( str );
      if ( defaultHex == null )
        rc = temp;
      else if ( temp.length() != defaultHex.length() )
      {
        String msg = "The hex command must contain exactly " + defaultHex.length() + " bytes.";
        KeyMapMaster.showMessage( msg, tf );
        throw new NumberFormatException( msg );
      }
      else
      {
        KeyMapMaster.clearMessage( tf );
        rc = temp;
      }
    }

    return rc;
  }

  /** The default hex. */
  Hex defaultHex = null;
}

