package com.hifiremote.jp1;

public class FavKey
{
  public FavKey( int keyCode, int deviceButtonAddress, int maxEntries,
                 int entrySize,  boolean segregated )
  {
    this.keyCode = keyCode;
    this.deviceButtonAddress = deviceButtonAddress;
    this.maxEntries = maxEntries;
    this.entrySize = entrySize;
    this.segregated = segregated;
  }

  public int getKeyCode(){ return keyCode; }
  public int getDeviceButtonAddress(){ return deviceButtonAddress; }
  public int getMaxEntries(){ return maxEntries; }
  public int getEntrySize(){ return entrySize; }
  public boolean isSegregated(){ return segregated; }

  public String toString()
  {
    StringBuffer temp = new StringBuffer( 25 );
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

  private int keyCode;
  private int deviceButtonAddress;
  private int maxEntries;
  private int entrySize;
  private boolean segregated;
}
