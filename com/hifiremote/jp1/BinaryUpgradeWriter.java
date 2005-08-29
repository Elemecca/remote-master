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
      JFileChooser chooser = new JFileChooser( prefs.getBinaryUpgradePath());
      try
      {
        chooser.setAcceptAllFileFilterUsed( false );
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
      String tag = deviceUpgrade.getRemote().getSignature().substring( 3 );
      BinaryFileFilter filter = new BinaryFileFilter( tag );
      chooser.setFileFilter( filter );
      String setupString = Integer.toString( deviceUpgrade.getSetupCode());
      setupString = "0000".substring( 0, 4 - setupString.length()) + setupString;
      String defaultName = deviceUpgrade.getDeviceType().getAbbreviation() +
                           setupString + filter.getEnding();
      chooser.setSelectedFile( new File( prefs.getBinaryUpgradePath(), defaultName ));
      int returnVal = chooser.showSaveDialog( km );
      if ( returnVal == JFileChooser.APPROVE_OPTION )
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
          name = name + filter.getEnding();
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
          prefs.setUpgradePath( file.getParentFile());
          int[] data = deviceUpgrade.getBinaryUpgrade();
          DataOutputStream out = new DataOutputStream( new FileOutputStream( file ));
          EncrypterDecrypter encdec = deviceUpgrade.getRemote().getEncrypterDecrypter();
          for ( int i = 0; i < data.length; i++ )
          {
            int val = data[ i ];
            if ( encdec != null )
              val = encdec.encrypt( val );
            out.writeByte( val );
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
