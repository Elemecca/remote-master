package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class PopupEditor
  extends DefaultCellEditor
  implements TableCellEditor, ActionListener, PopupMenuListener, Runnable
{
  private JButton button = new JButton();
  private JTable table = null;
  private Object value = null;
  private JPopupMenu popup = new JPopupMenu();

  private class ObjectItem 
    extends JMenuItem
  {
    private Object value;
    
    public ObjectItem( Object value )
    {
      super( value.toString());
      this.value = value;
    }
    
    public Object getValue()
    {
      return value;
    }
  }   
  
  public PopupEditor()
  {
    super( new JTextField());
    setClickCountToStart( 2 );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 3 ));
    button.setHorizontalAlignment( SwingConstants.LEADING );
/*    button.addActionListener( this ); */
    button.setBorderPainted( false );
    
    popup.setLayout( new GridLayout( 0, 3 ));
    popup.addPopupMenuListener( this );
  }
  
  public void addObject( Object value )
  {
    ObjectItem item = new ObjectItem( value );
    popup.add( item );
    item.addActionListener( this );
  }
  
  public void removeAll()
  {
    MenuElement[] elements = popup.getSubElements();
    for ( int i = 0; i < elements.length; ++i )
    {
      ObjectItem item = ( ObjectItem )elements[ i ];
      item.removeActionListener( this );
    }
    popup.removeAll();
  }  
  
  /**
   * Handles events from the editor button and from
   * the dialog's OK button.
   */
  public void actionPerformed( ActionEvent e )
  {
    /*
    if ( e.getSource() == button )
    {
      popup.show( button, 0, button.getSize().height );
    }
    else
    */
    {
      value = (( ObjectItem )( e.getSource())).getValue();
      fireEditingStopped();
      giveFocusToTable();
    }
  }

  public void popupMenuCanceled( PopupMenuEvent e )
  {
    fireEditingCanceled();
    giveFocusToTable();
  }
  public void popupMenuWillBecomeInvisible( PopupMenuEvent e ){}
  public void popupMenuWillBecomeVisible( PopupMenuEvent e ){}

  //Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue()
  {
    return value;
  }

  //Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent( JTable table, Object value, boolean isSelected, int row, int column )
  {
    this.table = table;
    this.value = value;
    if ( value == null )
      button.setText( "" );
    else
      button.setText( value.toString());    
    
    MenuElement[] elements = popup.getSubElements();
    for ( int i = 0; i < elements.length; ++i )
    {
      ObjectItem item = ( ObjectItem )elements[ i ];
      if ( item.getValue().equals( value ))
      {
        popup.setSelected( item );
        break;
      }
    }
    SwingUtilities.invokeLater( this );
    return button;
  }
  
  private void giveFocusToTable()
  {
    table.requestFocusInWindow();
  }
  
  public void run()
  {
    popup.show( button, 0, button.getSize().height );
  }

}

