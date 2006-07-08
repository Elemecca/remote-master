package com.hifiremote.jp1;

import javax.swing.*;
import javax.swing.table.*;

public class SettingsTableModel
  extends JP1TableModel< Setting >
  implements CellEditorModel
{
  public SettingsTableModel()
  {
    comboEditor.setClickCountToStart( 2 );    
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    setData( remoteConfig.getRemote().getSettings());
  }

  public int getColumnCount(){ return colNames.length; }

  private static final String[] colNames = 
  {
    "#", "Setting", "Value"
  };
  public String getColumnName( int col )
  {
    return colNames[ col ];
  }
  
  private static final String[] colPrototypeNames =
  {
    "00", "A Long Setting Name", "A Longer Setting Value Name"
  };
  public String getColumnPrototypeName( int col )
  {
    return colPrototypeNames[ col ];
  }

  public boolean isColumnWidthFixed( int col )
  {
    if ( col == 0 )
      return true;
    return false;
  }

  private static final Class[] colClasses =
  {
    Integer.class, String.class, Setting.class
  };
  public Class getColumnClass( int col )
  {
    return colClasses[ col ];
  }

  public boolean isCellEditable( int row, int col )
  {
    if ( col > 1 )
      return true;

    return false;
  }

  public Object getValueAt(int row, int column)
  {
    Remote r = remoteConfig.getRemote();
    short[] data = remoteConfig.getData();
    Setting setting = r.getSettings()[ row ];
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return setting.getTitle();
      case 2:
      {
        int val = setting.getValue( data );
        Object[] choices = setting.getOptions( r );
        if ( choices == null )
          return new Integer( val );
        else
          return choices[ val ];
      }
    }
    return null;
  }

  public void setValueAt( Object value, int row, int col )
  {
    if ( col == 2 )
    {
      Remote r = remoteConfig.getRemote();
      Setting setting = r.getSettings()[ row ];
      short[] data = remoteConfig.getData();
      Object[] choices = setting.getOptions( r );
      if ( choices == null )
        setting.setValue( data, (( Integer )value ).intValue());
      else
        for ( int i = 0; i < choices.length; ++i )
          if ( choices[ i ].equals( value ))
            setting.setValue( data, i );
    }
  }
  
  public TableCellRenderer getColumnRenderer( int col )
  {
    if ( col == 0 )
      return new RowNumberRenderer();
    return null;
  }

  public TableCellEditor getCellEditor( int row, int col )
  {
    if ( col != 2 )
      return null;
    Remote r = remoteConfig.getRemote();
    Setting setting = remoteConfig.getRemote().getSettings()[ row ];
    Object[] options = setting.getOptions( r );
    if ( options == null )
    {
      int bits = setting.getNumberOfBits();
      intEditor.setMin( 0 );
      intEditor.setMax(( 1 << bits ) - 1 );
      return intEditor;
    }

    JComboBox cb = ( JComboBox )comboEditor.getComponent();
    cb.setModel( new DefaultComboBoxModel( options ));
    return comboEditor;
  }
  
  private RemoteConfiguration remoteConfig = null;
  private BoundedIntegerEditor intEditor = new BoundedIntegerEditor();
  private DefaultCellEditor comboEditor = new DefaultCellEditor( new JComboBox());
}
