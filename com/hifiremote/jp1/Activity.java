package com.hifiremote.jp1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

public class Activity extends Highlight
{
  public static final String[] assistType = { "Picture", "Sound", "Power" };
  
  public static class Assister
  {
    KeySpec ks = null;
    private String deviceName = null;
    private int buttonCode = -1;
    
    public static void store( LinkedHashMap< Integer, List<Assister> > assists, PropertyWriter pw )
    {
      if ( assists == null )
      {
        return;
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
    }
    
    public static LinkedHashMap< Integer, List< Assister > > load( Properties props )
    {
      LinkedHashMap< Integer, List< Assister > > assists = null;
      for ( int i = 0; i < 3; i++ )
      {
        String temp = props.getProperty( "Assist." + assistType[ i ] );
        if ( temp != null )
        {
          if ( assists == null || assists.isEmpty() )
          {
            assists = new LinkedHashMap< Integer, List<Assister> >();
            for ( int j = 0; j < 3; j++ )
            {
              assists.put( j , new ArrayList< Assister >() );
            }
          }
          temp = temp.trim();
          List< Assister > aList = assists.get( i );
          StringTokenizer st = new StringTokenizer( temp, "," );
          while ( st.hasMoreTokens() )
          {
            aList.add( new Assister( st.nextToken() ) );
          }
        }
      }
      return assists;
    }
    
    public static void setFunctions( LinkedHashMap< Integer, List< Assister > > assists, Remote remote )
    {
      if ( assists == null )
      {
        return;
      }
      for ( int i = 0; i < assists.size(); i++ )
      {
        List< Assister > aList = assists.get( i );
        for ( Assister a : aList )
        {
          a.set( remote );
        }
      }
    }
    
    public Assister( DeviceButton device, Button button )
    {
      ks = new KeySpec( device, button );
      deviceName = device.getName();
      buttonCode = button.getKeyCode();
      ks.fn = device.getUpgrade().getAssignments().getAssignment( button );
    }
    
    public Assister( DeviceButton device, GeneralFunction function )
    {
      ks = new KeySpec( device, function );
      deviceName = device.getName();
      if ( !function.getUsers().isEmpty() )
      {
        ks.btn = function.getUsers().get( 0 ).button;
        buttonCode = ks.btn.getKeyCode();
      }
    }
    
    public Assister( String str )
    {
      ks = new KeySpec();
      str = str.trim();
      int pos = str.indexOf( '/' );
      if ( pos != -1 )
      {
        deviceName = str.substring( 1, pos - 1 );  // omit quotes
        if ( str.charAt( pos + 1 ) == '/' )
        {
          ks.irSerial = Integer.parseInt( str.substring( pos + 2 ) );
        }
        else
        {
          buttonCode = Integer.parseInt( str.substring( pos + 1 ) );
        }
      }
    }
    
    @Override
    public String toString()
    {
      Button b = ks.getButton();
      if ( b != null )
      {
        return "\"" + ks.db.getName() + "\"/" + b.getKeyCode();
      }
      else
      {
        return "\"" + ks.db.getName() + "\"//" + ks.fn.serial;
      }
    }
    
    public void set( Remote remote )
    {
      for ( DeviceButton device : remote.getDeviceButtons() )
      {
        if ( device.getName().trim().equalsIgnoreCase( deviceName ) )
        {
          ks.db = device;
          break;
        }
      }
      if ( buttonCode >= 0 )
      {
        ks.btn = remote.getButton( buttonCode );
        if ( ks.btn != null && ks.fn == null )
        {
          ks.fn = ks.db.getUpgrade().getAssignments().getAssignment( ks.btn );
        }
      }
      if ( ks.irSerial >= 0 )
      {
        ks.fn = ks.db.getUpgrade().getFunctionMap().get( ks.irSerial );
      }
    }

    public String getDeviceName()
    {
      return deviceName;
    }
    
    public void setDevice( DeviceButton device )
    {
      ks.db = device;
      deviceName = device.getName();
    }
    
    public void setButton( Button button )
    {
      ks.btn = button;
      buttonCode = button == null ? -1 : button.getKeyCode();
    }
  }
  
