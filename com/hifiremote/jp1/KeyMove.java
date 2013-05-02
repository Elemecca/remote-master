package com.hifiremote.jp1;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class KeyMove.
 */
public class KeyMove extends AdvancedCode implements Cloneable
{

  /**
   * Instantiates a new key move.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param data
   *          the data
   * @param notes
   *          the notes
   */
  public KeyMove( int keyCode, int deviceButtonIndex, Hex data, String notes )
  {
    super( keyCode, data, notes );
    cmd = data.subHex( cmdIndex );
    this.deviceButtonIndex = deviceButtonIndex;
    short[] hex = data.getData();
    deviceType = hex[ DEVICE_TYPE_INDEX ] >> ( setupCodeIndex == DEVICE_TYPE_INDEX ? 4 : 0 );
    setupCode = Hex.get( hex, setupCodeIndex ) & SetupCode.getMax();
  }

  /**
   * Instantiates a new key move.
   * 
   * @param keyCode
   *          the key code
   * @param deviceButtonIndex
   *          the device button index
   * @param deviceType
   *          the device type
   * @param setupCode
   *          the setup code
   * @param cmd
   *          the cmd
   * @param notes
   *          the notes
   */
  public KeyMove( int keyCode, int deviceButtonIndex, int deviceType, int setupCode, Hex cmd, String notes )
  {
    super( keyCode, null, notes );
    Hex cmdHex = cmd;
    if ( cmdIndex == 3 && cmd.length() < 3 )
    {
      cmdHex = new Hex( 3 );
      cmdHex.put( cmd, 1 );
    }
    setData( getRawHex( deviceType, setupCode, cmdHex ) );
    this.cmd = cmd;
    setDeviceButtonIndex( deviceButtonIndex );
    this.deviceType = deviceType;
    this.setupCode = setupCode;
  }

  public Hex getRawHex( int deviceType, int setupCode, Hex cmd )
  {
    Hex hex = new Hex( cmdIndex + cmd.length() );
    update( deviceType, setupCode, hex );
    hex.put( cmd, cmdIndex );
    return hex;
  }

  /**
   * Instantiates a new key move.
   * 
   * @param props
   *          the props
   */
  public KeyMove( Properties props )
  {
    super( props );
    cmd = data.subHex( cmdIndex == 2 ? 2 : ( this instanceof KeyMoveKey ) ? 3 : 4 );
    if ( this instanceof KeyMoveLong )
    {
      // KeyMoveLong should only be used for 1-byte commands when bind format is LONG
      // so extract the 1-byte command from the 2-byte hex.
      cmd = cmd.subHex( 0, 1 );
    }
    deviceButtonIndex = Integer.parseInt( props.getProperty( "DeviceButtonIndex" ) );
    setDeviceType( Integer.parseInt( props.getProperty( "DeviceType" ) ) );
    setSetupCode( Integer.parseInt( props.getProperty( "SetupCode" ) ) );
  }

