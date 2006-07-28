package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class DeviceButtonTableModel
  extends JP1TableModel< DeviceButton >
{
  public DeviceButtonTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    deviceTypeBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceTypes()));
    setData( remoteConfig.getRemote().getDeviceButtons());
    fireTableDataChanged();
  }
  
  public void setEditable( boolean flag )
  {
    editable = false;
  }

  public int getColumnCount(){ return 4; }

  private static final String[] colNames = 
  {
    "#", "Device Button", "Type", "<html>Setup<br>Code</html>"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  private static String[] colPrototypeNames = 
  {  
    "00", "Device Button", "__VCR/DVD__", "Setup"
  };
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }
  
  public boolean isColumnWidthFixed( int col )
  {
    return true;
  }
  
  private static final Class[] colClasses =
  {
    Integer.class, String.class, DeviceType.class, SetupCode.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    if ( editable && ( col > 1 ))
      return true;

    return false;
  }

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

  public void setValueAt( Object value, int row, int col )
  {
    short[] data = remoteConfig.getData();
    DeviceButton db = ( DeviceButton )getRow( row );
    int highOffset = db.getHighAddress();
    int lowOffset = db.getLowAddress();
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
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }
  
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

  private RemoteConfiguration remoteConfig = null;
  private JComboBox deviceTypeBox = new JComboBox();
  private boolean editable = true;
}
