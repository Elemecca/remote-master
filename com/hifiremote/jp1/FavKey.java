package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class FavKey.
 */
public class FavKey
{
  
  /**
   * Instantiates a new fav key.
   * 
   * @param keyCode the key code
   * @param deviceButtonAddress the device button address
   * @param maxEntries the max entries
   * @param entrySize the entry size
   * @param segregated the segregated
   */
  public FavKey( int keyCode, int deviceButtonAddress, int maxEntries,
                 int entrySize,  boolean segregated )
  {
    this.keyCode = keyCode;
    this.deviceButtonAddress = deviceButtonAddress;
    this.maxEntries = maxEntries;
    this.entrySize = entrySize;
    this.segregated = segregated;
  }

  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public int getKeyCode(){ return keyCode; }
  
  /**
   * Gets the device button address.
   * 
   * @return the device button address
   */
  public int getDeviceButtonAddress(){ return deviceButtonAddress; }
  
  /**
   * Gets the max entries.
   * 
   * @return the max entries
   */
  public int getMaxEntries(){ return maxEntries; }
  
  /**
   * Gets the entry size.
   * 
   * @return the entry size
   */
  public int getEntrySize(){ return entrySize; }
  
  /**
   * Checks if is segregated.
   * 
   * @return true, if is segregated
   */
  public boolean isSegregated(){ return segregated; }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder temp = new StringBuilder( 25 );
    temp.append( '$' )
        .append( Integer.toHexString( keyCode ))
        .append( ", $" )
        .append( Integer.toHexString( deviceButtonAddress ))
        .append( ", " )
        .append( maxEntries )
        .append( ", " )
        .append( entrySize );

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
