package com.hifiremote.jp1;

public class KeyMove
  extends AdvancedCode
{
  public KeyMove( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, data, notes );
    this.deviceButtonIndex = deviceButtonIndex;
  }

  public Object getValue()
  {
    return new EFC( data, CMD_INDEX );
  }

  public void setValue( Object value )
  {
    (( EFC )value ).toHex( data, CMD_INDEX );
  }

  private int deviceButtonIndex;
  public int getDeviceButtonIndex(){ return deviceButtonIndex; }
  public void setDeviceButtonIndex( int newIndex )
  {
    deviceButtonIndex = newIndex;
  }

  public int getDeviceType()
  {
    return data.getData()[ DEVICE_TYPE_INDEX ] >> 4;
  }
  
  public void setDeviceType( int newDeviceType )
  {
    short[] hex = data.getData();
    short temp = ( short )( hex[ DEVICE_TYPE_INDEX ] & 0x0F);
    temp |= ( short )( newDeviceType << 4 );
    hex[ DEVICE_TYPE_INDEX ] = ( short )temp;
  }

  public int getSetupCode()
  {
    return data.get( SETUP_CODE_INDEX ) & 0x0FFF;
  }

  public void setSetupCode( int newCode )
  {
    int temp = data.get( 0 );
    temp &= 0xF000;
    data.put(( temp & 0xF000 ) | newCode, SETUP_CODE_INDEX );
  }

  public Hex getCmd()
  {
    return data.subHex( CMD_INDEX );
  }

  protected final static int DEVICE_TYPE_INDEX = 0;
  protected final static int SETUP_CODE_INDEX = 0;
  protected final static int CMD_INDEX = 2;
}
