package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

/**
 * The Class RMTablePanel.
 */
public abstract class RMTablePanel< E > extends RMPanel implements ActionListener, ListSelectionListener
{

  /**
   * Instantiates a new rM table panel.
   * 
   * @param model
   *          the model
   */
  public RMTablePanel( JP1TableModel< E > model )
  {
    this( model, BorderLayout.CENTER );
  }

  /**
   * Instantiates a new rM table panel.
   * 
   * @param tableModel
   *          the table model
   * @param location
   *          the location
   */
  public RMTablePanel( JP1TableModel< E > tableModel, String location )
  {
    super();
    model = tableModel;
    sorter = new TableSorter( model );
    table = new JP1Table( sorter );
    JTableHeader header = table.getTableHeader();
    header.setReorderingAllowed( true );
    sorter.setTableHeader( header );
    // sorter.addMouseListenerToHeaderInTable( table );
    table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    table.getSelectionModel().addListSelectionListener( this );
    table.setCellSelectionEnabled( true );
    table.setSurrendersFocusOnKeystroke( true );
    table.getTableHeader().setToolTipText(
        "Click to sort in ascending order, or shift-click to sort in descending order." );

    openEditor = new MouseAdapter()
    {
      @Override
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
        {
          return;
        }
        int row = table.getSelectedRow();
        if ( row == -1 )
        {
          return;
        }
        if ( !table.isCellEditable( row, table.columnAtPoint( e.getPoint() ) ) )
        {
          editRowObject( row );
        }
      }
    };
    table.addMouseListener( openEditor );

    popup = new JPopupMenu();
    editItem = new JMenuItem( "Edit" );
    editItem.setEnabled( false );
    editItem.addActionListener( this );
    popup.add( editItem );

    newItem = new JMenuItem( "New" );
    newItem.addActionListener( this );
    // newItem.setEnabled( false );
    popup.add( newItem );

    cloneItem = new JMenuItem( "Clone" );
    cloneItem.addActionListener( this );
    cloneItem.setEnabled( false );
    popup.add( cloneItem );

    deleteItem = new JMenuItem( "Delete" );
    deleteItem.addActionListener( this );
    deleteItem.setEnabled( false );
    popup.add( deleteItem );
    
    editProtocolItem = new JMenuItem( "Edit Protocol" );
    editProtocolItem.addActionListener( this );
    editProtocolItem.setEnabled( false );
    editProtocolItem.setVisible( false );
    popup.add( editProtocolItem );

    popup.add( new JSeparator() );

    copyItem = new JMenuItem( "Copy" );
    copyItem.setToolTipText( "Copy the selected text to the clipboard" );
    copyItem.setEnabled( false );
    copyItem.addActionListener( this );
    popup.add( copyItem );

    MouseAdapter mh = new MouseAdapter()
    {
      @Override
      public void mousePressed( MouseEvent e )
      {
        showPopup( e );
      }

      @Override
      public void mouseReleased( MouseEvent e )
      {
        showPopup( e );
      }
    };
    table.addMouseListener( mh );

    table.initColumns( model );
    JScrollPane scrollPane = new JScrollPane( table );
    Dimension d = table.getPreferredScrollableViewportSize();
    d.width = table.getPreferredSize().width;
    table.setPreferredScrollableViewportSize( d );
    add( scrollPane, location );

    footerPanel = new JPanel( new BorderLayout() );
    add( footerPanel, BorderLayout.PAGE_END );
    buttonPanel = new JPanel();
    footerPanel.add( buttonPanel, BorderLayout.PAGE_END );

    editButton = new JButton( "Edit" );
    editButton.addActionListener( this );
    editButton.setToolTipText( "Edit the selected item." );
    editButton.setEnabled( false );
    buttonPanel.add( editButton );

    newButton = new JButton( "New" );
    newButton.addActionListener( this );
    newButton.setToolTipText( "Add a new item." );
    buttonPanel.add( newButton );

    cloneButton = new JButton( "Clone" );
    cloneButton.addActionListener( this );
    cloneButton.setToolTipText( "Add a copy of the selected item." );
    cloneButton.setEnabled( false );
    buttonPanel.add( cloneButton );

    deleteButton = new JButton( "Delete" );
    deleteButton.addActionListener( this );
    deleteButton.setToolTipText( "Delete the selected item." );
    deleteButton.setEnabled( false );
    buttonPanel.add( deleteButton );

    upButton = new JButton( "Up" );
    upButton.addActionListener( this );
    upButton.setToolTipText( "Move the selected item up in the list." );
    upButton.setEnabled( false );
    buttonPanel.add( upButton );

    downButton = new JButton( "Down" );
    downButton.addActionListener( this );
    downButton.setToolTipText( "Move the selected item down in the list." );
    downButton.setEnabled( false );
    buttonPanel.add( downButton );
    
