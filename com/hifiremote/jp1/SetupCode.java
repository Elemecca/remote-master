package com.hifiremote.jp1;

public class SetupCode
{
  private int value;

  public SetupCode( String s )
  {
    this( Integer.parseInt( s ));
  }

  public SetupCode( int value )
  {
    if (( value < 0 ) || ( value > 2047 ))
      throw new NumberFormatException( "Value must be between 0 and 2047" );
    this.value = value;
  }

  public String toString()
  {
    return toString( value );
  }

  public static String toString( int value )
  {
    StringBuilder buff = new StringBuilder();
    if ( value < 1000 )
      buff.append( '0' );
    if ( value < 100 )
      buff.append( '0' );
    if ( value < 10 )
      buff.append( '0' );
    buff.append( Integer.toString( value ));
    return buff.toString();
  }

  public int getValue(){ return value; }
}
