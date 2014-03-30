package com.hifiremote.jp1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.GeneralFunction.User;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroTableModel.
 */
public class MacroTableModel extends JP1TableModel< Macro >
{

  /**
   * Instantiates a new macro table model.
   */
  public MacroTableModel()
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
      Remote remote = remoteConfig.getRemote();
      deviceButtonBox.setModel( new DefaultComboBoxModel( remote.getDeviceButtons() ) );
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      keyRenderer.setRemote( remote );
      keyEditor.setRemote( remote );
      if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() )
      {
        List< Macro > list = new ArrayList< Macro >();
        for ( Macro macro : remoteConfig.getMacros() )
        {
          if ( macro.accept() )
          {
            list.add( macro );
          }
        }
        setData( list );
      }
      else
      {
        setData( remoteConfig.getMacros() );
      }
    }
  }

  /**
   * Gets the remote config.
   * 
   * @return the remote config
   */
  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount()
  {
    int count = colNames.length - 4;
    if ( remoteConfig != null && remoteConfig.getRemote().usesEZRC() )
    {
      count += 3;
    }
    if ( remoteConfig != null && remoteConfig.allowHighlighting() )
    {
      ++count;
    }
    return count;
  }

  /** The Constant colNames. */
  private static final String[] colNames =
  {
      "#", "Name", "Device", "Key", "Macro Keys", "Serial", "Notes", "<html>Size &amp<br>Color</html>"
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

  /** The Constant colClasses. */
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, DeviceButton.class, Integer.class, String.class, Integer.class, String.class, Color.class
  };
  
  private int getEffectiveColumn( int col )
  {
    if ( remoteConfig == null || !remoteConfig.getRemote().usesEZRC() )
    {
      col += col > 2 ? 3 : col > 0 ? 2 : 0;
    }
    return col;
  }

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

  /** The Constant colPrototypeNames. */
  private static final String[] colPrototypeNames =
  {
      " 00 ", "MacroName_____", "DeviceName", "_xShift-VCR/DVD_", "A reasonable length macro with a reasonable number of steps ",
      "0000_", "A reasonable length note for a macro", "Color_"
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

  /** The Constant colWidths. */
  private static final boolean[] colWidths =
  {
      true, false, false, true, false, true, false, true
  };

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#isColumnWidthFixed(int)
   */
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    return colWidths[ getEffectiveColumn( col ) ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int row, int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 0 || col == 4 )
    {
      return false;
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int row, int column )
  {
    column = getEffectiveColumn( column );
    Macro macro = getRow( row );
    switch ( column )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return macro.getName();
      case 2:
        return macro.getDeviceButton( remoteConfig );
      case 3:
        return new Integer( macro.getKeyCode() );
      case 4:
        return macro.getValueString( remoteConfig );
      case 5:
        return macro.getSerial();
      case 6:
        return macro.getNotes();
      case 7:
        return macro.getHighlight();
      default:
        return null;
    }
  }

