package com.hifiremote.jp1;

import javax.swing.table.*;
import java.util.*;

public class ButtonTableModel
  extends AbstractTableModel
{
  private Button[] buttons = null;
  private static final int buttonCol = 0;
  private static final int functionCol = 1;
  private static final int shiftedCol = 2;

  private static final String[] columnNames =
  { "Button", "Function", "Shifted" };
  private static final Class[] columnClasses =
  { Button.class, Function.class, Function.class };

  public ButtonTableModel() { }

  public void setButtons( Button[] buttons )
  {
    this.buttons = buttons;
    fireTableDataChanged();
  }

  public int getRowCount()
  {
    if ( buttons == null )
      return 0;
    else
      return buttons.length;
  }

  public int getColumnCount()
  {
    return 3;
  }

  public Object getValueAt( int row, int col )
  {
    Button button = buttons[ row ];
    Object rc = null;
    switch ( col )
    {
      case buttonCol:
        rc = button;
        break;
      case functionCol:
        rc = button.getFunction();
        break;
      case shiftedCol:
          rc = button.getShiftedFunction();
        break;
      default:
        break;
    }

    return rc;
  }

  public void setValueAt( Object value, int row, int col )
  {
    Button button = buttons[ row ];
    switch ( col )
    {
      case buttonCol:
        break;
      case functionCol:
        button.setFunction(( Function )value );
        break;
      case shiftedCol:
        button.setShiftedFunction(( Function )value );
        break;
      default:
        break;
    }
    fireTableRowsUpdated( row, row );
  }

  public String getColumnName( int col )
  {
    return columnNames[ col ];
  }

  public Class getColumnClass( int col )
  {
    return columnClasses[ col ];
  }
}

