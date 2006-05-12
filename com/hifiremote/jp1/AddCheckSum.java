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

  public short calculateCheckSum( short[] data, int start, int end )
  {
    short sum = 0;
    for ( int i = start; i <= end; i++ )
    {
      sum += data[ i ];
    }
    return ( short )( sum & 0xFF );
  }
}
