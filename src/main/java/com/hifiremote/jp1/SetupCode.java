package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class SetupCode.
 */
public class SetupCode
{

  /** The value. */
  private int value;

  private static int max = 2047;

  public static void setMax( int newMax )
  {
    max = newMax;
  }

  public static int getMax()
  {
    return max;
  }

  /**
   * Instantiates a new setup code.
   * 
   * @param s
   *          the s
   */
  public SetupCode( String s, boolean allowEmpty )
  {
    if ( allowEmpty && s.trim().isEmpty() )
    {
      value = 0xFFFF;
    }
    else
    {
      value = Integer.parseInt( s );
      if ( value < 0 || value > max )
      {
        throw new IllegalArgumentException();
      }
    }
  }

  /**
   * Instantiates a new setup code.
   * 
   * @param value
   *          the value
   */
  public SetupCode( int value )
  {
    this.value = value;
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
   * @param value
   *          the value
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
    buff.append( Integer.toString( value ) );
    return buff.toString();
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
}
