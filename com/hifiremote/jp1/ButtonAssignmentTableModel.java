package com.hifiremote.jp1;

import javax.swing.table.*;
import java.util.*;

public class ButtonAssignmentTableModel
  extends AbstractTableModel
{
  private ButtonAssignment[] assignments = null;
  private static final int buttonCol = 0;
  private static final int functionCol = 1;
  private static final int shiftedCol = 2;

  private static final String[] columnNames =
  { "Button", "Function", "Shifted" };
  private static final Class[] columnClasses =
  { Button.class, Function.class, Function.class };

  public ButtonAssignmentTableModel() { }

  public void setAssignments( ButtonAssignment[] assignments )
  {
    this.assignments = assignments;
    fireTableDataChanged();
  }

  public int getRowCount()
  {
    if ( assignments == null )
      return 0;
    else
      return assignments.length;
  }

  public int getColumnCount()
  {
    return 3;
  }

  public Object getValueAt( int row, int col )
  {
    ButtonAssignment assignment = assignments[ row ];
    Object rc = null;
    switch ( col )
    {
      case buttonCol:
        rc = assignment.getButton();
        break;
      case functionCol:
        rc = assignment.getFunction();
        break;
      case shiftedCol:
        rc = assignment.getShiftedFunction();
        break;
      default:
        break;
    }

    return rc;
  }

  public void setValueAt( Object value, int row, int col )
  {
    ButtonAssignment assignment = assignments[ row ];
    switch ( col )
    {
      case buttonCol:
        break;
      case functionCol:
        assignment.setFunction(( Function )value );
        break;
      case shiftedCol:
        assignment.setShiftedFunction(( Function )value );
        break;
      default:
        break;
    }
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

