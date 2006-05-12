package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class KeyMoveTableModel
  extends JP1TableModel
{
  public KeyMoveTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    setData( remoteConfig.getKeyMoves());
    this.remoteConfig = remoteConfig;
    deviceButtonBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceButtons()));
    keyRenderer.setRemote( remoteConfig.getRemote());
    keyEditor.setRemote( remoteConfig.getRemote());
    deviceTypeBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceTypes()));
  }

  public int getColumnCount(){ return 8; }

  private static final String[] colNames = 
  {
    "#", "<html>Device<br>Button</html>", "Key", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>", "Raw Data", "<html>EFC or<br>Key Name</html>", "Notes"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  private static final String[] colPrototypeNames = 
  {
    "00", "__VCR/DVD__", "_xshift-Thumbs_Down_", "__VCR/DVD__", "Setup", "Raw Data", "xshift-CBL/SAT", "A reasonable length long note"
  };

  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  private static boolean[] colWidths = 
  {
    true, true, false, true, true, true, false, false 
  };

  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ col ];
  }
  
  private static final Class[] colClasses =
  {
    Integer.class, DeviceButton.class, Integer.class, DeviceType.class, SetupCode.class, Hex.class, Object.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    if (( col == 1 ) || ( col == 2 ) || ( col == 3 ) || ( col == 4 ) || ( col == 7 ))
      return true;

    return false;
  }

  public Object getValueAt(int row, int column)
  {
    KeyMove keyMove = ( KeyMove )getRow( row );
    Remote r = remoteConfig.getRemote();
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return r.getDeviceButtons()[ keyMove.getDeviceButtonIndex()];
      case 2:
        return new Integer( keyMove.getKeyCode());
      case 3:
        return r.getDeviceTypeByIndex( keyMove.getDeviceType());
      case 4:
        return new SetupCode( keyMove.getSetupCode());
      case 5:
        return keyMove.getCmd();
      case 6:
        return keyMove.getValue();
      case 7:
        return keyMove.getNotes();
      default:
        return null;
    }
  }

  public void setValueAt( Object value, int row, int col )
  {
    KeyMove keyMove = ( KeyMove )getRow( row );
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
    else if ( col == 3 )
      keyMove.setDeviceType((( DeviceType )value ).getNumber());
    else if ( col == 4 )
      keyMove.setSetupCode((( SetupCode )value ).getValue());
    else if ( col == 7 )
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
    else if ( col == 3 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceTypeBox );
      editor.setClickCountToStart( 2 );
      return editor;
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
  private JComboBox deviceTypeBox = new JComboBox();
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
  private KeyEditor keyEditor = new KeyEditor();
}
