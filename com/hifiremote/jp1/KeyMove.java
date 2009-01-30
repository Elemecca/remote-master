package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMove.
 */
public class KeyMove
  extends AdvancedCode
  implements Cloneable
{
  
  /**
   * Instantiates a new key move.
   * 
   * @param keyCode the key code
   * @param deviceButtonIndex the device button index
   * @param data the data
   * @param notes the notes
   */
  public KeyMove( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, data.subHex( CMD_INDEX ), notes );
    this.deviceButtonIndex = deviceButtonIndex;
    setDeviceType( data.getData()[ DEVICE_TYPE_INDEX ] >> 4 );
    setSetupCode( data.get( SETUP_CODE_INDEX ) & 0x07FF );
  }
  
  /**
   * Instantiates a new key move.
   * 
   * @param keyCode the key code
   * @param deviceButtonIndex the device button index
   * @param deviceType the device type
   * @param setupCode the setup code
   * @param cmd the cmd
   * @param notes the notes
   */
  public KeyMove( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, cmd, notes );
    setDeviceButtonIndex( deviceButtonIndex );
    setDeviceType( deviceType );
    setSetupCode( setupCode );
  }    
  
  /**
   * Instantiates a new key move.
   * 
   * @param props the props
   */
  public KeyMove( Properties props )
  {
    super( props );
    deviceButtonIndex = Integer.parseInt( props.getProperty( "DeviceButtonIndex" ));
    deviceType = Integer.parseInt( props.getProperty( "DeviceType" ));
    setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));
  }
  
  /**
   * Instantiates a new key move.
   * 
   * @param keyMove the key move
   */
  public KeyMove( KeyMove keyMove )
  {
    this( keyMove.getKeyCode(), keyMove.getDeviceButtonIndex(), keyMove.getDeviceType(),
          keyMove.getSetupCode(), new Hex( keyMove.getCmd()), keyMove.getNotes());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  protected Object clone()
    throws CloneNotSupportedException
  {
    return new KeyMove( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), ( Hex )getCmd().clone(), getNotes());
  }
  
  /**
   * Gets the eFC.
   * 
   * @return the eFC
   */
  public EFC getEFC()
  {
    return new EFC( data );
  }
  
  /**
   * Sets the eFC.
   * 
   * @param efc the new eFC
   */
  public void setEFC( EFC efc )
  {
    efc.toHex( data );
  }
  
  /**
   * Gets the eF c5.
   * 
   * @return the eF c5
   */
  public EFC5 getEFC5()
  {
    return new EFC5( data );
  }
  
  /**
   * Gets the cmd.
   * 
   * @return the cmd
   */
  public Hex getCmd()
  {
    return data;
  }
  
  /**
   * Sets the cmd.
   * 
   * @param hex the new cmd
   */
  public void setCmd( Hex hex )
  {
    data = hex;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.AdvancedCode#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    String rc = getEFC().toString();
    if ( remoteConfig.getRemote().getEFCDigits() == 3 )
      return rc;
    if ( rc.length() == 3 )
      rc = "00" + rc;
    return rc + " or " + getEFC5().toString(); 
  }

  /** The device button index. */
  private int deviceButtonIndex;
  
  /**
   * Gets the device button index.
   * 
   * @return the device button index
   */
  public int getDeviceButtonIndex(){ return deviceButtonIndex; }
  
  /**
   * Sets the device button index.
   * 
   * @param newIndex the new device button index
   */
  public void setDeviceButtonIndex( int newIndex )
  {
    deviceButtonIndex = newIndex;
  }

  /** The device type. */
  private int deviceType;
  
  /**
   * Gets the device type.
   * 
   * @return the device type
   */
  public int getDeviceType()
  {
    return deviceType;
  }
  
  /**
   * Sets the device type.
   * 
   * @param newDeviceType the new device type
   */
  public void setDeviceType( int newDeviceType )
  {
    deviceType = newDeviceType;
  }
  
  /** The setup code. */
  private int setupCode;
  
  /**
   * Gets the setup code.
   * 
   * @return the setup code
   */
  public int getSetupCode()
  {
    return setupCode;
  }

  /**
   * Sets the setup code.
   * 
   * @param newCode the new setup code
   */
  public void setSetupCode( int newCode )
  {
    setupCode = newCode;
  }

  /**
   * Gets the raw hex.
   * 
   * @return the raw hex
   */
  public Hex getRawHex()
  {
    Hex hex = new Hex( CMD_INDEX + data.length());
    int temp = ( deviceType << 12 ) | setupCode;
    hex.put( temp, SETUP_CODE_INDEX );
    hex.put( data, CMD_INDEX );
    return hex;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.AdvancedCode#store(com.hifiremote.jp1.PropertyWriter)
   */
  public void store( PropertyWriter pw )
  {
    pw.print( "DeviceButtonIndex", deviceButtonIndex );
    pw.print( "DeviceType", deviceType );
    pw.print( "SetupCode", setupCode );
    super.store( pw );
  }

  /** The Constant DEVICE_TYPE_INDEX. */
  protected final static int DEVICE_TYPE_INDEX = 0;
  
  /** The Constant SETUP_CODE_INDEX. */
  protected final static int SETUP_CODE_INDEX = 0;
  
  /** The Constant CMD_INDEX. */
  protected final static int CMD_INDEX = 2;
}
