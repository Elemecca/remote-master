package com.hifiremote.jp1;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

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
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer()
    {
      public Component getTableCellRendererComponent( JTable table,
                                                      Object value,
                                                      boolean isSelected,
                                                      boolean hasFocus,
                                                      int row,
                                                      int column )
      {
        Button b = ( Button )value;
        if (( b.getFunction() == null ) && ( b.getShiftedFunction() == null ))
          setForeground( Color.red );
        else
          setForeground( Color.black );

        String temp = b.getName();
        if ( !deviceUpgrade.getDeviceType().getButtonMap().isPresent( b ))
          temp = temp + '*';

        return super.getTableCellRendererComponent( table, temp, isSelected,
                                                    hasFocus, row, column );
      }
    };
    renderer.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    table.setDefaultRenderer( Button.class, renderer );

//    table.getTableHeader().setReorderingAllowed( false );
    (( DefaultTableCellRenderer )table.getDefaultRenderer( Function.class )).setToolTipText(
     "Drag or double-click a function to set the functions for a button, or use the popup menu of available functions." );

    TransferHandler th = new TransferHandler()
    {
      public boolean canImport( JComponent comp, DataFlavor[] flavors )
      {
        boolean rc = false;
        if ( table.convertColumnIndexToModel( table.getSelectedColumn()) != 0 )
        {
          for ( int i = 0; i < flavors.length; i++ )
          {
            if ( flavors[ i ] == LocalObjectTransferable.getFlavor())
            {
              rc = true;
              break;
            }
          }
        }
        return rc;
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
        else
          rc = false;

        return rc;
      }
    };
    table.setTransferHandler( th );
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

  private void addFunction( Function f, FunctionLabel l )
  {
    l.addMouseListener( doubleClickListener );
    functionPanel.add( l );

    FunctionItem item = new FunctionItem( f );
    item.addActionListener( this );
    popup.add( item );
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
      addFunction( function, function.getLabel());
    }
    funcs = deviceUpgrade.getExternalFunctions();
    for ( int i = 0; i < funcs.size(); i++ )
    {
      function = ( Function )funcs.elementAt( i );
      addFunction( function, function.getLabel());
    }
    addFunction( null, new FunctionLabel( null ));
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

