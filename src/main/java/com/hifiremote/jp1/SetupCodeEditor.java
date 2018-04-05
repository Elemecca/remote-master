package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SetupCodeEditor extends SelectAllCellEditor implements DocumentListener
{
  public SetupCodeEditor( SetupCodeRenderer setupCodeRenderer )
  {
    this.setupCodeRenderer = setupCodeRenderer;
  }
  
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    DeviceButtonTableModel dbTableModel = ( DeviceButtonTableModel )table.getModel();
    setupCodeRenderer.setDeviceType( ( DeviceType )dbTableModel.getValueAt( row, 2 ) );
    setupCodeRenderer.setDeviceButton( dbTableModel.getRow( row ) );
    textField = ( JTextField )super.getTableCellEditorComponent( table, value, isSelected, row, column );
    textField.getDocument().addDocumentListener( this );
    return textField;
  }

  @Override
  public void changedUpdate( DocumentEvent e ){};

  @Override
  public void insertUpdate( DocumentEvent e )
  {
    update(e);
  }

  @Override
  public void removeUpdate( DocumentEvent e )
  {
    update(e);
  }

  private void update( DocumentEvent e )
  {
    int setupCodeValue = 0;
    try
    {
      setupCodeValue = Integer.parseInt( textField.getText() );
    }
    catch ( NumberFormatException e1 )
    {
      return;
    }
    textField.setForeground( setupCodeRenderer.getTextColor( setupCodeValue, false ) ); 

  }
  
  private JTextField textField = null;
  private SetupCodeRenderer setupCodeRenderer = null;

}
