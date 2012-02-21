package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class EFC5.
 */
public class EFC5 extends EFC
{

  /**
   * Instantiates a new eF c5.
   * 
   * @param text
   *          the text
   */
  public EFC5( String text )
  {
    super( ( short )0 );
    value = Integer.parseInt( text ) & 0x1FFFF;
  }

  /**
   * Instantiates a new eF c5.
   * 
   * @param value
   *          the value
   */
  public EFC5( int value )
  {
    super( ( short )value );
    this.value = value & 0x1FFFF;
  }

  /**
   * Instantiates a new eF c5.
   * 
   * @param hex
   *          the hex
   */
  public EFC5( Hex hex )
  {
    this( hex, 0 );
  }

  public EFC5( Hex hex, int offset )
  {
    super( ( short )0 );
    value = parseHex( hex, offset );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.EFC#getValue()
   */
  @Override
  public int getValue()
  {
    return value & 0x1FFFF;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.EFC#toString()
   */
  @Override
  public String toString()
  {
    return String.format( "%1$05d", value & 0x1FFFF );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.EFC#toHex(com.hifiremote.jp1.Hex)
   */
  public Hex toHex( Hex hex )
  {
    return toHex( value, hex );
  }

  /**
   * To hex.
   * 
   * @param val
   *          the val
   * @return the hex
   */
  public static Hex toHex( int val )
  {
    return toHex( val, null );
  }

  /**
   * To hex.
   * 
   * @param val
   *          the val
   * @param hex
   *          the hex
   */
  public static Hex toHex( int val, Hex hex )
  {
    short[] data = null;
    if ( hex != null )
    {
      data = hex.getData();
    }

    if ( val < 1000 )
    {
      if ( hex == null )
      {
        hex = new Hex( 1 );
        data = hex.getData();
      }
      int temp = val & 0xFF;
      EFC.toHex( temp, hex, 0 );
      if ( data.length > 1 )
      {
        data[ 1 ] = ( short )temp;
      }
    }
    else
    {
      if ( hex == null )
      {
        hex = new Hex( 2 );
        data = hex.getData();
      }
      short byte1 = ( short )( val >> 8 & 0x00FF );
      byte1 += 100;
      byte1 &= 0xFF;
      byte1 = ( short )( byte1 << 5 | byte1 >> 3 );
      byte1 ^= 0x00D5;
      data[ 0 ] = ( short )( byte1 & 0x00FF );

      data[ 1 ] = ( short )( val & 0x00FF ^ 0x00C5 );
    }
    return hex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.EFC#fromHex(com.hifiremote.jp1.Hex)
   */
  @Override
  public void fromHex( Hex hex )
  {
    value = parseHex( hex ) & 0x1FFFF;
  }

  /**
   * Parses the hex.
   * 
   * @param hex
   *          the hex
   * @return the short
   */
  public static int parseHex( Hex hex )
  {
    return parseHex( hex, 0 );
  }

  public static int parseHex( Hex hex, int offset )
  {
    short[] data = hex.getData();
    if ( data.length == offset + 2 )
    {
      short byte1 = ( short )( data[ offset ] & 0xFF );
      byte1 ^= 0x00D5;
      byte1 = ( short )( ( byte1 >> 5 | byte1 << 3 ) & 0xFF );
      byte1 = ( short )( byte1 - 100 & 0xFF );

      short byte2 = ( short )( data[ offset + 1 ] & 0xFF ^ 0xC5 );

      int rc = ( byte1 << 8 ) + byte2;
      if ( rc < 1000 )
      {
        rc += 65536;
      }
      return rc;
    }
    else
    {
      return EFC.parseHex( hex, offset );
    }
  }
}
