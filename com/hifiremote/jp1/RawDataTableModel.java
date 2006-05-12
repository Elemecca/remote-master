package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;

public class RawDataTableModel
  extends JP1TableModel
{
  public RawDataTableModel(){}
  
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    fireTableDataChanged();
  }

  public int getRowCount()
  {
    if ( remoteConfig == null )
      return 0;
    return remoteConfig.getData().length / 16;
  }

  public int getColumnCount(){ return  17; }

  private int getOffset( int row, int col )
  {
    return ( row * 16 ) + col - 1;
  }

  public boolean isCellEditable( int row, int col ) 
  {
    return col > 0;
  }

  public Object getValueAt( int row, int col )
  {
    int base = row * 16;
    if ( col == 0 )
      return new Integer( base );
    else
      return new UnsignedByte( remoteConfig.getData()[ getOffset( row, col )]);
  }

  public void setValueAt( Object value, int row, int col )
  {
    remoteConfig.getData()[ getOffset( row, col )] = (( UnsignedByte )value ).getValue();
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  public Class getColumnClass( int col )
  {
    if ( col == 0 )
      return Integer.class;
    return UnsignedByte.class;
  }

  private final static String[] colNames = 
  {
    "    ", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  public String getColumnPrototypeName( int col )
  {
    if ( col == 0 )
      return "0000";
    return "CC";
  }
  
  public boolean isColumnWidthFixed( int col )
  {
    return true;
  }
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer( true );
    return null;
  }
  
  private RemoteConfiguration remoteConfig = null;
}