    editProtocolButton = new JButton( "Edit Protocol" );
    editProtocolButton.addActionListener( this );
    editProtocolButton.setToolTipText( "Edit the protocol of this device upgrade." );
    editProtocolButton.setEnabled( false );
    editProtocolButton.setVisible( false );
    buttonPanel.add( editProtocolButton );
  }

  protected boolean showPopup( MouseEvent e )
  {
    if ( e.isPopupTrigger() )
    {
      finishEditing();
      popupRow = table.rowAtPoint( e.getPoint() );
      popup.show( table, e.getX(), e.getY() );
      return true;
    }
    else
    {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setFont(java.awt.Font)
   */
  @Override
  public void setFont( Font aFont )
  {
    super.setFont( aFont );
    if ( table == null || aFont == null )
    {
      return;
    }
    table.setRowHeight( aFont.getSize() + 2 );
    // if ( model != null )
    table.initColumns( model );
  }

  /**
   * Finish editing.
   */
  protected void finishEditing()
  {
    int editRow = table.getEditingRow();
    if ( editRow != -1 )
    {
      TableCellEditor editor = table.getCellEditor( editRow, table.getEditingColumn() );
      if ( !editor.stopCellEditing() )
      {
        editor.cancelCellEditing();
      }
    }
  }

  /**
   * Creates the row object.
   * 
   * @param baseObject
   *          the base object
   * @return the e
   */
  protected abstract E createRowObject( E baseObject );

  /**
   * Gets the row object.
   * 
   * @param row
   *          the row
   * @return the row object
   */
  protected E getRowObject( int row )
  {
    if ( row != -1 )
    {
      return model.getRow( sorter.modelIndex( row ) );
    }
    return null;
  }

  /**
   * Can delete.
   * 
   * @param o
   *          the o
   * @return true, if successful
   */
  protected boolean canDelete( Object o )
  {
    return true;
  }

  /**
   * Do not delete.
   * 
   * @param o
   *          the o
   */
  protected void doNotDelete( Object o )
  {}

  /**
   * Edits the row object.
   * 
   * @param row
   *          the row
   */
  protected void editRowObject( int row )
  {
    E o = createRowObject( getRowObject( row ) );
    if ( o != null )
    {
      model.setRow( sorter.modelIndex( row ), o );
    }
  }
  
  protected void editRowProtocol( int row )
  {
    // Only used when overridden
  }

  protected void newRowObject( E baseObject, int row, int modelRow, boolean select )
  {
    E o = createRowObject( baseObject );
    if ( o == null )
    {
      return;
    }
    if ( row == -1 )
    {
      model.addRow( o );
      row = model.getRowCount();
    }
    else
    {
      model.insertRow( modelRow, o );
    }

    if ( select )
    {
      table.setRowSelectionInterval( row, row );
    }
  }

  protected void deleteRow( int row, boolean select )
  {
    int modelRow = -1;
    if ( row != -1 )
    {
      modelRow = sorter.modelIndex( row );
    }

    if ( !canDelete( model.getRow( sorter.modelIndex( row ) ) ) )
    {
      deleteButton.setEnabled( false );
      deleteItem.setEnabled( false );
      doNotDelete( model.getRow( modelRow ) );
    }
    else
    {
      int rowToSelect = row;
      if ( rowToSelect == sorter.getRowCount() - 1 )
      {
        --rowToSelect;
      }
      else
      {
        ++rowToSelect;
      }
      if ( select && rowToSelect > -1 )
      {
        table.setRowSelectionInterval( rowToSelect, rowToSelect );
      }

      model.removeRow( modelRow );
    }
  }

  // Interface ActionListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    int row = 0;
    boolean select = false;
    Object source = e.getSource();
    if ( source.getClass() == JButton.class )
    {
      row = table.getSelectedRow();
      if ( row != -1 )
      {
        select = true;
      }
    }
    else
    {
      row = popupRow;
      if ( table.isRowSelected( row ) )
      {
        select = true;
      }
    }
    int modelRow = -1;
    if ( row != -1 )
    {
      modelRow = sorter.modelIndex( row );
    }

    if ( source == editButton || source == editItem )
    {
      editRowObject( row );
    }
    else if ( source == newButton || source == newItem )
    {
      newRowObject( null, row, modelRow, select );
    }
    else if ( source == cloneButton || source == cloneItem )
    {
      newRowObject( getRowObject( row ), row, modelRow, select );
    }
    else if ( source == editProtocolButton || source == editProtocolItem )
    {
      editRowProtocol( modelRow );
    }    
    else if ( source == deleteButton || source == deleteItem )
    {
      deleteRow( row, select );
    }
    else if ( source == upButton || source == downButton )
    {
      int sel = 0;
      int from = row;
      int to;

      if ( source == upButton )
      {
        from = row - 1;
        to = row;
        sel = from;
      }
      else
      // down button
      {
        from = row;
        to = row + 1;
        sel = to;
      }
      model.moveRow( sorter.modelIndex( from ), sorter.modelIndex( to ) );
      if ( select )
      {
        table.setRowSelectionInterval( sel, sel );
      }
    }
    else if ( source == copyItem )
    {
      int[] rows = table.getSelectedRows();
      int[] cols = table.getSelectedColumns();
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      boolean firstRow = true;
      StringBuilder buff = new StringBuilder( 200 );
      for ( int tableRow : rows )
      {
        if ( firstRow )
        {
          firstRow = false;
        }
        else
        {
          buff.append( "\n" );
        }

        modelRow = table.convertRowIndexToModel( tableRow );

        boolean firstCol = true;
        for ( int tableCol : cols )
        {
          if ( firstCol )
          {
            firstCol = false;
          }
          else
          {
            buff.append( "\t" );
          }
          int modelCol = table.convertColumnIndexToModel( tableCol );

          Object value = sorter.getValueAt( modelRow, modelCol );
          if ( value != null )
          {
            DefaultTableCellRenderer cellRenderer = ( DefaultTableCellRenderer )table.getColumnModel().getColumn(
                tableCol ).getCellRenderer();
            if ( cellRenderer != null )
            {
              cellRenderer.getTableCellRendererComponent( table, value, false, false, modelRow, modelCol );
              value = cellRenderer.getText();
            }
            buff.append( value.toString() );
          }
        }
      }
      StringSelection data = new StringSelection( buff.toString() );
      clipboard.setContents( data, data );
    }
  }

  // Interface ListSelectionListener
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() )
    {
      RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
      rm.highlightAction.setEnabled( table.getSelectedRowCount() > 0 );
      if ( table.getSelectedRowCount() == 1 )
      {
        int row = table.getSelectedRow();

        boolean selected = row != -1;
        editButton.setEnabled( selected );
        editItem.setEnabled( selected );
        cloneButton.setEnabled( selected );
        cloneItem.setEnabled( selected );
        deleteButton.setEnabled( selected );
        deleteItem.setEnabled( selected );
        if ( editProtocolButton.isVisible() )
        {
          editProtocolButton.setEnabled( selected );
        }
        if ( editProtocolItem.isVisible() )
        {
          editProtocolItem.setEnabled( selected );
        }

        boolean deleteAllowed = selected && canDelete( model.getRow( sorter.modelIndex( row ) ) );
        deleteButton.setEnabled( deleteAllowed );
        deleteItem.setEnabled( deleteAllowed );

        upButton.setEnabled( row > 0 );
        downButton.setEnabled( selected && row < sorter.getRowCount() - 1 );
      }
      else
      {
        editButton.setEnabled( false );
        editItem.setEnabled( false );
        cloneButton.setEnabled( false );
        cloneItem.setEnabled( false );
        cloneButton.setEnabled( false );
        deleteButton.setEnabled( false );
        deleteItem.setEnabled( false );
        upButton.setEnabled( false );
        downButton.setEnabled( false );
        if ( editProtocolButton.isVisible() )
        {
          editProtocolButton.setEnabled( false );
        }
        if ( editProtocolItem.isVisible() )
        {
          editProtocolItem.setEnabled( false );
        }
      }
      copyItem.setEnabled( table.getSelectedRowCount() > 0 );
    }
  }

  /**
   * Commit.
   */
  public void commit()
  {
    finishEditing();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  @Override
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( model != null && listener != null )
    {
      model.addPropertyChangeListener( listener );
    }
  }

  /**
   * Gets the model.
   * 
   * @return the model
   */
  public JP1TableModel< E > getModel()
  {
    return model;
  }

  /** The table. */
  protected JP1Table table = null;

  /** The model. */
  protected JP1TableModel< E > model = null;

  /** The sorter. */
  protected TableSorter sorter = null;

  /** The button panel. */
  protected JPanel buttonPanel = null;

  protected JPanel footerPanel = null;

  /** The edit button. */
  protected JButton editButton = null;

  /** The new button. */
  protected JButton newButton = null;

  /** The clone button. */
  protected JButton cloneButton = null;

  /** The delete button. */
  protected JButton deleteButton = null;

  /** The up button. */
  protected JButton upButton = null;

  /** The down button. */
  protected JButton downButton = null;
  
  protected JButton editProtocolButton = null;

  /** The popup row. */
  protected int popupRow = 0;

  /** The popup. */
  protected JPopupMenu popup = null;

  /** The edit item. */
  protected JMenuItem editItem = null;

  /** The new item. */
  protected JMenuItem newItem = null;

  /** The clone item. */
  protected JMenuItem cloneItem = null;

  /** The delete item. */
  protected JMenuItem deleteItem = null;
  
  protected JMenuItem editProtocolItem = null;

  private JMenuItem copyItem = null;

  public MouseListener openEditor = null;
}
