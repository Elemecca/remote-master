/**
 * 
 */
package com.hifiremote.jp1;

/**
 * @author Greg
 * 
 */
public class BigEndianProcessor extends Processor
{
  /**
   * @param name
   *          name of the processor
   */
  public BigEndianProcessor( String name )
  {
    super( name );
  }

  /**
   * @param name
   *          name of the processor
   * @param version
   *          version of the processor
   */
  public BigEndianProcessor( String name, String version )
  {
    super( name, version );
  }

  public int getInt( short[] data, int offset )
  {
    return (((data[offset] & 0xFF) << 8) + (data[offset + 1] & 0xFF)) & 0xFFFF;
  }

  public void putInt( int val, short[] data, int offset )
  {
    data[offset] = (short) (val >> 8);
    data[offset + 1] = (short) (val & 0xFF);
  }
}
