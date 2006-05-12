package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class DeviceUpgradeTableModel
  extends JP1TableModel
{
  public DeviceUpgradeTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    setData( remoteConfig.getDeviceUpgrades());
  }

  public int getColumnCount(){ return colNames.length; }

  private static final String[] colNames = 
  {
    "#", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>", "PID", "Upgrade Code", "Notes"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  private static final String[] colPrototypeNames = 
  {
    "00", "CBL/SAT__", "Setup", "1CC", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F", "A reasonable length note"
  };
  public String getColumnPrototypeName( int col ){ return colPrototypeNames[ col ]; }
  
  public boolean isColumnWidthFixed( int col )
  { 
    if ( col < 4 )
      return true;
    else
      return false;
  }

  private static final Class[] colClasses =
  {
    Integer.class, DeviceType.class, SetupCode.class, String.class, Hex.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
     if ( col == 5 )
      return true;

    return false;
  }

  public Object getValueAt( int row, int column )
  {
    DeviceUpgrade device = ( DeviceUpgrade )remoteConfig.getDeviceUpgrades().elementAt( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return device.getDeviceType();
      case 2:
        return new SetupCode( device.getSetupCode());
      case 3:
        return device.getProtocol().getID().toString();
      case 4:
        return device.getUpgradeHex();
      case 5:
        return device.getNotes();
    }
    return null;
  }
  
  public void setValueAt( Object value, int row, int col )
  {
    DeviceUpgrade device = ( DeviceUpgrade )remoteConfig.getDeviceUpgrades().elementAt( row );
    if ( col == 5 )
      device.setNotes(( String )value );
  }    
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }
  
  private RemoteConfiguration remoteConfig = null;
}
