package com.hifiremote.jp1;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

// TODO: Auto-generated Javadoc
/**
 * The Class BinaryUpgradeWriter.
 */
public class BinaryUpgradeWriter
{

  /**
   * Write.
   * 
   * @param deviceUpgrade
   *          the device upgrade
   * @param defaultPath
   *          the default path
   * @return the file
   */
  public static File write( DeviceUpgrade deviceUpgrade, File defaultPath )
  {
    try
    {
      File file = null;
      RMFileChooser chooser = new RMFileChooser( defaultPath );
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
      if ( !tag.equals( "OBJ" ) )
        ending += ".bin";
      String[] endings =
      {
        ending
      };
      EndingFileFilter filter = new EndingFileFilter( "Binary upgrade files", endings );
      chooser.setFileFilter( filter );
      String setupString = Integer.toString( deviceUpgrade.getSetupCode() );
      setupString = "0000".substring( 0, 4 - setupString.length() ) + setupString;
      String defaultName = deviceUpgrade.getDeviceType().getAbbreviation() + setupString + ending;
      chooser.setSelectedFile( new File( defaultPath, defaultName ) );
      int returnVal = chooser.showSaveDialog( RemoteMaster.getFrame() );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();
        if ( !filter.accept( file ) )
        {
          String name = file.getAbsolutePath();
          int dot = name.lastIndexOf( '.' );
          if ( dot != -1 )
          {
            String ext = name.substring( dot );
            if ( ext.equalsIgnoreCase( ".bin" ) )
              name = name.substring( 0, dot );
          }
          name = name + ending;
          file = new File( name );
        }
        int rc = JOptionPane.YES_OPTION;
        if ( file.exists() )
        {
          rc = JOptionPane.showConfirmDialog( RemoteMaster.getFrame(), file.getName()
              + " already exists.  Do you want to replace it?", "Replace existing file?", JOptionPane.YES_NO_OPTION );
        }
        if ( rc == JOptionPane.YES_OPTION )
        {
          List< short[] > v = new ArrayList< short[] >();

          v.add( deviceUpgrade.getHexSetupCode() );
          v.add( deviceUpgrade.getUpgradeHex().getData() );

          short length = 0;
          for ( short[] data : v )
            length += data.length;

          short protocolOffset = length;

          if ( deviceUpgrade.needsProtocolCode() )
          {
            Hex code = deviceUpgrade.getCode();
            v.add( code.getData() );
            length += code.length();
          }
          else
            protocolOffset = 0;

          short[] header = null;
          if ( tag.equals( "OBJ" ) )
          {
            header = new short[ 1 ];
            if ( protocolOffset != 0 )
              protocolOffset++ ;
            header[ 0 ] = protocolOffset;
          }
          else
          {
            header = new short[ 2 ];
            header[ 0 ] = ( short )( length + 1 );
            if ( protocolOffset != 0 )
              protocolOffset++ ;
            header[ 1 ] = protocolOffset;
          }
          v.add( 0, header );

          DataOutputStream out = new DataOutputStream( new FileOutputStream( file ) );
          EncrypterDecrypter encdec = deviceUpgrade.getRemote().getEncrypterDecrypter();
          for ( short[] data : v )
          {
            for ( int i = 0; i < data.length; i++ )
            {
              short val = data[ i ];
              if ( encdec != null )
                val = encdec.encrypt( val );
              out.writeByte( val );
            }
          }
          out.close();
          return file.getParentFile();
        }
      }
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    return null;
  }
}
