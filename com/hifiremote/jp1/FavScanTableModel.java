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
  
  private int getEffectiveColumn( int col )
  {
    if ( remoteConfig != null && !remoteConfig.getRemote().usesEZRC() && col > 0 )
    {
      col++;
    }
    return col;
  }

  private static final String[] colPrototypeNames =
  {
      " 00 ", "Name of a favorite ", "A reasonable length macro with a reasonable number of steps ", 
      "A reasonable length note for a macro", "Color_"
  };

  /** The Constant colWidths. */
  private static final boolean[] colWidths =
  {
      true, false, false, false, true
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ getEffectiveColumn( col ) ];
  }

  @Override
  public boolean isCellEditable( int row, int col )
  {
    col = getEffectiveColumn( col );
    if ( col != 1 && col < 3 )
    {
      return false;
    }
    return true;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Name", "Macro Keys", "Notes", "<html>Size &amp<br>Color</html>"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ getEffectiveColumn( col ) ];
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, String.class, String.class, Color.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ getEffectiveColumn( col ) ];
  }

  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ getEffectiveColumn( col ) ];
  }

  @Override
  public int getColumnCount()
  {
    int count = colNames.length - 2;
    if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() )
    {
      ++count;
    }
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }

  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 4 )
    {
      return colorRenderer;
    }
    return null;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 1 )
    {
      return selectAllEditor;
    }
    if ( col == 4 )
    {
      return colorEditor;
    }
    return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    FavScan favScan = remoteConfig.getFavScans().get( row );
    column = getEffectiveColumn( column );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return favScan.getName();
      case 2:
        return favScan.getValueString( remoteConfig );
      case 3:
        return favScan.getNotes();
      case 4:
        return favScan.getHighlight();
      default:
        return null;
    }
  }

  @Override
  public void setValueAt( Object value, int row, int col )
  {
    FavScan favScan = getRow( row );
    col = getEffectiveColumn( col );
    if ( col == 1 )
    {
      favScan.setName( ( String )value );
    }
    if ( col == 3 )
    {
      favScan.setNotes( ( String )value );
    }
    else if ( col == 4 )
    {
      favScan.setHighlight( ( Color  )value );
    }
    propertyChangeSupport.firePropertyChange( col == 3 ? "highlight" : "data", null, null );
  }

  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  private RemoteConfiguration remoteConfig = null;
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
}
