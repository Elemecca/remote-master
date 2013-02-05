package com.hifiremote.jp1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class Activity extends Highlight
{
  public static final String[] assistType = { "Picture", "Sound", "Power" };
  
  public static class Assister
  {
    public DeviceButton device = null;
    public Button button = null;
    private String deviceName = null;
    private int buttonCode = 0;
    
    public Assister( DeviceButton device, Button button )
    {
      this.device = device;
      this.button = button;
      deviceName = device.getName();
      buttonCode = button.getKeyCode();
    }
    
    public Assister( String str )
    {
      str = str.trim();
      int pos = str.indexOf( '/' );
      if ( pos != -1 )
      {
        deviceName = str.substring( 1, pos - 1 );  // omit quotes
        buttonCode = Integer.parseInt( str.substring( pos + 1 ) );
      }
    }
    
    @Override
    public String toString()
    {
      return "\"" + device.getName() + "\"/" + button.getKeyCode();
    }
    
    public void set( Remote remote )
    {
      for ( DeviceButton device : remote.getDeviceButtons() )
      {
        if ( device.getName().trim().equalsIgnoreCase( deviceName ) )
        {
          this.device = device;
          break;
        }
      }
      button = remote.getButton( buttonCode );
    }

    public String getDeviceName()
    {
      return deviceName;
    }
    
    public void setDevice( DeviceButton device )
    {
      this.device = device;
      deviceName = device.getName();
    }
    
    public void setButton( Button button )
    {
      this.button = button;
      buttonCode = button.getKeyCode();
    }
  }
  
  public Activity( Button button, Remote remote )
  {
    this.button = button;
    name = button.getName();
    setSegmentFlags( 0xFF );
    activityGroups = new ActivityGroup[ remote.getActivityButtonGroups().length ];
    for ( int i = 0; i < activityGroups.length; i++ )
    {
      activityGroups[ i ] = new ActivityGroup( i, remote );
    }
    if ( remote.usesEZRC() )
    {
      int keyCode = button.getKeyCode();
      macro = new Macro( keyCode, new Hex( 0 ), keyCode, 0, null );
      macro.setSegmentFlags( 0xFF );
      for ( int i = 0; i < 3; i++ )
      {
        assists.put( i, new ArrayList< Assister >() );
      }
    }
  }

  public Activity( Properties props )
  {
    super( props );
    active = true;
    name = props.getProperty( "Name" );
    String temp = props.getProperty( "HelpSegmentFlags" );
    if ( temp != null )
    {
      helpSegmentFlags = Integer.parseInt( temp );
    }
    
    temp = props.getProperty( "HelpSettings" );
    if ( temp != null )
    {
      Hex hex = new Hex( props.getProperty( "HelpSettings" ) );
      audioHelp = hex.getData()[ 0 ];
      videoHelp = hex.getData()[ 1 ];
    }
    
    for ( int i = 0; i < 3; i++ )
    {
      temp = props.getProperty( "Assist." + assistType[ i ] );
      if ( temp != null )
      {
        temp = temp.trim();
        List< Assister > aList = new ArrayList< Assister >();
        StringTokenizer st = new StringTokenizer( temp, "," );
        while ( st.hasMoreTokens() )
        {
          aList.add( new Assister( st.nextToken() ) );
        }
        assists.put( i, aList );
      }
      else
      {
        assists.put(  i, new ArrayList< Assister >() );
      }
    }

    notes = props.getProperty( "Notes" );
    selectorName = props.getProperty( "Selector" );
    
    ActivityGroup.parse( props, this );
//    int groupSegmentFlags = 0;
//    temp = props.getProperty( "GroupSegmentFlags" );
//    if ( temp != null )
//    {
//      groupSegmentFlags = Integer.parseInt( temp );
//    }
//    
//    temp = props.getProperty( "GroupSettings" );
//    if ( temp != null )
//    {
//      Hex hex = new Hex( temp );
//      activityGroups = new ActivityGroup[ hex.length() ];
//      for ( int index = 0; index < hex.length(); index++ )
//      {
//        activityGroups[ index ] = new ActivityGroup( index, hex.getData()[ index ] );
//        activityGroups[ index ].setNotes( props.getProperty( "GroupNotes" + index ) );
//        activityGroups[ index ].setSegmentFlags( groupSegmentFlags );
//      }
//    }
  }
  
  public void set( Remote remote )
  {   
    if ( selectorName != null )
    {
      selector = remote.getButton( selectorName );
    }
    button = remote.usesEZRC() && selector != null ? selector : remote.getButton( name );
    
    if ( activityGroups != null )
    {
      for ( ActivityGroup group : activityGroups )
      {
        group.set( remote );
      }
    }
    if ( remote.usesEZRC() )
    {
      int keyCode = button.getKeyCode();
      macro = new Macro( keyCode, new Hex( 0 ), keyCode, 0, null );
      macro.setSegmentFlags( 0xFF );
      for ( int i = 0; i < 3; i++ )
      {
        List< Assister > a = assists.get( i );
        for ( Assister assist : a )
        {
          assist.set( remote );
        }
      }
    }
    else
    {
      assists.clear();
    }
  }

  public ActivityGroup[] getActivityGroups()
  {
    return activityGroups;
  }

  public void setActivityGroups( ActivityGroup[] activityGroups )
  {
    this.activityGroups = activityGroups;
  }

  public Button getButton()
  {
    return button;
  }
  
  public void setButton( Button button )
  {
    this.button = button;
  }

  public Macro getMacro()
  {
    return macro;
  }

  public void setMacro( Macro macro )
  {
    this.macro = macro;
  }

  public int getAudioHelp()
  {
    return audioHelp;
  }

  public void setAudioHelp( int audioHelp )
  {
    this.audioHelp = audioHelp;
  }

  public int getVideoHelp()
  {
    return videoHelp;
  }

  public void setVideoHelp( int videoHelp )
  {
    this.videoHelp = videoHelp;
  }
  
  @Override
  public void setHighlight( Color color )
  {
    super.setHighlight( color );
    if ( macro != null )
    {
      macro.setHighlight( color );
    }
  }

  public Segment getHelpSegment()
  {
    return helpSegment;
  }

  public void setHelpSegment( Segment helpSegment )
  {
    this.helpSegment = helpSegment;
  }

  public int getHelpSegmentFlags()
  {
    return helpSegmentFlags;
  }

  public void setHelpSegmentFlags( int helpSegmentFlags )
  {
    this.helpSegmentFlags = helpSegmentFlags;
  }

  public String getNotes()
  {
    return notes;
  }

  public void setNotes( String notes )
  {
    this.notes = notes;
  }
  
  public void setName( String name )
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public Button getSelector()
  {
    return selector;
  }

  public void setSelector( Button selector )
  {
    this.selector = selector;
    selectorName = ( selector == null ) ? null : selector.getName();
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive( boolean active )
  {
    this.active = active;
  }

  public boolean isNew()
  {
    return isNew;
  }

  public void setNew( boolean isNew )
  {
    this.isNew = isNew;
  }

  public void store( PropertyWriter pw )
  {
    if ( !active )
    {
      return;
    }
    super.store( pw );
    pw.print( "Name", name );
    pw.print( "HelpSegmentFlags", helpSegmentFlags );
    
    if ( helpSegment != null && assists.isEmpty() )
    {
      Hex hex = new Hex( 2 );
      hex.set( ( short )audioHelp, 0 );
      hex.set( ( short )videoHelp, 1 );
      pw.print( "HelpSettings", hex.toString() );
    }
    
    for ( int i = 0; i < assists.size(); i++ )
    {
      List< Assister > a = assists.get( i );
      if ( a.size() > 0 )
      {
        String aStr = "";
        for ( int j = 0; j < a.size(); j++ )
        {
          if ( j > 0 )
          {
            aStr += ", ";
          }
          aStr += a.get( j );
        }
        pw.print( "Assist." + assistType[ i ], aStr );
      }
    }
    
    if ( notes != null && !notes.trim().isEmpty() )
    {
      pw.print( "Notes", notes );
    }
    if ( selector != null )
    {
      pw.print(  "Selector", selector.getName() );
    }
    
    ActivityGroup.store( pw, activityGroups );
  }
  
  public LinkedHashMap< Integer, List< Assister > >  getAssists()
  {
    return assists;
  }
  
  public int getIconRef()
  {
    return iconRef;
  }

  public void setIconRef( int iconRef )
  {
    this.iconRef = iconRef;
  }

  public static Comparator< Activity > activitySort = new Comparator< Activity >()
  {
    @Override
    public int compare( Activity a1, Activity a2 )
    {
      Button b1 = a1.getSelector();
      Button b2 = a2.getSelector();
      if ( b1 == null || b2 == null )
      {
        return 0;
      }
      return ( ( Short )b1.getKeyCode() ).compareTo( ( Short )b2.getKeyCode() );
    }    
  };

  private ActivityGroup[] activityGroups = null;
  private Button button = null;
  private Button selector = null;
  private String name = null;
  private String selectorName = null;
  private Macro macro = null;
  private String notes = null;
  private int audioHelp = 0;
  private int videoHelp = 0;
  private LinkedHashMap< Integer, List< Assister > > assists = new LinkedHashMap< Integer, List<Assister> >();
  private int helpSegmentFlags = 0xFF;
  private Segment helpSegment = null;
  private boolean active = false;
  private boolean isNew = false;
  private int iconRef = 0;
}
