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

  protected abstract byte calculateCheckSum( byte[] data, int start, int end );

  public void setCheckSum( byte[] data )
  {
    byte sum = calculateCheckSum( data,
                                  addressRange.getStart(),
                                  addressRange.getEnd() );
    data[ checkSumAddress ] = sum;
    data[ checkSumAddress + 1 ] = (byte)~sum;
  }
  public String toString()
  {
    return "$" + Integer.toHexString( checkSumAddress ) +
           ":" + addressRange;
  }

  private int checkSumAddress;
  private AddressRange addressRange;
}
