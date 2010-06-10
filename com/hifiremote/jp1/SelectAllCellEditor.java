package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SelectAllCellEditor extends DefaultCellEditor implements Runnable
{
  private JTextField textField = null;
  public SelectAllCellEditor()
  {
    super( new JTextField() );
    setClickCountToStart( 1 );
    textField = ( JTextField )super.getComponent();
  }
  
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    super.getTableCellEditorComponent( table, value, isSelected, row, column );
    SwingUtilities.invokeLater( this );
    return textField;
  }

  @Override
  public void run()
  {
    textField.selectAll();
  }
}
