package com.hifiremote.jp1;

import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class RawDataTableModel.
 */
public class RawDataTableModel extends JP1TableModel< short[] >
{

  /**
   * Instantiates a new raw data table model.
   */
  public RawDataTableModel()
  {}

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( short[] data, int baseAddress )
  {
    this.data = data;
    this.baseAddress = baseAddress;
    fireTableDataChanged();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getRowCount()
   */
  public int getRowCount()
  {
    if ( data == null )
      return 0;
    return data.length / 16;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return 17;
  }

  /**
   * Gets the offset.
   * 
   * @param row
   *          the row
   * @param col
   *          the col
   * @return the offset
   */
  private int getOffset( int row, int col )
  {
    return ( row * 16 ) + col - 1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    return col > 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int col )
  {
    int base = row * 16;
    if ( col == 0 )
      return new Integer( base + baseAddress );
    else if ( data == null )
      return null;
    else
      return new UnsignedByte( ( short )( data[ getOffset( row, col ) ] & 0xFF ) );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    data[ getOffset( row, col ) ] = ( ( UnsignedByte )value ).getValue();
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class< ? > getColumnClass( int col )
  {
    if ( col == 0 )
      return Integer.class;
    return UnsignedByte.class;
  }

  /** The Constant colNames. */
  private final static String[] colNames =
  {
      "    ", "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0A", "0B", "0C", "0D", "0E", "0F"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  public String getColumnPrototypeName( int col )
  {
    if ( col == 0 )
      return "0000: ";
    return " CC ";
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer( true );
    return null;
  }

  /** The remote config. */
  private short[] data = null;
  private int baseAddress = 0;
}
