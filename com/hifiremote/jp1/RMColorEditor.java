package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

public class RMColorEditor extends DefaultCellEditor implements TableCellEditor, ActionListener
{
  public RMColorEditor( RemoteMaster owner )
  {
    super( new JTextField() );
    this.owner = owner;
    setClickCountToStart( RMConstants.ClickCountToStart );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
    button.setActionCommand( EDIT );
    button.addActionListener( this );
    button.setBorderPainted( false );
    this.colorDialog = owner.getColorDialog();
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {
    if ( EDIT.equals( e.getActionCommand() ) )
    {
      RemoteMaster.Preview preview = ( RemoteMaster.Preview )owner.getColorChooser().getPreviewPanel();
      preview.getSelectors().setVisible( false );
      owner.getColorChooser().setColor( value );
      colorDialog.pack();
      colorDialog.setVisible( true );
      Color result = preview.getColor();
      if ( ( result != null ) )
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
  
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    this.value = ( Color )value;
    return button;
  }
  
  @Override
  public Object getCellEditorValue()
  {
    return value;
  }
  
  private RemoteMaster owner = null;
  private JButton button = null;
  private JDialog colorDialog = null;
  private Color value = null;
  
  private static final String EDIT = "edit";
}
