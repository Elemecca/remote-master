package com.hifiremote.jp1;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceUpgradeTableModel.
 */
public class DeviceUpgradeTableModel extends JP1TableModel< DeviceUpgrade > implements PropertyChangeListener
{

  /**
   * Instantiates a new device upgrade table model.
   */
  public DeviceUpgradeTableModel()
  {}

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    if ( this.remoteConfig != null )
      for ( DeviceUpgrade upgrade : this.remoteConfig.getDeviceUpgrades() )
        upgrade.removePropertyChangeListener( this );

    this.remoteConfig = remoteConfig;
    for ( DeviceUpgrade upgrade : remoteConfig.getDeviceUpgrades() )
      upgrade.addPropertyChangeListener( this );
    setData( remoteConfig.getDeviceUpgrades() );

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
      "#", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>", "Description"
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

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "CBL/SAT__", "Setup ", "A long description"
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

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    if ( col < 3 )
      return true;
    else
      return false;
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, SetupCode.class, String.class
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

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    if ( col == 3 )
      return true;
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
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
        return new SetupCode( device.getSetupCode() );
      case 3:
        return device.getDescription();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    DeviceUpgrade device = getRow( row );
    if ( col == 3 )
      device.setDescription( ( String )value );
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
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#removeRow(int)
   */
  public void removeRow( int row )
  {
    getRow( row ).removePropertyChangeListener( this );
    super.removeRow( row );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#insertRow(int, java.lang.Object)
   */
  public void insertRow( int row, DeviceUpgrade upgrade )
  {
    upgrade.addPropertyChangeListener( this );
    super.insertRow( row, upgrade );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#addRow(java.lang.Object)
   */
  public void addRow( DeviceUpgrade upgrade )
  {
    upgrade.addPropertyChangeListener( this );
    super.addRow( upgrade );
  }

  // PropertyChangeListener
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    List< DeviceUpgrade > upgrades = remoteConfig.getDeviceUpgrades();
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

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;
}
