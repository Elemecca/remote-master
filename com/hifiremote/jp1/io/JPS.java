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

import com.hifiremote.jp1.RemoteMaster;
import com.hifiremote.jp1.settings.SettingFactory;
import com.hifiremote.jp1.settings.SettingImpl;
import com.hifiremote.jp1.settings.Settings;

public class JPS extends IO
{
  private static final String dosfslabelCommand = "dosfslabel";
  
  private String signature = null;
  private String filePath = null;
  private int eepromAddress = 0;
  private int eepromSize = 0;
  private Settings s = null;
  
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
  public native boolean getJP2info( byte[] buffer, int length );

  public String[] getPortNames() {
    return new String[ 0 ];
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
        String message = "Direct loading from the remote is not yet supported for Mac OS X.";
        String title = "OS Error";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
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
    String dosfslabel = null; 
    File dosfslabelFile = null;
    String filePath = null;
    for ( String path : System.getenv("PATH").split(File.pathSeparator ) ) 
    { 
      dosfslabelFile = new File( path + File.separator + dosfslabelCommand ); 
      if ( dosfslabelFile.exists() )
      { 
        dosfslabel = dosfslabelFile.getAbsolutePath(); 
        break; 
      } 
    } 

    if ( dosfslabel == null )
    { 
      String message = "Command dosfslabel is not found. Please use your system package\n"
                     + "management to install package containing dosfslabel.";
      String title = "OS Error";
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
      return null;
    } 

    Runtime rt = Runtime.getRuntime(); 
    BufferedReader mountsReader = new BufferedReader( new InputStreamReader( new FileInputStream( "/proc/mounts" ) ) ); 
    String mountEntry; 
    while ( ( mountEntry = mountsReader.readLine() ) != null )
    { 
      String[] values = mountEntry.split(" ", 4); 
      if ( !"vfat".equals( values[2] ) )
      {
        continue;
      }

      Process p = rt.exec( dosfslabel + " " + values[0] ); 
      p.waitFor(); 
      if ( p.exitValue() == 0 ) 
      {
        BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) ); 
        String label = br.readLine(); 
        if ( label.contains( "REMOTE" ) ) 
        {
          filePath = unescapeJavaString( values[1] ) + File.separator + "settings.bin";
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
      }
      p.destroy();
    }
    mountsReader.close();
    return filePath;
  } 

  public static String unescapeJavaString( String st ) 
  { 
    StringBuilder sb = new StringBuilder( st.length() ); 

    for ( int i = 0; i < st.length(); i++ ) 
    { 
      char ch = st.charAt( i ); 
      if ( ch == '\\' ) 
      { 
        char nextChar = ( i == st.length() - 1 ) ? '\\' : st.charAt(i + 1); 
        // Octal escape? 
        if ( nextChar >= '0' && nextChar <= '7' ) 
        { 
          String code = "" + nextChar; 
          i++; 
          if ( ( i < st.length() - 1 ) && st.charAt( i + 1 ) >= '0' && st.charAt( i + 1 ) <= '7') 
          { 
            code += st.charAt( i + 1 ); 
            i++; 
            if ( ( i < st.length() - 1 ) && st.charAt( i + 1 ) >= '0' && st.charAt( i + 1 ) <= '7') 
            { 
              code += st.charAt( i + 1 ); 
              i++; 
            } 
          } 
          sb.append( ( char ) Integer.parseInt( code, 8 ) ); 
          continue; 
        } 
        switch ( nextChar ) 
        { 
          case '\\': 
            ch = '\\'; 
            break; 
          case 'b': 
            ch = '\b'; 
            break; 
          case 'f': 
            ch = '\f'; 
            break; 
          case 'n': 
            ch = '\n'; 
            break; 
          case 'r': 
            ch = '\r'; 
            break; 
          case 't': 
            ch = '\t'; 
            break; 
          case '\"': 
            ch = '\"'; 
            break; 
          case '\'': 
            ch = '\''; 
            break; 
            // Hex Unicode: u???? 
          case 'u': 
            if ( i >= st.length() - 5 )
            { 
              ch = 'u'; 
              break; 
            } 
            int code = Integer.parseInt ( "" + st.charAt( i + 2 ) + st.charAt( i + 3 ) 
                + st.charAt( i + 4 ) + st.charAt( i + 5 ), 16 ); 
            sb.append( Character.toChars( code ) ); 
            i += 5; 
            continue; 
        } 
        i++; 
      } 
      sb.append(ch); 
    } 
    return sb.toString();
  }
}
