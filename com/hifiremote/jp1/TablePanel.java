package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import com.hifiremote.jp1.clipboard.ClipboardReader;
import com.hifiremote.jp1.clipboard.ClipboardReaderFactory;

/**
 * The Class TablePanel.
 */
public abstract class TablePanel< E > extends KMPanel implements ActionListener, ListSelectionListener
{

  /**
   * Instantiates a new table panel.
   * 
   * @param name
   *          the name
   * @param devUpgrade
   *          the dev upgrade
   * @param tableModel
   *          the table model
   */
  public TablePanel( String name, DeviceUpgrade devUpgrade, KMTableModel< E > tableModel )
  {
    super( name, devUpgrade );
    setLayout( new BorderLayout() );

    kit = Toolkit.getDefaultToolkit();
    clipboard = kit.getSystemClipboard();

    model = tableModel;
    sorter = new TableSorter( model );
    table = new JTableX( sorter );
    sorter.setTableHeader( table.getTableHeader() );
    table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    table.getSelectionModel().addListSelectionListener( this );
    table.setCellSelectionEnabled( true );
    table.setSurrendersFocusOnKeystroke( true );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    table.getTableHeader().setToolTipText(
        "Click to sort is ascending order, or shift-click to sort in descending order." );
    SelectAllCellEditor e = new SelectAllCellEditor();
    new TextPopupMenu( ( JTextComponent )e.getComponent() );
    table.setDefaultEditor( String.class, e );

    TransferHandler th = new TransferHandler()
    {
      protected Transferable createTransferable( JComponent c )
      {
        return new LocalObjectTransferable( new Integer( table.getSelectedRow() ) );
      }

      public int getSourceActions( JComponent c )
      {
        return TransferHandler.COPY_OR_MOVE;
      }

      public boolean canImport( JComponent comp, DataFlavor[] flavors )
      {
        boolean rc = false;
        for ( int i = 0; i < flavors.length; i++ )
        {
          if ( ( flavors[ i ] == DataFlavor.stringFlavor ) || ( flavors[ i ] == LocalObjectTransferable.getFlavor() ) )
          {
            rc = true;
            break;
          }
        }
        return rc;
      }

      public boolean importData( JComponent c, Transferable t )
      {
        boolean rc = false;
        if ( t.isDataFlavorSupported( DataFlavor.stringFlavor ) )
        {
          try
          {
            ClipboardReader rdr = ClipboardReaderFactory.getClipboardReader( t, pasteFromIE );

            int colCount = table.getModel().getColumnCount();
            int addedRow = -1;
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            for ( List< String > tokens = rdr.readNextLine(); tokens != null; tokens = rdr.readNextLine() )
            {
              if ( tokens.isEmpty() )
              {
                continue;
              }

              if ( row == model.getRowCount() )
              {
                model.addRow( createRowObject() );
                if ( addedRow == -1 )
                  addedRow = row;
              }

              int workCol = col;
              for ( String token : tokens )
              {
                if ( token.length() == 0 )
                {
                  token = null;
                }
                if ( workCol == colCount )
                  break;

                Object value = null;
                int modelCol = table.convertColumnIndexToModel( workCol );

                Class< ? > aClass = sorter.getColumnClass( modelCol );
                if ( aClass == String.class )
                {
                  if ( ( token != null ) && ( token.length() == 5 ) && token.startsWith( "num " )
                      && Character.isDigit( token.charAt( 4 ) ) )
                    value = token.substring( 4 );
                  else
                    value = token;
                }
                else
                  value = token;

                sorter.setValueAt( value, row, modelCol );
                workCol++ ;
              }
              row++ ;
            }
            rdr.close();
            if ( addedRow != -1 )
              sorter.fireTableRowsInserted( addedRow, row - 1 );
            sorter.fireTableRowsUpdated( popupRow, row - 1 );
          }
          catch ( Exception ex )
          {
            String message = ex.getMessage();
            if ( message == null )
              message = ex.toString();
            // KeyMapMaster.showMessage( message );
            ex.printStackTrace( System.err );
          }
        }
        else if ( t.isDataFlavorSupported( LocalObjectTransferable.getFlavor() ) )
        {
          try
          {
            int dragRow = ( ( Integer )t.getTransferData( LocalObjectTransferable.getFlavor() ) ).intValue();
            int dropRow = table.getSelectedRow();
            if ( dropRow != dragRow )
            {
              dragRow = sorter.modelIndex( dragRow );
              dropRow = sorter.modelIndex( dropRow );
              model.moveRow( dragRow, dropRow );

              if ( dropRow < dragRow )
                model.fireTableRowsUpdated( dropRow, dragRow );
              else
                model.fireTableRowsUpdated( dragRow, dropRow );
              rc = true;
            }
          }
          catch ( Exception e )
          {
            e.printStackTrace( System.err );
          }
        }
        return rc;
      }

      public void exportToClipboard( JComponent comp, Clipboard clip, int action )
      {
        JTable table = ( JTable )comp;
        int[] selectedRows = table.getSelectedRows();
        int[] selectedCols = table.getSelectedColumns();
        StringBuilder buff = new StringBuilder( 200 );
        for ( int rowNum = 0; rowNum < selectedRows.length; rowNum++ )
        {
          if ( rowNum != 0 )
            buff.append( "\n" );
          for ( int colNum = 0; colNum < selectedCols.length; colNum++ )
          {
            if ( colNum != 0 )
              buff.append( "\t" );
            int selRow = selectedRows[ rowNum ];
            // int convertedRow = sorter.convertRowIndexToModel( selRow );
            int selCol = selectedCols[ colNum ];
            int convertedCol = table.convertColumnIndexToModel( selCol );
            Object value = table.getValueAt( selRow, selCol );
            if ( value != null )
            {
              DefaultTableCellRenderer cellRenderer = ( DefaultTableCellRenderer )table.getColumnModel()
                  .getColumn( selCol ).getCellRenderer();
              if ( cellRenderer != null )
              {
                cellRenderer.getTableCellRendererComponent( table, value, false, false, selRow, convertedCol );
                value = cellRenderer.getText();
              }
              buff.append( value.toString() );
            }
          }
        }
        StringSelection data = new StringSelection( buff.toString() );
        clipboard.setContents( data, data );
      }
    };
    table.setTransferHandler( th );

    popup = new JPopupMenu();
    newItem = new JMenuItem( "New" );
    newItem.setToolTipText( "Create a new function" );
    newItem.addActionListener( this );
    popup.add( newItem );

    deleteItem = new JMenuItem( "Delete" );
    deleteItem.setToolTipText( "Delete the selected function" );
    deleteItem.addActionListener( this );
    popup.add( deleteItem );

    popup.add( new JSeparator() );

    copyItem = new JMenuItem( "Copy" );
    copyItem.setToolTipText( "Copy the selection to the clipboard." );
    copyItem.addActionListener( this );
    popup.add( copyItem );

    pasteItem = new JMenuItem( "Paste" );
    pasteItem.setToolTipText( "Paste from the clipboard into the selection." );
    pasteItem.addActionListener( this );
    popup.add( pasteItem );

    pasteFromIEItem = new JMenuItem( "Paste (IE)" );
    if ( System.getProperty( "os.name" ).indexOf( "Windows" ) != -1 )
    {
      pasteFromIEItem.setToolTipText( "Paste multiple columns from Internet Explorer into the selection." );
      pasteFromIEItem.addActionListener( this );
      popup.add( pasteFromIEItem );
    }

    MouseAdapter mh = new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        showPopup( e );
      }

