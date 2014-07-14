package com.hifiremote.jp1.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import com.hifiremote.jp1.RemoteMaster;
import com.hifiremote.jp1.settings.SettingFactory;
import com.hifiremote.jp1.settings.SettingImpl;
import com.hifiremote.jp1.settings.Settings;

public class JPS extends IO
{
  String signature = null;
  String filePath = null;
  int eepromAddress = 0;
  int eepromSize = 0;
  Settings s = null;
 
  @Override
  public String getInterfaceName() {
    return "JPS";
  }

  @Override
  public String getInterfaceVersion() {
    return "0.1";
  }
  
  @Override
  public int getInterfaceType() {
    return 0x301;
  }
  
  @Override
  public native boolean getJP2info( byte[] buffer, int length );

  public String[] getPortNames() {
    return new String[ 0 ];
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#openRemote(java.lang.String)
   */
  @Override
  public String openRemote( String filePath )
  {
    if ( filePath == null )
    {
      String ofaDriveName = null;
      List <File>files = Arrays.asList(File.listRoots());
      for (File f : files) {
        String s1 = FileSystemView.getFileSystemView().getSystemDisplayName (f);
        String s2 = FileSystemView.getFileSystemView().getSystemTypeDescription(f);
        System.err.println("getSystemDisplayName : " + s1);
        System.err.println("getSystemTypeDescription : " + s2);
        if ( s1.indexOf( "REMOTE" ) >= 0 )
        {
          int n = s1.indexOf( '(' );
          String driveLetter = s1.substring( n+1, n+2 );
          System.err.println( "Loading from path: " + driveLetter + ":\\settings.bin" );
          filePath = driveLetter + ":\\settings.bin";
          if ( new File( filePath ).exists() )
          {
            break;
          }
          else
          {
            System.err.println( "File does not exist" );
            filePath = null;
          }
        }
      }
      if ( filePath == null )
      {
        return null;
      }
    }
    this.filePath = filePath;
    SettingFactory cf = new SettingFactory(SettingImpl.class);
    InputStream in = null;
    try
    {
      in = new FileInputStream( filePath );
    }
    catch ( FileNotFoundException e )
    {
      System.err.println( "File not found: " + filePath );
      return null;
    }

    try
    {
      s = new Settings( in, cf );
      if ( !s.isValid() )
      {
        System.err.println( "Not a Simpleset file: " + filePath );
        String message = "File \"" + filePath+ "\" is not a Simpleset file";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, "File error", JOptionPane.ERROR_MESSAGE );
        return null;
      }
    }
    catch ( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
   
    byte[] data = new byte[ 0x40 ];
    s.read( 0, data );
    int len = getInt32( data, 2 );
    s.read( len, data );
    char[] sigArray = new char[ 6 ];
    for ( int i = 0; i < 6; i++ )
    {
      sigArray[ i ] = ( char)( data[ 6 + i ] & 0xFF );
    }
    signature = String.valueOf( sigArray );
    s.setFlashOffset( getInt32( data, 0x2F ) );
    eepromAddress = getInt32( data, 0x3B );
    s.read( eepromAddress, data );
    eepromSize = getInt32( data, 2 );
    this.filePath = filePath;
    return filePath;
  }
  
  private int getInt32( byte[] data, int offset )
  {
    int val = 0;
    for ( int i = 0; i < 4; i++) 
    {
      val += (data[ offset + i ] & 0xFF) << 8 * (3 - i);
    }
    return val;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#closeRemote()
   */
  @Override
  public void closeRemote(){}

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getRemoteSignature()
   */
  @Override
  public String getRemoteSignature()
  {
    return signature;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getRemoteEepromAddress()
   */
  @Override
  public int getRemoteEepromAddress()
  {
    return eepromAddress;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#getRemoteEepromSize()
   */
  @Override
  public int getRemoteEepromSize()
  {
    return eepromSize;
  }

  @Override
  public int readRemote( int address, byte[] buffer, int length ) 
  {
    try
    {
      s.read( address, buffer );
    }
    catch ( RuntimeException e )
    {
      System.err.println( e.getMessage() );
      return -1;
    }
    return buffer.length;
  }
  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.io.IO#writeRemote(int, byte[], int)
   */
  @Override
  public int writeRemote( int address, byte[] buffer, int length )
  {
    try
    {
      s.write( address, buffer );
    }
    catch ( RuntimeException e )
    {
      System.err.println( e.getMessage() );
      return -1;
    }
    try
    {
      ByteArrayOutputStream bao = new ByteArrayOutputStream(); 
      s.save( bao ); 
      OutputStream o = new FileOutputStream( filePath ); 
      o.write( bao.toByteArray() );
      bao.close();
      o.close();;
    }
    catch ( Exception e )
    {
      return -1;
    }
    
    return buffer.length;
  }

  /** The is loaded. */
  private static boolean isLoaded = false;

  public JPS() throws UnsatisfiedLinkError
  {
    super( null );
  }


  public JPS( File folder ) throws UnsatisfiedLinkError
  {
    super( null );
  }

}
