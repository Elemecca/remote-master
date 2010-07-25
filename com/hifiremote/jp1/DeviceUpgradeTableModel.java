package com.hifiremote.jp1;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
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
    this.remoteConfig = remoteConfig;
    if ( remoteConfig != null )
    {
      for ( DeviceUpgrade upgrade : remoteConfig.getDeviceUpgrades() )
      {
        upgrade.removePropertyChangeListener( this );
      }

      for ( DeviceUpgrade upgrade : remoteConfig.getDeviceUpgrades() )
      {
        upgrade.addPropertyChangeListener( this );
      }
      setData( remoteConfig.getDeviceUpgrades() );

      Remote remote = remoteConfig.getRemote();
      if ( remote.getDeviceUpgradeAddress() != null )
      {
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel( remote.getDeviceButtons() );
        comboModel.insertElementAt( DeviceButton.noButton, 0 );
        deviceButtonBox.setModel( comboModel );
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    int count = 4;

    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      if ( remote.getDeviceUpgradeAddress() != null )
      {
        count += 2;
      }
    }
    return count;
  }

  public int getEffectiveColumn( int col )
  {
    if ( ( remoteConfig == null || remoteConfig != null && remoteConfig.getRemote().getDeviceUpgradeAddress() == null )
        && col == 3 )
    {
      return 5;
    }
    return col;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "<html>Device<br>Type</html>", "<html>Setup<br>Code</html>", "<html>Specific to<br>Device Button</html>",
      "<html>Available on<br>Other Buttons?</html>", "Description"
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

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "CBL/SAT__", "Setup ", "Device Button", "Other Buttons?", "A long description"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ getEffectiveColumn( col ) ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    if ( getEffectiveColumn( col ) < 5 )
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, SetupCode.class, String.class, Boolean.class, String.class
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

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int row, int col )
  {
    if ( col >= 3 )
    {
      return true;
    }
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
    switch ( getEffectiveColumn( column ) )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return device.getDeviceTypeAliasName();
      case 2:
        return new SetupCode( device.getSetupCode() );
      case 3:
        return device.getButtonRestriction().getName();
      case 4:
        return device.getButtonIndependent();
      case 5:
        return device.getDescription();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    DeviceUpgrade device = getRow( row );
    switch ( getEffectiveColumn( col ) )
    {
      case 3:
        device.setButtonRestriction( ( ( DeviceButton )value ) );
        propertyChangeSupport.firePropertyChange( "device", null, null );
        break;
      case 4:
        device.setButtonIndependent( ( Boolean )value );
        propertyChangeSupport.firePropertyChange( "device", null, null );
        break;
      case 5:
        device.setDescription( ( String )value );
        break;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    return null;
  }

  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    switch ( getEffectiveColumn( col ) )
    {
      case 3:
        DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
        editor.setClickCountToStart( 1 );
        return editor;
      case 5:
        return descriptionEditor;
    }
    return null;
  }

  // private JCheckBox otherAvailabilityBox = new JCheckBox();

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#removeRow(int)
   */
  @Override
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
  @Override
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
  @Override
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

  private SelectAllCellEditor descriptionEditor = new SelectAllCellEditor();

  private DefaultCellEditor deviceButtonEditor = null;
  private JComboBox deviceButtonBox = new JComboBox();
}
