package com.hifiremote.jp1;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * The Class FavKey.
 */
public class FavKey extends RDFParameter
{
  public void parse( String text, Remote remote ) throws Exception
  {
    StringTokenizer st = new StringTokenizer( text, "=, \t" );
    keyCode = RDFReader.parseNumber( st.nextToken() );
    deviceButtonAddress = RDFReader.parseNumber( st.nextToken() );
    maxEntries = RDFReader.parseNumber( st.nextToken() );
    entrySize = RDFReader.parseNumber( st.nextToken() );
    if ( st.hasMoreTokens() )
    {
      segregated = RDFReader.parseNumber( st.nextToken() ) != 0;
      if ( segregated )
      {
        AddressRange favScanAddr = new AddressRange();
        favScanAddr.setStart( deviceButtonAddress - 1 );
        favScanAddr.setEnd( deviceButtonAddress + ( maxEntries * entrySize ) );
        remote.setFavScanAddress( favScanAddr );
      }
    }  
  }

  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public int getKeyCode()
  {
    return keyCode;
  }

  /**
   * Gets the device button address.
   * 
   * @return the device button address
   */
  public int getDeviceButtonAddress()
  {
    return deviceButtonAddress;
  }

  /**
   * Gets the max entries.
   * 
   * @return the max entries
   */
  public int getMaxEntries()
  {
    return maxEntries;
  }

  /**
   * Gets the entry size.
   * 
   * @return the entry size
   */
  public int getEntrySize()
  {
    return entrySize;
  }

  /**
   * Checks if is segregated.
   * 
   * @return true, if is segregated
   */
  public boolean isSegregated()
  {
    return segregated;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 25 );
    temp.append( '$' ).append( Integer.toHexString( keyCode ) ).append( ", $" ).append(
        Integer.toHexString( deviceButtonAddress ) ).append( ", " ).append( maxEntries ).append( ", " ).append(
        entrySize );

    if ( segregated )
      temp.append( ", 1" );

    return temp.toString();

  }

  /** The key code. */
  private int keyCode;

  /** The device button address. */
  private int deviceButtonAddress;

  /** The max entries. */
  private int maxEntries;

  /** The entry size. */
  private int entrySize;

  /** The segregated. */
  private boolean segregated;
}
