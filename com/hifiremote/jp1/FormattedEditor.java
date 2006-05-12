package com.hifiremote.jp1;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.text.*;
import javax.swing.text.*;

/**
 * Implements a cell editor that uses a formatted text field
 * to edit Integer values.
 */
public class FormattedEditor
  extends DefaultCellEditor
{
  Format format;

  public FormattedEditor( Format format )
  {
    super( new JTextField());
    this.format = format;
  }

  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    String s = format.format( value );
    return super.getTableCellEditorComponent( table, s, isSelected, row, column );
  }

  //Override to ensure that the value remains an Integer.
  public Object getCellEditorValue() 
  {
    String s = ( String )getCellEditorValue();
    Object o = null;
    try 
    {
      o = format.parseObject( s );
    }
    catch ( java.text.ParseException exc )
    {
      throw new RuntimeException( exc );
    }
    return o;
  }

  //Override to check whether the edit is valid,
  //setting the value if it is and complaining if
  //it isn't.  If it's OK for the editor to go
  //away, we need to invoke the superclass's version 
  //of this method so that everything gets cleaned up.
  public boolean stopCellEditing()
  {
    JFormattedTextField ftf = (JFormattedTextField)getComponent();
    if ( ftf.isEditValid()) 
    {
      try
      {
            ftf.commitEdit();
      }
      catch ( java.text.ParseException exc ){ }
    }
    else
    { //text is invalid
      return false; //don't let the editor go away
    }
    return super.stopCellEditing();
  }
}

