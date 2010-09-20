package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

public class ManualSettingsEditor extends DefaultCellEditor implements TableCellEditor, ActionListener
{

  public ManualSettingsEditor( Remote remote, int column )
  {
    super( new JTextField() );
    setClickCountToStart( RMConstants.ClickCountToStart );
    this.remote = remote;
    this.column = column;
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
    button.setHorizontalAlignment( SwingConstants.LEADING );
    button.setActionCommand( EDIT );
    button.addActionListener( this );
    button.setBorderPainted( false );
  }
  
  @Override
  public Object getCellEditorValue()
  {
    return value;
  }
  
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int col )
  {
    this.value = ( Protocol )value;
    switch ( column )
    {
      case 5:
        button.setText( this.value.getStarredID( remote ) );
        break;
      case 6:
        button.setText( this.value.getVariantName() );
        break;
      case 7:
        button.setText( this.value.toString() );
        break;
    }
    return button;
  }
  
  
  @Override
  public void actionPerformed( ActionEvent e )
  {
    if ( EDIT.equals( e.getActionCommand() ) )
    {
      // The user has clicked the cell, so
      // bring up the dialog.
      
      ManualProtocol mp = null;
      if ( value.getClass() == ManualProtocol.class )
      {
        mp = ( ManualProtocol )value;
//          mp = new ManualProtocol(null, null);
      }
      ManualSettingsDialog dialog = new ManualSettingsDialog( ( JFrame )SwingUtilities.getRoot( button ), mp );

      dialog.setVisible( true );
      Protocol result = dialog.getProtocol();

      if ( result != null )
      {
        value = result;
        fireEditingStopped();
      }
      else
      {
        fireEditingCanceled();
      }
    }
  }
  
  private JButton button = null;
  private Protocol value = null;
  private int column = 0;
  private Remote remote = null;
  
  protected static final String EDIT = "edit";

}
