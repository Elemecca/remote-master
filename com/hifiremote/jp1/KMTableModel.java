package com.hifiremote.jp1;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public abstract class KMTableModel
  extends AbstractTableModel
{
  public KMTableModel()
  {
    super();
  }

  public KMTableModel( Vector data )
  {
    super();
    this.data = data;
  }

  public void setData( Vector data )
  {
    this.data = data;
  }

  public Vector getData()
  {
    return data;
  }
  
  public int getRowCount()
  {
    int rc = 0;
    if ( data != null )
      rc = data.size();

    return rc;
  }

  public Object getRow( int row )
  {
    return data.elementAt( row );
  }

  public void removeRow( int row )
  {
    data.remove( row );
  }

  public void insertRow( int row, Object value )
  {
    data.insertElementAt( value, row );
  }

  public void addRow( Object value )
  {
    data.add( value );
  }

  public void moveRow( int from, int to )
  {
    Object o = data.remove( from );
    if ( to > from )
      to--;
    data.insertElementAt( o, to );
  }

  public boolean isColumnWidthFixed( int col )
  {
    int lastCol = getColumnCount() - 1;
    if (( col == 1 ) || ( col == lastCol ))
      return false;
    return true;
  }

  public abstract TableCellEditor getColumnEditor( int col );
  public abstract TableCellRenderer getColumnRenderer( int col );

  protected Vector data = null;
}
