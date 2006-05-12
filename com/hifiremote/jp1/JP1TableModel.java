package com.hifiremote.jp1;

import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public abstract class JP1TableModel
  extends AbstractTableModel
{
  public JP1TableModel()
  {
    super();
  }
  
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( listener );
  }
  
  public abstract String getColumnPrototypeName( int col );
  
  public void setData( Vector data )
  {
    this.data = data;
    fireTableDataChanged();
  }
  
  public void setData( Object[] array )
  {
    this.array = array;
    fireTableDataChanged();
  }

  public Vector getData(){ return data; }
  
  public Object[] getArray(){ return array; }
  
  public int getRowCount()
  {
    if ( data == null )
    {
      if ( array == null )
        return 0;
      return array.length;
    }
    return data.size();
  }

  public Object getRow( int row )
  {
    if ( data != null )
      return data.elementAt( row );
    if ( array != null )
      return array[ row ];
    return null;
  }

  public void removeRow( int row )
  {
    data.remove( row );
    propertyChangeSupport.firePropertyChange( "size", null, null );
  }

  public void insertRow( int row, Object value )
  {
    data.insertElementAt( value, row );
    propertyChangeSupport.firePropertyChange( "size", null, null );
  }

  public void addRow( Object value )
  {
    data.add( value );
    propertyChangeSupport.firePropertyChange( "size", null, null );

  }

  public void moveRow( int from, int to )
  {
    Object o = data.remove( from );
    if ( to > from )
      to--;
    data.insertElementAt( o, to );
    propertyChangeSupport.firePropertyChange( "order", null, null );
  }
  
  public abstract boolean isColumnWidthFixed( int col );

  public TableCellEditor getColumnEditor( int col ){ return null; }
  public TableCellRenderer getColumnRenderer( int col ){ return null; }

  protected Vector data = null;
  protected Object[] array = null;
  protected SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
}
