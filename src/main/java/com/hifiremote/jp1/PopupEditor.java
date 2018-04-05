package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

// TODO: Auto-generated Javadoc
/**
 * The Class PopupEditor.
 */
public class PopupEditor extends DefaultCellEditor implements TableCellEditor, ActionListener, PopupMenuListener,
    Runnable
{

  /** The button. */
  private JButton button = new JButton();

  /** The table. */
  private JTable table = null;

  /** The value. */
  private Object value = null;

  /** The popup. */
  private JPopupMenu popup = new JPopupMenu();

  /**
   * The Class ObjectItem.
   */
  private class ObjectItem extends JMenuItem
  {

    /** The value. */
    private Object value;

    /**
     * Instantiates a new object item.
     * 
     * @param value
     *          the value
     */
    public ObjectItem( Object value )
    {
      super( value.toString() );
      this.value = value;
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public Object getValue()
    {
      return value;
    }
  }

  /**
   * Instantiates a new popup editor.
   */
  public PopupEditor()
  {
    super( new JTextField() );
    setClickCountToStart( RMConstants.ClickCountToStart );
    button = new JButton();
    button.setBorder( BorderFactory.createEmptyBorder( 0, 3, 0, 3 ) );
    button.setHorizontalAlignment( SwingConstants.LEADING );
    /* button.addActionListener( this ); */
    button.setBorderPainted( false );

    popup.setLayout( new GridLayout( 0, 3 ) );
    popup.addPopupMenuListener( this );
  }

  /**
   * Adds the object.
   * 
   * @param value
   *          the value
   */
  public void addObject( Object value )
  {
    ObjectItem item = new ObjectItem( value );
    popup.add( item );
    item.addActionListener( this );
  }

  /**
   * Removes the all.
   */
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
   * Handles events from the editor button and from the dialog's OK button.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed( ActionEvent e )
  {
    /*
     * if ( e.getSource() == button ) { popup.show( button, 0, button.getSize().height ); } else
     */
    {
      value = ( ( ObjectItem )e.getSource() ).getValue();
      fireEditingStopped();
      giveFocusToTable();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
   */
  public void popupMenuCanceled( PopupMenuEvent e )
  {
    fireEditingCanceled();
    giveFocusToTable();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
   */
  public void popupMenuWillBecomeInvisible( PopupMenuEvent e )
  {}

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
   */
  public void popupMenuWillBecomeVisible( PopupMenuEvent e )
  {}

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.DefaultCellEditor#getCellEditorValue()
   */
  @Override
  public Object getCellEditorValue()
  {
    return value;
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
    this.table = table;
    this.value = value;
    if ( value == null )
    {
      button.setText( "" );
    }
    else
    {
      button.setText( value.toString() );
    }

    MenuElement[] elements = popup.getSubElements();
    for ( int i = 0; i < elements.length; ++i )
    {
      ObjectItem item = ( ObjectItem )elements[ i ];
      if ( item.getValue().equals( value ) )
      {
        popup.setSelected( item );
        break;
      }
    }
    SwingUtilities.invokeLater( this );
    return button;
  }

  /**
   * Give focus to table.
   */
  private void giveFocusToTable()
  {
    table.requestFocusInWindow();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    popup.show( button, 0, button.getHeight() );
  }

}
