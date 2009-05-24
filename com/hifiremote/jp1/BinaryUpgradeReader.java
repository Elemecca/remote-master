package com.hifiremote.jp1;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

// TODO: Auto-generated Javadoc
/**
 * The Class BinaryUpgradeReader.
 */
public class BinaryUpgradeReader
{

  /**
   * Instantiates a new binary upgrade reader.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public BinaryUpgradeReader( File file ) throws IOException
  {
    String name = file.getName();
    int underscore = name.lastIndexOf( '_' );
    int dot = name.lastIndexOf( '.' );
    if ( dot == -1 )
      dot = name.length();
    String tag = null;
    if ( ( underscore != -1 ) && ( dot != -1 ) && ( underscore < dot ) )
    {
      tag = name.substring( underscore + 1, dot );
      List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( "BIN" + tag.toUpperCase() );
      if ( remotes.size() == 0 )
      {
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "The binary file \"" + name
            + "\" isn't a supported binary upgrade file.", "Invalid binary file", JOptionPane.ERROR_MESSAGE );
        return;
      }
      else if ( remotes.size() == 1 )
      {
        remote = remotes.get( 0 );
      }
      else
      {
        Remote[] choices = new Remote[ remotes.size() ];
        choices = remotes.toArray( choices );
        remote = ( Remote )JOptionPane.showInputDialog( RemoteMaster.getFrame(),
            "The selected binary upgrade can be used for multiple remotes.  Please select the desired remote.",
            "Select a remote", JOptionPane.QUESTION_MESSAGE, null, choices, choices[ 0 ] );
        if ( remote == null )
          return;
      }
    }
    EncrypterDecrypter encdec = remote.getEncrypterDecrypter();
    dis = new DataInputStream( new FileInputStream( file ) );
    int upgradeLength = 0;
    if ( tag.equals( "OBJ" ) )
      upgradeLength = ( int )file.length();
    else
      upgradeLength = readUnsignedByte( encdec );

    int protocolOffset = readUnsignedByte( encdec );
    int temp1 = readUnsignedByte( encdec );
    int temp2 = readUnsignedByte( encdec );
    deviceIndex = temp1 >> 4;
    setupCode = ( ( temp1 & 0x07 ) << 8 ) | temp2;
    boolean pidGreaterThanFF = ( ( temp1 & 0x08 ) != 0 );

    int deviceUpgradeLength = upgradeLength - 3;
    int protocolUpradeLength = 0;
    if ( protocolOffset != 0 )
    {
      deviceUpgradeLength = protocolOffset - 3;
      protocolUpradeLength = upgradeLength - protocolOffset;
    }

    short[] upgradeCode = new short[ deviceUpgradeLength ];
    for ( int i = 0; i < deviceUpgradeLength; i++ )
      upgradeCode[ i ] = readUnsignedByte( encdec );
    code = new Hex( upgradeCode );

    short[] temp = new short[ 2 ];
    temp[ 0 ] = ( short )( pidGreaterThanFF ? 1 : 0 );
    temp[ 1 ] = upgradeCode[ 0 ];
    pid = new Hex( temp );

    short[] protocolCode = null;
    if ( protocolOffset != 0 )
    {
      protocolCode = new short[ protocolUpradeLength ];
      for ( int i = 0; i < protocolUpradeLength; i++ )
        protocolCode[ i ] = readUnsignedByte( encdec );
      pCode = new Hex( protocolCode );
    }
  }

  /**
   * Read unsigned byte.
   * 
   * @param encdec
   *          the encdec
   * @return the short
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private short readUnsignedByte( EncrypterDecrypter encdec ) throws IOException
  {
    short val = ( short )dis.readUnsignedByte();
    if ( encdec != null )
      val = encdec.decrypt( val );
    return val;
  }

  /**
   * Gets the remote.
   * 
   * @return the remote
   */
  public Remote getRemote()
  {
    return remote;
  }

  /**
   * Gets the setup code.
   * 
   * @return the setup code
   */
  public int getSetupCode()
  {
    return setupCode;
  }

  /**
   * Gets the device index.
   * 
   * @return the device index
   */
  public int getDeviceIndex()
  {
    return deviceIndex;
  }

  /**
   * Gets the pid.
   * 
   * @return the pid
   */
  public Hex getPid()
  {
    return pid;
  }

  /**
   * Gets the code.
   * 
   * @return the code
   */
  public Hex getCode()
  {
    return code;
  }

  /**
   * Gets the protocol code.
   * 
   * @return the protocol code
   */
  public Hex getProtocolCode()
  {
    return pCode;
  }

  /** The remote. */
  private Remote remote = null;

  /** The dis. */
  private DataInputStream dis = null;

  /** The setup code. */
  private int setupCode = 0;

  /** The device index. */
  private int deviceIndex = 0;

  /** The pid. */
  private Hex pid = null;

  /** The code. */
  private Hex code = null;

  /** The p code. */
  private Hex pCode = null;
}
