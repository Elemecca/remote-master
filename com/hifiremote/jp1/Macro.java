package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.hifiremote.jp1.Activity.Assister;
import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

// TODO: Auto-generated Javadoc
/**
 * The Class Macro.
 */
public class Macro extends AdvancedCode
{
  
  public Macro( Macro macro )
  {
    this( macro.keyCode, macro.data, macro.deviceButtonIndex, macro.sequenceNumber, macro.notes );
    this.setSegmentFlags( macro.getSegmentFlags() );
  }

  /**
   * Instantiates a new macro.
   * 
   * @param keyCode
   *          the key code
   * @param keyCodes
   *          the key codes
   * @param notes
   *          the notes
   */
  public Macro( int keyCode, Hex keyCodes, String notes )
  {
    super( keyCode, keyCodes, notes );
    deviceButtonIndex = 0x0F;
  }

  public Macro( int keyCode, Hex keyCodes, int deviceButtonIndex, int sequenceNumber, String notes )
  {
    super( keyCode, keyCodes, notes );
    this.deviceButtonIndex = deviceButtonIndex;
    this.sequenceNumber = sequenceNumber;
  }
  
  /**
   * Instantiates a new macro.
   * 
   * @param props
   *          the props
   */
  public Macro( Properties props )
  {
    super( props );
    String temp = props.getProperty( "SystemMacro" );
    if ( temp != null )
    {
      systemMacro = true;
    }
    try
    {
      temp = props.getProperty( "SequenceNumber" );
      if ( temp != null )
      {
        sequenceNumber = Integer.parseInt( temp );
      }
      temp = props.getProperty( "DeviceIndex" );
      if ( temp != null )
      {
        deviceButtonIndex = Integer.parseInt( temp );
      }
      else
      {
        deviceButtonIndex = 0x0F;
      }
      temp = props.getProperty( "Users" );
      if ( temp != null )
      {
        userItems = new ArrayList< Integer >();
        StringTokenizer st = new StringTokenizer( temp, ",/" );
        if ( st.hasMoreTokens() )
        {
          int val = Integer.parseInt( st.nextToken() ) << 16;
          val |= Integer.parseInt( st.nextToken() );
          userItems.add( val );
        }
      }
      temp = props.getProperty( "Serial" );
      if ( temp != null )
      {
        serial = Integer.parseInt( temp );
      }
      assists = Assister.load( props );
      if ( assists == null && props.getProperty( "Assistant" ) != null )
      {
        assists = new LinkedHashMap< Integer, List<Assister> >();
        for ( int j = 0; j < 3; j++ )
        {
          assists.put( j , new ArrayList< Assister >() );
        }
      }
    }
    catch ( NumberFormatException nfe )
    {
      nfe.printStackTrace( System.err );
    }
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public Object getValue()
  {
    if ( items != null )
    {
      return items;
    }
    return getData();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.AdvancedCode#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    if ( items != null )
    {
      return getValueString( items );
    }
    else
    {
      return getValueString( data, remoteConfig );
    }
  }
  
  public static String getValueString( List< KeySpec > items )
  {
    StringBuilder buff = new StringBuilder();
    for ( int i = 0; i < items.size(); ++i )
    {
      if ( i != 0 )
      {
        buff.append( ';' );
      }
      buff.append( items.get( i ) );
    }
    return buff.toString();
  }
  
  public static String getValueString( Hex hex, RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    DeviceButton db = null;
    short[] data = hex.getData();

    for ( int i = 0; i < hex.length(); ++i )
    {
      if ( i != 0 )
      {
        buff.append( ';' );
      }
      int keyCode = data[ i ] & 0xFF;
      DeviceButton temp = remote.getDeviceButton( keyCode );
      if ( temp != null )
      {
        db = temp;
      }
      String name = remote.getButtonName( data[ i ] & 0xFF );
      buff.append( name );
      int duration = ( data[ i ] >> 8 ) & 0xFF;
      if ( duration > 0 )
      {
        if ( duration == 0xFF )
        {
          duration = 0;
        }
        buff.append( "(" +  duration / 10 + "." + duration % 10 + ")" );
      }
    }
    return buff.toString();
  }

  /**
   * Sets the value.
   * 
   * @param value
   *          the new value
   */
  @SuppressWarnings( "unchecked" )
  public void setValue( Object value )
  {
    if ( value instanceof Hex )
    {
      setData( ( Hex )value );
    }
    else if ( value instanceof List< ? >)
    {
      setItems( ( List< KeySpec > )value );
    }
  }
  
  public int dataLength()
  {
    if ( items != null )
    {
      int length = 0;
      DeviceButton db = null;
      for ( KeySpec ks : items )
      {
        if ( !ks.isValid() )
        {
          continue;
        }
        length += ks.db != db ? 1 : 0;
        length += ks.duration >= 0 ? 1 : 0;
        length += ks.getButton() != null ? 1 : ks.fn != null ? 
            ks.db.getUpgrade().getRemote().isSSD() ? 2 : 1 : 0;
        db = ks.db;
      }
      return length;
    }
    if ( data != null )
    {
      return data.length();
    }
    return 0;
  }
  
  public Hex getItemData()
  {
    if ( items == null )
    {
      return new Hex( 0 );
    }
    int size = dataLength();
    short[] vals = new short[ 2 * size ];
    DeviceButton db = null;
    int pos = 0;
    for ( KeySpec ks : items )
    {
      if ( !ks.isValid() )
      {
        String str = "Invalid keySpec";
        if ( ks.db != null )
        {
          str += " for upgrade \"" + ks.db + "\"";
        }
        if ( ks.fn != null )
        {
          str += ", function \"" + ks.fn + "\"";
        }
        else if ( ks.btn != null )
        {
          str += ", button \"" + ks.btn + "\"";
        }
        str += " in macro " + this;
        System.err.println( str );
        continue;
      }
      if ( ks.db != db )
      {
        vals[ pos + size ] = 0;
        vals[ pos++ ] = ( short )( ks.db.getButtonIndex() );
        db = ks.db;
      }
      if ( ks.duration >= 0 )
      {
        vals[ pos + size ] =  ( short )ks.duration;
        vals[ pos++ ] = 0xFE;
      }
      Button btn = ks.getButton();
      if ( btn != null )
      {
        vals[ pos + size ] = ( short )ks.delay;
        vals[ pos++ ] = btn.getKeyCode();
      }
      else if ( ks.fn != null )
      {
        int serial = ks.fn.getSerial();
        Remote remote = ks.db.getUpgrade().getRemote();
        if ( remote.isSSD() )
        {
          // Delay value 0xFF acts as indicator in SSD remotes
          vals[ pos + size ] = ( short )0xFF;
          vals[ pos++ ] = ( short )( serial & 0xFF );
          vals[ pos + size ] = ( short )ks.delay;
          vals[ pos++ ] = ( short )( serial >> 8 );
        }
        else
        {
          // In non-SSD XSights the serial value encodes a system button to which
          // the function gets assigned if there is no other assignment.
          DeviceButton sysdb = remote.getDeviceButton( serial >> 8 );
          vals[ pos + size ] = ( short )ks.delay;
          vals[ pos++ ] = ( short )( db == sysdb ? serial & 0xFF : 0 );
        }
      }
    }
    return new Hex( vals );
  }
  
  public void setItems( Hex hex, Remote remote )
  {
    int count = hex.length() / 2;
    DeviceButton db = remote.getDeviceButtons()[ 0 ]; // default, only used for ill-formed macro
    int duration = -1;
    items = new ArrayList< KeySpec >();
    for ( int i = 0; i < count; i++ )
    {
      int keyCode = hex.getData()[ i ];
      DeviceButton db2 = remote.getDeviceButton( keyCode );
      Button btn = remote.getButton( keyCode );
      if ( db2 != null )
      {
        db = db2;
      }
      else if ( keyCode == 0xFE )
      {
        duration = hex.getData()[ i + count ];
      }
      else if ( hex.getData()[ i + count ] == 0xFF )
      {
        // first byte of ir serial
        int irSerial = hex.getData()[ i++ ];
        irSerial += hex.getData()[ i ] << 8;
        KeySpec ks = new KeySpec( db, irSerial );
        // irSerial is converted to a function after loading of device upgrades
        ks.delay = hex.getData()[ i + count ];
        ks.duration = duration;
        items.add( ks );
        duration = -1; 
      }
      else if ( btn != null )
      {
        KeySpec ks = new KeySpec( db, btn );
        // button is converted to a function after loading of device upgrades
        ks.delay = hex.getData()[ i + count ];
        ks.duration = duration;
        items.add( ks );
        duration = -1; 
      }
    }
  }
  
  public int store( short[] buffer, int offset, Remote remote )
  {
    buffer[ offset++ ] = ( short )getKeyCode();
    if ( remote.getAdvCodeBindFormat() == BindFormat.NORMAL )
    {
      buffer[ offset ] = 0x10;
    }
    else if ( remote.getMacroCodingType().get_Type() == 2 )
    {
      // With deviceIndex $F this allows for MultiMacro types $4, $5, $6, $7 (value in high
      // nibble) for type 2 coding even though no remote yet implements them.  With deviceIndex
      // other than $F these represent internal special protocols.
      buffer[ offset ] = ( short )( ( 0x30 + ( sequenceNumber << 4 ) + deviceButtonIndex ) & 0xFF );
      buffer[ ++offset ] = 0;
    }
    else
    {
      // High nibbles $9, $A, $B, $C, $D correspond to MultiMacros if deviceIndex is $F,
      // for other values of deviceIndex they correspond to internal special protocols.
      buffer[ offset ] = ( short )( ( 0x80 | ( sequenceNumber << 4 ) | deviceButtonIndex ) & 0xFF );
      buffer[ ++offset ] = 0;
    }
    int dataLength = data.length();
    buffer[ offset++ ] |= ( short )dataLength;
    Hex.put( data, buffer, offset );

    return offset + dataLength;
  }

  public void store( PropertyWriter pw )
  {
//    if ( getSegment().get_Type() == 3 || getSegment().get_Type() == 0x1E )
    if ( name != null )  // XSight remotes
    {
      int segmentFlags = getSegmentFlags();
      if ( segmentFlags > 0 )
      {
        pw.print( "SegmentFlags", segmentFlags );
      }
      Hex hex = null;
      if ( items != null )
      {
        hex = getItemData();
      }
      else
      {
        int dataLen = dataLength();
        hex = new Hex( 2 * dataLen );
        for ( int i = 0; i < dataLen; i++ )
        {
          int val = data.getData()[ i ];
          hex.set( ( short )( val & 0xFF ), i );
          hex.set( ( short )( ( val >> 8 ) & 0xFF ), dataLen + i );
        }
      }
      pw.print( "Name", name );
      pw.print( "DeviceIndex", deviceButtonIndex );
      pw.print( "KeyCode", keyCode );
      String userStr = "";
      for ( User u : users )
      {
        int dbi = u.db.getButtonIndex();
        int kc = u.button.getKeyCode();
        if ( dbi != deviceButtonIndex || kc != keyCode )
        {
          userStr += ( userStr.isEmpty() ? "" : "," ) + Integer.toString( dbi ) + "/" + Integer.toString( kc );
        }
      }
      if ( !userStr.isEmpty() )
      {
        pw.print( "Users", userStr );
      }
      if ( systemMacro )
      {
        pw.print( "SystemMacro", 1 );
      }
      pw.print( "Data", hex );
      pw.print( "Serial", serial );
      if ( assists != null && !assists.isEmpty() )
      {
        pw.print(  "Assistant", 1 );
        Assister.store( assists, pw );
      }
      if ( notes != null && notes.length() > 0 )
      {
        pw.print( "Notes", notes );
      }
      return;
    }
    
    super.store( pw );
    if ( sequenceNumber != 0 )
    {
      pw.print( "SequenceNumber", sequenceNumber );
    }
    if ( deviceButtonIndex != 0x0F )
    {
      pw.print( "DeviceIndex", deviceButtonIndex );
    }
  }

  private int sequenceNumber = 0;

  public int getSequenceNumber()
  {
    return sequenceNumber;
  }

  public void setSequenceNumber( int sequenceNumber )
  {
    this.sequenceNumber = sequenceNumber;
  }

  public DeviceButton getDeviceButton( RemoteConfiguration config )
  {
    return config.getRemote().getDeviceButton( deviceButtonIndex );
  }

  private List< Integer > userItems = null;
  
  public List< Integer > getUserItems()
  {
    return userItems;
  }

  public void setUserItems( List< Integer > userItems )
  {
    this.userItems = userItems;
  }

  private boolean systemMacro = false;

  public boolean isSystemMacro()
  {
    return systemMacro;
  }
  
  public void setSystemMacro( boolean systemMacro )
  {
    this.systemMacro = systemMacro;
  }
  
  private Activity activity = null;
  
  public Activity getActivity()
  {
    return activity;
  }

  public void setActivity( Activity activity )
  {
    this.activity = activity;
  }
  
  private LinkedHashMap< Integer, List< Assister > > assists = null;
  
  public LinkedHashMap< Integer, List< Assister >> getAssists()
  {
    return assists;
  }
  
  public void setAssists( LinkedHashMap< Integer, List< Assister >> assists )
  {
    this.assists = assists;
  }

  public String toString()
  {
    return name != null && !name.isEmpty() ? name : super.toString();  
  }
  
  protected static MacroCodingType macroCodingType = null;

  public static void setMacroCodingType( MacroCodingType aMacroCodingType )
  {
    macroCodingType = aMacroCodingType;
  }

  public static MacroCodingType getMacroCodingType()
  {
    return macroCodingType;
  }
  
}
