package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroTableModel.
 */
public class MacroTableModel extends JP1TableModel< Macro >
{

  /**
   * Instantiates a new macro table model.
   */
  public MacroTableModel()
  {}

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();
    keyRenderer.setRemote( remote );
    keyEditor.setRemote( remote );
    setData( remoteConfig.getMacros() );
  }

  /**
   * Gets the remote config.
   * 
   * @return the remote config
   */
  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return colNames.length;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Key", "Macro Keys", "Notes"
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
      Integer.class, Integer.class, String.class, String.class
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

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "_xShift-VCR/DVD_", "A reasonable length macro with a reasonable number of steps ",
      "A reasonable length note for a macro"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  /** The Constant colWidths. */
  private static final boolean[] colWidths =
  {
      true, true, false, false
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

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    if ( ( col == 0 ) || ( col == 2 ) )
      return false;

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    Macro macro = remoteConfig.getMacros().get( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return new Integer( macro.getKeyCode() );
      case 2:
        return macro.getValueString( remoteConfig );
      case 3:
        return macro.getNotes();
      default:
        return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    Macro macro = getRow( row );
    if ( col == 1 )
      macro.setKeyCode( ( ( Integer )value ).intValue() );
    else if ( col == 3 )
      macro.setNotes( ( String )value );
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    else if ( col == 1 )
      return keyRenderer;
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
      return keyEditor;
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();
}
