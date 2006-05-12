package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class KeyEditor
  extends DefaultCellEditor
  implements TableCellEditor, ActionListener 
{
  JButton button;
  protected static final String EDIT = "edit";
  private Remote remote;
  private Integer keyCode;

  public KeyEditor() 
  {
    super( new JTextField());
    setClickCountToStart( 2 );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ));
    button.setHorizontalAlignment( SwingConstants.LEADING );
    button.setActionCommand( EDIT );
    button.addActionListener( this );
    button.setBorderPainted( false );
  }

  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }

  /**
   * Handles events from the editor button and from
   * the dialog's OK button.
   */
  public void actionPerformed( ActionEvent e )
  {
    if ( EDIT.equals( e.getActionCommand())) 
    {
      // The user has clicked the cell, so
      // bring up the dialog.
      Integer result = KeyChooser.showDialog( button, remote, keyCode );
      if (( result != null ) && !result.equals( keyCode ))
      {
        keyCode = result;
        fireEditingStopped();
      }
      else
        fireEditingCanceled();
    }
  }

  //Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue() 
  {
    return keyCode;
  }

  //Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    keyCode = ( Integer )value;
    button.setText( remote.getButtonName( keyCode.intValue()));
    return button;
  }
}


