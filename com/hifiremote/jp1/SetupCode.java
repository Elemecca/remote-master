package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class SetupCode.
 */
public class SetupCode
{
  
  /** The value. */
  private int value;

  /**
   * Instantiates a new setup code.
   * 
   * @param s the s
   */
  public SetupCode( String s )
  {
    this( Integer.parseInt( s ));
  }

  /**
   * Instantiates a new setup code.
   * 
   * @param value the value
   */
  public SetupCode( int value )
  {
    if (( value < 0 ) || ( value > 2047 ))
      throw new NumberFormatException( "Value must be between 0 and 2047" );
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
   * @param value the value
   * 
   * @return the string
   */
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

  /**
   * Gets the value.
   * 
   * @return the value
   */
  public int getValue(){ return value; }
}
