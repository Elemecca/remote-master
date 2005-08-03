package com.hifiremote.jp1;

public class EFC5
  extends EFC
{
  public EFC5( String text )
  {
    super( 0 );
    value = Integer.parseInt( text );
  }

  public EFC5( int value )
  {
    super( value );
    this.value = value;
  }

  public EFC5( Hex hex )
  {
    super( 0 );
    fromHex( hex );
  }

  public int getValue(){ return value; }

  public String toString()
  {
    StringBuffer buff = new StringBuffer( 5 );
    String temp = Integer.toString( value & 0x0FFFF);
    int len = 5 - temp.length();
    for ( int i = 0; i < len; i++ )
      buff.append( '0' );
    buff.append( temp );
    return buff.toString();
  }

  public Hex toHex()
  {
    Hex rc = new Hex( 2 );
    toHex( rc );
    return rc;
  }

  public void toHex( Hex hex )
  {
    toHex( value, hex );
  }

  public static void toHex( int val, Hex hex )
  {
    int[] data = hex.getData();
    if ( hex.length() == 2 )
    {
      int byte1 = val >> 8 & 0x00FF;
      byte1 += 100;
      byte1 = ( byte1 << 5 ) | ( byte1 >> 3 );
      byte1 ^= 0x00D5;
      data[ 0 ] = byte1 & 0x00FF;

      data[ 1 ] = ( val & 0x00FF ) ^ 0x00C5;
    }
    else
    {
      EFC.toHex( val, hex, 0 );
    }
  }

  public void fromHex( Hex hex )
  {
    value = parseHex( hex );
  }
  
  public static int parseHex( Hex hex )
  {
    int[] data = hex.getData();
    if ( data.length == 2 )
    {
      int byte1 = data[ 0 ];
      byte1 ^= 0x00D5;
      byte1 = ( byte1 >> 5 ) | ( byte1 << 3 );
      byte1 = ( byte1 - 100 ) & 0x00FF;

      int byte2 = data[ 1 ] ^ 0x00C5;

      return ( byte1 << 8 ) + byte2;
    }
    else
    {
      return EFC.parseHex( hex, 0 );
    }
  }
}
