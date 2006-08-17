package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class MacroTableModel
  extends JP1TableModel< Macro >
{
  public MacroTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();
    keyRenderer.setRemote( remote );
    keyEditor.setRemote( remote );
    setData( remoteConfig.getMacros());
  }
  
  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  public int getColumnCount(){ return colNames.length; }

  private static final String[] colNames = 
  {
    "#", "Key", "Macro Keys", "Notes"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  private static final Class[] colClasses =
  {
    Integer.class, Integer.class, String.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  private static final String[] colPrototypeNames = 
  {
    "00", "_xShift-VCR/DVD_", "A reasonable length macro with a reasonable number of steps ", 
    "A reasonable length note for a macro"
  };
  public String getColumnPrototypeName( int col ){ return colPrototypeNames[ col ]; }
  
  private static final boolean[] colWidths = { true, true, false, false };
  public boolean isColumnWidthFixed( int col ){ return colWidths[ col ]; }
  
  public boolean isCellEditable( int row, int col )
  {
    if (( col == 0 ) || ( col == 2 ))
      return false;

    return true;
  }

  public Object getValueAt(int row, int column)
  {
    Macro macro = ( Macro )remoteConfig.getMacros().elementAt( row );
    Remote r = remoteConfig.getRemote();
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return new Integer( macro.getKeyCode());
      case 2:
       return macro.getValueString( remoteConfig );
      case 3:
        return macro.getNotes();
      default:
        return null;
    }
  }

  public void setValueAt( Object value, int row, int col )
  {
    Macro macro = ( Macro )getRow( row );
    if ( col == 1 )
      macro.setKeyCode((( Integer )value ).intValue());
    else if ( col == 3 )
      macro.setNotes(( String )value );
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }

  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    else if ( col == 1 )
      return keyRenderer;
    return null;
  }
  
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
      return keyEditor;
    return null;
  }
  
  private RemoteConfiguration remoteConfig = null;
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
  private KeyEditor keyEditor = new KeyEditor();
}
