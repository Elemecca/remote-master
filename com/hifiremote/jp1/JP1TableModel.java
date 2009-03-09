package com.hifiremote.jp1;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class JP1TableModel.
 */
public abstract class JP1TableModel< E > extends AbstractTableModel
{

  /**
   * Instantiates a new j p1 table model.
   */
  public JP1TableModel()
  {
    super();
  }

  /**
   * Adds the property change listener.
   * 
   * @param listener
   *          the listener
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( listener );
  }

  /**
   * Gets the column prototype name.
   * 
   * @param col
   *          the col
   * @return the column prototype name
   */
  public abstract String getColumnPrototypeName( int col );

  /**
   * Sets the data.
   * 
   * @param data
   *          the new data
   */
  public void setData( List< E > data )
  {
    this.data = data;
    fireTableStructureChanged();
  }

  /**
   * Sets the data.
   * 
   * @param array
   *          the new data
   */
  public void setData( E[] array )
  {
    this.array = array;
    fireTableStructureChanged();
  }

  /**
   * Gets the data.
   * 
   * @return the data
   */
  public List< E > getData()
  {
    return data;
  }

  /**
   * Gets the array.
   * 
   * @return the array
   */
  public E[] getArray()
  {
    return array;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getRowCount()
   */
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

  /**
   * Gets the row.
   * 
   * @param row
   *          the row
   * @return the row
   */
  public E getRow( int row )
  {
    if ( data != null )
      return data.get( row );
    if ( array != null )
      return array[ row ];
    return null;
  }

  /**
   * Sets the row.
   * 
   * @param row
   *          the row
   * @param value
   *          the value
   */
  public void setRow( int row, E value )
  {
    data.set( row, value );
    propertyChangeSupport.firePropertyChange( "data", null, null );
    fireTableRowsUpdated( row, row );
  }

  /**
   * Removes the row.
   * 
   * @param row
   *          the row
   */
  public void removeRow( int row )
  {
    data.remove( row );
    propertyChangeSupport.firePropertyChange( "size", null, null );
    fireTableRowsDeleted( row, row );
  }

  /**
   * Insert row.
   * 
   * @param row
   *          the row
   * @param value
   *          the value
   */
  public void insertRow( int row, E value )
  {
    data.add( row, value );
    propertyChangeSupport.firePropertyChange( "size", null, null );
    fireTableRowsInserted( row, row );
  }

  /**
   * Adds the row.
   * 
   * @param value
   *          the value
   */
  public void addRow( E value )
  {
    data.add( value );
    propertyChangeSupport.firePropertyChange( "size", null, null );
    int row = data.size() - 1;
    fireTableRowsInserted( row, row );
  }

  /**
   * Move row.
   * 
   * @param from
   *          the from
   * @param to
   *          the to
   */
  public void moveRow( int from, int to )
  {
    E o = data.remove( from );
    data.add( to, o );
    propertyChangeSupport.firePropertyChange( "order", null, null );
    fireTableRowsUpdated( from, to );
  }

  /**
   * Checks if is column width fixed.
   * 
   * @param col
   *          the col
   * @return true, if is column width fixed
   */
  public boolean isColumnWidthFixed( int col )
  {
    return col == 0;
  }

  /**
   * Gets the column editor.
   * 
   * @param col
   *          the col
   * @return the column editor
   */
  public TableCellEditor getColumnEditor( int col )
  {
    return null;
  }

  /**
   * Gets the column renderer.
   * 
   * @param col
   *          the col
   * @return the column renderer
   */
  public TableCellRenderer getColumnRenderer( int col )
  {
    return null;
  }

  /** The data. */
  protected List< E > data = null;

  /** The array. */
  protected E[] array = null;

  /** The property change support. */
  protected SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
}
