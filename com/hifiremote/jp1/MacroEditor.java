package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class MacroEditor
  extends DefaultCellEditor
  implements TableCellEditor, ActionListener 
{
  JButton button;
  protected static final String EDIT = "edit";
  private RemoteConfiguration config;
  private Macro macro;

  public MacroEditor() 
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
      Macro result = MacroDialog.showDialog( button, macro, config );
      if ( result != null )
      {
        macro = result;
        fireEditingStopped();
      }
      else
        fireEditingCanceled();
    }
  }

  //Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue() 
  {
    return macro;
  }

  //Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    macro = ( Macro )value;
    button.setText( macro.getValueString( config.getRemote()));
    return button;
  }
}


