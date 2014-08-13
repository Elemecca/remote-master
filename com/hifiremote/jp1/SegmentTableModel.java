package com.hifiremote.jp1;

import java.util.List;

import javax.swing.table.TableCellRenderer;

public class SegmentTableModel extends JP1TableModel< Segment >
{
  List< Segment > segments = null;
  
  public SegmentTableModel()
  {}
  
  private static final String[] colNames =
  {
    "    ", "Length", "Type", "Flags", "Data"
  };

  private static final String[] colPrototypeNames =
  {
    " 00 ", "Length_", "Type_", "Flags_", "Data___________________________"
  };

  private static final Class< ? >[] colClasses =
  {
    Integer.class, String.class, String.class, String.class, Hex.class
  };

  @Override
  public int getColumnCount()
  {
    return colNames.length;
  }
  
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }
  
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return col != 4;
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return false;
  }
  
  public void set( List< Segment > segments )
  {
    this.segments = segments;
    fireTableDataChanged();
  }

  @Override
  public Object getValueAt( int row, int col )
  {
    Segment seg = getRow( row );
    switch ( col )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return String.format( "%04X", seg.getHex().length() + 4 );
      case 2:
        return String.format( "%02X", seg.get_Type() );
      case 3:
        return String.format( "%02X", seg.getFlags() );
      case 4:
        return seg.getHex().toString();
      default:
        return null;
    }
  }
}
