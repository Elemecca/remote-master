package com.hifiremote.jp1;

public class OEMDevice
{
  public OEMDevice( int deviceNumber, int deviceAddress )
  {
    this.deviceNumber = deviceNumber;
    this.deviceAddress = deviceAddress;
  }

  public int getDeviceNumber(){ return deviceNumber; }
  public int getDeviceAddress(){ return deviceAddress; }
  public String toString()
  {
    StringBuffer temp = new StringBuffer( 10 );
    temp.append( deviceNumber )
        .append( ", $" )
        .append( Integer.toHexString( deviceAddress ));
    return temp.toString();
  }

  private int deviceNumber;
  private int deviceAddress;
}
