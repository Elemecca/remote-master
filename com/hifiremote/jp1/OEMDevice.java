package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class OEMDevice.
 */
public class OEMDevice
{
  
  /**
   * Instantiates a new oEM device.
   * 
   * @param deviceNumber the device number
   * @param deviceAddress the device address
   */
  public OEMDevice( int deviceNumber, int deviceAddress )
  {
    this.deviceNumber = deviceNumber;
    this.deviceAddress = deviceAddress;
  }

  /**
   * Gets the device number.
   * 
   * @return the device number
   */
  public int getDeviceNumber(){ return deviceNumber; }
  
  /**
   * Gets the device address.
   * 
   * @return the device address
   */
  public int getDeviceAddress(){ return deviceAddress; }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 10 );
    temp.append( deviceNumber )
        .append( ", $" )
        .append( Integer.toHexString( deviceAddress ));
    return temp.toString();
  }

  /** The device number. */
  private int deviceNumber;
  
  /** The device address. */
  private int deviceAddress;
}