  /**
   * Instantiates a new key move.
   * 
   * @param keyMove
   *          the key move
   */
  public KeyMove( KeyMove keyMove )
  {
    this( keyMove.getKeyCode(), keyMove.getDeviceButtonIndex(), keyMove.getDeviceType(), keyMove.getSetupCode(),
        new Hex( keyMove.getCmd() ), keyMove.getNotes() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  @Override
  protected Object clone() throws CloneNotSupportedException
  {
    return new KeyMove( getKeyCode(), getDeviceButtonIndex(), getDeviceType(), getSetupCode(), ( Hex )getCmd().clone(),
        getNotes() );
  }

  /**
   * Gets the eFC.
   * 
   * @return the eFC
   */
  public EFC getEFC()
  {
    return new EFC( cmd );
  }

  /**
   * Gets the eF c5.
   * 
   * @return the eF c5
   */
  public EFC5 getEFC5()
  {
    // 26/01/12 Added test of cmdIndex here so that JP1.4/JP2 remotes get efc for KeyMoveLong 
    // based on 1-byte command, not the 2-byte value in the hex data.  Not sure why earlier
    // remotes need efc of the 2-byte value.
    if ( this instanceof KeyMoveLong && cmd.length() == 1 && cmdIndex == 2 )
    {
      return new EFC5( getRawHex( 0, 0, cmd ).subHex( cmdIndex == 2 ? 2 : 4 ) );
    }
    return new EFC5( cmd );
  }

  /**
   * Sets the cmd.
   * 
   * @param hex
   *          the new cmd
   */
  public void setCmd( Hex cmd )
  {
    this.cmd = cmd;
    data = getRawHex( deviceType, setupCode, cmd );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.AdvancedCode#getValueString(com.hifiremote.jp1.RemoteConfiguration)
   */
  @Override
  public String getValueString( RemoteConfiguration remoteConfig )
  {
    DeviceUpgrade deviceUpgrade = remoteConfig.findDeviceUpgrade( getDeviceType(), getSetupCode() );
    if ( deviceUpgrade != null )
    {
      Function f = deviceUpgrade.getFunction( getCmd() );
      if ( f != null )
      {
        return "\"" + f.getName() + '"';
      }
    }
    
    if ( cmd.length() == 1 && !( this instanceof KeyMoveLong ) )
    {
      return getEFC().toString();
    }
    else
    {
      return getEFC5().toString();
    }
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
   * @param newDeviceType
   *          the new device type
   */
  public void setDeviceType( int newDeviceType )
  {
    deviceType = newDeviceType;
    update();
  }

  private DeviceButton targetDevice = null;
  
  
  public DeviceButton getTargetDevice()
  {
    return targetDevice;
  }

  public void setTargetDevice( DeviceButton targetDevice )
  {
    this.targetDevice = targetDevice;
  }

  private void update()
  {
    update( deviceType, setupCode, data );
  }

  protected static void update( int deviceType, int setupCode, Hex data )
  {
    if ( setupCodeIndex == DEVICE_TYPE_INDEX )
    {
      int temp = deviceType << 12 | setupCode;
      data.put( temp, setupCodeIndex );
    }
    else
    {
      data.set( ( short)deviceType, DEVICE_TYPE_INDEX );
      data.put( setupCode, setupCodeIndex );
    }
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
   * @param newCode
   *          the new setup code
   */
  public void setSetupCode( int newCode )
  {
    setupCode = newCode;
    update();
  }

  private Hex cmd = null;

  public Hex getCmd()
  {
    return cmd;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.AdvancedCode#store(com.hifiremote.jp1.PropertyWriter)
   */
  @Override
  public void store( PropertyWriter pw )
  {
    pw.print( "DeviceButtonIndex", deviceButtonIndex );
    pw.print( "DeviceType", deviceType );
    pw.print( "SetupCode", setupCode );
    super.store( pw );
  }

  /** The Constant DEVICE_TYPE_INDEX. */
  protected final static int DEVICE_TYPE_INDEX = 0;

  private static int setupCodeIndex = 0;

  public static void setSetupCodeIndex( int setupCodeIndex )
  {
    KeyMove.setupCodeIndex = setupCodeIndex;
  }

  private static int cmdIndex = 2;

  public static int getCmdIndex()
  {
    return cmdIndex;
  }

  public static void setCmdIndex( int cmdIndex )
  {
    KeyMove.cmdIndex = cmdIndex;
  }

  private Integer irSerial = null;
  
  public Integer getIrSerial()
  {
    return irSerial;
  }

  public void setIrSerial( Integer irSerial )
  {
    this.irSerial = irSerial;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.AdvancedCode#store(short[], int)
   */
  @Override
  public int store( short[] buffer, int offset, Remote remote )
  {
    int hexLength;
    if ( cmdIndex == 2 )  // remotes without segments
    {
      buffer[ offset++ ] = ( short )keyCode;
      int lengthOffset;

      if ( remote.getAdvCodeBindFormat() == BindFormat.NORMAL )
      {
        int temp = deviceButtonIndex << 5;
        buffer[ offset ] = ( short )temp;
        lengthOffset = offset++ ;
      }
      else
        // LONG Format
      {
        buffer[ offset++ ] = ( short )( 0x10 | deviceButtonIndex );
        lengthOffset = offset++ ;
        buffer[ lengthOffset ] = 0;
      }
      hexLength = data.length();
      if ( hexLength == 5 && this instanceof KeyMoveEFC5 )
      {
        hexLength = 4;
        Hex.put( data.subHex( 0, 4 ), buffer, offset );
      }
      else
      {
        Hex.put( data, buffer, offset );
      }
      buffer[ lengthOffset ] |= ( short )hexLength;
    }
    else
    {
      buffer[ offset++ ] = ( short )deviceButtonIndex;
      buffer[ offset++ ] = ( short )keyCode;
      hexLength = data.length();
      Hex.put( data, buffer, offset );
    }
    return offset + hexLength;
  }
  
}
