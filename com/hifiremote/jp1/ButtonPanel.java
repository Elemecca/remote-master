package com.hifiremote.jp1;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;

public class ButtonPanel
  extends KMPanel
  implements ActionListener
{
  public ButtonPanel( DeviceUpgrade devUpgrade )
  {
    super( "Buttons", devUpgrade );
    setLayout( new BorderLayout());

    table = new JTable();
    model = new ButtonTableModel( devUpgrade );
    table.setModel( model );
    table.setRowSelectionAllowed( false );
    table.setColumnSelectionAllowed( false );
    table.setCellSelectionEnabled( true );
    table.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
    table.setSurrendersFocusOnKeystroke( true );

    TableColumnModel colModel = table.getColumnModel();
    table.setDefaultRenderer( Button.class, new FunctionRenderer( deviceUpgrade ));
    table.getTableHeader().setReorderingAllowed( false );

    TransferHandler th = new TransferHandler()
    {
      public boolean canImport( JComponent comp, DataFlavor[] flavors )
      {
        for ( int i = 0; i < flavors.length; i++ )
        {
          if ( flavors[ i ] == LocalObjectTransferable.getFlavor())
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
        if ( table.convertColumnIndexToModel( col ) != 0 )
        {
          try
          {
            Function f = ( Function )t.getTransferData( LocalObjectTransferable.getFlavor());
            setFunctionAt( f, row, col );
          }
          catch ( Exception e )
          {
            rc = false;
            System.err.println( "ButtonPanel.importData() caught an exception!" );
            e.printStackTrace( System.err );
          }
        }

        return rc;
      }
    };
    table.setTransferHandler( th );
    try
    {
      table.getDropTarget().addDropTargetListener( new DropTargetAdapter()
      {
        public void dragOver( DropTargetDragEvent dte )
        {
          int col = table.convertColumnIndexToModel( table.getSelectedColumn());
          int row = table.getSelectedRow();
          if ( canAssign( row, col ))
            dte.acceptDrag( dte.getDropAction());
          else
            dte.rejectDrag();
        }

        public void drop( DropTargetDropEvent dte )
        {
          ;
        }
      });
    }
    catch ( Exception x )
    {
      x.printStackTrace( System.err );
    }
    table.addMouseListener( new PopupListener());

    add( new JScrollPane( table ), BorderLayout.CENTER );

    JPanel panel = new JPanel( new BorderLayout());
    JLabel label = new JLabel( "Available Functions:" );
    label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ));
    panel.add( label, BorderLayout.NORTH );
    add( panel, BorderLayout.EAST );

    JPanel outerPanel = new JPanel( new BorderLayout());
    functionPanel = new JPanel( new GridLayout( 0, 3 ));
    outerPanel.add( functionPanel, BorderLayout.NORTH );
    panel.add( new JScrollPane( outerPanel ), BorderLayout.CENTER );

    panel = new JPanel();
    autoAssign = new JButton( "Auto assign" );
    autoAssign.setToolTipText( "Assign functions to buttons of the same name that don't have a functon." );
    autoAssign.addActionListener( this );
    panel.add( autoAssign );

    add( panel, BorderLayout.SOUTH );
  }

  public void update()
  {
    setButtons( deviceUpgrade.getRemote().getUpgradeButtons());
    setFunctions();
  }

  private void setButtons( Button[] buttons )
  {
    this.buttons = buttons;
    model.setButtons();
  }

  private void addFunction( Function f )
  {
    if (( f == null ) ||
        (( f.getHex() != null ) && ( f.getName() != null ) && (f.getName().length() > 0 )))
    {
      FunctionLabel l;
      if ( f == null )
        l = new FunctionLabel( null );
      else
        l = f.getLabel();
      l.addMouseListener( doubleClickListener );
      functionPanel.add( l );

      FunctionItem item;
      if ( f == null )
        item = new FunctionItem( null );
      else
        item = f.getItem();
      item.addActionListener( this );
      popup.add( item );
    }
  }

  private void setFunctions()
  {
    popup = new JPopupMenu();
    popup.setLayout( new GridLayout( 0, 3 ));
    FunctionItem item = null;

    functionPanel.removeAll();
    FunctionLabel label = null;
    Function function = null;

    Vector funcs = deviceUpgrade.getFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function );
    }
    funcs = deviceUpgrade.getExternalFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function );
    }
    addFunction( null );
  }

  // From interface ActionListener
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source.getClass() == FunctionItem.class )
    {
      Function function = (( FunctionItem )source ).getFunction();
      setFunctionAt( function, mouseRow, mouseCol );
    }
    else if ( source == autoAssign )
    {
      deviceUpgrade.autoAssignFunctions();
      model.setButtons();
    }
  }

  class PopupListener
    extends MouseAdapter
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
        mouseCol = table.columnAtPoint( e.getPoint());
        mouseRow = table.rowAtPoint( e.getPoint());
        if ( canAssign( mouseRow, mouseCol ))
          popup.show( e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  private boolean canAssign( int row, int col )
  {
    if ( col == 0 )
      return false;

    DeviceType devType = deviceUpgrade.getDeviceType();
    ButtonMap map = devType.getButtonMap();
    Button b = ( Button )model.getValueAt( row, col );
    if ( col == 1 )
    {
      if ( b.allowsKeyMove() || map.isPresent( b ))
        return true;
      else
        return false;
    }
    else if ( col == 2 )
    {
      if ( b.allowsShiftedKeyMove())
        return true;
      else
        return false;
    }
    else if ( col == 3 )
    {
      if ( b.allowsXShiftedKeyMove())
        return true;
      else
        return false;
    }
    return false;
  }

  private void setFunctionAt( Function function, int row, int col )
  {
    int modelCol = table.convertColumnIndexToModel( col );
    if (( modelCol > 0 ) && ( row != -1 ))
    {
      model.setValueAt( function, row, modelCol );
      model.fireTableCellUpdated( row, modelCol );
    }
  }

  class DoubleClickListener
    extends MouseAdapter
  {
    public void mouseClicked( MouseEvent e )
    {
      FunctionLabel label = ( FunctionLabel )e.getSource();
      if ( e.getClickCount() == 2 )
      {
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
        if ( canAssign( row, col ))
          setFunctionAt( label.getFunction(), row, col );
      }
    }
  }

  private JTable table = null;
  private ButtonTableModel model = null;
  private JPanel functionPanel = null;
  private JPopupMenu popup = null;
  private int mouseRow = 0;
  private int mouseCol = 0;
  private DoubleClickListener doubleClickListener = new DoubleClickListener();
  private JButton autoAssign = null;
  private Button[] buttons = null;
}

