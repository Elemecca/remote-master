package com.hifiremote.jp1.translate;

import com.hifiremote.jp1.DeviceParameter;
import com.hifiremote.jp1.Hex;
import com.hifiremote.jp1.Value;

// TODO: Auto-generated Javadoc
/**
 * The Class Translate.
 */
public abstract class Translate
{

  /**
   * Instantiates a new translate.
   * 
   * @param textParms
   *          the text parms
   */
  public Translate( String[] textParms )
  {}

  /**
   * In.
   * 
   * @param parms
   *          the parms
   * @param hex
   *          the hex
   * @param devParms
   *          the dev parms
   * @param onlyIndex
   *          the only index
   */
  public abstract void in( Value[] parms, Hex hex, DeviceParameter[] devParms, int onlyIndex );

  /**
   * Out.
   * 
   * @param hex
   *          the hex
   * @param parms
   *          the parms
   * @param devParms
   *          the dev parms
   */
  public abstract void out( Hex hex, Value[] parms, DeviceParameter[] devParms );

  /**
   * Reverse.
   * 
   * @param b
   *          the b
   * @return the int
   */
  public static int reverse( int b )
  {
    return reverse( b, 8 );
  }

  /**
   * Reverse.
   * 
   * @param v
   *          the v
   * @param bits
   *          the bits
   * @return the int
   */
  public static int reverse( int v, int bits )
  {
    int rc;
    rc = v >> 1 & 0x55555555 | ( v & 0x55555555 ) << 1;
    rc = rc >> 2 & 0x33333333 | ( rc & 0x33333333 ) << 2;
    rc = rc >> 4 & 0x0F0F0F0F | ( rc & 0x0F0F0F0F ) << 4;
    rc = rc >> 8 & 0x00FF00FF | ( rc & 0x00FF00FF ) << 8;
    rc = rc >>> 16 | rc << 16;
    return rc >>> 32 - bits;
  }

  /**
   * Complement.
   * 
   * @param b
   *          the b
   * @return the int
   */
  public static int complement( int b )
  {
    return complement( b, 8 );
  }

  /**
   * Complement.
   * 
   * @param v
   *          the v
   * @param bits
   *          the bits
   * @return the int
   */
  public static int complement( int v, int bits )
  {
    return ( 2 << bits - 1 ) - 1 - v;
  }

  /**
   * Byte2int.
   * 
   * @param b
   *          the b
   * @return the int
   */
  public static int byte2int( int b )
  {
    return b & 0xFF;
  }

  // insert a field of up to 32 bits crossing up to 9 bytes
  /**
   * Insert.
   * 
   * @param hexData
   *          the hex data
   * @param msbOffset
   *          the msb offset
   * @param bits
   *          the bits
   * @param v
   *          the v
   */
  public static void insert( Hex hexData, int msbOffset, int bits, int v )
  {
    short[] hex = hexData.getData();
    int lastOffset = msbOffset + bits - 1;
    int by = lastOffset / 8; // byte position of lowest bit
    if ( by >= hex.length )
    {
      System.err.println( "insert(offset=" + msbOffset + ", bits=" + bits + ") exceeds " + hex.length + " byte buffer" );
      return;
    }
    int bi = 7 - lastOffset % 8; // lsb position of lowest bit
    int mask = ( 2 << bits - 1 ) - 1; // Works for bits = 1 to 32
    while ( mask != 0 )
    {
      int mask2 = mask << bi;
      hex[ by ] = ( short )( ( hex[ by ] & ~mask2 | v << bi & mask2 ) & 0xFF );
      mask = mask >>> 8 - bi;
      v = v >> 8 - bi;
      bi = 0;
      --by;
    }
  }

  // insert a field of up to 32 bits into a single Value object
  /**
   * Insert.
   * 
   * @param data
   *          the data
   * @param lsbOffset
   *          the lsb offset
   * @param bits
   *          the bits
   * @param v
   *          the v
   * @return the value
   */
  public static Value insert( Value data, int lsbOffset, int bits, int v )
  {
    int mask = ( 1 << bits ) - 1 << lsbOffset;
    int old = 0;
    if ( data != null )
    {
      old = ( ( Integer )data.getValue() ).intValue() & -1 - mask;
    }
    return new Value( new Integer( old + ( v << lsbOffset & mask ) ), null );
  }

  // extract a field of up to 32 bits crossing up to 9 bytes
  /**
   * Extract.
   * 
   * @param hexData
   *          the hex data
   * @param msbOffset
   *          the msb offset
   * @param bits
   *          the bits
   * @return the int
   */
  public static int extract( Hex hexData, int msbOffset, int bits )
  {
    short[] hex = hexData.getData();
    if ( msbOffset + bits > 8 * hex.length )
    {
      System.err.println( "extract(offset=" + msbOffset + ", bits=" + bits + ") exceeds " + hex.length + " int buffer" );
      return 0;
    }
    int v = 0;
    int by = msbOffset / 8;
    int bi = msbOffset % 8;
    int mask = ( 0x100 >> bi ) - 1;
    bits += bi;
    while ( bits > 8 )
    {
      v = ( v << 8 ) + ( hex[ by ] & mask );
      mask = 0xFF;
      by++ ;
      bits -= 8;
    }
    return ( v << bits ) + ( ( hex[ by ] & mask ) >> 8 - bits );
  }

}
