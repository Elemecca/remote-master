package com.hifiremote.jp1;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public abstract class KMTableModel< E >
  extends JP1TableModel< E >
{
  public KMTableModel()
  {
    super();
  }

  public KMTableModel( List< E > data )
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
