/**
 * 
 */
package com.hifiremote.jp1;

/**
 * @author Greg
 */
public class HexReader
{
  int offset = 0;
  int end = 0;
  short[] data = null;

  public HexReader( short[] data, AddressRange range )
  {
    this.data = data;
    offset = range.getStart();
    end = range.getEnd();
  }

  public short peek()
  {
    if ( offset > end )
    {
      throw new ArrayIndexOutOfBoundsException( offset );
    }
    return ( short )( data[ offset ] & 0xFF );
  }

  public short read()
  {
    short rc = peek();
    offset++ ;
    return rc;
  }

  public int available()
  {
    return end - offset + 1;
  }

  public short[] read( int length )
  {
    short[] rc = new short[ length ];
    for ( int i = 0; i < length; ++i )
    {
      rc[ i ] = read();
    }
    return rc;
  }
}
