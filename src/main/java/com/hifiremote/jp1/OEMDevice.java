package com.hifiremote.jp1;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class OEMDevice.
 */
public class OEMDevice extends RDFParameter
{
  public void parse( String text, Remote remote ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( ",= " );
    deviceNumber = RDFReader.parseNumber( st.nextToken() );
    deviceAddress = RDFReader.parseNumber( st.nextToken() );
  }

  /**
   * Gets the device number.
   * 
   * @return the device number
   */
  public int getDeviceNumber()
  {
    return deviceNumber;
  }

  /**
   * Gets the device address.
   * 
   * @return the device address
   */
  public int getDeviceAddress()
  {
    return deviceAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 10 );
    temp.append( deviceNumber ).append( ", $" ).append( Integer.toHexString( deviceAddress ) );
    return temp.toString();
  }

  /** The device number. */
  private int deviceNumber;

  /** The device address. */
  private int deviceAddress;
}
