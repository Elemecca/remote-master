package com.hifiremote.jp1;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public abstract class TablePanel
  extends KMPanel
  implements ActionListener, ListSelectionListener
{
  public TablePanel( String name, DeviceUpgrade devUpgrade, KMTableModel model )
  {
    super( name, devUpgrade );
    setLayout( new BorderLayout());

    kit = Toolkit.getDefaultToolkit();
    clipboard = kit.getSystemClipboard();

    this.model = model;
    sorter = new TableSorter( model );
    table = new JTable( sorter );
    sorter.addMouseListenerToHeaderInTable( table );
    table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    table.getSelectionModel().addListSelectionListener( this );
    table.setCellSelectionEnabled( true );
    table.setRowSelectionAllowed( true );
    table.setSurrendersFocusOnKeystroke( true );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    table.getTableHeader().setToolTipText( "Click to sort is ascending order, or shift-click to sort in descending order." );
    (( DefaultCellEditor )table.getDefaultEditor( String.class )).setClickCountToStart( 1 );

    TransferHandler th = new TransferHandler()
    {
      protected Transferable createTransferable( JComponent c )
      {
        return new StringSelection( Integer.toString( table.getSelectedRow()));
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
          if ( flavors[ i ] == DataFlavor.stringFlavor )
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
        try
        {
          int dragRow = Integer.parseInt(( String )t.getTransferData( DataFlavor.stringFlavor ));
          int dropRow = table.getSelectedRow();
          if ( dropRow != dragRow )
          {
            sorter.moveRow( dragRow, dropRow );

            if ( dropRow < dragRow )
              sorter.fireTableRowsUpdated( dropRow, dragRow );
            else
              sorter.fireTableRowsUpdated( dragRow, dropRow );
            rc = true;
          }
        }
        catch ( Exception e )
        {
          e.printStackTrace( System.err );
        }

        return rc;
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

    popup.add( new JSeparator());

    copyItem = new JMenuItem( "Copy" );
    copyItem.setToolTipText( "Copy the selection to the clipboard." );
    copyItem.addActionListener( this );
    popup.add( copyItem );

    pasteItem = new JMenuItem( "Paste" );
    pasteItem.setToolTipText( "Paste from the clipboard into the selection." );
    pasteItem.addActionListener( this );
    popup.add( pasteItem );

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

      private void showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          finishEditing();
          popupRow = table.rowAtPoint( e.getPoint());
          popupCol = table.columnAtPoint( e.getPoint());

          Function func = ( Function )sorter.getRow( popupRow );
          deleteItem.setEnabled( !func.assigned());

          Transferable clipData = clipboard.getContents( clipboard );
          if (( clipData != null ) &&
              clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
            pasteItem.setEnabled( true );
          else
            pasteItem.setEnabled( false );
          popup.show( table, e.getX(), e.getY());
        }
      }
    };
    table.addMouseListener( mh );

    MouseMotionAdapter mmh = new MouseMotionAdapter()
    {
      public void mouseDragged( MouseEvent e )
      {
        int tableCol = table.columnAtPoint( e.getPoint());
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

    upButton = new JButton( "Move up" );
    upButton.addActionListener( this );
    upButton.setToolTipText( "Move the selected function up in the list." );
    upButton.setEnabled( false );
    buttonPanel.add( upButton );

    downButton = new JButton( "Move down" );
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
  }

  private void finishEditing()
  {
    int editRow = table.getEditingRow();
    if ( editRow != -1 )
    {
      TableCellEditor editor =
        table.getCellEditor( editRow, table.getEditingColumn());
      if ( !editor.stopCellEditing() )
        editor.cancelCellEditing();
    }
  }

  protected abstract Object createRowObject();
  protected boolean canDelete( Object o ){ return true; }
  protected void doNotDelete( Object o ){}

  // Interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    KeyMapMaster.clearMessage();
    Vector functions = deviceUpgrade.getFunctions();
    int row = 0;
    int col = 0;
    boolean select = false;
    Object source = e.getSource();
    if ( source.getClass() == JButton.class )
    {
      row = table.getSelectedRow();
      col = table.getSelectedColumn();
      if ( row != -1 )
        select = true;
    }
    else
    {
      row = popupRow;
      col = popupCol;
      if ( table.isRowSelected( row ))
        select = true;
    }

    if (( source == newButton ) ||
        ( source == newItem ))
    {
      Object o = createRowObject();
      if ( row == -1 )
      {
        sorter.addRow( o );
        row = sorter.getRowCount();
      }
      else
      {
        sorter.insertRow( row, o );
      }

      sorter.fireTableRowsInserted( row, row );
      if ( select )
        table.setRowSelectionInterval( row, row );
    }
    else if (( source == deleteButton ) ||
             ( source == deleteItem ))
    {
      Function func = ( Function )functions.elementAt( sorter.convertRowIndexToModel( row ));
      if ( !canDelete( sorter.getRow( row )))
      {
        deleteButton.setEnabled( false );
        doNotDelete( sorter.getRow( row ));
      }
      else
      {
        sorter.removeRow( row );
        sorter.fireTableRowsDeleted( row, row );
        if ( row == sorter.getRowCount())
          --row;
        if ( select )
          table.setRowSelectionInterval( row, row );
      }
    }
    else if (( source == upButton ) ||
             ( source == downButton ))
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
      sorter.moveRow( from, to );
      sorter.fireTableRowsUpdated( start, end );
      if ( select )
        table.setRowSelectionInterval( sel, sel );
    }
    else if (( source == copyItem ) || ( source == copyButton ))
    {
      int[] selectedRows = table.getSelectedRows();
      int[] selectedCols = table.getSelectedColumns();
      StringBuffer buff = new StringBuffer( 200 );
      for ( int rowNum = 0; rowNum < selectedRows.length; rowNum ++ )
      {
        if ( rowNum != 0 )
          buff.append( "\n" );
        for ( int colNum = 0; colNum < selectedCols.length; colNum++ )
        {
          if ( colNum != 0 )
            buff.append( "\t" );
          Object value =
            sorter.getValueAt( selectedRows[ rowNum ],
                               table.convertColumnIndexToModel( selectedCols[ colNum ]));
          if ( value != null )
          {
            buff.append( value.toString() );
          }
        }
      }
      StringSelection data = new StringSelection( buff.toString());
      clipboard.setContents( data, data );
    }
    else if (( source == pasteItem ) || ( source == pasteButton ))
    {
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
      {
        try
        {
          if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
          {
            String s =
              ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
            BufferedReader in = new BufferedReader( new StringReader( s ));
            int colCount = model.getColumnCount();
            int addedRow = -1;
            for ( String line = in.readLine(); line != null; line = in.readLine())
            {
              if ( row == sorter.getRowCount() )
              {
                sorter.addRow( createRowObject());
                if ( addedRow == -1 )
                  addedRow = row;
              }

              StringTokenizer st = new StringTokenizer( line, "\t", true );
              int workCol = col;
              boolean done = false;
              String token = null;
              String prevToken = null;
              while ( !done )
              {
                if ( workCol == colCount )
                  break;
                if ( st.hasMoreTokens())
                  token = st.nextToken();
                else
                  token = null;

                Object value = null;
                int modelCol = table.convertColumnIndexToModel( workCol );
                if ( token == null )
                {
                  done = true;
                  if ( prevToken != null )
                    break;
                }
                else if ( token.equals( "\t" ))
                {
                  if ( prevToken == null )
                    token = null;
                  else
                  {
                    prevToken = null;
                    continue;
                  }
                }
                prevToken = token;

                Class aClass = sorter.getColumnClass( modelCol );
                if ( aClass == String.class )
                {
                  if (( token.length() == 5 ) &&
                      token.startsWith( "num " ) &&
                      Character.isDigit( token.charAt( 4 )))
                    value = token.substring( 4 );
                  else
                    value = token;
                }
                else
                  value = token;

                sorter.setValueAt( value, row, modelCol );
                workCol++;
              }
              row++;
            }
            if ( addedRow != -1 )
              sorter.fireTableRowsInserted( addedRow, row - 1 );
            sorter.fireTableRowsUpdated( popupRow, row - 1 );
          }
          else
          {
            kit.beep();
          }
        }
        catch (Exception ex)
        {
          String message = ex.getMessage();
          if ( message == null )
            message = ex.toString();
          KeyMapMaster.showMessage( message );
          ex.printStackTrace( System.err );
        }
      }
    }
  }

  // Interface ListSelectionListener
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() )
    {
      int row = table.getSelectedRow();
      if ( row != -1 )
      {
        upButton.setEnabled( row > 0 );
        downButton.setEnabled( row < ( sorter.getRowCount() - 1 ));
        deleteButton.setEnabled( canDelete( sorter.getRow( row )));
        Transferable clipData = clipboard.getContents( clipboard );
        copyButton.setEnabled( true );
        if (( clipData != null ) &&
            clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
          pasteButton.setEnabled( true );
        else
          pasteButton.setEnabled( false );
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

  public void commit()
  {
    finishEditing();
  }

  protected void initColumns()
  {
    JLabel l = new JLabel();
    l.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 4 ));

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn column;
    int width;

    int cols = model.getColumnCount();
    for ( int i = 0; i < cols; i++ )
    {
      column = columnModel.getColumn( i );

      if ( model.isColumnWidthFixed( i ))
      {
        l.setText( model.getColumnName( i ));
        width =  l.getPreferredSize().width;
        column.setMaxWidth( width );
        column.setPreferredWidth( width );
      }

      TableCellEditor editor = model.getColumnEditor( i );
      if ( editor != null )
        column.setCellEditor( editor );

      TableCellRenderer renderer = model.getColumnRenderer( i );
      if ( renderer != null )
        column.setCellRenderer( renderer );
    }
    table.doLayout();
  }

  protected JTable table = null;
  protected KMTableModel model = null;
  private TableSorter sorter = null;
  protected JPanel buttonPanel = null;
  private JButton newButton = null;
  private JButton deleteButton = null;
  private JButton upButton = null;
  private JButton downButton = null;
  private JButton copyButton = null;
  private JButton pasteButton = null;
  private int popupRow = 0;
  private int popupCol = 0;
  protected JPopupMenu popup = null;
  private JMenuItem newItem = null;
  private JMenuItem deleteItem = null;
  private JMenuItem copyItem = null;
  private JMenuItem pasteItem = null;
  private Clipboard clipboard = null;
  private Toolkit kit = null;
  private final static Class[] classes = { String.class };
}
