/**
 * 
 */
package com.hifiremote.jp1;

/**
 * @author Greg
 * 
 */
public class LittleEndianProcessor extends Processor
{
  /**
   * @param name
   *          name of the remote
   */
  public LittleEndianProcessor( String name )
  {
    super(name);
  }

  /**
   * @param name
   *          name of the remote
   * @param version
   *          version of the remote
   */
  public LittleEndianProcessor( String name, String version )
  {
    super(name, version);
  }

  public int getInt( short[] data, int offset )
  {
    return ((( data[ offset + 1 ] & 0xFF ) << 8 ) + ( data[ offset ] & 0xFF )) & 0xFFFF;
  }

  public void putInt( int val, short[] data, int offset )
  {
    data[offset] = (short) (val & 0xFF);
    data[offset + 1] = (short) (val >> 8);
  }
}
