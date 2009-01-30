package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceButton.
 */
public class DeviceButton
{
  
  /**
   * Instantiates a new device button.
   * 
   * @param name the name
   * @param hiAddr the hi addr
   * @param lowAddr the low addr
   * @param typeAddr the type addr
   */
  public DeviceButton( String name, int hiAddr,
                       int lowAddr, int typeAddr )
  {
    this.name = name;
    highAddress = hiAddr;
    lowAddress = lowAddr;
    typeAddress = typeAddr;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName(){ return name; }
  
  /**
   * Gets the high address.
   * 
   * @return the high address
   */
  public int getHighAddress(){ return highAddress; }
  
  /**
   * Gets the low address.
   * 
   * @return the low address
   */
  public int getLowAddress(){ return lowAddress; }
  
  /**
   * Gets the type address.
   * 
   * @return the type address
   */
  public int getTypeAddress(){ return typeAddress; }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return name;
  }
  
  /**
   * Gets the device setup code.
   * 
   * @param data the data
   * 
   * @return the device setup code
   */
  public int getDeviceSetupCode( short[] data )
  {
    return ( data[ highAddress ] << 7 ) | data[ lowAddress ];
  }
  
  /**
   * Gets the device type index.
   * 
   * @param data the data
   * 
   * @return the device type index
   */
  public int getDeviceTypeIndex( short[] data )
  {
    return data[ highAddress ] >> 4;
  }
  
  /**
   * Sets the device type index.
   * 
   * @param index the index
   * @param data the data
   */
  public void setDeviceTypeIndex( short index, short[] data )
  {
      data[ highAddress ] &= 0x0F;
      index <<= 4;
      data[ highAddress ] |= index;
  }
  
  /**
   * Gets the setup code.
   * 
   * @param data the data
   * 
   * @return the setup code
   */
  public short getSetupCode( short[] data )
  {
     short setupCode = data[ highAddress ];
     setupCode &= 0x07;
     setupCode <<= 8;
     setupCode |= data[ lowAddress ];
     return setupCode;
  }
  
  /**
   * Sets the setup code.
   * 
   * @param setupCode the setup code
   * @param data the data
   */
  public void setSetupCode( short setupCode, short[] data )
  {
    short temp = setupCode;
    temp >>= 8;
    data[ highAddress ] &= 0xF8;
    data[ highAddress ] |= temp;

    setupCode &= 0xFF;
    data[ lowAddress ] = setupCode;
  }

  /** The name. */
  private String name;
  
  /** The high address. */
  private int highAddress = 0;
  
  /** The low address. */
  private int lowAddress = 0;
  
  /** The type address. */
  private int typeAddress = 0;
}
