package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;

// TODO: Auto-generated Javadoc
/**
 * The Interface CellEditorModel.
 */
public interface CellEditorModel
{
  
  /**
   * Gets the cell editor.
   * 
   * @param row the row
   * @param col the col
   * 
   * @return the cell editor
   */
  public TableCellEditor getCellEditor( int row, int col  );
}