  public Activity( Button button, Remote remote )
  {
    this.button = button;
    name = button.getName();
    setSegmentFlags( 0xFF );
    boolean onlyHardButtons = remote.isSSD() && !remote.getButtonGroups().get( "Activity" ).contains( button );
    List< Integer > indices = new ArrayList< Integer >();
    for ( int i = 0; i < remote.getActivityButtonGroups().length; i++)
    {
      Button[] btns = remote.getActivityButtonGroups()[ i ];
      if ( btns.length > 0 && !( onlyHardButtons && remote.isSoftButton( btns[ 0 ] ) ) )
      {
        indices.add( i );
      }
    }
    activityGroups = new ActivityGroup[ indices.size() ];
    for ( int i = 0; i < activityGroups.length; i++ )
    {
      activityGroups[ i ] = new ActivityGroup( indices.get( i ), remote );
    }
    if ( remote.usesEZRC() )
    {
      int keyCode = button.getKeyCode();
      macro = new Macro( keyCode, null, keyCode, 0, null );
      macro.setActivity( this );
      macro.setItems( new ArrayList< KeySpec >() );
      assists = new LinkedHashMap< Integer, List<Assister> >();
      for ( int i = 0; i < 3; i++ )
      {
        assists.put( i, new ArrayList< Assister >() );
      }
    }
    if ( remote.isSSD() )
    {
      icon = new RMIcon( 5 );
    }
    else if ( macro != null )
    {
      macro.setSegmentFlags( 0xFF );
    }
  }

  public Activity( Properties props )
  {
    super( props );
    active = true;
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
    
    assists = Assister.load( props );

    selectorName = props.getProperty( "Selector" );
    temp = props.getProperty( "ProfileIndex" );
    if ( temp != null )
    {
      profileIndex = Integer.parseInt( temp );
    }
    
    ActivityGroup.parse( props, this );

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
    if ( remote.usesEZRC() && profileIndex < 0 )
    {
      if ( assists == null || assists.isEmpty() )
      {
        assists = new LinkedHashMap< Integer, List<Assister> >();
        for ( int i = 0; i < 3; i++ )
        {
          assists.put( i , new ArrayList< Assister >() );
        }
      }
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
      assists = null;
    }
    if ( remote.isSSD() )
    {
      icon = new RMIcon( profileIndex < 0 ? 5 : 8 );
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

  public void setNotes( String notes )
  {
    this.notes = notes;
  }
  
  public void setName( String name )
  {
    this.name = name;
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
    if ( !active && profileIndex < 0 )
    {
      return;
    }
    super.store( pw );
    pw.print( "HelpSegmentFlags", helpSegmentFlags );
    
    if ( helpSegment != null && assists.isEmpty() )
    {
      Hex hex = new Hex( 2 );
      hex.set( ( short )audioHelp, 0 );
      hex.set( ( short )videoHelp, 1 );
      pw.print( "HelpSettings", hex.toString() );
    }
    
    Assister.store( assists, pw );

    if ( selector != null )
    {
      pw.print(  "Selector", selector.getName() );
    }
    if ( profileIndex >= 0 )
    {
      pw.print(  "ProfileIndex", profileIndex );
    }
    
    ActivityGroup.store( pw, activityGroups );
  }
  
  public LinkedHashMap< Integer, List< Assister > >  getAssists()
  {
    return assists;
  }
  
  public int getProfileIndex()
  {
    return profileIndex;
  }

  public void setProfileIndex( int profileIndex )
  {
    this.profileIndex = profileIndex;
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
  
  @Override
  public String toString()
  {
    return name;
  }

  private ActivityGroup[] activityGroups = null;
  private Button button = null;
  private Button selector = null;
  private String selectorName = null;
  private Macro macro = null;
  private int audioHelp = 0;
  private int videoHelp = 0;
  private LinkedHashMap< Integer, List< Assister > > assists = null; // new LinkedHashMap< Integer, List<Assister> >();
  private int helpSegmentFlags = 0xFF;
  private Segment helpSegment = null;
  private boolean active = false;
  private boolean isNew = false;
  private int profileIndex = -1;
}
