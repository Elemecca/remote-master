package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class SpecialFunctionTableModel
  extends JP1TableModel< SpecialProtocolFunction >
{
  public SpecialFunctionTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();

    setData( remoteConfig.getSpecialFunctions());
    deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons()));
    keyRenderer.setRemote( remote );
    keyEditor.setRemote( remote );
  }
  
  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  public int getColumnCount(){ return colNames.length; }

  private static String[] colNames = 
  {
    "#", "<html>Device<br>Button</html>", "Key", "Type", "Function", "Hex", "Notes"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  private static final String[] colPrototypeNames = 
  {
    "00", "_VCR/DVD_", "shift-Thumbs_Up", " ToadTog(0,ForceOff) ", "[Short]:DiscreteON;DiscreteON; [Long]:DiscreteOFF;DiscreteOFF", "00 11 22 33", "A reasonable length note"
  };

  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  private static boolean[] colWidths = 
  {
    true, true, true, false, false, false, false 
  };

  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }
  
  private static final Class[] colClasses =
  {
    Integer.class, DeviceButton.class, Integer.class, String.class, String.class, Hex.class, String.class
  };
  
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    if (( col == 0 ) || ( col == 3 ) || ( col == 4 ) || ( col == 5 ))
      return false;

    return true;
  }

  public Object getValueAt(int row, int column)
  {
    SpecialProtocolFunction sf = getRow( row );
    Remote r = remoteConfig.getRemote();
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        int index = sf.getDeviceButtonIndex();
        return r.getDeviceButtons()[ index ];
      case 2:
        return new Integer( sf.getKeyCode());
      case 3:
        return sf.getDisplayType();
      case 4:
        return sf.getValueString( remoteConfig );
      case 5:
        return sf.getData();
      case 6:
        return sf.getNotes();
      default:
        return null;
    }
  }

  public void setValueAt( Object value, int row, int col )
  {
    KeyMove keyMove = getRow( row );
    if ( col == 1 )
    {
      Remote r = remoteConfig.getRemote();
      DeviceButton[] deviceButtons = r.getDeviceButtons();
      for ( int i = 0; i < deviceButtons.length; ++i )
        if ( deviceButtons[ i ] == value )
        {
          keyMove.setDeviceButtonIndex( i );
          break;
        }
    }
    else if ( col == 2 )
      keyMove.setKeyCode((( Integer )value ).intValue());
    else if ( col == 6 )
      keyMove.setNotes(( String )value );
    else
      return;
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }
  
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
      editor.setClickCountToStart( 2 );
      return editor;
    }
    else if ( col == 2 )
    {
      return keyEditor;
    }
    return null;
  }
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    else if ( col == 2 )
      return keyRenderer;
    
    return null;
  }
  
  private RemoteConfiguration remoteConfig = null;
  private JComboBox deviceButtonBox = new JComboBox();
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
  private KeyEditor keyEditor = new KeyEditor();
}
