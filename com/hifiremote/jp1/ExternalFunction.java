package com.hifiremote.jp1;

import java.util.Properties;

public class ExternalFunction
  extends Function
{
  public boolean isExternal(){ return true; }

  public void store( Properties props, String prefix )
  {
    super.store( props, prefix );
    props.setProperty( prefix + ".type", Integer.toString( type ));
    if ( deviceType != null )
      props.setProperty( prefix + ".deviceType", deviceType.getName());
    props.setProperty( prefix + ".setupCode", Integer.toString( setupCode ));
  }

  public void load( Properties props, String prefix, Remote remote )
  {
    super.load( props, prefix );
    String str = props.getProperty( prefix + ".type" );
    if ( str != null )
      setType( new Integer( str ));
    str = props.getProperty( prefix + ".deviceType" );
    if ( str != null )
      setDeviceType( remote.getDeviceType( str ));
    str = props.getProperty( prefix + ".setupCode" );
    if ( str != null )
      setSetupCode( Integer.parseInt( str ));
  }

  public DeviceType getDeviceType(){ return deviceType; }
  public void setDeviceType( DeviceType newType ){ deviceType = newType; }

  public final static int EFCType = 0;
  public final static int HexType = 1;

  public void setType( int type )
  {
    this.type = type;
  }

  public void setType( Integer type )
  {
    setType( type.intValue());
  }

  public int getType()
  {
    return type;
  }

  public void setValue( Object value )
  {
    if ( value == null )
      setHex( null );
    else
      if ( type == EFCType )
        setEFC(( Integer )value );
      else
        setHex(( byte[] )value );
  }

  public Object getValue()
  {
    if ( type == EFCType )
      return getEFC();
    else
      return getHex();
  }


  public void setSetupCode( int code ){ setupCode = code; }
  public int getSetupCode(){ return setupCode; }

  public Integer getEFC()
  {
    Integer rc = null;
    byte[] hex = hexData;
    if ( hexData != null )
    {
      rc = new Integer( Protocol.hex2efc( hexData, 0 ));
    }
    return rc;
  }

  public void setEFC( Integer val )
  {
    if ( val != null )
    {
      if ( hexData == null )
        hexData = new byte[ 1 ];
      Protocol.efc2hex( val.byteValue(), hexData, 0 );
    }
    else
      hexData = null;
  }

  private DeviceType deviceType;
  private int setupCode;
  private int type;
}
