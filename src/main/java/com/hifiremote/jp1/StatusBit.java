package com.hifiremote.jp1;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class StatusBit.
 */
public class StatusBit extends RDFParameter
{
  public void parse( String text, Remote remote ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( ".=" );
    address = RDFReader.parseNumber( st.nextToken( ".=" ) );
    bit = RDFReader.parseNumber( st.nextToken() );
    if ( st.hasMoreTokens() )
      onValue = RDFReader.parseNumber( st.nextToken() );
  }

  /**
   * Gets the address.
   * 
   * @return the address
   */
  public int getAddress()
  {
    return address;
  }

  /**
   * Gets the bit.
   * 
   * @return the bit
   */
  public int getBit()
  {
    return bit;
  }

  /**
   * Gets the on value.
   * 
   * @return the on value
   */
  public int getOnValue()
  {
    return onValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 20 );
    temp.append( '$' ).append( Integer.toHexString( address ) ).append( '.' ).append( bit );
    if ( onValue == 0 )
      temp.append( ".0" );
    return temp.toString();
  }

  /** The address. */
  private int address = 0;

  /** The bit. */
  private int bit = 0;

  /** The on value. */
  private int onValue = 1;
}
