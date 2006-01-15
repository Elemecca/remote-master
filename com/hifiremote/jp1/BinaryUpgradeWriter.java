package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class BinaryUpgradeWriter
{
  public static void write( DeviceUpgrade deviceUpgrade )
  {
    try
    {
      File file = null;
      KeyMapMaster km = KeyMapMaster.getKeyMapMaster();
      Preferences prefs = km.getPreferences();
      RMFileChooser chooser = new RMFileChooser( prefs.getBinaryUpgradePath());
      try
      {
        chooser.setAcceptAllFileFilterUsed( false );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
      String tag = deviceUpgrade.getRemote().getSignature().substring( 3 );
      String ending = "_" + tag;
      if ( !tag.equals( "OBJ" ))
        ending += ".bin";
      String[] endings = { ending };
      EndingFileFilter filter = new EndingFileFilter( "Binary upgrade files", endings );
      chooser.setFileFilter( filter );
      String setupString = Integer.toString( deviceUpgrade.getSetupCode());
      setupString = "0000".substring( 0, 4 - setupString.length()) + setupString;
      String defaultName = deviceUpgrade.getDeviceType().getAbbreviation() +
                           setupString + ending;
      chooser.setSelectedFile( new File( prefs.getBinaryUpgradePath(), defaultName ));
      int returnVal = chooser.showSaveDialog( km );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();
        if ( !filter.accept( file ))
        {
          String name = file.getAbsolutePath();
          int dot = name.lastIndexOf( '.' );
          if ( dot != -1 )
          {
            String ext = name.substring( dot );
            if ( ext.equalsIgnoreCase( ".bin" ))
              name = name.substring( 0, dot );
          }
          name = name + ending;
          file = new File( name );
        }
        int rc = JOptionPane.YES_OPTION;
        if ( file.exists())
        {
          rc = JOptionPane.showConfirmDialog( km,
                                              file.getName() + " already exists.  Do you want to replace it?",
                                              "Replace existing file?",
                                              JOptionPane.YES_NO_OPTION );
        }
        if ( rc == JOptionPane.YES_OPTION )
        {
          prefs.setBinaryUpgradePath( file.getParentFile());

          Vector v = new Vector();

          Remote remote = deviceUpgrade.getRemote();
          Protocol protocol = deviceUpgrade.getProtocol();

          v.add( deviceUpgrade.getHexSetupCode());
          v.add( deviceUpgrade.getUpgradeHex().getData());

          int length = 0;
          for ( Enumeration e = v.elements(); e.hasMoreElements();)
            length += (( int[] )e.nextElement()).length;

          int protocolOffset = length;

          if (( protocol.getClass() == ManualProtocol.class ) || protocol.needsCode( remote ))
          {
            Hex code = deviceUpgrade.getCode();
            v.add( code.getData());
            length += code.length();
          }
          else
            protocolOffset = 0;

          int[] header = null;
          if ( tag.equals( "OBJ" ))
          {
            header = new int[ 1 ];
            if ( protocolOffset != 0 )
              protocolOffset++;
            header[ 0 ] = protocolOffset;
          }
          else
          {
            header = new int[ 2 ];
            header[ 0 ] = length + 1;
            if ( protocolOffset != 0 )
              --protocolOffset;
            header[ 1 ] = protocolOffset + 2;
          }
          v.add( 0, header );

          DataOutputStream out = new DataOutputStream( new FileOutputStream( file ));
          EncrypterDecrypter encdec = deviceUpgrade.getRemote().getEncrypterDecrypter();
          for ( Enumeration e = v.elements(); e.hasMoreElements();)
          {
            int[] data = ( int[] )e.nextElement();
            for ( int i = 0; i < data.length; i++ )
            {
              int val = data[ i ];
              if ( encdec != null )
                val = encdec.encrypt( val );
              out.writeByte( val );
            }
          }
          out.close();
        }
      }
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.out );
    }
  }
}
