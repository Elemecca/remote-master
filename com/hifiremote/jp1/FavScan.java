package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class FavScan extends AdvancedCode
{
  private DeviceButton deviceButton = null;

  public FavScan( int keyCode, Hex data, String notes )
  {
    super( keyCode, data, notes );
  }

  public FavScan( Properties props )
  {
    super( props );
    String temp = props.getProperty( "Channel" );
    if ( temp != null )
    {
      channel = temp;
    }
    temp = props.getProperty( "DeviceIndex" );
    deviceIndex = temp != null ? Integer.parseInt( temp ) : -1;
    temp = props.getProperty( "Serial" );
    if ( temp != null )
    {
      serial = Integer.parseInt( temp );
    }
    temp = props.getProperty( "ProfileIndices" );
    if ( temp != null )
    {
      StringTokenizer st = new StringTokenizer( temp, " " );
      profileIndices = new ArrayList< Integer >();
      while ( st.hasMoreTokens() )
      {
        String token = st.nextToken();
        profileIndices.add( Integer.parseInt( token, 16 ) );
      }
    }
  }
  
  public FavScan( FavScan favScan )
  {
    super( favScan.getKeyCode(), new Hex( favScan.getData() ), favScan.getNotes() );
    deviceButton = favScan.getDeviceButton();
    name = favScan.getName();
    icon = favScan.icon;
    channel = favScan.channel;
  }

  public static FavScan read( HexReader reader, Remote remote )
  {
    if ( ( reader.available() < 4 ) || ( reader.peek() == remote.getSectionTerminator() ) )
    {
      return null;
    }
    
    FavKey favKey = remote.getFavKey();
    int keyCode = favKey.getKeyCode();
    int length = reader.read();
    length *= favKey.getEntrySize();
    int deviceIndex = reader.read();
    deviceIndex &= 0x0F;
    Hex hex = new Hex( reader.read( length ) );
    
    FavScan favScan = new FavScan( keyCode, hex, null );
    favScan.setDeviceIndex( deviceIndex );
    return favScan;
  }
  
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    Remote remote = remoteConfig.getRemote();
    StringBuilder buff = new StringBuilder();
    short[] keys = data.getData();
    int entrySize = remote.getFavKey().getEntrySize();
    boolean paused = false;
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
      if ( paused )
      {
        buff.append( ' ' );
        paused = false;
      }
      if ( keys[ i ] != 0 )
      {
        buff.append( remote.getButtonName( keys[ i ] ) );
      }
      if ( entrySize >= 0 && ( keys[ i ] == 0 || ( ( i + 1 ) % entrySize ) == 0 ) )
      {
        while ( ( i < keys.length ) &&  ( ( keys[ i ] == 0 ) || ( i % entrySize ) != 0 ) )
        {  
          ++i;
        }
        --i;
        if ( keys[ i ] != 0 )
        {
          buff.append( ';' );
        }
        buff.append( "{Pause}" );
        paused = true;
      }     
    }
    return buff.toString();
  }

  @Override
  public int store( short[] buffer, int offset, Remote remote )
  {
    FavKey favKey = remote.getFavKey();
    int dataLength = data.length();
    if ( favKey.isSegregated() )
    {
      buffer[ offset++ ] = ( short )( dataLength / favKey.getEntrySize() );
      offset++; // Skip the device byte
    }
    else if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL )
    {
      buffer[ offset++ ] = ( short )favKey.getKeyCode();
      buffer[ offset++ ] = ( short )( 0x30 | ( dataLength / favKey.getEntrySize() ) );
    }
    else
    {
      buffer[ offset++ ] = ( short )favKey.getKeyCode();
      // Button index of DeviceButton.noButton is -1, and the "& 0x0F" turns this into 0x0F as required.
      buffer[ offset++ ] = ( short )( 0x30 | ( deviceButton.getButtonIndex() & 0x0F ) );
      buffer[ offset++ ] = ( short )dataLength;
    }
    Hex.put( data, buffer, offset );
    return offset + dataLength;
  }
  
  public void store( PropertyWriter pw, RemoteConfiguration remoteConfig )
  {
    super.store( pw );
    if ( channel != null && channel.length() > 0 )
    {
      pw.print( "Channel", channel );
    }
    Remote remote = remoteConfig.getRemote();
    DeviceButton devBtn = ( remote.getAdvCodeBindFormat() ==  AdvancedCode.BindFormat.NORMAL 
        || remoteConfig.hasSegments() ) ? remoteConfig.getFavKeyDevButton() : this.getDeviceButton();
    if ( devBtn != DeviceButton.noButton )
    {
      pw.print( "DeviceIndex", devBtn.getButtonIndex() );
    }
    if ( serial >= 0 )
    {
      pw.print( "Serial", serial );
    }
    if ( profileIndices != null )
    {
      String str = "";
      for ( int i = 0; i < profileIndices.size(); i++ )
      {
        if ( i > 0 )
        {
          str += " ";
        }
        str += profileIndices.get( i );
      }
      pw.print( "ProfileIndices", str );
    }
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public DeviceButton getDeviceButton()
  {
    return deviceButton;
  }

  public void setDeviceButton( DeviceButton deviceButton )
  {
    this.deviceButton = deviceButton;
  }
  
  private int deviceIndex = 0x0F;

  public void setDeviceIndex( int deviceIndex )
  {
    this.deviceIndex = deviceIndex;
  }

  public int getDeviceIndex()
  {
    return deviceIndex;
  }

  public DeviceButton getDeviceButtonFromIndex( Remote remote )
  {
    // It seems as if <none> corresponds to a deviceIndex of 0xFF if AdvCodeBindFormat == NORMAL,
    // where it is a byte value, but of 0x0F if it is LONG, where it is a nibble value.  
    if ( deviceIndex == 0xFF || deviceIndex == 0x0F && remote.getAdvCodeBindFormat() == BindFormat.LONG )
      return DeviceButton.noButton;
    else
      return remote.getDeviceButton( deviceIndex );
  }
  
  private List< Integer > profileIndices = null;

  public List< Integer > getProfileIndices()
  {
    return profileIndices;
  }

  public void setProfileIndices( List< Integer > profileIndices )
  {
    this.profileIndices = profileIndices;
  }
  
  private String channel = "";

  public String getChannel()
  {
    return channel;
  }

  public void setChannel( String channel )
  {
    this.channel = channel;
  }

}
