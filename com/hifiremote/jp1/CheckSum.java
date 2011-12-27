package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class CheckSum.
 */
public abstract class CheckSum
{

  /**
   * Gets the check sum address.
   * 
   * @return the check sum address
   */
  public int getCheckSumAddress()
  {
    return checkSumAddress;
  }

  /**
   * Gets the address range.
   * 
   * @return the address range
   */
  public AddressRange getAddressRange()
  {
    return addressRange;
  }

  /**
   * Instantiates a new check sum.
   * 
   * @param sumAddr
   *          the sum addr
   * @param addrRange
   *          the addr range
   */
  public CheckSum( int sumAddr, AddressRange addrRange, boolean comp )
  {
    checkSumAddress = sumAddr;
    addressRange = addrRange;
    complement = comp;
  }

  /**
   * Calculate check sum.
   * 
   * @param data
   *          the data
   * @param start
   *          the start
   * @param end
   *          the end
   * @return the short
   */
  protected abstract short calculateCheckSum( short[] data, int start, int end );

  /**
   * Sets the check sum.
   * 
   * @param data
   *          the new check sum
   */
  public void setCheckSum( short[] data )
  {
    short sum = calculateCheckSum( data, addressRange.getStart(), addressRange.getEnd() );
    if ( complement )
    {
      sum = ( short )( ~sum & 0xFF );
    }
    data[ checkSumAddress ] = sum;
    data[ checkSumAddress + 1 ] = ( short )( ~sum & 0xFF );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return "$" + Integer.toHexString( checkSumAddress ) + ":" + addressRange;
  }

  /** The check sum address. */
  private int checkSumAddress;

  /** The address range. */
  private AddressRange addressRange;
  
  private boolean complement = false;
}
