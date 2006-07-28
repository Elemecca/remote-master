package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class KeyMoveEditor
  extends DefaultCellEditor
  implements TableCellEditor, ActionListener 
{
  JButton button;
  protected static final String EDIT = "edit";
  private RemoteConfiguration config;
  private KeyMove keyMove;

  public KeyMoveEditor() 
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

  public void setRemoteConfiguration( RemoteConfiguration config )
  {
    this.config = config;
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
      KeyMove result = KeyMoveDialog.showDialog( button, keyMove, config );
      if ( result != null )
      {
        keyMove = result;
        fireEditingStopped();
      }
      else
        fireEditingCanceled();
    }
  }

  //Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue() 
  {
    return keyMove;
  }

  //Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    keyMove = ( KeyMove )value;
    String text = null;
    if ( column == 5 )
    {
      text = keyMove.getData().toString();
      if ( keyMove instanceof KeyMoveKey )
        text += " (keycode)";
    }
    else if ( column == 6 )
    {
      Hex cmd = keyMove.getCmd();
      if ( cmd != null )
        text = cmd.toString();
    }
    else if ( column == 7 )
      text = keyMove.getValueString( config.getRemote());
    button.setText( text );
    return button;
  }
}


