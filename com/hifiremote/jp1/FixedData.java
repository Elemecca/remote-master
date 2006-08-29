package com.hifiremote.jp1;

public class FixedData
{
  public FixedData( int addr, byte[] bytes )
  {
    address = addr;
    data = bytes;
  }

  public int getAddress(){ return address; }
  public byte[] getData() { return data; }

  public String toString()
  {
    StringBuilder temp = new StringBuilder( 200 );
    temp.append( '$' ).append( Integer.toHexString( address ))
        .append( " =" );
    for ( int i = 0; i < data.length ; i++ )
    {
      temp.append( " $" );
      String str = Integer.toHexString( data[ i ]);
      int len = str.length();
      if ( len > 2 )
        str = str.substring( len - 2 );
      if ( len < 2  )
        temp.append( '0' );
      temp.append( str );
    }
    return temp.toString();
  }

  private int address;
  private byte[] data;
}
