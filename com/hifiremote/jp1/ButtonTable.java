package com.hifiremote.jp1;

import java.awt.datatransfer.*;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.*;

public class ButtonTable
  extends JTable
{
  private ButtonTableModel model;

  public ButtonTable()
  {
    model = new ButtonTableModel();
    setModel( model );
    setCellSelectionEnabled( true );
    setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    setSurrendersFocusOnKeystroke( true );
    TableCellRenderer renderer = new DefaultTableCellRenderer()
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

        return super.getTableCellRendererComponent( table, value, isSelected,
                                                    hasFocus, row, column );
      }
    };
    setDefaultRenderer( Button.class, renderer );

    getTableHeader().setReorderingAllowed( false );
    (( DefaultTableCellRenderer )getDefaultRenderer( Function.class )).setToolTipText(
     "Drag or double-click a function to set the functions for a button, or use the popup menu of available functions." );

    TransferHandler th = new TransferHandler()
    {
      public boolean canImport( JComponent comp, DataFlavor[] flavors )
      {
        boolean rc = false;
        for ( int i = 0; i < flavors.length; i++ )
        {
          if ( flavors[ i ] == LocalObjectTransferable.getFlavor())
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
        ButtonTable table = ( ButtonTable )c;
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if ( col != 0 )
        {
          try
          {
            Function f = ( Function )t.getTransferData( LocalObjectTransferable.getFlavor());
            model.setValueAt( f, row, col );
            model.fireTableCellUpdated( row, col );
          }
          catch ( Exception e )
          {
            rc = false;
            System.err.println( "ButtonTable.importData() caught an exception!" );
            e.printStackTrace( System.err );
          }
        }
        else
          rc = false;

        return rc;
      }
    };
    setTransferHandler( th );
  }

  public void setButtons( Button[] buttons )
  {
    model.setButtons( buttons );
  }
}
