package com.hifiremote.jp1;

public class EFC
  implements Comparable
{
  public EFC( String text )
  {
    value = Integer.parseInt( text ) & 0xFF;
  }

  public EFC( int value )
  {
    this.value = value & 0xFF;
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

  public static int parseHex( Hex hex )
  {
    return parseHex( hex, 0 );
  }

  public static int parseHex( Hex hex, int index )
  {
    int rc = hex.getData()[ index ] & 0xFF;
    rc = ( rc << 3 ) | ( rc >> 5 );
    rc = ( rc ^ 0xAE ) - 156;
    return rc & 0xFF;
  }

  public int getValue(){ return value; }

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
    int temp = val + 156;
    temp = ( temp & 0xFF ) ^ 0xAE;
    temp = ( temp >> 3 ) | ( temp << 5 );
    hex.getData()[ index ] = temp;
  }

  public String toString()
  {
    StringBuffer buff = new StringBuffer( 3 );
    String temp = Integer.toString( value );
    if ( value < 100 )
      buff.append( '0' );
    if ( value < 10 )
      buff.append( '0' );
    buff.append( Integer.toString( value ));
    return buff.toString();
  }

  public int compareTo( Object o )
  {
    int other = (( EFC )o ).value;
    if ( value < other )
      return -1;
    else if ( value == other )
      return 0;
    else
      return 1;
  }

  protected int value = 0;
}
