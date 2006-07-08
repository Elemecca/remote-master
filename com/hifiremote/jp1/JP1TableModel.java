package com.hifiremote.jp1;

import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public abstract class JP1TableModel < E >
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
  
  public void setData( Vector< E > data )
  {
    this.data = data;
    fireTableDataChanged();
  }
  
  public void setData( E[] array )
  {
    this.array = array;
    fireTableDataChanged();
  }

  public Vector< E > getData(){ return data; }
  
  public E[] getArray(){ return array; }
  
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

  public E getRow( int row )
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

  public void insertRow( int row, E value )
  {
    data.insertElementAt( value, row );
    propertyChangeSupport.firePropertyChange( "size", null, null );
  }

  public void addRow( E value )
  {
    data.add( value );
    propertyChangeSupport.firePropertyChange( "size", null, null );

  }

  public void moveRow( int from, int to )
  {
    E o = data.remove( from );
    if ( to > from )
      to--;
    data.insertElementAt( o, to );
    propertyChangeSupport.firePropertyChange( "order", null, null );
  }
  
  public abstract boolean isColumnWidthFixed( int col );

  public TableCellEditor getColumnEditor( int col ){ return null; }
  public TableCellRenderer getColumnRenderer( int col ){ return null; }

  protected Vector< E > data = null;
  protected E[] array = null;
  protected SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
}
