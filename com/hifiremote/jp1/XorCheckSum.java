package com.hifiremote.jp1;

public class XorCheckSum extends CheckSum
{
  public XorCheckSum( int addr, AddressRange range )
  {
    super( addr, range );
  }

  public String toString()
  {
    return "^" + super.toString();
  }

  protected short calculateCheckSum( short[] data, int start, int end )
  {
    short sum = 0;
    for ( int i = start; i <= end; i++ )
    {
      sum ^= data[ i ];
    }
    return sum;
  }
}
