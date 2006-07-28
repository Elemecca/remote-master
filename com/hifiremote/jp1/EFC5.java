package com.hifiremote.jp1;

public class EFC5
  extends EFC
{
  public EFC5( String text )
  {
    super(( short )0 );
    value = Integer.parseInt( text ) & 0x0FFFF;
  }

  public EFC5( int value )
  {
    super(( short )value );
    this.value = value & 0x0FFFF;
  }

  public EFC5( Hex hex )
  {
    super(( short )0 );
    fromHex( hex );
  }

  public int getValue(){ return value; }

  public String toString()
  {
    StringBuffer buff = new StringBuffer( 5 );
    String temp = Integer.toString( value & 0x0FFFF );
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
  
  public static Hex toHex( int val )
  {
    Hex hex = new Hex( 2 );
    toHex( val, hex );
    return hex;
  }

  public static void toHex( int val, Hex hex )
  {
    short[] data = hex.getData();
    if ( hex.length() == 2 )
    {
      short byte1 = ( short )( val >> 8 & 0x00FF );
      byte1 += 100;
      byte1 = ( short )(( byte1 << 5 ) | ( byte1 >> 3 ));
      byte1 ^= 0x00D5;
      data[ 0 ] = ( short )( byte1 & 0x00FF );

      data[ 1 ] = ( short )(( val & 0x00FF ) ^ 0x00C5 );
    }
    else
    {
      EFC.toHex( val, hex, 0 );
    }
  }

  public void fromHex( Hex hex )
  {
    value = parseHex( hex ) & 0x0FFFF;
  }
  
  public static short parseHex( Hex hex )
  {
    short[] data = hex.getData();
    if ( data.length == 2 )
    {
      short byte1 = ( short )( data[ 0 ] & 0x00FF );
      byte1 ^= 0x00D5;
      byte1 = ( short )(( byte1 >> 5 ) | ( byte1 << 3 ));
      byte1 = ( short )(( byte1 - 100 ) & 0x00FF );

      short byte2 = ( short )(( data[ 1 ] & 0x00FF ) ^ 0x00C5 );

      return ( short )(( byte1 << 8 ) + byte2 );
    }
    else
    {
      return EFC.parseHex( hex, 0 );
    }
  }
}
