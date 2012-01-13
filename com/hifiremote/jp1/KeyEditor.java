package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyEditor.
 */
public class KeyEditor extends DefaultCellEditor implements TableCellEditor, ActionListener
{

  /** The button. */
  JButton button;

  /** The Constant EDIT. */
  protected static final String EDIT = "edit";

  /** The remote. */
  private Remote remote;

  /** The key code. */
  private Integer keyCode;

  /**
   * Instantiates a new key editor.
   */
  public KeyEditor()
  {
    super( new JTextField() );
    setClickCountToStart( RMConstants.ClickCountToStart );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
    button.setHorizontalAlignment( SwingConstants.LEADING );
    button.setActionCommand( EDIT );
    button.addActionListener( this );
    button.setBorderPainted( false );
  }

  /**
   * Sets the remote.
   * 
   * @param remote
   *          the new remote
   */
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }

  /**
   * Handles events from the editor button and from the dialog's OK button.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed( ActionEvent e )
  {
    if ( EDIT.equals( e.getActionCommand() ) )
    {
      // The user has clicked the cell, so
      // bring up the dialog.
      Integer result = KeyChooser.showDialog( button, remote, keyCode );
      if ( result != null && !result.equals( keyCode ) )
      {
        keyCode = result;
        fireEditingStopped();
      }
      else
      {
        fireEditingCanceled();
      }
    }
  }

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  @Override
  public Object getCellEditorValue()
  {
    return keyCode;
  }

  // Implement the one method defined by TableCellEditor.
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int,
   * int)
   */
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    keyCode = ( Integer )value;
    if ( keyCode == null )
    {
      keyCode = ( int )remote.getButtons().get( 0 ).getKeyCode();
    }
    button.setText( remote.getButtonName( keyCode.intValue() ) );
    return button;
  }
}
