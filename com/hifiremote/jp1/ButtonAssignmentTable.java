package com.hifiremote.jp1;

import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.table.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonAssignmentTable.
 */
public class ButtonAssignmentTable
  extends JTable
{
  
  /** The model. */
  private ButtonAssignmentTableModel model;

  /**
   * Instantiates a new button assignment table.
   */
  public ButtonAssignmentTable()
  {
    model = new ButtonAssignmentTableModel();
    setModel( model );
    setCellSelectionEnabled( true );
    setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    setSurrendersFocusOnKeystroke( true );

    (( DefaultTableCellRenderer )getDefaultRenderer( Button.class )).setHorizontalAlignment( SwingConstants.CENTER );
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
        ButtonAssignmentTable table = ( ButtonAssignmentTable )c;
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

  /**
   * Sets the assignments.
   * 
   * @param assignments the new assignments
   */
  public void setAssignments( ButtonAssignment[] assignments )
  {
    model.setAssignments( assignments );
  }
}