      public void mouseReleased( MouseEvent e )
      {
        showPopup( e );
      }

      private boolean showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          finishEditing();
          popupRow = table.rowAtPoint( e.getPoint() );
          popupCol = table.columnAtPoint( e.getPoint() );

          if ( popupCol == 0 )
            return false;

          Function func = ( Function )model.getRow( sorter.modelIndex( popupRow ) );
          deleteItem.setEnabled( !func.assigned() );

          Transferable clipData = clipboard.getContents( clipboard );
          if ( ( clipData != null ) && clipData.isDataFlavorSupported( DataFlavor.stringFlavor ) && ( popupCol != 0 ) )
          {
            pasteItem.setEnabled( true );
            pasteFromIEItem.setEnabled( true );
          }
          else
          {
            pasteItem.setEnabled( false );
            pasteFromIEItem.setEnabled( false );
          }

          copyItem.setEnabled( table.getSelectedRowCount() > 0 );
          popup.show( table, e.getX(), e.getY() );
          return true;
        }
        else
          return false;
      }
    };
    table.addMouseListener( mh );

    MouseMotionAdapter mmh = new MouseMotionAdapter()
    {
      public void mouseDragged( MouseEvent e )
      {
        int tableCol = table.columnAtPoint( e.getPoint() );
        int modelCol = table.convertColumnIndexToModel( tableCol );
        if ( modelCol == 0 )
          table.getTransferHandler().exportAsDrag( table, e, TransferHandler.MOVE );
      }
    };
    table.addMouseMotionListener( mmh );

    add( new JScrollPane( table ), BorderLayout.CENTER );

    buttonPanel = new JPanel();
    add( buttonPanel, BorderLayout.SOUTH );

    newButton = new JButton( "New" );
    newButton.addActionListener( this );
    newButton.setToolTipText( "Add a new function." );
    buttonPanel.add( newButton );

    deleteButton = new JButton( "Delete" );
    deleteButton.addActionListener( this );
    deleteButton.setToolTipText( "Delete a function." );
    deleteButton.setEnabled( false );
    buttonPanel.add( deleteButton );

    cleanButton = new JButton( "Clean up" );
    cleanButton.addActionListener( this );
    cleanButton.setToolTipText( "Delete all undefined functions (no hex)" );
    cleanButton.setEnabled( false );
    buttonPanel.add( cleanButton );

    upButton = new JButton( "Up" );
    upButton.addActionListener( this );
    upButton.setToolTipText( "Move the selected function up in the list." );
    upButton.setEnabled( false );
    buttonPanel.add( upButton );

    downButton = new JButton( "Down" );
    downButton.addActionListener( this );
    downButton.setToolTipText( "Move the selected function down in the list." );
    downButton.setEnabled( false );
    buttonPanel.add( downButton );

    copyButton = new JButton( "Copy" );
    copyButton.addActionListener( this );
    copyButton.setToolTipText( "Copy" );
    copyButton.setEnabled( false );
    buttonPanel.add( copyButton );

    pasteButton = new JButton( "Paste" );
    pasteButton.addActionListener( this );
    pasteButton.setToolTipText( "Paste" );
    pasteButton.setEnabled( false );
    buttonPanel.add( pasteButton );

    pasteFromIEButton = new JButton( "Paste (IE)" );
    if ( System.getProperty( "os.name" ).indexOf( "Windows" ) != -1 )
    {
      pasteFromIEButton.addActionListener( this );
      pasteFromIEButton.setToolTipText( "Paste multiple columns from Internet Explorer" );
      pasteFromIEButton.setEnabled( false );
      buttonPanel.add( pasteFromIEButton );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#setDeviceUpgrade(com.hifiremote.jp1.DeviceUpgrade)
   */
  public void setDeviceUpgrade( DeviceUpgrade deviceUpgrade )
  {
    super.setDeviceUpgrade( deviceUpgrade );
    this.initColumns();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    cleanButton.setEnabled( table.getRowCount() > 0 );
  }

  /**
   * Finish editing.
   */
  private void finishEditing()
  {
    int editRow = table.getEditingRow();
    if ( editRow != -1 )
    {
      TableCellEditor editor = table.getCellEditor( editRow, table.getEditingColumn() );
      if ( !editor.stopCellEditing() )
        editor.cancelCellEditing();
    }
  }

  /**
   * Creates the row object.
   * 
   * @return the e
   */
  protected abstract E createRowObject();

  /**
   * Can delete.
   * 
   * @param o
   *          the o
   * @return true, if successful
   */
  protected boolean canDelete( E o )
  {
    return true;
  }

  protected void delete( E o )
  {}

  /**
   * Do not delete.
   * 
   * @param o
   *          the o
   */
  protected void doNotDelete( E o )
  {}

  // Interface ActionListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    // KeyMapMaster.clearMessage();
    int row = 0;
    boolean select = false;
    Object source = e.getSource();
    if ( source.getClass() == JButton.class )
    {
      row = table.getSelectedRow();
      if ( row != -1 )
        select = true;
    }
    else
    {
      row = popupRow;
      if ( table.isRowSelected( row ) )
        select = true;
    }

    if ( ( source == newButton ) || ( source == newItem ) )
    {
      E o = createRowObject();
      if ( row == -1 )
      {
        model.addRow( o );
        row = 0;
      }
      else
      {
        model.insertRow( sorter.modelIndex( row ), o );
      }

      model.fireTableRowsInserted( sorter.modelIndex( row ), sorter.modelIndex( row ) );
      if ( select )
        table.setRowSelectionInterval( row, row );
    }
    else if ( ( source == deleteButton ) || ( source == deleteItem ) )
    {
      E rowObject = model.getRow( sorter.modelIndex( row ) );
      if ( !canDelete( rowObject ) )
      {
        deleteButton.setEnabled( false );
        doNotDelete( rowObject );
      }
      else
      {
        delete( rowObject );
        model.removeRow( sorter.modelIndex( row ) );
        model.fireTableRowsDeleted( sorter.modelIndex( row ), sorter.modelIndex( row ) );
        if ( row == model.getRowCount() )
          --row;
        if ( select )
          table.setRowSelectionInterval( row, row );
      }
    }
    else if ( source == cleanButton )
    {
      java.util.List< E > functions = model.getData();
      for ( ListIterator< E > i = functions.listIterator(); i.hasNext(); )
      {
        Function f = ( Function )i.next();
        if ( ( f.getHex() == null ) || ( f.getHex().length() == 0 ) )
          i.remove();
      }
      model.fireTableDataChanged();
    }
    else if ( ( source == upButton ) || ( source == downButton ) )
    {
      int start = 0;
      int end = 0;
      int sel = 0;
      int from = row;
      int to;

      if ( source == upButton )
      {
        start = row - 1;
        end = row;
        to = start;
        sel = start;
      }
      else
      {
        start = row;
        end = row + 1;
        to = end;
        sel = end;
      }
      model.moveRow( sorter.modelIndex( from ), sorter.modelIndex( to ) );
      model.fireTableRowsUpdated( sorter.modelIndex( start ), sorter.modelIndex( end ) );
      if ( select )
        table.setRowSelectionInterval( sel, sel );
    }
    else if ( ( source == copyItem ) || ( source == copyButton ) )
    {
      table.getTransferHandler().exportToClipboard( table, clipboard, TransferHandler.COPY );
    }
    else if ( ( source == pasteItem ) || ( source == pasteButton ) )
    {
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
      {
        pasteFromIE = false;
        table.getTransferHandler().importData( table, clipboard.getContents( clipboard ) );
      }
      else
        kit.beep();
    }
    else if ( ( source == pasteFromIEItem ) || ( source == pasteFromIEButton ) )
    {
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
      {
        pasteFromIE = true;
        table.getTransferHandler().importData( table, clipboard.getContents( clipboard ) );
      }
      else
        kit.beep();
    }
    cleanButton.setEnabled( table.getRowCount() > 0 );
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
      cleanButton.setEnabled( table.getRowCount() > 0 );
      int row = table.getSelectedRow();
      if ( row != -1 )
      {
        upButton.setEnabled( row > 0 );
        downButton.setEnabled( row < ( sorter.getRowCount() - 1 ) );
        deleteButton.setEnabled( canDelete( model.getRow( sorter.modelIndex( row ) ) ) );
        Transferable clipData = clipboard.getContents( clipboard );
        copyButton.setEnabled( true );
        if ( ( clipData != null ) && clipData.isDataFlavorSupported( DataFlavor.stringFlavor )
            && ( table.convertColumnIndexToModel( table.getSelectedColumn() ) != 0 ) )
        {
          pasteButton.setEnabled( true );
          pasteFromIEButton.setEnabled( true );
        }
        else
        {
          pasteButton.setEnabled( false );
          pasteFromIEButton.setEnabled( false );
        }
      }
      else
      {
        deleteButton.setEnabled( false );
        pasteButton.setEnabled( false );
        copyButton.setEnabled( false );
        upButton.setEnabled( false );
        downButton.setEnabled( false );
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#commit()
   */
  public void commit()
  {
    finishEditing();
  }

  /**
   * Inits the columns.
   */
  protected void initColumns()
  {
    JLabel l = new JLabel();
    l.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 4 ) );

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn column;

    int cols = model.getColumnCount();
    for ( int i = 0; i < cols; i++ )
    {
      table.setColumnWidth( i, model.getColumnPrototypeName( i ), model.isColumnWidthFixed( i ), 4 );
      column = columnModel.getColumn( i );

      TableCellEditor editor = model.getColumnEditor( i );
      if ( editor != null )
        column.setCellEditor( editor );

      TableCellRenderer renderer = model.getColumnRenderer( i );
      if ( renderer != null )
        column.setCellRenderer( renderer );

      if ( model.getColumnName( i ).startsWith( "<html>" ) )
      {
        column.setHeaderRenderer( table.getTableHeader().getDefaultRenderer() );
      }
    }
    // table.doLayout();
    table.validate();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#setFont(java.awt.Font)
   */
  public void setFont( Font aFont )
  {
    super.setFont( aFont );
    if ( ( aFont == null ) || ( table == null ) )
      return;
    table.setRowHeight( aFont.getSize() + 2 );
    initColumns();
  }

  /** The table. */
  protected JTableX table = null;

  /** The model. */
  protected KMTableModel< E > model = null;

  /** The sorter. */
  private TableSorter sorter = null;

  /** The button panel. */
  protected JPanel buttonPanel = null;

  /** The new button. */
  private JButton newButton = null;

  /** The delete button. */
  private JButton deleteButton = null;

  /** The clean button. */
  private JButton cleanButton = null;

  /** The up button. */
  private JButton upButton = null;

  /** The down button. */
  private JButton downButton = null;

  /** The copy button. */
  private JButton copyButton = null;

  /** The paste button. */
  private JButton pasteButton = null;
  private JButton pasteFromIEButton = null;

  /** The popup row. */
  private int popupRow = 0;

  /** The popup col. */
  private int popupCol = 0;

  /** The popup. */
  protected JPopupMenu popup = null;

  /** The new item. */
  private JMenuItem newItem = null;

  /** The delete item. */
  private JMenuItem deleteItem = null;

  /** The copy item. */
  private JMenuItem copyItem = null;

  /** The paste item. */
  private JMenuItem pasteItem = null;
  private JMenuItem pasteFromIEItem = null;

  private boolean pasteFromIE = false;

  /** The clipboard. */
  private Clipboard clipboard = null;

  /** The kit. */
  private Toolkit kit = null;
}
