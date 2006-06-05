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
  
  public int getDeviceTypeIndex( short[] data )
  {
    return data[ highAddress ] >> 4;
  }
  
  public void setDeviceTypeIndex( short index, short[] data )
  {
      data[ highAddress ] &= 0x0F;
      index <<= 4;
      data[ highAddress ] |= index;
  }
  
  public short getSetupCode( short[] data )
  {
     short setupCode = data[ highAddress ];
     setupCode &= 0x07;
     setupCode <<= 8;
     setupCode |= data[ lowAddress ];
     return setupCode;
  }
  
  public void setSetupCode( short setupCode, short[] data )
  {
    short temp = setupCode;
    temp >>= 8;
    data[ highAddress ] &= 0xF8;
    data[ highAddress ] |= temp;

    setupCode &= 0xFF;
    data[ lowAddress ] = setupCode;
  }

  private String name;
  private int highAddress = 0;
  private int lowAddress = 0;
  private int typeAddress = 0;
}
