package com.hifiremote.jp1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ActivityFunctionTableModel extends JP1TableModel< Activity > implements ButtonEnabler
{
  public void set( Button btn, RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig; 
    if ( remoteConfig != null )
    {
      colorEditor = new RMColorEditor( remoteConfig.getOwner() );
      Remote remote = remoteConfig.getRemote();
      keyRenderer.setRemote( remote );
      keyEditor.setRemote( remote );
      macroEditor.setTitle( "Power Macro Editor" );
      macroEditor.setButtonEnabler( this );
      macroEditor.setRemoteConfiguration( remoteConfig );
      setData( new Activity[] { remoteConfig.getActivities().get( btn ) } );
      setHelpSetting( "AudioHelp" );
      audioHelpSettingBox.setModel( new DefaultComboBoxModel( helpSetting ) );
      setHelpSetting( "VideoHelp" );
      videoHelpSettingBox.setModel( new DefaultComboBoxModel( helpSetting ) );
    }
  }
  
  private void setHelpSetting( String title )
  {
    Remote remote = remoteConfig.getRemote();
    Setting setting = remote.getSetting( title );
    if ( setting != null )
    {
      Object[] options = setting.getOptions( remote );
      helpSetting = new String[ options.length ];
      for ( int i = 0; i < options.length; i++ )
      {
        helpSetting[ i ] = ( String )options[ i ];
      }
    }
    else
    {
      helpSetting = new String[] { "Off", "On" };
    }
  }
  
  @Override
  public void enableButtons( Button b, MacroDefinitionBox macroBox )
  {
    int limit = 15;
    if ( remoteConfig.getRemote().getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
      limit = 255;
    boolean canAdd = ( b != null ) && macroBox.isMoreRoom( limit );

    macroBox.add.setEnabled( canAdd && b.canAssignToPowerMacro() );
    macroBox.insert.setEnabled( canAdd && b.canAssignToPowerMacro() );
    macroBox.addShift.setEnabled( canAdd && b.canAssignShiftedToPowerMacro() );
    macroBox.insertShift.setEnabled( canAdd && b.canAssignShiftedToPowerMacro() );
    boolean xShiftEnabled = remoteConfig.getRemote().getXShiftEnabled();
    macroBox.addXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToPowerMacro() );
    macroBox.insertXShift.setEnabled( xShiftEnabled && canAdd && b.canAssignXShiftedToPowerMacro() );
  }
  
  @Override
  public boolean isAvailable( Button b )
  {
    return  b.canAssignToPowerMacro() 
    || b.canAssignShiftedToPowerMacro() 
    || b.canAssignXShiftedToPowerMacro();
  }
  
  private int getEffectiveColumn( int col )
  {
    if ( remoteConfig != null )
    {
      // Assume that using EZRC also implies has Master Power Support
      Remote remote = remoteConfig.getRemote();
      if ( !remote.usesEZRC() && col > 0 )
      {
        ++col;  // skip Name
      }
      else if ( remote.usesEZRC() && col > 3 )
      {
        col += 2;  // skip Audio and Video Actions
      }
      if ( !remote.hasMasterPowerSupport() && col > 0 )
      {
        if ( remote.hasActivityControl() && col > 2 )
        {
          ++col;  // skip Power Macro
        }
        else if ( !remote.hasActivityControl() )
        {
          col += 2;  // skip Key, Power Macro
        }
      }
    }
    return col;
  }

  private static final String[] colNames =
  {
      "#", "Name", "Key", "Power Macro", "Audio Action", "Video Action", "Notes", "<html>Size &amp<br>Color</html>"
  };
  
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Activity Name ___", "Key__", "A power macro with a lot of keys_________", "Audio Action__", "Video Action__",
      "A reasonable length note", "Color_"
  };
  
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, Integer.class, Hex.class, String.class, String.class, String.class, Color.class
  };

  @Override
  public Class< ? > getColumnClass( int col )
  {
    return colClasses[ getEffectiveColumn( col ) ];
  }
  
  @Override
  public String getColumnName( int col )
  {
    return colNames[ getEffectiveColumn( col ) ];
  }
  @Override
  public String getColumnPrototypeName( int col )
  {
    if ( remoteConfig.getRemote().usesEZRC() && col == 2 )
    {
      return "Activity 0__";
    }
    return colPrototypeNames[ getEffectiveColumn( col ) ];
  }

  @Override
  public int getColumnCount()
  {
    int count = colNames.length - 4;  // omit Name, Key, Macro, Color
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      if ( remote.hasMasterPowerSupport() )
      {
        count += 2;  // add back Key, Macro
      }
      else if ( remote.hasActivityControl() )
      {
        ++count;  // add back Key
      }
      if ( remoteConfig.allowHighlighting() )
      {
        ++count;  // add back Color;
      }
      if ( remote.usesEZRC() )
      {
        --count;  // add Name but omit Audio Action, Video Action
      }
    }
    return count;
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    return col > 0;
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    col = getEffectiveColumn( col );
    return col != 1 && col != 3 && col != 6;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    DefaultCellEditor editor = null;
    switch ( getEffectiveColumn( col ) )
    {
      case 1:
      case 6:
        return selectAllEditor;
      case 2:
        Remote remote = remoteConfig.getRemote();
        if ( remote.usesEZRC() && remote.getButtonGroups() != null
            && remote.getButtonGroups().keySet().contains( "Activity" ) )
        {
          JComboBox cb = ( JComboBox )comboEditor.getComponent();
          List< Button > freeBtns = new ArrayList< Button >( remote.getButtonGroups().get( "Activity" ) );
          for ( Activity a : remoteConfig.getActivities().values() )
          {
            if ( a != getRow( 0 ) && a.isActive() )
            {
              freeBtns.remove( a.getSelector() );
            }
          }
          cb.setModel( new DefaultComboBoxModel( freeBtns.toArray( new Button[ 0 ] ) ) );
          comboEditor.setClickCountToStart( RMConstants.ClickCountToStart );
          return comboEditor;
        }
        else
        {
          return keyEditor;
        }
      case 3:
        return macroEditor;
      case 4:
        editor = new DefaultCellEditor( audioHelpSettingBox );
        editor.setClickCountToStart( RMConstants.ClickCountToStart );
        return editor;
      case 5:
        editor = new DefaultCellEditor( videoHelpSettingBox );
        editor.setClickCountToStart( RMConstants.ClickCountToStart );
        return editor;
      case 7:
        return colorEditor;
      default:
        return null;
    }
  }
  
  @Override
  public TableCellRenderer getColumnRenderer( int col )
  {
    col = getEffectiveColumn( col );
    if ( col == 0 )
    {
      return new RowNumberRenderer();
    }
    else if ( col == 2 )
    {
      return keyRenderer;
    }
    else if ( col == 3 )
    {
      return new DefaultTableCellRenderer()
      {
        @Override
        protected void setValue( Object value )
        {
          if ( value == null )
            super.setValue( null );
          else
          {
            super.setValue( Macro.getValueString( ( Hex )value , remoteConfig ) );
          }
        }
      };
    }
    else if ( col == 7 )
    {
      return colorRenderer;
    }
    else
      return null;
  }
  
  @Override
  public Object getValueAt( int row, int col)
  {
    Activity activity = getRow( row );
    Macro macro = activity.getMacro();
    switch ( getEffectiveColumn( col ) )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return activity.getName();
      case 2:
        Button selector = activity.getSelector();
        return selector == null ? null : new Integer( activity.getSelector().getKeyCode() );
      case 3:
        return macro == null ? null : macro.getData();
      case 4:
        return audioHelpSettingBox.getModel().getElementAt( activity.getAudioHelp() );
      case 5:
        return videoHelpSettingBox.getModel().getElementAt( activity.getVideoHelp() );
      case 6:
        return activity.getNotes();
      case 7:
        return activity.getHighlight();
      default:
        return null;
    }
  }
  
  @Override
  public void setValueAt( Object value, int row, int col )
  {
    col = getEffectiveColumn( col );
    Activity activity = getRow( row );
    Macro macro = activity.getMacro();
    Remote remote = remoteConfig.getRemote();
    if ( col == 1 )
    {
      String name = ( String )value;
      activity.setName( name );
      panel.setTabTitle( name );
    }
    if ( col == 2 )
    {
      int keyCode;
      Button btn;
      if ( remote.usesEZRC() )
      {
        btn = ( Button )value;
        keyCode = btn.getKeyCode();
      }
      else
      {
        keyCode = ( Integer )value;
        btn = remote.getButton( keyCode );
      }
      activity.setSelector( btn );
      if ( remote.hasMasterPowerSupport() )
      {
        if ( macro == null )
        {
          macro = new Macro( keyCode, new Hex( 0 ), activity.getButton().getKeyCode(), 0, null );
          macro.setSegmentFlags( 0xFF );
          activity.setMacro( macro );
        }
        else
        {
          macro.setKeyCode( keyCode );
        }
      }
      if ( remote.hasActivityControl() )
      {
        int tabIndex = remote.getButtonGroups().get( "Activity" ).indexOf( activity.getButton() );
        for ( ActivityGroup group : activity.getActivityGroups() )
        {
          if ( group.getDevice() == null || group.getDevice() == DeviceButton.noButton )
          {
            group.setDevice( remote.getActivityControl()[ tabIndex ][ group.getIndex()][ 0 ] );
          }
        }
        activityGroupModel.fireTableDataChanged();
      }
    }
    else if ( col == 3 )
    {
      macro.setData( ( Hex )value );
    }
    else if ( col == 4 )
    {
      activity.setAudioHelp( audioHelpSettingBox.getSelectedIndex() );
    }
    else if ( col == 5 )
    {
      activity.setVideoHelp( videoHelpSettingBox.getSelectedIndex() );
    }
    else if ( col == 6 )
    {
      activity.setNotes( ( String )value );
    }
    else if ( col == 7 )
    {
      activity.setHighlight( ( Color )value );
    }
    propertyChangeSupport.firePropertyChange( col == 7 ? "highlight" : "data", null, null );
  }

  public void setActivityGroupModel( ActivityGroupTableModel activityGroupModel )
  {
    this.activityGroupModel = activityGroupModel;
  }

  public void setPanel( ActivityPanel panel )
  {
    this.panel = panel;
  }

  private RemoteConfiguration remoteConfig = null;
  private KeyEditor keyEditor = new KeyEditor();
  private RMColorEditor colorEditor = null;
  private RMSetterEditor< Hex, MacroDefinitionBox > macroEditor = 
    new RMSetterEditor< Hex, MacroDefinitionBox >( MacroDefinitionBox.class );
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
  private DefaultCellEditor comboEditor = new DefaultCellEditor( new JComboBox() );
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer();
  private JComboBox audioHelpSettingBox = new JComboBox();
  private JComboBox videoHelpSettingBox = new JComboBox();
  private String[] helpSetting = null;
  private ActivityGroupTableModel activityGroupModel = null;
  private ActivityPanel panel = null;

}
