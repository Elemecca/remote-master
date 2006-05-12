package com.hifiremote.jp1;

public class DeviceButton
{
  public DeviceButton( String name, int hiAddr,
                       int lowAddr, int typeAddr )
  {
    this.name = name;
    highAddress = hiAddr;
    lowAddress = lowAddr;
    typeAddress = typeAddr;
  }

  public String getName(){ return name; }
  public int getHighAddress(){ return highAddress; }
  public int getLowAddress(){ return lowAddress; }
  public int getTypeAddress(){ return typeAddress; }

  public String toString()
  {
    return name;
  }

  private String name;
  private int highAddress = 0;
  private int lowAddress = 0;
  private int typeAddress = 0;
}