//  @Override
//  public void setRow( int row, Macro macro )
//  {
//    super.setRow( row, macro );
//  }
  
  
  
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    col = getEffectiveColumn( col );
    Macro macro = getRow( row );
    Remote remote = remoteConfig.getRemote();
    
    DeviceButton db = null;
    DeviceUpgrade du = null;
    if ( remote.usesEZRC() )
    {
      db = remote.getDeviceButton( macro.getDeviceButtonIndex() );
      du = db.getUpgrade();
    }
    
    if ( col == 1 )
    {
      macro.setName( ( String )value );
    }
    else if ( col == 2 )
    {
      if ( remote.usesEZRC() )
      {
        du.getMacroMap().remove( macro.getKeyCode() );
        Button b = remote.getButton( macro.getKeyCode() );
        macro.removeReference( db, b );
        db = ( DeviceButton )value;
        du = db.getUpgrade();
        du.getMacroMap().put( macro.getKeyCode(), macro );
        macro.addReference( db, b );
      }
      if ( remote.isSSD() )
      {
        Button b = remote.getButton( macro.getKeyCode() );
        du = macro.getUpgrade( remote );
        du.setFunction( b, null, Button.NORMAL_STATE );
        db = ( DeviceButton )value;
        macro.setDeviceButtonIndex( db.getButtonIndex() );
        du = db.getUpgrade();
        du.setFunction( b, macro, Button.NORMAL_STATE );
      }
      else
      {
        macro.setDeviceButtonIndex( ( ( DeviceButton )value ).getButtonIndex() );
      }
    }
    else if ( col == 3 )
    {
      if ( remote.usesEZRC() )
      {
        du.getMacroMap().remove( macro.getKeyCode() );
        Button b = remote.getButton( macro.getKeyCode() );
        macro.removeReference( db, b );
        int keyCode = ( Integer )value;
        du.getMacroMap().put( keyCode, macro );
        b = remote.getButton( keyCode );
        macro.addReference( db, b );
      }        
      if ( remote.isSSD() )
      {
        Button b = remote.getButton( macro.getKeyCode() );
        du = macro.getUpgrade( remote );
        du.setFunction( b, null, Button.NORMAL_STATE );
        int keyCode = ( Integer )value;
        b = remote.getButton( keyCode );
        macro.setKeyCode( keyCode );
        du.setFunction( b, macro, Button.NORMAL_STATE );
      }
      else
      {
        macro.setKeyCode( ( ( Integer )value ).intValue() );
      }
    }
    else if ( col == 5 )
    {
      macro.setSerial( ( ( Integer )value ).intValue() );
    }
    else if ( col == 6 )
    {
      macro.setNotes( ( String )value );
    }
    else if ( col == 7 )
    {
      macro.setHighlight( ( Color  )value );
    }
    propertyChangeSupport.firePropertyChange( col == 4 ? "highlight" : "data", null, null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnRenderer(int)
   */
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 3 )
    {
      return keyRenderer;
    }
    else if ( col == 7 )
    {
      return colorRenderer;
    }
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.JP1TableModel#getColumnEditor(int)
   */
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 1 || col == 6 )
    {
      return selectAllEditor;
    }
    else if ( col == 2 )
    {
      DefaultCellEditor editor = new DefaultCellEditor( deviceButtonBox );
      editor.setClickCountToStart( RMConstants.ClickCountToStart );
      return editor;
    }
    if ( col == 3 )
    {
      return keyEditor;
    }
    else if ( col == 7 )
    {
      return colorEditor;
    }
    return null;
  }
  
  @Override
  public void removeRow( int row )
  {
    Remote remote = null;
    if ( remoteConfig != null && ( remote = remoteConfig.getRemote() ).usesEZRC() )
    {
      Macro macro = getRow( row );
      for ( User user : macro.getUsers() )
      {
        DeviceButton db = user.db;
        DeviceUpgrade upg = db.getUpgrade();
        upg.getMacroMap().remove( user.button.getKeyCode() );
      }
      remoteConfig.getMacros().remove( macro );
    }
    super.removeRow( row );
  }
  
  @Override
  /** ToolTip text to override that provided by JTableX */
  public String getToolTipText( int row, int col )
  {
    if ( remoteConfig == null || !remoteConfig.getRemote().usesEZRC() )
    {
      return null;
    }
    col = getEffectiveColumn( col );
    if ( col == 0 || col > 3 )
    {
      return null;
    }
    Macro macro = getRow( row );
    String str = "<html>Edit the Device Upgrade to put this macro on additional<br>"
        + "devices or buttons.  Currently assigned to:<br><br>";
    str += User.getUserNames( macro.getUsers() ) + "</html>";
    return str;
  }

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;
  private JComboBox deviceButtonBox = new JComboBox();

  /** The key renderer. */
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();

  /** The key editor. */
  private KeyEditor keyEditor = new KeyEditor();
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
  private RMColorEditor colorEditor = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
}
