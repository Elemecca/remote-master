package com.hifiremote.jp1;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.Activity.Assister;
import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

public class ActivityAssistTableModel extends JP1TableModel< Activity.Assister > implements CellEditorModel
{
  
  public void set( Button btn, int type, RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      deviceButtonBox.setModel( new DefaultComboBoxModel( remote.usesEZRC() ? 
          remoteConfig.getDeviceButtonList().toArray( new DeviceButton[0] ) : remote.getDeviceButtons() ) );
      activity = remoteConfig.getActivities().get( btn );
      setData( activity.getAssists().get( type ) );
//      keyRenderer.setRemote( remote );
//      keyEditor.setRemote( remote );
    }
  }
  
  private static final String[] colNames =
  {
      "#", "Device", "Function"
  };
  
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Device Button", "Function Name"
  };
  
  private static final Class< ? >[] colClasses =
  {
      Integer.class, DeviceButton.class, Function.class
  };

  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ col ];
  }
  
  @Override
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  @Override
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  @Override
  public int getColumnCount()
  {
    return 3;
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return col > 0;
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return col == 0;
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
//    else if ( col == 2 )
//    {
//      return keyRenderer;
//    }
    else
      return null;
  }
  
  @Override
  public TableCellEditor getCellEditor( int row, int col )
  {
    if ( col == 1 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
      editor.setClickCountToStart( RMConstants.ClickCountToStart );
      return editor;
    }
    else if ( col == 2 )
    {
      KeySpec ks = getRow( row ).ks;
      DefaultComboBoxModel model = new DefaultComboBoxModel( ks.db.getUpgrade().getGeneralFunctionList().toArray() );
      functionBox.setModel( model );
//      model.setSelectedItem( ks.fn );
      DefaultCellEditor editor = new DefaultCellEditor( functionBox );
      editor.setClickCountToStart( RMConstants.ClickCountToStart );
      return editor;
    }
//    else if ( col == 2 )
//    {
//      return keyEditor;
//    }
    return null;
  }

  @Override
  public Object getValueAt( int row, int column )
  {
    Assister assister = getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return assister.ks.db;
//      case 2:
//        if ( assister.ks.btn == null )
//        {
//          return null;
//        }
//        return new Integer( assister.ks.btn.getKeyCode() );
      case 2:
        return assister.ks.fn;
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    Assister assister = getRow( row );
    Remote remote = remoteConfig.getRemote();
    if ( col == 1 )
    {
      assister.setDevice( ( DeviceButton )value );
      assister.ks.fn = null;
      assister.setButton( null );
    }
    else if ( col == 2 )
    {
      assister.ks.fn = ( Function )value;
    }
//    else if ( col == 2 )
//    {
//      assister.setButton( remote.getButton( ( ( Integer )value ).intValue() ) );
//    }
    assister.set( remote );
    fireTableDataChanged();
    propertyChangeSupport.firePropertyChange( "data", null, null );
  }
  
  private RemoteConfiguration remoteConfig = null;
  private Activity activity = null;
//  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
//  private KeyEditor keyEditor = new KeyEditor();
  private JComboBox deviceButtonBox = new JComboBox();
  private JComboBox functionBox = new JComboBox();

}
