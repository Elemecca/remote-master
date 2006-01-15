package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class BinaryUpgradeReader
{
  public BinaryUpgradeReader( File file )
    throws IOException
  {
    String name = file.getName();
    int underscore = name.lastIndexOf( '_' );
    int dot = name.lastIndexOf( '.' );
    if ( dot == -1 )
      dot = name.length();
    String tag = null;
    if (( underscore != -1 ) && ( dot != -1 ) && ( underscore < dot ))
    {
      tag = name.substring( underscore + 1, dot );
      Vector remotes = RemoteManager.getRemoteManager().findRemoteBySignature( "BIN" + tag.toUpperCase() );
      if ( remotes.size() == 0 )
      {
        JOptionPane.showMessageDialog( KeyMapMaster.getKeyMapMaster(),
                                       "The binary file \"" + name + "\" isn't a supported binary upgrade file.",
                                       "Invalid binary file",
                                       JOptionPane.ERROR_MESSAGE );
        return;
      }
      else if ( remotes.size() == 1 )
        remote = ( Remote ) remotes.firstElement();
      else
      {
        Remote[] values = new Remote[ 0 ];
        values = ( Remote[] )remotes.toArray(( Object[]) values );
        remote = ( Remote )JOptionPane.showInputDialog( KeyMapMaster.getKeyMapMaster(),
                                                        "The selected binary upgrade can be used for multiple remotes.  Please select the desired remote.",
                                                        "Select a remote",
                                                        JOptionPane.QUESTION_MESSAGE,
                                                        null,
                                                        values,
                                                        values[ 0 ]);
        if ( remote == null )
          return;
      }
    }
    EncrypterDecrypter encdec = remote.getEncrypterDecrypter();
    dis = new DataInputStream( new FileInputStream( file ));
    int upgradeLength = 0;
    if ( tag.equals( "OBJ" ))
      upgradeLength = ( int )file.length();
    else
      upgradeLength = readUnsignedByte( encdec );

    int protocolOffset = readUnsignedByte( encdec );
    int temp1 = readUnsignedByte( encdec );
    int temp2 = readUnsignedByte( encdec );
    deviceIndex = temp1 >> 4;
    setupCode = (( temp1 & 0x07 ) << 8 ) | temp2;
    boolean pidGreaterThanFF = (( temp1 & 0x08 ) != 0 );

    int deviceUpgradeLength = upgradeLength - 3;
    int protocolUpradeLength = 0;
    if ( protocolOffset != 0 )
    {
      deviceUpgradeLength = protocolOffset - 3;
      protocolUpradeLength = upgradeLength - protocolOffset;
    }

    int[] upgradeCode = new int[ deviceUpgradeLength ];
    for ( int i = 0; i < deviceUpgradeLength; i++ )
      upgradeCode[ i ] = readUnsignedByte( encdec );
    code = new Hex( upgradeCode );

    int[] temp = new int[ 2 ];
    temp[ 0 ] = pidGreaterThanFF ? 1: 0;
    temp[ 1 ] = upgradeCode[ 0 ];
    pid = new Hex( temp );

    int[] protocolCode = null;
    if ( protocolOffset != 0 )
    {
      protocolCode = new int[ protocolUpradeLength ];
      for ( int i = 0; i < protocolUpradeLength ; i++ )
        protocolCode[ i ] = readUnsignedByte( encdec );
      pCode = new Hex( protocolCode );
    }
  }

  private int readUnsignedByte( EncrypterDecrypter encdec )
    throws IOException
  {
    int val = dis.readUnsignedByte();
    if ( encdec != null )
      val = encdec.decrypt( val );
    return val;
  }

  public Remote getRemote(){ return remote; }
  public int getSetupCode(){ return setupCode; }
  public int getDeviceIndex(){ return deviceIndex; }
  public Hex getPid(){ return pid; }
  public Hex getCode(){ return code; }
  public Hex getProtocolCode(){ return pCode; }

  private Remote remote = null;
  private DataInputStream dis = null;
  private int setupCode = 0;
  private int deviceIndex = 0;
  private Hex pid = null;
  private Hex code = null;
  private Hex pCode = null;
}
