package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class StatusBit.
 */
public class StatusBit
{
  
  /**
   * Instantiates a new status bit.
   * 
   * @param addr the addr
   * @param bit the bit
   * @param onValue the on value
   */
  public StatusBit( int addr, int bit, int onValue )
  {
    this.address = addr;
    this.bit = bit;
    this.onValue = onValue;
  }

  /**
   * Gets the address.
   * 
   * @return the address
   */
  public int getAddress(){ return address; }
  
  /**
   * Gets the bit.
   * 
   * @return the bit
   */
  public int getBit(){ return bit; }
  
  /**
   * Gets the on value.
   * 
   * @return the on value
   */
  public int getOnValue(){ return onValue; }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 20 );
    temp.append( '$' ).append( Integer.toHexString( address ))
        .append( '.' ).append( bit );
    if ( onValue == 0 )
      temp.append( ".0" );
    return temp.toString();
  }

  /** The address. */
  private int address;
  
  /** The bit. */
  private int bit;
  
  /** The on value. */
  private int onValue;
}
