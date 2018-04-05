package com.hifiremote.jp1;

import java.awt.Color;

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
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
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
    int count = colNames.length - 1;
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Name", "PID", "Protocol Code", "Notes", "<html>Size &amp<br>Color</html>"
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
      " 00 ", "Manual Settings: 01CC (2)", "01CC", 
      "00 01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F", 
      "A resonable length note", "Color_"
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
    if ( col == 0 || col == 2 || col == 5 )
    {
      return true;
    }
    return false;
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, String.class, Hex.class, String.class, Color.class
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
    if ( col > 3 )
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
//          short[] pidHex = new short[ 2 ];
//          int pid = pu.getPid();
//          pidHex[ 0 ] = ( short )( pid / 0x100 );
//          pidHex[ 1 ] = ( short )( pid % 0x100 );
//          return "Custom code for PID: " + new Hex( pidHex );
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
      case 5:
        return pu.getHighlight();
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
      propertyChangeSupport.firePropertyChange( "device", null, null );
    }
    else if ( col == 5 )
    {
      pu.setHighlight( ( Color )value );
      propertyChangeSupport.firePropertyChange( "highlight", null, null );
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
    else if ( col == 5 )
    {
      return colorRenderer;
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
    else if ( col == 5 )
    {
      return colorEditor;
    }
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;
  private SelectAllCellEditor noteEditor = new SelectAllCellEditor();
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
}
