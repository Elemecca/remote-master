package com.hifiremote.jp1;

public class AddCheckSum extends CheckSum
{
  public AddCheckSum( int addr, AddressRange range )
  {
    super( addr, range );
  }

  public String toString()
  {
    return "+" + super.toString();
  }

  public byte calculateCheckSum( byte[] data, int start, int end )
  {
    byte sum = 0;
    for ( int i = start; i <= end; i++ )
    {
      sum += data[ i ];
    }
    return sum;
  }
}
