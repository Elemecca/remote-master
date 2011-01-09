package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonPanel.
 */
public class ButtonPanel extends KMPanel implements ActionListener
{

  /**
   * Instantiates a new button panel.
   * 
   * @param devUpgrade
   *          the dev upgrade
   */
  public ButtonPanel( DeviceUpgrade devUpgrade )
  {
    super( "Buttons", devUpgrade );
    setLayout( new BorderLayout() );

    table = new JTableX();
    model = new ButtonTableModel( devUpgrade );
    table.setModel( model );
    table.setRowSelectionAllowed( false );
    table.setColumnSelectionAllowed( false );
    table.setCellSelectionEnabled( true );
    table.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
    table.setSurrendersFocusOnKeystroke( true );
    table.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), "delete" );
    table.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
    table.setDefaultEditor( Function.class, popupEditor );

    deleteAction = new AbstractAction( "Remove" )
    {
      public void actionPerformed( ActionEvent e )
      {
        int[] cols = table.getSelectedColumns();
        int[] rows = table.getSelectedRows();
        for ( int c = 0; c < cols.length; c++ )
          for ( int r = 0; r < rows.length; r++ )
            setFunctionAt( null, rows[ r ], cols[ c ] );
      }
    };
    deleteAction.setEnabled( false );
    deleteAction.putValue( AbstractAction.SHORT_DESCRIPTION, "Remove the assigned function from the button." );
    table.getActionMap().put( "delete", deleteAction );

    renderer = new FunctionRenderer( deviceUpgrade );
    table.setDefaultRenderer( Button.class, renderer );
    table.getTableHeader().setReorderingAllowed( false );

    ListSelectionListener lsl = new ListSelectionListener()
    {
      public void valueChanged( ListSelectionEvent e )
      {
        // Ignore extra messages.
        if ( e.getValueIsAdjusting() )
          return;

        selectionChanged();
      }
    };
    table.getSelectionModel().addListSelectionListener( lsl );
    table.getColumnModel().getSelectionModel().addListSelectionListener( lsl );

    TransferHandler th = new TransferHandler()
    {
      public boolean canImport( JComponent comp, DataFlavor[] flavors )
      {
        for ( int i = 0; i < flavors.length; i++ )
        {
          if ( ( flavors[ i ] == DataFlavor.stringFlavor ) || flavors[ i ] == LocalObjectTransferable.getFlavor() )
          {
            return true;
          }
        }
        return false;
      }

      public boolean importData( JComponent c, Transferable t )
      {
        boolean rc = false;
        JTable table = ( JTable )c;
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if ( col != 0 )
        {
          if ( t.isDataFlavorSupported( DataFlavor.stringFlavor ) )
          {
            try
            {
              String s = ( String )( t.getTransferData( DataFlavor.stringFlavor ) );
              BufferedReader in = new BufferedReader( new StringReader( s ) );
              int colCount = table.getModel().getColumnCount();
              int workRow = row;
              for ( String line = in.readLine(); line != null; line = in.readLine() )
              {
                if ( row == model.getRowCount() )
                {
                  break;
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
                  if ( st.hasMoreTokens() )
                    token = st.nextToken();
                  else
                    token = null;

                  if ( token == null )
                  {
                    done = true;
                    if ( prevToken != null )
                      break;
                  }
                  else if ( token.equals( "\t" ) )
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

                  if ( token != null )
                  {
                    model.setValueAt( deviceUpgrade.getFunction( token ), workRow, workCol );
                  }
                  workCol++ ;
                }
                workRow++ ;
              }
              model.fireTableRowsUpdated( row, workRow - 1 );
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
              Function f = ( Function )t.getTransferData( LocalObjectTransferable.getFlavor() );
              setFunctionAt( f, row, col );
            }
            catch ( Exception e )
            {
              rc = false;
              System.err.println( "ButtonPanel.importData() caught an exception!" );
              e.printStackTrace( System.err );
            }
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
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents( data, data );
      }
    };
    table.setTransferHandler( th );
    try
    {
      table.getDropTarget().addDropTargetListener( new DropTargetAdapter()
      {
        public void dragOver( DropTargetDragEvent dte )
        {
          int col = table.getSelectedColumn();
          int row = table.getSelectedRow();
          if ( canAssign( row, col ) )
            dte.acceptDrag( dte.getDropAction() );
          else
            dte.rejectDrag();
        }

        public void drop( DropTargetDropEvent dte )
        {
          ;
        }
      } );
    }
    catch ( Exception x )
    {
      x.printStackTrace( System.err );
    }

    popup = new JPopupMenu();

    deleteItem = new JMenuItem( deleteAction );
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

      private boolean showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          finishEditing();
          popupRow = table.rowAtPoint( e.getPoint() );
          popupCol = table.columnAtPoint( e.getPoint() );

          if ( popupCol == 0 )
            return false;

          deleteItem.setEnabled( table.getValueAt( popupRow, popupCol ) != null );

          Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
          Transferable clipData = clipboard.getContents( clipboard );
          boolean flag = ( clipData != null ) && clipData.isDataFlavorSupported( DataFlavor.stringFlavor )
              && ( popupCol != 0 );
          pasteItem.setEnabled( flag );

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

    JPanel panel = new JPanel( new BorderLayout() );
    JLabel label = new JLabel( "Available Functions:" );
    label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ) );
    panel.add( label, BorderLayout.NORTH );
    add( panel, BorderLayout.EAST );

    JPanel outerPanel = new JPanel( new BorderLayout() );
    functionPanel = new JPanel( new GridLayout( 0, 3 ) );
    outerPanel.add( functionPanel, BorderLayout.NORTH );
    panel.add( new JScrollPane( outerPanel ), BorderLayout.CENTER );

    JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, new JScrollPane( table ), new JScrollPane(
        panel ) );
    splitPane.setResizeWeight( 0.3 );

    add( splitPane, BorderLayout.CENTER );
    panel = new JPanel();
    autoAssign = new JButton( "Auto assign" );
    autoAssign.setToolTipText( "Assign functions to buttons of the same name that don't have a functon." );
    autoAssign.addActionListener( this );
    panel.add( autoAssign );

    JButton button = new JButton( deleteAction );
    button.setToolTipText( "Remove the assigned function from the button." );
    panel.add( button );

    add( panel, BorderLayout.SOUTH );
  }

  /**
   * Selection changed.
   */
  private void selectionChanged()
  {
    boolean enableDelete = false;
    int[] rows = table.getSelectedRows();
    int[] cols = table.getSelectedColumns();
    for ( int r = 0; ( r < rows.length ) && !enableDelete; r++ )
    {
      int row = rows[ r ];
      Button b = ( Button )model.getValueAt( row, 0 );
      for ( int c = 0; ( c < cols.length ) && !enableDelete; c++ )
      {
        int col = cols[ c ];
        if ( col > 0 )
        {
          Function f = null;
          if ( col == 1 )
            f = deviceUpgrade.getFunction( b, Button.NORMAL_STATE );
          else if ( col == 2 )
            f = deviceUpgrade.getFunction( b, Button.SHIFTED_STATE );
          else if ( col == 3 )
            f = deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE );
          if ( f != null )
            enableDelete = true;
        }
      }
    }
    deleteAction.setEnabled( enableDelete );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    model.setDeviceUpgrade( deviceUpgrade );
    renderer.setDeviceUpgrade( deviceUpgrade );

    if ( deviceUpgrade != null )
    {
      setButtons( deviceUpgrade.getRemote().getUpgradeButtons() );
      setFunctions();
    }
  }

  /**
   * Sets the buttons.
   * 
   * @param buttons
   *          the new buttons
   */
  private void setButtons( Button[] buttons )
  {
    model.setButtons();
  }

  /**
   * Adds the function.
   * 
   * @param f
   *          the f
   */
  private void addFunction( Function f )
  {
    if ( ( f == null ) || ( ( f.getHex() != null ) && ( f.getName() != null ) && ( f.getName().length() > 0 ) ) )
    {
      FunctionLabel l;
      if ( f == null )
        l = new FunctionLabel( null );
      else
        l = f.getLabel();
      l.addMouseListener( doubleClickListener );
      functionPanel.add( l );

      popupEditor.addObject( f );
    }
  }

  /**
   * Sets the functions.
   */
  private void setFunctions()
  {
    popupEditor.removeAll();

    functionPanel.removeAll();

    for ( Function function : deviceUpgrade.getFunctions() )
      addFunction( function );

    for ( ExternalFunction function : deviceUpgrade.getExternalFunctions() )
      addFunction( function );

    functionPanel.doLayout();
  }

  // From interface ActionListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == autoAssign )
    {
      deviceUpgrade.autoAssignFunctions();
      model.setButtons();
    }
    else if ( source == copyItem )
    {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      table.getTransferHandler().exportToClipboard( table, clipboard, TransferHandler.COPY );
    }
    else if ( source == pasteItem )
    {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
        table.getTransferHandler().importData( table, clipboard.getContents( clipboard ) );
      else
        Toolkit.getDefaultToolkit().beep();
    }
    deviceUpgrade.checkSize();
  }

  /**
   * Can assign.
   * 
   * @param row
   *          the row
   * @param col
   *          the col
   * @return true, if successful
   */
  private boolean canAssign( int row, int col )
  {
    return model.isCellEditable( row, col );
  }

  /**
   * Sets the function at.
   * 
   * @param function
   *          the function
   * @param row
   *          the row
   * @param col
   *          the col
   */
  private void setFunctionAt( Function function, int row, int col )
  {
    int[] rows = null;
    int[] cols = null;
    if ( table.isCellSelected( row, col ) )
    {
      rows = table.getSelectedRows();
      cols = table.getSelectedColumns();
    }
    else
    {
      rows = new int[ 1 ];
      rows[ 0 ] = row;
      cols = new int[ 1 ];
      cols[ 0 ] = col;
    }

    int firstRow = row;
    for ( int r = 0; r < rows.length; r++ )
    {
      row = rows[ r ];
      if ( r == 0 )
        firstRow = row;
      for ( int c = 0; c < cols.length; c++ )
      {
        col = cols[ c ];

        if ( ( col > 0 ) && ( row != -1 ) )
        {
          model.setValueAt( function, row, col );
        }
      }
      model.fireTableRowsUpdated( firstRow, row );
    }
    selectionChanged();
  }

  /**
   * The listener interface for receiving doubleClick events. The class that is interested in processing a doubleClick
   * event implements this interface, and the object created with that class is registered with a component using the
   * component's <code>addDoubleClickListener<code> method. When
   * the doubleClick event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DoubleClickEvent
   */
  class DoubleClickListener extends MouseAdapter
  {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked( MouseEvent e )
    {
      FunctionLabel label = ( FunctionLabel )e.getSource();
      if ( e.getClickCount() == 2 )
      {
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if ( canAssign( row, col ) )
        {
          setFunctionAt( label.getFunction(), row, col );
          deviceUpgrade.checkSize();
        }
      }
    }
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
  }

  /** The table. */
  private JTableX table = null;

  /** The model. */
  private ButtonTableModel model = null;

  /** The function panel. */
  private JPanel functionPanel = null;
  // private JPopupMenu popup = null;
  /** The double click listener. */
  private DoubleClickListener doubleClickListener = new DoubleClickListener();

  /** The auto assign. */
  private JButton autoAssign = null;

  /** The delete action. */
  private AbstractAction deleteAction = null;

  /** The popup editor. */
  private PopupEditor popupEditor = new PopupEditor();

  /** The renderer. */
  private FunctionRenderer renderer = null;

  /** The popup row. */
  private int popupRow = 0;

  /** The popup col. */
  private int popupCol = 0;

  /** The popup. */
  protected JPopupMenu popup = null;

  /** The delete item. */
  private JMenuItem deleteItem = null;

  /** The copy item. */
  private JMenuItem copyItem = null;

  /** The paste item. */
  private JMenuItem pasteItem = null;
}
