package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class AddCheckSum.
 */
public class AddCheckSum extends CheckSum
{

  /**
   * Instantiates a new adds the check sum.
   * 
   * @param addr
   *          the addr
   * @param range
   *          the range
   */
  public AddCheckSum( int addr, AddressRange range, boolean comp )
  {
    super( addr, range, comp );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.CheckSum#toString()
   */
  public String toString()
  {
    return "+" + super.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.CheckSum#calculateCheckSum(short[], int, int)
   */
  public short calculateCheckSum( short[] data, int start, int end )
  {
    short sum = 0;
    for ( int i = start; i <= end; i++ )
    {
      sum += ( data[ i ] & 0xFF );
    }
    return ( short )( sum & 0xFF );
  }
}
