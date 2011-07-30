/**
 * 
 */
package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class LittleEndianProcessor.
 * 
 * @author Greg
 */
public class LittleEndianProcessor extends Processor
{

  /**
   * The Constructor.
   * 
   * @param name
   *          name of the remote
   */
  public LittleEndianProcessor( String name )
  {
    super( name );
    setRAMAddress( 0x0132 );
  }

  /**
   * The Constructor.
   * 
   * @param name
   *          name of the remote
   * @param version
   *          version of the remote
   */
  public LittleEndianProcessor( String name, String version )
  {
    super( name, version );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Processor#getInt(short[], int)
   */
  public int getInt( short[] data, int offset )
  {
    return ( ( ( data[ offset + 1 ] & 0xFF ) << 8 ) + ( data[ offset ] & 0xFF ) ) & 0xFFFF;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.Processor#putInt(int, short[], int)
   */
  public void putInt( int val, short[] data, int offset )
  {
    data[ offset ] = ( short )( val & 0xFF );
    data[ offset + 1 ] = ( short )( ( val >> 8 ) & 0xFF );
  }
}
