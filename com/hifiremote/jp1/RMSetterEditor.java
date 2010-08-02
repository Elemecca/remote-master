package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

public class RMSetterEditor< T, C extends JComponent & RMSetter< T > > 
extends DefaultCellEditor implements TableCellEditor, ActionListener
{
  public RMSetterEditor( Class< C > panelClass )
  {
    super( new JTextField() );
    setClickCountToStart( RMConstants.ClickCountToStart );
    this.panelClass = panelClass;
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 1, 1, 1, 1 ) );
    button.setHorizontalAlignment( SwingConstants.LEADING );
    button.setActionCommand( EDIT );
    button.addActionListener( this );
    button.setBorderPainted( false );
  }
  
  public void setRemote( Remote remote )
  {
    this.remote = remote;
  }
  
  @Override
  public Object getCellEditorValue()
  {
    return value;
  }
  
  @SuppressWarnings( "unchecked" )
  @Override
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    this.value = ( T )value;
    button.setText( this.value.toString() );
    return button;
  }

  @Override
  public void actionPerformed( ActionEvent e )
  {
    if ( EDIT.equals( e.getActionCommand() ) )
    {
      // The user has clicked the cell, so
      // bring up the dialog.
      
      RMSetterDialog<T> dialog = new RMSetterDialog<T>();
      dialog.setTitle( title );
      T result = dialog.showDialog(button, remote, panelClass, value);
      if ( ( result != null ) && ! result.toString().equals( value.toString() ))
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
  
  public void setTitle( String title )
  {
    this.title = title;
  }

  private JButton button = null;
  private Remote remote = null;
  private String title = null;
  
  private T value = null;
  private Class< C > panelClass = null;
  
  protected static final String EDIT = "edit";

}
