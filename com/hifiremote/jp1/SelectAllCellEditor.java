package com.hifiremote.jp1;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SelectAllCellEditor extends DefaultCellEditor implements Runnable
{
  private JTextField textField = null;
  private String selectText = null;

  public SelectAllCellEditor()
  {
    super( new JTextField() );
    setClickCountToStart( RMConstants.ClickCountToStart );
    textField = ( JTextField )super.getComponent();
  }

  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    super.getTableCellEditorComponent( table, value, isSelected, row, column );
    if ( RMConstants.EnableCellEditorSelectAll )
    {
      selectText = textField.getText();
      SwingUtilities.invokeLater( this );
    }
    return textField;
  }

  @Override
  public void run()
  {
    String text = textField.getText();
    if ( text.equals( selectText ) )
    {
      textField.selectAll();
    }
    else
    {
      textField.setText( text.substring( selectText.length() ) );
    }
  }
}
