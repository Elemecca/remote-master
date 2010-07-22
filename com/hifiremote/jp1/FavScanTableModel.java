package com.hifiremote.jp1;

import javax.swing.table.TableCellRenderer;

public class FavScanTableModel extends JP1TableModel< FavScan >
{

  public FavScanTableModel()
  {
  // TODO Auto-generated constructor stub
  }
  
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    setData( remoteConfig.getFavScans() );
  }

  private static final String[] colPrototypeNames =
  {
      " 00 ", "A reasonable length macro with a reasonable number of steps ",
      "A reasonable length note for a macro"
  };
  
  /** The Constant colWidths. */
  private static final boolean[] colWidths =
  {
      true, false, false
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }
  
  public boolean isCellEditable( int row, int col )
  {
    if ( col < 2 )
    {
      return false;
    }
    return true;
  }
 
  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Macro Keys", "Notes"
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
  
  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, String.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }
  
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  @Override
  public int getColumnCount()
  {
    return 3;
  }
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    FavScan favScan = remoteConfig.getFavScans().get( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return favScan.getValueString( remoteConfig );
      case 2:
        return favScan.getNotes();
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    FavScan favScan = getRow( row );
    if ( col == 2 )
    {  
      favScan.setNotes( ( String )value );
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }
  
  private RemoteConfiguration remoteConfig = null;
}
