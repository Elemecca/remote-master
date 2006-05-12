package com.hifiremote.jp1;

public abstract class CheckSum
{
  public int getCheckSumAddress(){ return checkSumAddress; }
  public AddressRange getAddressRange(){ return addressRange; }

  public CheckSum( int sumAddr, AddressRange addrRange )
  {
    checkSumAddress = sumAddr;
    addressRange = addrRange;
  }

  protected abstract short calculateCheckSum( short[] data, int start, int end );

  public void setCheckSum( short[] data )
  {
    short sum = calculateCheckSum( data,
                                   addressRange.getStart(),
                                   addressRange.getEnd());
    data[ checkSumAddress ] = sum;
    data[ checkSumAddress + 1 ] = ( short )( ~sum & 0xFF );
  }
  public String toString()
  {
    return "$" + Integer.toHexString( checkSumAddress ) +
           ":" + addressRange;
  }

  private int checkSumAddress;
  private AddressRange addressRange;
}
