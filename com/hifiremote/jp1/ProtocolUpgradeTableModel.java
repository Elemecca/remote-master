package com.hifiremote.jp1;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolUpgradeTableModel.
 */
public class ProtocolUpgradeTableModel extends JP1TableModel< ProtocolUpgrade >
{

  /**
   * Instantiates a new protocol upgrade table model.
   */
  public ProtocolUpgradeTableModel()
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
      setData( remoteConfig.getProtocolUpgrades() );
    }
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
      "#", "Name", "PID", "Protocol Code", "Notes"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Manual Settings: 01CC (2)", "01CC", "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F", "A resonable length note"
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnPrototypeName(int)
   */
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    if ( col == 0 || col == 2 )
    {
      return true;
    }
    return false;
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, String.class, Hex.class, String.class
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int row, int col )
  {
    if ( col == 4 )
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
    ProtocolUpgrade pu = remoteConfig.getProtocolUpgrades().get( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        ManualProtocol mp = pu.getManualProtocol( remoteConfig.getRemote() );
        if ( mp != null )
        {
          return mp.getName();
        }
        else
        {
          return "<none>";
        }
      case 2:
      {
        int pid = pu.getPid();
        StringBuilder buff = new StringBuilder( 4 );
        buff.append( '0' );
        if ( pid < 0x100 )
        {
          buff.append( '0' );
        }
        buff.append( Integer.toHexString( pid ).toUpperCase() );
        return buff.toString();
      }
      case 3:
        return pu.getCode();
      case 4:
        return pu.getNotes();
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
    ProtocolUpgrade pu = remoteConfig.getProtocolUpgrades().get( row );
    if ( col == 4 )
    {
      pu.setNotes( ( String )value );
    }
  }
  
  @Override
  public void removeRow( int row )
  {
    // Protocols in this table are unused, so delete from ProtocolManager
    ProtocolUpgrade pu = remoteConfig.getProtocolUpgrades().get( row );
    ManualProtocol mp = pu.getManualProtocol( remoteConfig.getRemote() );
    if ( mp != null )
    {
      ProtocolManager.getProtocolManager().remove( mp );
    }
    super.removeRow( row );
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
    if ( col == 4 )
    {
      return noteEditor;
    }
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;
  private SelectAllCellEditor noteEditor = new SelectAllCellEditor();
}
