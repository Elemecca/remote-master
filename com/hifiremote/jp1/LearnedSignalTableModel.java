package com.hifiremote.jp1;

import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalTableModel.
 */
public class LearnedSignalTableModel extends JP1TableModel< LearnedSignal >
{

  /**
   * Instantiates a new learned signal table model.
   */
  public LearnedSignalTableModel()
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
    deviceComboBox.setModel( new DefaultComboBoxModel( remoteConfig.getRemote().getDeviceButtons() ) );
    keyRenderer.setRemote( remoteConfig.getRemote() );
    keyEditor.setRemote( remoteConfig.getRemote() );
    setData( remoteConfig.getLearnedSignals() );
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "<html>Device<br>Button</html>", "Key", "Notes", "Size", "Freq.", "Protocol", "Device",
      "<html>Sub<br>Device</html>", "OBC", "Hex Cmd", "Misc"
  };

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    return colNames.length;
  }

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
      "00", "__VCR/DVD__", "_xshift-VCR/DVD_", "A longish comment or note", "1024", "99999", "Protocol", "Device",
      "Device", "OBC", "Hex Cmd", "Miscellaneous"
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
    if ( ( col == 3 ) || ( col == 6 ) || ( col == 11 ) )
      return false;
    else
      return true;
  }

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, // row
      DeviceButton.class, // DeviceButton
      Integer.class, // keycode
      String.class, // notes
      Integer.class, // size
      Integer.class, // frequency
      String.class, // protocol
      Integer.class, // device
      Integer.class, // sub-device
      Integer.class, // OBC
      String.class, // hex cmd
      String.class
  // misc
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
    if ( ( col > 0 ) && ( col < 4 ) )
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
    LearnedSignal l = ( LearnedSignal )getRow( row );
    UnpackLearned ul = l.getUnpackLearned();
    ArrayList< LearnedSignalDecode > da = l.getDecodes();
    int numDecodes = 0;
    if ( da != null )
      numDecodes = da.size();
    if ( ( numDecodes != 1 ) && ( column > 6 ) )
      return null;
    LearnedSignalDecode decode = null;
    if ( ( numDecodes == 1 ) && ( column > 5 ) )
      decode = da.get( 0 );
    switch ( column )
    {
      case 0: // row number
        return new Integer( row + 1 );
      case 1: // deviceButton
        return remoteConfig.getRemote().getDeviceButtons()[ l.getDeviceButtonIndex() ];
      case 2: // key
        return new Integer( l.getKeyCode() );
      case 3: // notes
        return l.getNotes();
      case 4: // size
        return l.getData().length();
      case 5: // frequency
        return new Integer( ul.frequency );
      case 6: // protocol
        if ( numDecodes == 0 )
          return "** None **";
        if ( numDecodes > 1 )
          return "** Multiple **";
        return decode.protocolName;
      case 7: // device
        return new Integer( decode.device );
      case 8: // subDevice
        if ( decode.subDevice == -1 )
          return null;
        return new Integer( decode.subDevice );
      case 9: // obc
        return new Integer( decode.obc );
      case 10:
        return Hex.toString( decode.hex );
      case 11:
        return decode.miscMessage;
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
    LearnedSignal l = ( LearnedSignal )getRow( row );
    switch ( col )
    {
      case 1:
      {
        DeviceButton[] deviceButtons = remoteConfig.getRemote().getDeviceButtons();
        for ( short i = 0; i < deviceButtons.length; ++i )
        {
          if ( deviceButtons[ i ] == value )
            l.setDeviceButtonIndex( i );
        }
        break;
      }
      case 2:
        l.setKeyCode( ( ( Integer )value ).shortValue() );
        break;
      case 4:
        l.setNotes( ( String )value );
    }
    propertyChangeSupport.firePropertyChange( "data", null, null );
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
    else if ( col == 2 )
      return keyRenderer;
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    if ( col == 1 )
    {
      DefaultCellEditor e = new DefaultCellEditor( deviceComboBox );
      e.setClickCountToStart( 2 );
      return e;
    }
    else if ( col == 2 )
      return keyEditor;
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The device combo box. */
  private JComboBox deviceComboBox = new JComboBox();

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();
}
