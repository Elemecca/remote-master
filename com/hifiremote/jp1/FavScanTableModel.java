package com.hifiremote.jp1;

import java.awt.Color;

import javax.swing.table.TableCellEditor;
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
    if ( remoteConfig != null )
    {
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      setData( remoteConfig.getFavScans() );
    }
  }

  private static final String[] colPrototypeNames =
  {
      " 00 ", "A reasonable length macro with a reasonable number of steps ", 
      "A reasonable length note for a macro", "Color"
  };

  /** The Constant colWidths. */
  private static final boolean[] colWidths =
  {
      true, false, false, true
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }

  @Override
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
      "#", "Macro Keys", "Notes", "Color"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, String.class, Color.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
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
    return colNames.length;
  }

  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 3 )
    {
      return colorRenderer;
    }
    return null;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 3 )
    {
      return colorEditor;
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
      case 3:
        return favScan.getHighlight();
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
    else if ( col == 3 )
    {
      favScan.setHighlight( ( Color  )value );
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  private RemoteConfiguration remoteConfig = null;
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
}
