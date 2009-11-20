package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceButtonTableModel.
 */
public class DeviceButtonTableModel extends JP1TableModel< DeviceButton >
{

  /**
   * Instantiates a new device button table model.
   */
  public DeviceButtonTableModel()
  {
    deviceTypeEditor = new DefaultCellEditor( deviceTypeBox );
    deviceTypeEditor.setClickCountToStart( 2 );
    sequenceEditor = new DefaultCellEditor( sequenceBox );
    sequenceEditor.setClickCountToStart( 2 );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    Remote remote = remoteConfig.getRemote();
    setData( remote.getDeviceButtons() );
    DefaultComboBoxModel comboModel = new DefaultComboBoxModel( remote.getDeviceTypes() );
    comboModel.addElement( new DeviceType( "", 0, 15 ) );
    deviceTypeBox.setModel( comboModel );
    SoftDevices softDevices = remote.getSoftDevices();
    if ( ( softDevices != null ) && softDevices.usesSequence() )
    {
      adjustSequenceRange( remote.getDeviceButtons() );
    }
  }

  private void adjustSequenceRange( DeviceButton[] deviceButtons )
  {
    int len = 0;
    for ( int i = 0; i < deviceButtons.length; i++ )
    {
      if ( deviceButtons[ i ].getDeviceTypeIndex( remoteConfig.getData() ) != 15 )
      {
        len++ ;
      }
    }
    Integer[] values = new Integer[ len ];
    for ( int i = 0; i < len; i++ )
    {
      values[ i ] = i + 1;
    }
    sequenceBox.setModel( new DefaultComboBoxModel( values ) );
  }

  /**
   * Sets the editable.
   * 
   * @param flag
   *          the new editable
   */
  public void setEditable( boolean flag )
  {
    editable = flag;
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
      if ( remote.getDeviceLabels() != null )
      {
        ++count;
      }
      SoftDevices softDevices = remote.getSoftDevices();
      if ( ( softDevices != null ) && softDevices.usesSequence() )
      {
        ++count;
      }
    }
    return count;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Device Button", "Type", "<html>Setup<br>Code</html>", "Label", "Seq"
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

  /** The col prototype names. */
  private static String[] colPrototypeNames =
  {
      " 00 ", "Device Button", "__VCR/DVD__", "Setup", "Label", "Seq"
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

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, DeviceType.class, SetupCode.class, String.class, Integer.class
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
    if ( editable && ( col > 1 ) )
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
    short[] data = remoteConfig.getData();
    Remote remote = remoteConfig.getRemote();
    DeviceButton db = ( DeviceButton )getRow( row );
    int typeIndex = db.getDeviceTypeIndex( data );
    if ( ( typeIndex == 15 ) && ( column > 1 ) )
    {
      return null;
    }
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return db.getName();
      case 2:
      {
        return remote.getDeviceTypeByIndex( typeIndex );
      }
      case 3:
      {
        return new SetupCode( db.getSetupCode( data ) );
      }
      case 4:
      {
        DeviceLabels labels = remote.getDeviceLabels();
        return labels.getText( data, row );
      }
      case 5:
      {
        SoftDevices softDevices = remote.getSoftDevices();
        int seq = softDevices.getSequence( row, data );
        if ( seq == -1 )
        {
          return null;
        }
        else
        {
          return seq + 1;
        }
      }
      default:
        return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  public void setValueAt( Object value, int row, int col )
  {
    short[] data = remoteConfig.getData();
    Remote remote = remoteConfig.getRemote();
    DeviceButton db = ( DeviceButton )getRow( row );
    SoftDevices softDevices = remote.getSoftDevices();
    if ( col == 2 )
    {
      int oldIndex = db.getDeviceTypeIndex( data );
      int newIndex = ( ( DeviceType )value ).getNumber();
      if ( oldIndex == newIndex )
        return;
      db.setDeviceTypeIndex( ( short )newIndex, data );
      DeviceLabels labels = remote.getDeviceLabels();
      if ( labels != null )
      {
        labels.setText( remote.getDeviceTypeByIndex( newIndex ).getName(), row, data );
      }
      if ( ( softDevices != null ) && softDevices.usesSequence() )
      {
        adjustSequenceRange( remote.getDeviceButtons() );
        if ( oldIndex == 15 )
        {
          softDevices.setSequence( sequenceBox.getItemCount() - 1, row, data );
        }
        else if ( newIndex == 15 )
        {
          softDevices.setSequence( -1, row, data );
        }
      }
      if ( oldIndex == 15 )
      {

      }
    }
    else if ( col == 3 )
    {
      db.setSetupCode( ( short )( ( SetupCode )value ).getValue(), data );
    }
    else if ( col == 4 )
    {
      remote.getDeviceLabels().setText( ( String )value, row, data );
    }
    else if ( col == 5 )
    {
      int rows = getRowCount();
      int newSeq = ( ( Integer )value ).intValue() - 1;
      int oldSeq = softDevices.getSequence( row, data );

      if ( newSeq == oldSeq )
      {
        return;
      }

      int first = 0;
      int last = 0;
      int adjust = 0;

      if ( newSeq < oldSeq )
      {
        adjust = 1;
        first = newSeq;
        last = oldSeq - 1;
      }
      else
      // old < new
      {
        adjust = -1;
        first = oldSeq + 1;
        last = newSeq;
      }
      for ( int index = 0; index < rows; ++index )
      {
        int seq = softDevices.getSequence( index, data );
        if ( ( seq >= first ) && ( seq <= last ) )
        {
          softDevices.setSequence( seq + adjust, index, data );
        }
      }
      softDevices.setSequence( newSeq, row, data );
      fireTableDataChanged();
    }
    propertyChangeSupport.firePropertyChange( "value", null, null );
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
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  public TableCellEditor getColumnEditor( int col )
  {
    if ( !editable )
      return null;

    if ( col == 2 )
    {
      return deviceTypeEditor;
    }
    else if ( col == 5 )
    {
      return sequenceEditor;
    }
    return null;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  /** The device type box. */
  private DefaultCellEditor deviceTypeEditor = null;
  private JComboBox deviceTypeBox = new JComboBox();

  private DefaultCellEditor sequenceEditor = null;
  private JComboBox sequenceBox = new JComboBox();

  /** The editable. */
  private boolean editable = true;
}
