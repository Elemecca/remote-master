package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceButtonTableModel.
 */
public class DeviceButtonTableModel
  extends JP1TableModel< DeviceButton >
{
  
  /**
   * Instantiates a new device button table model.
   */
  public DeviceButtonTableModel(){}

  /**
   * Sets the.
   * 
   * @param remoteConfig the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    deviceTypeBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceTypes()));
    setData( remoteConfig.getRemote().getDeviceButtons());
    fireTableDataChanged();
  }
  
  /**
   * Sets the editable.
   * 
   * @param flag the new editable
   */
  public void setEditable( boolean flag )
  {
    editable = false;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount(){ return 4; }

  /** The Constant colNames. */
  private static final String[] colNames = 
  {
    "#", "Device Button", "Type", "<html>Setup<br>Code</html>"
  };
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /** The col prototype names. */
  private static String[] colPrototypeNames = 
  {  
    "00", "Device Button", "__VCR/DVD__", "Setup"
  };
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  public boolean isColumnWidthFixed( int col )
  {
    return true;
  }
  
  /** The Constant colClasses. */
  private static final Class<?>[] colClasses =
  {
    Integer.class, String.class, DeviceType.class, SetupCode.class
  };
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  public boolean isCellEditable( int row, int col )
  {
    if ( editable && ( col > 1 ))
      return true;

    return false;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(int row, int column)
  {
    short[] data = remoteConfig.getData();
    DeviceButton db = ( DeviceButton )getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return db.getName();
      case 2:
      {
        int type = db.getDeviceTypeIndex( data );
        return remoteConfig.getRemote().getDeviceTypeByIndex( type );
      }
      case 3:
      {
        return new SetupCode( db.getSetupCode( data ));
      }
      default:
        return null;
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    short[] data = remoteConfig.getData();
    DeviceButton db = ( DeviceButton )getRow( row );
    if ( col == 2 )
    {
      db.setDeviceTypeIndex( ( short )(( DeviceType )value ).getNumber(), data ); 
    }
    else if ( col == 3 )
    {
      db.setSetupCode(( short )(( SetupCode )value ).getValue(), data );
    }
    propertyChangeSupport.firePropertyChange( "value", null, null );
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }
  
  /* (non-Javadoc)
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    if ( !editable )
      return null;
    
    if ( col == 2 )
    {
      DefaultCellEditor e = new DefaultCellEditor( deviceTypeBox );
      e.setClickCountToStart( 2 );
      return e;
    }
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;
  
  /** The device type box. */
  private JComboBox deviceTypeBox = new JComboBox();
  
  /** The editable. */
  private boolean editable = true;
}
