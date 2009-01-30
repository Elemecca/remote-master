package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;

// TODO: Auto-generated Javadoc
/**
 * The Class ButtonAssignmentTableModel.
 */
public class ButtonAssignmentTableModel
  extends AbstractTableModel
{
  
  /** The assignments. */
  private ButtonAssignment[] assignments = null;
  
  /** The Constant buttonCol. */
  private static final int buttonCol = 0;
  
  /** The Constant functionCol. */
  private static final int functionCol = 1;
  
  /** The Constant shiftedCol. */
  private static final int shiftedCol = 2;

  /** The Constant columnNames. */
  private static final String[] columnNames =
  { "Button", "Function", "Shifted" };
  
  /** The Constant columnClasses. */
  private static final Class<?>[] columnClasses =
  { Button.class, Function.class, Function.class };

  /**
   * Instantiates a new button assignment table model.
   */
  public ButtonAssignmentTableModel() { }

  /**
   * Sets the assignments.
   * 
   * @param assignments the new assignments
   */
  public void setAssignments( ButtonAssignment[] assignments )
  {
    this.assignments = assignments;
    fireTableDataChanged();
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount()
  {
    if ( assignments == null )
      return 0;
    else
      return assignments.length;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 3;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
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

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
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

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    return columnNames[ col ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass( int col )
  {
    return columnClasses[ col ];
  }
}

