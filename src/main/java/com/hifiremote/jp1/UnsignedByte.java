package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class UnsignedByte.
 */
public class UnsignedByte
{
  
  /** The value. */
  private short value;
  
  /**
   * Check.
   * 
   * @param val the val
   */
  private static void check( short val )
  {
    if (( val < 0 ) || ( val > 255 ))
      throw new NumberFormatException( "Value must be between 0 and FF" );
  }

  /**
   * Instantiates a new unsigned byte.
   * 
   * @param value the value
   */
  public UnsignedByte( short value )
  {
    check( value );
    this.value = value;
  }

  /**
   * Instantiates a new unsigned byte.
   * 
   * @param text the text
   */
  public UnsignedByte( String text )
  {
    value = parseUnsignedByte( text );
  } 

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public short getValue(){ return value; }
  
  /**
   * Sets the value.
   * 
   * @param value the new value
   */
  public void setValue( short value )
  {
    check( value );
    this.value = value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return toString( value );
  }

  /**
   * To string.
   * 
   * @param val the val
   * 
   * @return the string
   */
  public static String toString( short val )
  {
    return ( Integer.toHexString( 0x100 | val ).substring( 1 ).toUpperCase());  
  }

  /**
   * Parses the unsigned byte.
   * 
   * @param text the text
   * 
   * @return the short
   */
  public static short parseUnsignedByte( String text )
  {
    short rc = Short.parseShort( text, 16 );
    check( rc );
    return rc;
  }
}
