package com.hifiremote.jp1;

import javax.swing.table.AbstractTableModel;

public abstract class KMTableModel
  extends AbstractTableModel
{
  public KMTableModel()
  {
    super();
  }
  
  public abstract Object getRow( int row );
  public abstract void removeRow( int row );
  public abstract void insertRow( int row, Object value );
  public abstract void addRow( Object value );
  public abstract void moveRow( int from, int to );
}
