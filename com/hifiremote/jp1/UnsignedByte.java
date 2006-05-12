package com.hifiremote.jp1;

public class UnsignedByte
{
  private short value;
  
  private static void check( short val )
  {
    if (( val < 0 ) || ( val > 255 ))
      throw new NumberFormatException( "Value must be between 0 and FF" );
  }

  public UnsignedByte( short value )
  {
    check( value );
    this.value = value;
  }

  public UnsignedByte( String text )
  {
    value = parseUnsignedByte( text );
  } 

  public short getValue(){ return value; }
  public void setValue( short value )
  {
    check( value );
    this.value = value;
  }

  public String toString()
  {
    return toString( value );
  }

  public static String toString( short val )
  {
    return ( Integer.toHexString( 0x100 | val ).substring( 1 ).toUpperCase());  
  }

  public static short parseUnsignedByte( String text )
  {
    short rc = Short.parseShort( text, 16 );
    check( rc );
    return rc;
  }
}
