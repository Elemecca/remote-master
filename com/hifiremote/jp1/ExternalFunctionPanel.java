package com.hifiremote.jp1;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JLabel;
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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ExternalFunctionPanel
  extends KMPanel
  implements ActionListener, ListSelectionListener
{
  public ExternalFunctionPanel( DeviceUpgrade devUpgrade )
  {
    super( devUpgrade );

    setLayout( new BorderLayout() );

    kit = Toolkit.getDefaultToolkit();
    clipboard = kit.getSystemClipboard();

    model = new ExternalFunctionTableModel( devUpgrade );
    TableSorter sorter = new TableSorter( model );
    table = new JTable( sorter );
    sorter.addMouseListenerToHeaderInTable( table );
    table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    table.getSelectionModel().addListSelectionListener( this );
    table.setCellSelectionEnabled( true );
    table.setSurrendersFocusOnKeystroke( true );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    table.getTableHeader().setToolTipText( "Click to sort is ascending order, or shift-click to sort in descending order." );
    (( DefaultCellEditor )table.getDefaultEditor( String.class )).setClickCountToStart( 1 );
    TableColumnModel colModel = table.getColumnModel();
    JLabel l = new JLabel();
    l.setBorder( BorderFactory.createEmptyBorder( 0, 4, 0, 4 ));

    for ( int i = 0; i < model.getColumnCount() - 1; i++ )
    {
      TableColumn col = colModel.getColumn( i );
      
      if ( i != 1 )
      {
        l.setText( model.getColumnName( i ));
        col.setMaxWidth( l.getPreferredSize().width );
      }

      TableCellEditor editor = model.getEditor( i );
      if ( editor != null )
        col.setCellEditor( editor );

      TableCellRenderer renderer = model.getRenderer( i );
      if ( renderer != null )
        col.setCellRenderer( renderer );
    }

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
            AbstractTableModel model = ( AbstractTableModel )table.getModel();
            Vector functions = deviceUpgrade.getExternalFunctions();
            Object f = functions.remove( dragRow );
            functions.add( dropRow, f );

            if ( dropRow < dragRow )
              model.fireTableRowsUpdated( dropRow, dragRow );
            else
              model.fireTableRowsUpdated( dragRow, dropRow );
            rc = true;
          }
        }
        catch ( Exception e )
        {
          System.err.println( "ExtrenalFunctionPanel.importData() caught an exception!" );
          e.printStackTrace( System.err );
        }

        return rc;
      }
    };
    table.setTransferHandler( th );

    popup = new JPopupMenu();
    newItem = new JMenuItem( "New" );
    newItem.addActionListener( this );
    popup.add( newItem );

    deleteItem = new JMenuItem( "Delete" );
    deleteItem.addActionListener( this );
    popup.add( deleteItem );

    pasteItem = new JMenuItem( "Paste" );
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

          Function func = ( Function )deviceUpgrade.getExternalFunctions().elementAt( popupRow );
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

