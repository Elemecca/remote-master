package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class HexInteger.
 */
public class HexInteger
  extends Number
  implements Comparable< HexInteger >
{
  
  /**
   * Instantiates a new hex integer.
   * 
   * @param i the i
   */
  public HexInteger( int i )
  {
    value = new Integer( i );
  }

  /**
   * Instantiates a new hex integer.
   * 
   * @param text the text
   */
  public HexInteger( String text )
  {
    value = Integer.valueOf( text, 16 );
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return Integer.toString( value.intValue(), 16 );
  }

  /* (non-Javadoc)
   * @see java.lang.Number#byteValue()
   */
  public byte   byteValue(){ return value.byteValue(); }
  
  /* (non-Javadoc)
   * @see java.lang.Number#doubleValue()
   */
  public double doubleValue(){ return value.doubleValue(); }
  
  /* (non-Javadoc)
   * @see java.lang.Number#floatValue()
   */
  public float floatValue(){ return value.floatValue(); }
  
  /* (non-Javadoc)
   * @see java.lang.Number#intValue()
   */
  public int intValue(){ return value.intValue(); }
  
  /* (non-Javadoc)
   * @see java.lang.Number#longValue()
   */
  public long longValue(){ return value.longValue(); }
  
  /* (non-Javadoc)
   * @see java.lang.Number#shortValue()
   */
  public short shortValue(){ return value.shortValue(); }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo( HexInteger o )
  {
    return value.compareTo( o.value );
  }

  /** The value. */
  private Integer value = null;
}
