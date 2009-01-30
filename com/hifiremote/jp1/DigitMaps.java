package com.hifiremote.jp1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class DigitMaps.
 */
public class DigitMaps
{

  /**
   * Load.
   * 
   * @param file
   *          the file
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static void load( File file ) throws IOException
  {
    data = new short[ ( int ) file.length() ];
    BufferedInputStream in = new BufferedInputStream( new FileInputStream( file ) );
    for ( int i = 0; i < data.length; ++i )
      data[ i ] = ( short ) ( in.read() & 0xFF );
    in.close();
  }

  /**
   * Find digit map index.
   * 
   * @param digitMaps
   *          the digit maps
   * @param digitKeyCodes
   *          the digit key codes
   * 
   * @return the short
   */
  public static short findDigitMapIndex( short[] digitMaps, short[] digitKeyCodes )
  {
    for ( int i = 0; i < digitMaps.length; ++i )
    {
      int mapNum = digitMaps[ i ];
      if ( matches( mapNum, digitKeyCodes ) )
        return ( short ) ( i + 1 );
    }
    return ( short ) -1;
  }

  /**
   * Matches.
   * 
   * @param mapNumber
   *          the map number
   * @param digitKeyCodes
   *          the digit key codes
   * 
   * @return true, if successful
   */
  private static boolean matches( int mapNumber, short[] digitKeyCodes )
  {
    int offset = 10 * mapNumber;
    for ( int i = 0; i < digitKeyCodes.length; ++i, ++offset )
    {
      if ( ( offset >= data.length )
          || ( ( data[ offset ] & 0xFF ) != ( digitKeyCodes[ i ] & 0xFF ) ) )
        return false;
    }
    return true;
  }

  /**
   * Gets the hex cmds.
   * 
   * @param mapNumber
   *          the map number
   * @param cmdLength
   *          the cmd length
   * 
   * @return the hex cmds
   */
  public static Hex[] getHexCmds( int mapNumber, int cmdLength )
  {
    int offset = mapNumber * 10;
    Hex[] rc = new Hex[ 10 ];
    for ( int i = 0; i < rc.length; ++i, offset += cmdLength )
      rc[ i ] = Hex.subHex( data, offset, cmdLength );
    return rc;
  }

  /** The data. */
  public static short[] data = null;
}
