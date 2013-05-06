package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class FavScan extends AdvancedCode
{
  private String name = null;
  private DeviceButton deviceButton = null;

  public FavScan( int keyCode, Hex data, String notes )
  {
    super( keyCode, data, notes );
    // TODO Auto-generated constructor stub
  }

  public FavScan( Properties props )
  {
    super( props );
    name = props.getProperty( "Name" );
    String temp = props.getProperty( "DeviceIndex" );
    if ( temp != null )
    {
      deviceIndex = Integer.parseInt( temp );
    }
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
    temp = props.getProperty( "Iconref" );
    if ( temp != null )
    {
      iconref = Integer.parseInt( temp );
    }
  }
  
  public FavScan( FavScan favScan )
  {
    super( favScan.getKeyCode(), new Hex( favScan.getData() ), favScan.getNotes() );
    deviceButton = favScan.getDeviceButton();
    name = favScan.getName();
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
    for ( int i = 0; i < keys.length; ++i )
    {
      if ( i != 0 )
        buff.append( ';' );
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
    if ( name != null )
    {
      pw.print( "Name", name );
    }
    super.store( pw );
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
    if ( iconref != null )
    {
      pw.print( "Iconref", iconref );
    }
  }

  public String getName()
  {
    return name;
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

  public DeviceButton getDeviceButtonFromIndex( Remote remote )
  {
    if ( deviceIndex == 0x0F )
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
  
//  private Integer iconRef = null;
//
//  public Integer getIconRef()
//  {
//    return iconRef;
//  }
//
//  public void setIconRef( Integer iconRef )
//  {
//    this.iconRef = iconRef;
//  }
  

}
