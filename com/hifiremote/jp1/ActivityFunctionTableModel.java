package com.hifiremote.jp1;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.hifiremote.jp1.GeneralFunction.RMIcon;
import com.hifiremote.jp1.GeneralFunction.IconPanel;
import com.hifiremote.jp1.GeneralFunction.IconRenderer;
import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

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
      if ( remote.isSSD() )
      {
        iconEditor = new RMSetterEditor< RMIcon, IconPanel >( IconPanel.class );
        iconEditor.setRemoteConfiguration( remoteConfig );
        iconRenderer = new IconRenderer();
      }
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
    if ( remoteConfig.getRemote().usesEZRC() )
    {
      macroBox.add.setEnabled( true );
      macroBox.insert.setEnabled( true );
      macroBox.addShift.setVisible( false );
      macroBox.addXShift.setVisible( false );
      macroBox.insertShift.setVisible( false );
      macroBox.insertXShift.setVisible( false );
      return;
    }
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
      boolean needsDevCheckBoxes = remote.getSegmentTypes() != null && remote.getSegmentTypes().contains( 4 );
      int temp = 0;
      if ( !remote.usesEZRC() && col > 0 )
      {
        ++col;       // skip Name
      }
      if ( ( remote.usesEZRC() || remote.usesSimpleset() || !remote.hasActivityControl() && !remote.hasMasterPowerSupport() ) && col > 1 )
      {
        col++;       // skip key
      }
      if ( ( !remote.hasMasterPowerSupport() || needsDevCheckBoxes ) && col > 2 )
      {
        col++;  // skip Power Macro
      }
      if ( needsDevCheckBoxes )
      {
        if ( col > 3 + remote.getDeviceButtons().length )
        {
          col -= remote.getDeviceButtons().length;
        }
        else if ( col > 3 )
        {
          temp = col;
        }
      }
      if ( remote.getSetting( "AudioHelp" ) == null && col > 3 )
      {
        col++;  // skip Audio Action
      }
      if ( remote.getSetting( "VideoHelp" ) == null && col > 4 )
      {
        col++;  // skip Video Action
      }
      if ( !remote.isSSD() && col > 5 )
      {
        col++;  // skip Icon
      }
      if ( temp > 0 )
      {
        col = colNames.length + temp - 5;
      }
    }
    return col;
  }

  private static final String[] colNames =
  {
      "#", "Name", "Key", "Power Macro", "Audio Action", "Video Action", "Icon?", "Notes", "<html>Size &amp<br>Color</html>", "Power Dev?"
  };
  
  private static final String[] colPrototypeNames =
  {
      " 00 ", "Activity Name ___", "Key__", "A power macro with a lot of keys_________", "Audio Action__", "Video Action__",
      "Icon?_", "A reasonable length note", "Color_", "Power XXXXXXX"
  };
  
  private static final Class< ? >[] colClasses =
  {
      Integer.class, String.class, Integer.class, List.class, String.class, String.class, RMIcon.class, String.class, Color.class, Boolean.class
  };

  @Override
  public Class< ? > getColumnClass( int col )
  {
    col = getEffectiveColumn( col );
    if ( col > colClasses.length - 1 )
    {
      col = colClasses.length -1;
    }
    return colClasses[ col ];
  }
  
  @Override
  public String getColumnName( int col )
  {
    col = getEffectiveColumn( col );
    if ( remoteConfig != null && col > colClasses.length - 2 )
    {
      Remote remote = remoteConfig.getRemote();
      return "Power " + remote.getDeviceButtons()[ col - colClasses.length + 1 ].getName() + "?";
    }
    return colNames[ col ];
  }
  
  @Override
  public String getColumnPrototypeName( int col )
  {
    if ( remoteConfig.getRemote().usesEZRC() && col == 2 )
    {
      return "Activity 0__";
    }
    col = getEffectiveColumn( col );
    if ( col > colPrototypeNames.length - 1 )
    {
      col = colPrototypeNames.length - 1;
    }
    return colPrototypeNames[ col ];
  }

  @Override
  public int getColumnCount()
  {
    int count = 2;   // always include #, Notes
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      if ( remote.usesEZRC() )
      {
        count++;   // add Name
      }
      if ( !remote.usesEZRC() && !remote.usesSimpleset() && ( remote.hasActivityControl() || remote.hasMasterPowerSupport() ) )
      {
        count++;   // add Key
      }
      if ( remote.hasMasterPowerSupport() )
      {
        if ( remote.getSegmentTypes() != null && remote.getSegmentTypes().contains( 0x04 ) )
        {
          count += remote.getDeviceButtons().length;  // add one checkbox per device
        }
        else
        {
          count++;   // add Macro;
        }
      }
      if ( remote.getSetting( "AudioHelp" ) != null )
      {
        count++;   // add AudioHelp;
      }
      if ( remote.getSetting( "VideoHelp" ) != null )
      {
        count++;   // add VideoHelp;
      }
      if ( remote.isSSD() )
      {
        ++count;  // add back Icon
      }
      if ( remoteConfig.allowHighlighting() )
      {
        ++count;  // add back Color
      }
    }
    return count;
  }
  
  @Override
  public boolean isCellEditable( int row, int col )
  {
    Remote remote = remoteConfig.getRemote();
    if ( remote.hasActivityControl() && getEffectiveColumn( col ) == 2 )
    {
      // The only remote with activity control is the URC-7962 in which the activity setup
      // data in the OxDB segment is ignored by the remote, so make activities non-editable
      return false;
    }
    return col > 0;
  }
  
  @Override
  public boolean isColumnWidthFixed( int col )
  {
    col = getEffectiveColumn( col );
    return col != 1 && col != 3 && col != 7;
  }
  
  @Override
  public TableCellEditor getColumnEditor( int col )
  {
    DefaultCellEditor editor = null;
    switch ( getEffectiveColumn( col ) )
    {
      case 1:
      case 7:
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
      case 6:
        return iconEditor;
      case 8:
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
        @SuppressWarnings( "unchecked" )
        @Override
        protected void setValue( Object value )
        {
          if ( value == null )
            super.setValue( null );
          else if ( value instanceof Hex )
          {
            super.setValue( Macro.getValueString( ( Hex )value , remoteConfig ) );
          }
          else
          {
//            super.setValue( Macro.getValueString( ( Hex )value , remoteConfig ) );
            super.setValue( Macro.getValueString( ( List< KeySpec > )value ) );
          }
        }
      };
    }
    else if ( col == 6 )
    {
      return iconRenderer;
    }
    else if ( col == 8 )
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
    Remote remote = remoteConfig.getRemote();
    col = getEffectiveColumn( col );
    switch ( col )
    {
      case 0:
        return new Integer( row + 1 );
      case 1:
        return activity.getName();
      case 2:
        Button selector = activity.getSelector();
        return selector == null ? null : new Integer( activity.getSelector().getKeyCode() );
      case 3:
//        return macro == null ? remoteConfig.getRemote().usesEZRC() ?
//            new ArrayList< KeySpec >() : new Hex( 0 ) : macro.getItems();
        return macro == null ? null : macro.getValue();//macro.getItems();
      case 4:
        return audioHelpSettingBox.getModel().getElementAt( activity.getAudioHelp() );
      case 5:
        return videoHelpSettingBox.getModel().getElementAt( activity.getVideoHelp() );
      case 6:
        return activity.icon;
      case 7:
        return activity.getNotes();
      case 8:
        return activity.getHighlight();
      default:
        short[] data = macro.getData().getData();
        DeviceButton db = remote.getDeviceButtons()[ col - colNames.length + 1 ];
        int len = data.length;
        List< DeviceButton > devs = new ArrayList< DeviceButton >();
        for ( int i = 0; i < len / 2; i++ )
        {
          devs.add( remote.getDeviceButton( data[ 2 * i ] ) );
        }
        return devs.contains( db );
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
      if ( activity.getMacro() != null )
      {
        activity.getMacro().setName( name );
      }
      panel.setTabTitle( name, activity );
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
          macro.setActivity( activity );
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
//      if ( Macro.isEmpty( value ) )
//      {
//        macro = null;
//      }
//      else
//      {
        macro.setValue( value );
//      }
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
      activity.icon = ( RMIcon )value;
    }
    else if ( col == 7 )
    {
      activity.setNotes( ( String )value );
    }
    else if ( col == 8 )
    {
      activity.setHighlight( ( Color )value );
    }
    else if ( col > colNames.length - 2 )
    {
      short[] data = macro.getData().getData();
      
      int len = data.length;
      List< DeviceButton > devs = new ArrayList< DeviceButton >();
      DeviceButton db = null;
      for ( int i = 0; i < len / 2; i++ )
      {
        db = remote.getDeviceButton( data[ 2 * i ] );
        if ( db != null )
        {
          devs.add( db );
        }
      }
      db = remote.getDeviceButtons()[ col - colNames.length + 1 ];
      if ( ( Boolean )value && !devs.contains( db ) )
      {
        devs.add( db );
      }
      else if ( !( Boolean )value && devs.contains( db ) )
      {
        devs.remove( db );
      }
      Hex hex = new Hex( 2 * devs.size() );
      int n = 0;
      for ( int i = 0; i < remote.getDeviceButtons().length; i++ )
      {
        db = remote.getDeviceButtons()[ i ];
        if ( devs.contains( db ) )
        {
          hex.set( ( short )db.getButtonIndex(), n++ );
          hex.set( remote.getButtonByStandardName( "Power" ).getKeyCode(), n++ );
        }
      }
      macro.setData( hex );
    }
    fireTableRowsUpdated( row, row );
    propertyChangeSupport.firePropertyChange(  col == 8 ? "highlight" : "data", null, null );
  }

  public void setActivityGroupModel( ActivityGroupTableModel activityGroupModel )
  {
    this.activityGroupModel = activityGroupModel;
  }

  public void setPanel( ActivityPanel panel )
  {
    this.panel = panel;
  }
  
  @Override
  public String getToolTipText( int row, int col )
  {
    col = getEffectiveColumn( col );
    // For reasons unknown, the tooltip repeats if not checked in this way.
    // It seems that it may be something to do with the cell being a check box.
    int thisCell = row + col * 0x100;
    if ( thisCell == lastCell )
    {
      return null;
    }
    lastCell = thisCell;
    if ( col == 6 )
    {
      return "<html>Double click this column to open Icon Editor to set or<br>"
          + "remove a system icon from this activity.</html>";
    }
    return null;
  }
  
  private int lastCell = 0;
  
  private KeyCodeRenderer keyRenderer = new KeyCodeRenderer(){
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int col )
    {
      Component c = super.getTableCellRendererComponent( table, value, isSelected, false, row, col );
      Color bgColor = isCellEditable( row, col ) ? Color.BLACK : Color.GRAY;
      c.setForeground( bgColor );
      return c;
    }
  };

  private RemoteConfiguration remoteConfig = null;
  private KeyEditor keyEditor = new KeyEditor();
  private RMColorEditor colorEditor = null;
  private RMSetterEditor< Object, MacroDefinitionBox > macroEditor = 
    new RMSetterEditor< Object, MacroDefinitionBox >( MacroDefinitionBox.class );
  private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();
  private DefaultCellEditor comboEditor = new DefaultCellEditor( new JComboBox() );
  private RMSetterEditor< RMIcon, IconPanel > iconEditor = null;
  private IconRenderer iconRenderer = null;
  private RMColorRenderer colorRenderer = new RMColorRenderer();
  private JComboBox audioHelpSettingBox = new JComboBox();
  private JComboBox videoHelpSettingBox = new JComboBox();
  private String[] helpSetting = null;
  private ActivityGroupTableModel activityGroupModel = null;
  private ActivityPanel panel = null;

}
