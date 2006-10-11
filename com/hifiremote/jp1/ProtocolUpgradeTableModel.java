package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class ProtocolUpgradeTableModel
  extends JP1TableModel< ProtocolUpgrade >
{
  public ProtocolUpgradeTableModel(){}

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    setData( remoteConfig.getProtocolUpgrades());
  }

  public int getColumnCount(){ return colNames.length; }

  private static final String[] colNames = 
  {
    "#", "PID", "Protocol Code", "Notes"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  private static final String[] colPrototypeNames = 
  {
    "00", "01CC", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F", "A resonable length note"
  };
  public String getColumnPrototypeName( int col ){ return colPrototypeNames[ col ]; }

  public boolean isColumnWidthFixed( int col )
  {
    if ( col < 2 )
      return true;
    return false;
  }
  
  private static final Class[] colClasses =
  {
    Integer.class, String.class, Hex.class, String.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    if ( col == 3 )
       return true;

    return false;
  }

  public Object getValueAt( int row, int column )
  {
    ProtocolUpgrade pu = remoteConfig.getProtocolUpgrades().get( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
      {
        int pid = pu.getPid();
        StringBuilder buff = new StringBuilder( 4 );
        buff.append( '0' );
        if ( pid < 0x100 )
          buff.append( '0' );
        buff.append( Integer.toHexString( pid ).toUpperCase());
        return buff.toString();
      }
      case 2:
        return pu.getCode();
      case 3:
        return pu.getNotes();
    }
    return null;
  }
  
  public void setValueAt( Object value, int row, int col )
  {
    ProtocolUpgrade pu = remoteConfig.getProtocolUpgrades().get( row );
    if ( col == 3 )
      pu.setNotes(( String )value );
  }
 
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }
  
  private RemoteConfiguration remoteConfig = null;
}
