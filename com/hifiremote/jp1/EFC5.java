package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class EFC5.
 */
public class EFC5
  extends EFC
{
  
  /**
   * Instantiates a new eF c5.
   * 
   * @param text the text
   */
  public EFC5( String text )
  {
    super(( short )0 );
    value = Integer.parseInt( text ) & 0x0FFFF;
  }

  /**
   * Instantiates a new eF c5.
   * 
   * @param value the value
   */
  public EFC5( int value )
  {
    super(( short )value );
    this.value = value & 0x0FFFF;
  }

  /**
   * Instantiates a new eF c5.
   * 
   * @param hex the hex
   */
  public EFC5( Hex hex )
  {
    super(( short )0 );
    fromHex( hex );
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EFC#getValue()
   */
  public int getValue(){ return value; }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EFC#toString()
   */
  public String toString()
  {
    StringBuilder buff = new StringBuilder( 5 );
    String temp = Integer.toString( value & 0x0FFFF );
    int len = 5 - temp.length();
    for ( int i = 0; i < len; i++ )
      buff.append( '0' );
    buff.append( temp );
    return buff.toString();
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EFC#toHex()
   */
  public Hex toHex()
  {
    Hex rc = new Hex( 2 );
    toHex( rc );
    return rc;
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EFC#toHex(com.hifiremote.jp1.Hex)
   */
  public void toHex( Hex hex )
  {
    toHex( value, hex );
  }
  
  /**
   * To hex.
   * 
   * @param val the val
   * 
   * @return the hex
   */
  public static Hex toHex( int val )
  {
    Hex hex = new Hex( 2 );
    toHex( val, hex );
    return hex;
  }

  /**
   * To hex.
   * 
   * @param val the val
   * @param hex the hex
   */
  public static void toHex( int val, Hex hex )
  {
    short[] data = hex.getData();
    if ( hex.length() == 2 )
    {
      if ( val < 1000 )
      {
        int temp = val & 0xFF;
        EFC.toHex( temp, hex, 0 );
        data[ 1 ] = ( short )temp;
      }
      else
      {
        short byte1 = ( short )( val >> 8 & 0x00FF );
        byte1 += 100;
        byte1 = ( short )(( byte1 << 5 ) | ( byte1 >> 3 ));
        byte1 ^= 0x00D5;
        data[ 0 ] = ( short )( byte1 & 0x00FF );
  
        data[ 1 ] = ( short )(( val & 0x00FF ) ^ 0x00C5 );
      }
    }
    else
    {
      EFC.toHex( val, hex, 0 );
    }
  }

  /* (non-Javadoc)
   * @see com.hifiremote.jp1.EFC#fromHex(com.hifiremote.jp1.Hex)
   */
  public void fromHex( Hex hex )
  {
    value = parseHex( hex ) & 0x0FFFF;
  }
  
  /**
   * Parses the hex.
   * 
   * @param hex the hex
   * 
   * @return the short
   */
  public static short parseHex( Hex hex )
  {
    short[] data = hex.getData();
    if ( data.length == 2 )
    {
//      if ( EFC.parseHex( hex, 0 ) == data[ 1 ] )
//        return data[ 1 ];
      short byte1 = ( short )( data[ 0 ] & 0x00FF );
      byte1 ^= 0x00D5;
      byte1 = ( short )(( byte1 >> 5 ) | ( byte1 << 3 ));
      byte1 = ( short )(( byte1 - 100 ) & 0x00FF );

      short byte2 = ( short )(( data[ 1 ] & 0x00FF ) ^ 0x00C5 );

      short rc = ( short )(( byte1 << 8 ) + byte2 );
      if ( rc < 1000 )
        rc += 65536;
      return rc;
    }
    else
    {
      return EFC.parseHex( hex, 0 );
    }
  }
}
