package com.hifiremote.jp1;

import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class DeviceUpgradeTableModel
  extends JP1TableModel< DeviceUpgrade >
  implements PropertyChangeListener
{
  public DeviceUpgradeTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    if ( this.remoteConfig != null )
      for ( DeviceUpgrade upgrade : this.remoteConfig.getDeviceUpgrades())
        upgrade.removePropertyChangeListener( this );
      
    this.remoteConfig = remoteConfig;
    for ( DeviceUpgrade upgrade : remoteConfig.getDeviceUpgrades())
      upgrade.addPropertyChangeListener( this );
    setData( remoteConfig.getDeviceUpgrades());
      
  }

  public int getColumnCount(){ return colNames.length; }

  private static final String[] colNames = 
  {
    "#", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>", "Description"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  private static final String[] colPrototypeNames = 
  {
    "00", "CBL/SAT__", "Setup", "A long description"
  };
  public String getColumnPrototypeName( int col ){ return colPrototypeNames[ col ]; }
  
  public boolean isColumnWidthFixed( int col )
  { 
    if ( col < 3 )
      return true;
    else
      return false;
  }

  private static final Class[] colClasses =
  {
    Integer.class, String.class, SetupCode.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    return false;
  }

  public Object getValueAt( int row, int column )
  {
    DeviceUpgrade device = getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return device.getDeviceTypeAliasName();
      case 2:
        return new SetupCode( device.getSetupCode());
      case 3:
        return device.getDescription();
    }
    return null;
  }
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }
  
  public void removeRow( int row )
  {
    getRow( row ).removePropertyChangeListener( this );
    super.removeRow( row );
  }

  public void insertRow( int row, DeviceUpgrade upgrade )
  {
    upgrade.addPropertyChangeListener( this );
    super.insertRow( row, upgrade );
  }

  public void addRow( DeviceUpgrade upgrade )
  {
    upgrade.addPropertyChangeListener( this );
    super.addRow( upgrade );
  }

  // PropertyChangeListener
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    Vector< DeviceUpgrade > upgrades = remoteConfig.getDeviceUpgrades();
    for ( int i = 0; i < upgrades.size(); ++i )
    {
      DeviceUpgrade upgrade = upgrades.get( i );
      if ( upgrade == source )
      {
        fireTableRowsUpdated( i, i );
        return;
      }
    }
  }
  
  private RemoteConfiguration remoteConfig = null;
}