//    JToolBar toolbar = new JToolBar( SwingConstants.HORIZONTAL );
//    toolbar.setFloatable( false );
    JPanel toolbar = new JPanel();
    add( toolbar, BorderLayout.SOUTH );

    newButton = new JButton( "New" ); // loadImageIcon( "images/new.gif" ));
    newButton.addActionListener( this );
    newButton.setToolTipText( "Add a new function." );
    toolbar.add( newButton );

    deleteButton = new JButton( "Delete" ); // loadImageIcon( "images/delete.gif" ));
    deleteButton.addActionListener( this );
    deleteButton.setToolTipText( "Delete a function." );
    deleteButton.setEnabled( false );
    toolbar.add( deleteButton );

    upButton = new JButton( "Move up" ); // loadImageIcon( "images/up.gif" ));
    upButton.addActionListener( this );
    upButton.setToolTipText( "Move the selected function up in the list." );
    upButton.setEnabled( false );
    toolbar.add( upButton );

    downButton = new JButton( "Move down" ); // loadImageIcon( "images/down.gif" ));
    downButton.addActionListener( this );
    downButton.setToolTipText( "Move the selected function down in the list." );
    downButton.setEnabled( false );
    toolbar.add( downButton );

    pasteButton = new JButton( "Paste" ); // loadImageIcon( "images/paste.gif" ));
    pasteButton.addActionListener( this );
    pasteButton.setToolTipText( "Paste" );
    pasteButton.setEnabled( false );
    toolbar.add( pasteButton );
  }

  private javax.swing.ImageIcon loadImageIcon( String path )
  {
    javax.swing.ImageIcon rc = null;
    java.net.URL imgURL = getClass().getResource( path );
    if ( imgURL != null )
      rc = new javax.swing.ImageIcon( imgURL );
    return rc;
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

  public void update()
  {
    model.update();
  }

  // Interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    KeyMapMaster.clearMessage();
    Vector functions = deviceUpgrade.getExternalFunctions();
//    AbstractTableModel model = ( AbstractTableModel )table.getModel();
    int row = 0;
    int col = 0;
    boolean select = false;
    Object source = e.getSource();
    if ( source.getClass() == JButton.class )
    {
      row = table.getSelectedRow();
      col = table.getSelectedColumn();
      select = ( row != -1 );
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
      ExternalFunction function = new ExternalFunction();
      if ( row == -1 )
      {
        functions.add( function );
        row = functions.size();
      }
      else
        functions.add( ++row, function );

      model.fireTableRowsInserted( row, row );
      if ( select )
        table.setRowSelectionInterval( row, row );
    }
    else if (( source == deleteButton ) ||
             ( source == deleteItem ))
    {
      Function func = ( Function )functions.elementAt( row );
      if ( func.assigned() )
      {
        String message = "Function is assigned to a button, it can not be deleted.";
        KeyMapMaster.showMessage( message );
        deleteButton.setEnabled( false );
        throw new IllegalArgumentException( message );
      }
      functions.remove( row );
      model.fireTableRowsDeleted( row, row );
      if ( row == functions.size() )
        --row;
      if ( select && ( row >= 0 ))
        table.setRowSelectionInterval( row, row );
    }
    else if (( source == upButton ) ||
             ( source == downButton ))
    {
      int start = 0;
      int end = 0;
      int sel = 0;

      if ( source == upButton )
      {
        start = row - 1;
        end = row;
        sel = start;
      }
      else
      {
        start = row;
        end = row + 1;
        sel = end;
      }
      Object o = functions.elementAt( start );
      functions.set( start, functions.elementAt( end ));
      functions.set( end, o );
      model.fireTableRowsUpdated( start, end );
      if ( select )
        table.setRowSelectionInterval( sel, sel );
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
              if ( row == functions.size() )
              {
                functions.add( new ExternalFunction());
                if ( addedRow == -1 )
                  addedRow = row;
              }

              StringTokenizer st = new StringTokenizer( line, "\t", true );
              int workCol = col;
              while ( st.hasMoreTokens() )
              {
                if ( workCol == colCount )
                  break;
                int modelCol = table.convertColumnIndexToModel( workCol );
                String token = st.nextToken();
                Object value = null;
                if ( !token.equals( "\t" ))
                {
                  if ( st.hasMoreTokens())
                    st.nextToken();

                  Class aClass = model.getColumnClass( modelCol );
                  if ( aClass == String.class )
                  {
                    if (( token.length() == 5 ) &&
                        token.startsWith( "num " ) &&
                        Character.isDigit( token.charAt( 4 )))
                      value = token.substring( 4 );
                    else
                      value = token;
                  }
                  else if ( aClass == byte[].class)
                    value = Protocol.string2hex( token );
                  else
                  {
                    Constructor ct = aClass.getConstructor( classes );
                    Object[] args = { token };
                    value = ct.newInstance( args );
                  }
                }

                model.setValueAt( value, row, modelCol );
                workCol++;
              }
              row++;
            }
            if ( addedRow != -1 )
              model.fireTableRowsInserted( addedRow, row - 1 );
            model.fireTableRowsUpdated( popupRow, row - 1 );
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
      Vector functions = deviceUpgrade.getExternalFunctions();
      int row = table.getSelectedRow();
      boolean flag = ( row != -1 );
      if ( flag )
      {
        Function func = ( Function )functions.elementAt( row );
        upButton.setEnabled( row > 0 );
        downButton.setEnabled( row < ( functions.size() - 1 ));
        deleteButton.setEnabled( !func.assigned());
        Transferable clipData = clipboard.getContents( clipboard );
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
        upButton.setEnabled( false );
        downButton.setEnabled( false );
      }
    }
  }

  public void commit()
  {
    finishEditing();
  }

  private ExternalFunctionTableModel model = null;
  private JTable table = null;
  private JButton newButton = null;
  private JButton deleteButton = null;
  private JButton upButton = null;
  private JButton downButton = null;
  private JButton pasteButton = null;
  private int popupRow = 0;
  private int popupCol = 0;
  private JPopupMenu popup = null;
  private JMenuItem newItem = null;
  private JMenuItem deleteItem = null;
  private JMenuItem pasteItem = null;
  private Clipboard clipboard = null;
  private Toolkit kit = null;
  private final static Class[] classes = { String.class };
}
