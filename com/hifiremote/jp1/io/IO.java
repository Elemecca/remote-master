package com.hifiremote.jp1.io;

import java.io.File;

import com.hifiremote.LibraryLoader;

// TODO: Auto-generated Javadoc
/**
 * The Class IO.
 */
public abstract class IO
{
  protected IO( File homeFolder, String libraryName ) throws UnsatisfiedLinkError
  {
    LibraryLoader.loadLibrary( homeFolder, libraryName );
  }

  protected IO( String libraryName ) throws UnsatisfiedLinkError
  {
    LibraryLoader.loadLibrary( libraryName );
  }

  /**
   * Gets the interface name.
   * 
   * @return the interface name
   */
  public abstract String getInterfaceName();

  /**
   * Gets the interface version.
   * 
   * @return the interface version
   */
  public abstract String getInterfaceVersion();
  
  public int getInterfaceType()
  {
    return 0;
  }
  
  public boolean getJP2info( byte[] buffer, int length )
  {
    return false;
  }

  /**
   * Gets the port names.
   * 
   * @return the port names
   */
  public abstract String[] getPortNames();

  /**
   * Open remote.
   * 
   * @param portName
   *          the port name
   * @return the string
   */
  public abstract String openRemote( String portName );

  /**
   * Close remote.
   */
  public abstract void closeRemote();

  /**
   * Gets the remote signature.
   * 
   * @return the remote signature
   */
  public abstract String getRemoteSignature();

  /**
   * Gets the remote eeprom address.
   * 
   * @return the remote eeprom address
   */
  public abstract int getRemoteEepromAddress();

  /**
   * Gets the remote eeprom size.
   * 
   * @return the remote eeprom size
   */
  public abstract int getRemoteEepromSize();

  /**
   * Open remote.
   * 
   * @return the string
   */
  public String openRemote()
  {
    return openRemote( null );
  }

  /**
   * Read remote.
   * 
   * @param address
   *          the address
   * @param sBuffer
   *          the s buffer
   * @return the int
   */
  public int readRemote( int address, short[] sBuffer )
  {
    return readRemote( address, sBuffer, sBuffer.length );
  }

  /**
   * Read remote.
   * 
   * @param address
   *          the address
   * @param sBuffer
   *          the s buffer
   * @param length
   *          the length
   * @return the int
   */
  public int readRemote( int address, short[] sBuffer, int length )
  {
    byte[] buffer = new byte[ length ];
    int len = readRemote( address, buffer, length );
    for ( int i = 0; i < len; ++i )
      sBuffer[ i ] = ( short )( buffer[ i ] & 0xFF );
    return len;
  }

  /**
   * Read remote.
   * 
   * @param address
   *          the address
   * @param buffer
   *          the buffer
   * @return the int
   */
  public int readRemote( int address, byte[] buffer )
  {
    return readRemote( address, buffer, buffer.length );
  }

  /**
   * Read remote.
   * 
   * @param address
   *          the address
   * @param buffer
   *          the buffer
   * @param length
   *          the length
   * @return the int
   */
  public abstract int readRemote( int address, byte[] buffer, int length );

  /**
   * Write remote.
   * 
   * @param address
   *          the address
   * @param sBuffer
   *          the s buffer
   * @return the int
   */
  public int writeRemote( int address, short[] sBuffer )
  {
    return writeRemote( address, sBuffer, sBuffer.length );
  }

  /**
   * Write remote.
   * 
   * @param address
   *          the address
   * @param sBuffer
   *          the s buffer
   * @param length
   *          the length
   * @return the int
   */
  public int writeRemote( int address, short[] sBuffer, int length )
  {
    byte[] buffer = new byte[ length ];
    for ( int i = 0; i < length; ++i )
      buffer[ i ] = ( byte )( sBuffer[ i ] & 0xFF );
    return writeRemote( address, buffer, length );
  }

  /**
   * Write remote.
   * 
   * @param address
   *          the address
   * @param buffer
   *          the buffer
   * @return the int
   */
  public int writeRemote( int address, byte[] buffer )
  {
    return writeRemote( address, buffer, buffer.length );
  }

  /**
   * Write remote.
   * 
   * @param address
   *          the address
   * @param buffer
   *          the buffer
   * @param length
   *          the length
   * @return the int
   */
  public abstract int writeRemote( int address, byte[] buffer, int length );
}
