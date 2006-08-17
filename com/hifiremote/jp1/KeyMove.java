package com.hifiremote.jp1;

import java.util.Properties;

public class KeyMove
  extends AdvancedCode
  implements Cloneable
{
  public KeyMove( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, data.subHex( CMD_INDEX ), notes );
    this.deviceButtonIndex = deviceButtonIndex;
    setDeviceType( data.getData()[ DEVICE_TYPE_INDEX ] >> 4 );
    setSetupCode( data.get( SETUP_CODE_INDEX ) & 0x07FF );
  }
  
  public KeyMove( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, cmd, notes );
    setDeviceButtonIndex( deviceButtonIndex );
    setDeviceType( deviceType );
    setSetupCode( setupCode );
  }    
  
  public KeyMove( Properties props )
  {
    super( props );
    deviceButtonIndex = Integer.parseInt( props.getProperty( "DeviceButtonIndex" ));
    deviceType = Integer.parseInt( props.getProperty( "DeviceType" ));
    setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));
  }
  
  public KeyMove( KeyMove keyMove )
  {
    this( keyMove.getKeyCode(), keyMove.getDeviceButtonIndex(), keyMove.getDeviceType(),
          keyMove.getSetupCode(), new Hex( keyMove.getCmd()), keyMove.getNotes());
  }
  
  protected Object clone()
    throws CloneNotSupportedException
  {
    return new KeyMove( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), ( Hex )getCmd().clone(), getNotes());
  }
  
  public EFC getEFC()
  {
    return new EFC( data );
  }
  
  public void setEFC( EFC efc )
  {
    efc.toHex( data );
  }
  
  public Hex getCmd()
  {
    return data;
  }
  
  public void setCmd( Hex hex )
  {
    data = hex;
  }

  public String getValueString( RemoteConfiguration remoteConfig )
  {
    return getEFC().toString();
  }

  private int deviceButtonIndex;
  public int getDeviceButtonIndex(){ return deviceButtonIndex; }
  public void setDeviceButtonIndex( int newIndex )
  {
    deviceButtonIndex = newIndex;
  }

  private int deviceType;
  public int getDeviceType()
  {
    return deviceType;
  }
  
  public void setDeviceType( int newDeviceType )
  {
    deviceType = newDeviceType;
  }
  
  private int setupCode;
  public int getSetupCode()
  {
    return setupCode;
  }

  public void setSetupCode( int newCode )
  {
    setupCode = newCode;
  }

  public Hex getRawHex()
  {
    Hex hex = new Hex( CMD_INDEX + data.length());
    int temp = ( deviceType << 12 ) | setupCode;
    hex.put( temp, SETUP_CODE_INDEX );
    hex.put( data, CMD_INDEX );
    return hex;
  }

  public void store( PropertyWriter pw )
  {
    pw.print( "DeviceButtonIndex", deviceButtonIndex );
    pw.print( "DeviceType", deviceType );
    pw.print( "SetupCode", setupCode );
    super.store( pw );
  }

  protected final static int DEVICE_TYPE_INDEX = 0;
  protected final static int SETUP_CODE_INDEX = 0;
  protected final static int CMD_INDEX = 2;
}
