package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class EFC.
 */
public class EFC implements Comparable< EFC >
{

  /**
   * Instantiates a new eFC.
   * 
   * @param text
   *          the text
   */
  public EFC( String text )
  {
    value = Short.parseShort( text ) & 0xFF;
  }

  /**
   * Instantiates a new eFC.
   * 
   * @param value
   *          the value
   */
  public EFC( short value )
  {
    this.value = value & 0xFF;
  }

  /**
   * Instantiates a new eFC.
   * 
   * @param hex
   *          the hex
   * @param index
   *          the index
   */
  public EFC( Hex hex, int index )
  {
    value = parseHex( hex, index );
  }

  /**
   * Instantiates a new eFC.
   * 
   * @param hex
   *          the hex
   */
  public EFC( Hex hex )
  {
    this( hex, 0 );
  }

  /**
   * From hex.
   * 
   * @param hex
   *          the hex
   */
  public void fromHex( Hex hex )
  {
    fromHex( hex, 0 );
  }

  /**
   * From hex.
   * 
   * @param hex
   *          the hex
   * @param index
   *          the index
   */
  public void fromHex( Hex hex, int index )
  {
    value = parseHex( hex.getData()[ index ] );
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

  /**
   * Parses the hex.
   * 
   * @param hex
   *          the hex
   * @param index
   *          the index
   * @return the short
   */
  public static int parseHex( Hex hex, int index )
  {
    return parseHex( hex.getData()[ index ] );
  }

  public static int parseHex( short hex )
  {
    short rc = ( short )( hex & 0xFF );
    rc = ( short )( rc << 3 | rc >> 5 );
    rc = ( short )( ( rc ^ 0xAE ) - 156 );
    return ( short )( rc & 0xFF );
  }

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public int getValue()
  {
    return value;
  }

  /**
   * To hex.
   * 
   * @param hex
   *          the hex
   * @param index
   *          the index
   */
  public Hex toHex( Hex hex, int index )
  {
    return toHex( value, hex, index );
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
    return toHex( val, hex, 0 );
  }

  /**
   * To hex.
   * 
   * @param val
   *          the val
   * @param hex
   *          the hex
   * @param index
   *          the index
   */
  public static Hex toHex( int val, Hex hex, int index )
  {
    if ( hex == null )
    {
      hex = new Hex( index + 1 );
    }
    short temp = ( short )( val + 156 );
    temp = ( short )( temp & 0xFF ^ 0xAE );
    temp = ( short )( temp >> 3 | temp << 5 );
    hex.getData()[ index ] = ( short )( temp & 0xFF );
    return hex;
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
    Hex hex = new Hex( 1 );
    toHex( val, hex );
    return hex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return toString( value );
  }

  /**
   * To string.
   * 
   * @param efc
   *          the efc
   * @return the string
   */
  public static String toString( int efc )
  {
    StringBuilder buff = new StringBuilder( 3 );
    if ( efc < 100 )
    {
      buff.append( '0' );
    }
    if ( efc < 10 )
    {
      buff.append( '0' );
    }
    buff.append( Integer.toString( efc ) );
    return buff.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo( EFC efc )
  {
    int other = efc.value;
    if ( value < other )
    {
      return -1;
    }
    else if ( value == other )
    {
      return 0;
    }
    else
    {
      return 1;
    }
  }

  /** The value. */
  protected int value = 0;
}
