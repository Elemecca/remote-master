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

  protected byte calculateCheckSum( byte[] data, int start, int end )
  {
    byte sum = 0;
    for ( int i = start; i <= end; i++ )
    {
      sum = ( byte )( sum ^ data[ i ]);
    }
    return sum;
  }
}
