package com.hifiremote.jp1;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public abstract class KMTableModel
  extends JP1TableModel
{
  public KMTableModel()
  {
    super();
  }

  public KMTableModel( Vector data )
  {
    super();
    setData( data );
  }

  public boolean isColumnWidthFixed( int col )
  {
    int lastCol = getColumnCount() - 1;
    if (( col == 1 ) || ( col == lastCol ))
      return false;
    return true;
  }
}
