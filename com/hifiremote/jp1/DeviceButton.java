package com.hifiremote.jp1;

import java.awt.Color;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceButton.
 */
public class DeviceButton extends Highlight
{
  /**
   * Instantiates a new device button.
   * 
   * @param name
   *          the name
   * @param hiAddr
   *          the hi addr
   * @param lowAddr
   *          the low addr
   * @param typeAddr
   *          the type addr
   * @param setupCode
   *          the default setup code
   * @param maxSetupCode
   *          the maximum allowed setup code
   */
  public DeviceButton( String name, int hiAddr, int lowAddr, int typeAddr, int setupCode, int index  )
  {
    this.name = name;
    highAddress = hiAddr;
    lowAddress = lowAddr;
    typeAddress = typeAddr;
    defaultSetupCode = setupCode;
    buttonIndex = index;
  }
  
  public static final DeviceButton noButton = new DeviceButton( "<none>", 0, 0, 0, 0, -1 );

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Gets the default setup code.
   * 
   * @return the default setup code
   */
  public int getDefaultSetupCode()
  {
    return defaultSetupCode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return name;
  }

  /**
   * Gets the device type index.
   * 
   * @param data
   *          the data
   * @return the device type index
   */
  public int getDeviceTypeIndex( short[] data )
  {
    return highAddress > 0 ? data[ highAddress ] >> 4 : data[ 2 ];
  }
  
  /**
   * Sets the device type index.
   * 
   * @param index
   *          the index
   * @param data
   *          the data
   */
  public void setDeviceTypeIndex( short index, short[] data )
  {
    if ( highAddress > 0 )
    {
      if ( index == 0xFF )
      {
        data[ highAddress ] = 0xFF;
        data[ lowAddress ] = 0xFF;
      }
      else
      {
        data[ highAddress ] &= 0x0F;
        index <<= 4;
        data[ highAddress ] |= index;
      }
    }
    else
    {
      data[ 2 ] = index;
    }
  }
  
  public int getDeviceGroup( short[] data )
  {
    if ( typeAddress > 0 ) return data[ typeAddress ];
    else return -1;
  }
  
  public void setDeviceGroup( short group, short[] data )
  {
    if ( typeAddress > 0 )
    {
      data[ typeAddress ] = group;
    }
  }

  /**
   * Gets the setup code.
   * 
   * @param data
   *          the data
   * @return the setup code
   */
  public short getSetupCode( short[] data )
  {
    short setupCode = highAddress > 0 ? data[ highAddress ] : data[ 3 ];
    int mask = SetupCode.getMax() >> 8;
    if ( ( mask & 0xF0 ) == 0 )
    {
      // Don't apply mask if setup code allows highest nibble to be nonzero (remotes with segments)
      setupCode &= mask;
    }
    setupCode <<= 8;
    setupCode |= lowAddress > 0 ? data[ lowAddress ] : data[ 4 ];
    return setupCode;
  }

  /**
   * Sets the setup code.
   * 
   * @param setupCode
   *          the setup code
   * @param data
   *          the data
   */
  public void setSetupCode( short setupCode, short[] data )
  {
    if ( setupCode > SetupCode.getMax() )
    {
      throw new NumberFormatException( "Setup codes must be between 0 and " + SetupCode.getMax() );
    }
    if ( highAddress > 0 )
    {
      short temp = setupCode;
      temp >>= 8;
      int mask = 0xF8;
      if ( SetupCode.getMax() > 2047 )
        mask = 0xF0;
      data[ highAddress ] &= mask;
      data[ highAddress ] |= temp;
      data[ lowAddress ] = ( short )( setupCode & 0xFF );
    }
    else
    {
      Hex.put( ( int )setupCode, data, 3 );
    }
  }
  
  public int getDeviceSlot( short[] data )
  {
    return ( data[ highAddress ] << 8 ) | data[ lowAddress ];
  }
  
  public void setDeviceSlot( int value, short[] data )
  {
    data[ highAddress ] = ( short )( value >> 8 );
    data[ lowAddress ] = ( short )( value & 0xFF );
  }
  
  public void zeroDeviceSlot( short[] data )
  {
    if ( highAddress > 0 )
    {
      data[ highAddress ] = 0;
      data[ lowAddress ] = 0;
    }
  }
  
  public int getButtonIndex()
  {
    return buttonIndex;
  }

  public DeviceButton getVolumePT()
  {
    return volumePT;
  }

  public void setVolumePT( DeviceButton volumePT )
  {
    this.volumePT = volumePT;
  }

  public DeviceButton getTransportPT()
  {
    return transportPT;
  }

  public void setTransportPT( DeviceButton transportPT )
  {
    this.transportPT = transportPT;
  }

  public DeviceButton getChannelPT()
  {
    return channelPT;
  }

  public void setChannelPT( DeviceButton channelPT )
  {
    this.channelPT = channelPT;
  }

  /** The name. */
  private String name;

  /** The high address. */
  private int highAddress = 0;

  /** The low address. */
  private int lowAddress = 0;

  /** The type address. */
  private int typeAddress = 0;

  /** The default setup code */
  private int defaultSetupCode = 0;
  
  private int buttonIndex = 0;
  private DeviceButton volumePT = noButton;
  private DeviceButton transportPT = noButton;
  private DeviceButton channelPT = noButton;

  public void doHighlight( Color[] highlight )
  {
    if ( highAddress > 0 )
    {
      highlight[ highAddress ] = getHighlight();
      highlight[ lowAddress ] = getHighlight();
      if ( typeAddress > 0 )
      {
        highlight[ typeAddress ] = getHighlight();
      }
      setMemoryUsage( ( typeAddress > 0 ) ? 3 : 2 );
    }
    else
    {
      int address = getSegment().getAddress();
      for ( int i = 3; i < 16; i++ )
      {
        highlight[ address + i ] = getHighlight();
      }
      setMemoryUsage( 13 );
    }
  }
  
  public void store( Remote remote )
  {
    if ( getSegment() != null )
    {
      Hex hex = getSegment().getHex();
      hex.set( ( short )Math.max( volumePT.getButtonIndex(), 0 ), 6 );
      hex.set( ( short )Math.max( transportPT.getButtonIndex(), 0 ), 7 );
      hex.set( ( short )Math.max( channelPT.getButtonIndex(), 0 ), 8 );
    }
  }
}
