package com.hifiremote.jp1;

import java.io.BufferedReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import info.clearthought.layout.TableLayout;

public class FunctionPanel
  extends KMPanel
  implements ActionListener, ListSelectionListener
{
  public FunctionPanel( DeviceUpgrade devUpgrade )
  {
    super( devUpgrade );

    double s = 20;       // space between rows and cols
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
      { f, s, p, s },                     // cols
      { s, p, s, p, s, p, s, p, s, p, f } // rows
    };

    TableLayout layout = new TableLayout( size );
    setLayout( layout );

    kit = Toolkit.getDefaultToolkit();
    clipboard = kit.getSystemClipboard();

    table = new FunctionTable( devUpgrade.getFunctions());
//    table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    table.getSelectionModel().addListSelectionListener( this );
//    table.setCellSelectionEnabled( true );
    table.setRowSelectionAllowed( true );
    table.setSurrendersFocusOnKeystroke( true );
    table.setDragEnabled( true );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
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
            Vector functions = deviceUpgrade.getFunctions();
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
          System.err.println( "FunctionPanel.importData() caught an exception!" );
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

          Function func = ( Function )deviceUpgrade.getFunctions().elementAt( popupRow );
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

    add( new JScrollPane( table ), "0, 0, 0, 10" );

    newButton = new JButton( "New" );
    newButton.addActionListener( this );
    newButton.setToolTipText( "Add a new function." );
    add( newButton, "2, 1" );

    deleteButton = new JButton( "Delete" );
    deleteButton.addActionListener( this );
    deleteButton.setToolTipText( "Delete a function." );
    deleteButton.setEnabled( false );
    add( deleteButton, "2, 3" );

    upButton = new JButton( "Move up" );
    upButton.addActionListener( this );
    upButton.setToolTipText( "Move the selected function up in the list." );
    upButton.setEnabled( false );
    add( upButton, "2, 5" );

    downButton = new JButton( "Move down" );
    downButton.addActionListener( this );
    downButton.setToolTipText( "Move the selected function down in the list." );
    downButton.setEnabled( false );
    add( downButton, "2, 7" );

    pasteButton = new JButton( "Paste" );
    pasteButton.addActionListener( this );
    pasteButton.setToolTipText( "Paste" );
    pasteButton.setEnabled( false );
    add( pasteButton, "2, 9" );
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
    Protocol p = deviceUpgrade.getProtocol();
    p.initializeParms();
    table.setProtocol( p );
    table.setFunctions( deviceUpgrade.getFunctions());
  }

  // Interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    finishEditing();
    KeyMapMaster.clearMessage();
    Vector functions = deviceUpgrade.getFunctions();
    AbstractTableModel model = ( AbstractTableModel )table.getModel();
    int row = 0;
    int col = 0;
    boolean select = false;
    Object source = e.getSource();
    if ( source.getClass() == JButton.class )
    {
      row = table.getSelectedRow();
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
      Function function = new Function();
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
      if ( select )
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
                functions.add( new Function());
                if ( addedRow == -1 )
                  addedRow = row;
              }

              StringTokenizer st = new StringTokenizer( line, "\t", true );
              int workCol = col;
              while ( st.hasMoreTokens() )
              {
                if ( workCol == colCount )
                  break;
                String token = st.nextToken();
                Object value = null;
                int modelCol = table.convertColumnIndexToModel( workCol );
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
      Vector functions = deviceUpgrade.getFunctions();
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

  private FunctionTable table = null;
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
