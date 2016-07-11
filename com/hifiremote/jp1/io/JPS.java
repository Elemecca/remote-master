package com.hifiremote.jp1.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.lang3.StringEscapeUtils;

import com.hifiremote.jp1.Scanner;
import com.hifiremote.jp1.RemoteMaster;
import com.hifiremote.jp1.settings.SettingFactory;
import com.hifiremote.jp1.settings.SettingImpl;
import com.hifiremote.jp1.settings.Settings;

public class JPS extends IO
{
  private static final String lsblkCommand = "lsblk";

  private String signature = null;
  private String filePath = null;
  private int eepromAddress = 0;
  private int eepromSize = 0;
  private int codeAddress = 0;
  private int codeSize = 0;
  private int sigAddress = 0;
  private int irdbAddress = 0;
  private Settings s = null;
  private Scanner scanner = null;

  public JPS() throws UnsatisfiedLinkError
  {
    super( null );
  }

  public JPS( File folder ) throws UnsatisfiedLinkError
  {
    super( null );
  }

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
  public String[] getPortNames() {
    return new String[ 0 ];
  }

  public boolean isOpen()
  {
    return s != null;
  }

  @Override
  public void clear()
  {
    s = null;
  }

  public String getFilePath()
  {
    return filePath;
  }

  public void setFilePath( String filePath )
  {
    this.filePath = filePath;
  }

  @Override
  public String openRemote( String filePath )
  {
    if ( filePath == null )
    {
      String osName = System.getProperty( "os.name" );
      if ( osName.startsWith( "Windows" ) )
      {
        filePath = getSettingsWindows();
      }
      else if ( osName.equals( "Linux" ) )
      {
        try
        {
          filePath = getSettingsLinux();
        }
        catch ( Exception e )
        {
          System.err.println( "OS file system error: " + e.getMessage() );
          String message = "File system return error: " + e.getMessage();
          String title = "OS Error";
          JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
          filePath = null;
        }
      }
      else if ( osName.equals( "Mac OS X") )
      {
        filePath = getSettingsMacOSX();
      }
      else
      {
        String message = "This OS is not supported by RMIR.";
        String title = "OS Error";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
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
    codeAddress = getInt32( data, 0x2F );
    s.setFlashOffset( codeAddress );
    sigAddress = getInt32( data, 0x33 );
    codeSize = sigAddress - codeAddress;
    irdbAddress = getInt32( data, 0x37 );
    eepromAddress = getInt32( data, 0x3B );
    s.read( eepromAddress, data );
    eepromSize = getInt32( data, 2 );
    this.filePath = filePath;
    scanner = new Scanner( this, irdbAddress );
    if ( !scanner.scan() )
    {
      scanner = null;
    }
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

  @Override
  public boolean getJP2info( byte[] buffer, int length )
  {
    s.read( sigAddress, buffer );
    return true;
  }

  public Scanner getScanner()
  {
    return scanner;
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

  public int getCodeAddress()
  {
    return codeAddress;
  }

  public int getCodeSize()
  {
    return codeSize;
  }

  public int getSigAddress()
  {
    return sigAddress;
  }

  public int getSigSize()
  {
    return irdbAddress - sigAddress;
  }

  public int getIRdbAddress()
  {
    return irdbAddress;
  }

  public int getIRdbSize()
  {
    return eepromAddress - irdbAddress;
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

  public String getSettingsMacOSX()
  {
    FileSystemView fsv = FileSystemView.getFileSystemView();
    File vols = new File( "/Volumes/" );
    if ( vols.exists() )
    {
      for ( File f : fsv.getFiles( vols, false ) )
      {
        if ( f.isDirectory() && f.getName().contains( "REMOTE" ) )
        {
          f = fsv.getChild( f, "settings.bin" );
          if ( f.exists() )
          {
            return f.getAbsolutePath();
          }
        }
      }
    }
    return null;
  }

  public String getSettingsWindows()
  {
    String filePath = null;
    List <File> files = Arrays.asList( File.listRoots() );
    for ( File f : files )
    {
      String s1 = FileSystemView.getFileSystemView().getSystemDisplayName( f );
//      System.err.println( "getSystemDisplayName : " + s1 );
      if ( s1.indexOf( "REMOTE" ) >= 0 )
      {
        int n = s1.indexOf( '(' );
        String drivePath = s1.substring( n+1, n+2 );
        filePath = drivePath + ":\\settings.bin";
        System.err.println( "Loading from path: " + filePath );
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
    return filePath;
  }

  public String getSettingsLinux() throws IOException, InterruptedException
  {
    String lsblk = getCommandPath( lsblkCommand );
    String filePath = null;

    if ( lsblk == null )
    {
      String message = "Command lsblk is not found. Please use your system package\n"
                     + "management to install package containing lsblk.";
      String title = "OS Error";
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
      return null;
    }

    Runtime rt = Runtime.getRuntime();
    String[] cmd = { lsblk, "-npro", "label,fstype,mountpoint" };
    Process p = rt.exec( cmd );
    p.waitFor();
    if ( p.exitValue() != 0 ) {
      String message = "Command lsblk exited with an error code " + p.exitValue();
      String title = "OS Error";
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
      return null;
    }

    BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
    String line;
    while ( ( line = br.readLine()) != null )
    {
      String[] values = line.split( " ", 3 );
      if (!values[0].contains( "REMOTE" ) || !"vfat".equals( values[1] )) {
        continue;
      }
      if (values[2].isEmpty()) {
        continue; // TODO: Maybe inform the used that the remote is not mounted?
      }

      String mountPoint = StringEscapeUtils.unescapeJava( values[2].replace( "\\x", "\\u00" ) );
      filePath = mountPoint + File.separator + "settings.bin";
      System.err.println( "Loading from path: " + filePath );

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
    br.close();
    p.destroy();
    return filePath;
  }

  private String getCommandPath( String command )
  {
    File commandFile;
    for ( String path : System.getenv( "PATH" ).split( File.pathSeparator ) )
    {
      commandFile = new File( path + File.separator + command );
      if ( commandFile.exists() )
      {
        return commandFile.getAbsolutePath();
      }
    }
    return null;
  }
}
