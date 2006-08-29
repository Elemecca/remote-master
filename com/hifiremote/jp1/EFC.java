package com.hifiremote.jp1;

public class EFC
  implements Comparable< EFC >
{
  public EFC( String text )
  {
    value = Short.parseShort( text ) & 0xFF;
  }

  public EFC( short value )
  {
    this.value = ( value & 0xFF );
  }

  public EFC( Hex hex, int index )
  {
    value = parseHex( hex, index );
  }

  public EFC( Hex hex )
  {
    this( hex, 0 );
  }

  public void fromHex( Hex hex )
  {
    fromHex( hex, 0 );
  }

  public void fromHex( Hex hex, int index )
  {
    value = parseHex( hex, index );
  }

  public static short parseHex( Hex hex )
  {
    return parseHex( hex, 0 );
  }

  public static short parseHex( Hex hex, int index )
  {
    short rc = ( short )( hex.getData()[ index ] & 0xFF );
    rc = ( short )(( rc << 3 ) | ( rc >> 5 ));
    rc = ( short )(( rc ^ 0xAE ) - 156 );
    return ( short )( rc & 0xFF );
  }

  public int getValue(){ return value; }
  
  public Hex toHex()
  {
    Hex hex = new Hex( 1 );
    toHex( hex );
    return hex;
  }

  public void toHex( Hex hex )
  {
    toHex( hex, 0 );
  }

  public void toHex( Hex hex, int index )
  {
    toHex( value, hex, index );
  }

  public static void toHex( int val, Hex hex )
  {
    toHex( val, hex, 0 );
  }

  public static void toHex( int val, Hex hex, int index )
  {
    short temp = ( short )( val + 156 );
    temp = ( short )(( temp & 0xFF ) ^ 0xAE );
    temp = ( short )(( temp >> 3 ) | ( temp << 5 ));
    hex.getData()[ index ] = temp;
  }
  
  public static Hex toHex( int val )
  {
    Hex hex = new Hex( 1 );
    toHex( val, hex );
    return hex;
  }

  public String toString()
  {
    return toString( value );
  }
  
  public static String toString( int efc )
  {
    StringBuilder buff = new StringBuilder( 3 );
    if ( efc < 100 )
      buff.append( '0' );
    if ( efc < 10 )
      buff.append( '0' );
    buff.append( Integer.toString( efc ));
    return buff.toString();
  }

  public int compareTo( EFC efc )
  {
    int other = efc.value;
    if ( value < other )
      return -1;
    else if ( value == other )
      return 0;
    else
      return 1;
  }

  protected int value = 0;
}
