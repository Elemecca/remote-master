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
    super( devUpgrade );
    setLayout( new BorderLayout());

    table = new JTable();
    model = new ButtonTableModel();
    table.setModel( model );
    table.setCellSelectionEnabled( true );
    table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    table.setSurrendersFocusOnKeystroke( true );

    TableColumnModel colModel = table.getColumnModel();
    table.setDefaultRenderer( Button.class, new FunctionRenderer( deviceUpgrade ));

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
          if ( col == 0 )
            dte.rejectDrag();
          else if ( col == 2 )
          {
            int row = table.getSelectedRow();
            Button b = ( Button )model.getValueAt( row, col );
            if ( b.allowsShift())
              dte.acceptDrag( dte.getDropAction());
            else
              dte.rejectDrag();
          }
          else
            dte.acceptDrag( dte.getDropAction());
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
    model.setButtons( buttons );
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

  private void autoAssignFunctions()
  {
    autoAssignFunctions( deviceUpgrade.getFunctions());
    autoAssignFunctions( deviceUpgrade.getExternalFunctions());
    model.setButtons( buttons );
  }

  private void autoAssignFunctions( Vector funcs )
  {
    for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
    {
      Function func = ( Function )e.nextElement();
      if ( func.getHex() != null )
      {
        for ( int i = 0; i < buttons.length; i++ )
        {
          Button b = buttons[ i ];
          if ( b.getFunction() == null )
          {
            if ( b.getName().equalsIgnoreCase( func.getName()) ||
                 b.getStandardName().equalsIgnoreCase( func.getName()))
            {
              b.setFunction( func );
              break;
            }
          }
        }
      }
    }
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
      autoAssignFunctions();
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
        if ( table.convertColumnIndexToModel( mouseCol ) != 0 )
        {
          mouseRow = table.rowAtPoint( e.getPoint());
          popup.show( e.getComponent(), e.getX(), e.getY());
        }
      }
    }
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
      if ( e.getClickCount() < 2 )
        e.consume();
      else
      {
        FunctionLabel label = ( FunctionLabel )e.getSource();
        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();
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

