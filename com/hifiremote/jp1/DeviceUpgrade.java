package com.hifiremote.jp1;

import java.util.Arrays;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import javax.swing.JOptionPane;

public class DeviceUpgrade
{
  public DeviceUpgrade()
  {
    initFunctions();
  }

  public void reset( Remote[] remotes, Vector protocols )
  {
    setupCode = 0;

    // remove all currently assigned functions
    Button[] buttons = remote.getUpgradeButtons();
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      if ( b.getFunction() != null )
        b.setFunction( null );
      if ( b.getShiftedFunction() != null )
        b.setShiftedFunction( null );
    }

    remote = remotes[ 0 ];
    devType = remote.getDeviceTypes()[ 0 ];
    protocol = ( Protocol )protocols.elementAt( 0 );
    notes = null;
    file = null;

    functions.clear();
    initFunctions();

    extFunctions.clear();
  }

  private void initFunctions()
  {
    for ( int i = 0; i < defaultFunctionNames.length; i++ )
      functions.add( new Function( defaultFunctionNames[ i ]));
  }

  public void setSetupCode( int setupCode )
  {
    this.setupCode = setupCode;
  }

  public int getSetupCode()
  {
    return setupCode;
  }

  public void setRemote( Remote newRemote )
  {
    if (( remote != null ) && ( remote != newRemote ))
    {
      Button[] buttons = remote.getUpgradeButtons();
      Button[] newButtons = newRemote.getUpgradeButtons();
      for ( int i = 0; i < buttons.length; i++ )
      {
        Button b = buttons[ i ];
        Function f = b.getFunction();
        Function sf = b.getShiftedFunction();
        if (( f != null ) || ( sf != null ))
        {
          if ( f != null )
            b.setFunction( null );
          if ( sf != null )
            b.setShiftedFunction( null );

          Button newB = newRemote.findByStandardName( b );
          if ( newB != null )
          {
            if ( f != null )
              newB.setFunction( f );
            if ( sf != null )
              newB.setShiftedFunction( sf );
          }
        }
      }
    }
    remote = newRemote;
  }

  public Remote getRemote()
  {
    return remote;
  }

  public void setDeviceType( DeviceType newType )
  {
    devType = newType;
  }

  public DeviceType getDeviceType()
  {
    return devType;
  }

  public void setProtocol( Protocol protocol )
  {
    this.protocol = protocol;
  }

  public Protocol getProtocol()
  {
    return protocol;
  }

  public void setNotes( String notes )
  {
    this.notes = notes;
  }

  public String getNotes()
  {
    return notes;
  }

  public Vector getFunctions()
  {
    return functions;
  }

  public Function getFunction( String name )
  {
    Function rc = getFunction( name, functions );
    if ( rc == null )
      rc =  getFunction( name, extFunctions );
    return rc;
  }

  public Function getFunction( String name, Vector funcs )
  {
    System.err.println( "Searching for function " + name );
    Function rc = null;
    for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
    {
      Function func = ( Function )e.nextElement();
      if ( func.getName().equals( name ))
      {
        rc = func;
        System.err.println( "Found!" );
        break;
      }
    }
    return rc;
  }

  public Vector getExternalFunctions()
  {
    return extFunctions;
  }

  public String getUpgradeText()
  {
    StringBuffer buff = new StringBuffer( 400 );
    buff.append( "Upgrade code 0 = " );
    if ( devType != null )
    {
      byte[] id = protocol.getID();
      int temp = devType.getNumber() * 0x1000 +
                 ( id[ 0 ] & 1 ) * 0x08 +
                 setupCode - remote.getDeviceCodeOffset();

      byte[] deviceCode = new byte[2];
      deviceCode[ 0 ] = ( byte )(temp >> 8 );
      deviceCode[ 1 ] = ( byte )temp;

      buff.append( protocol.hex2String( deviceCode ));
      buff.append( " (" );
      buff.append( devType.getName());
      buff.append( '/' );
      buff.append( setupCode );
      buff.append( ")\n " );
      buff.append( protocol.byte2String( id[ 1 ]));
      buff.append( " 00" );  // Digit Map??

      buff.append( ' ' );
      ButtonMap map = devType.getButtonMap();
      buff.append( Protocol.hex2String( map.toBitMap()));

      buff.append( ' ' );
      buff.append( protocol.hex2String( protocol.getFixedData()));

      byte[] data = map.toCommandList();
      if (( data != null ) && ( data.length != 0 ))
      {
        buff.append( "\n " );
        buff.append( Protocol.hex2String( data, 16 ));
      }

      Button[] buttons = remote.getUpgradeButtons();
      boolean hasKeyMoves = false;
      int i;
      for ( i = 0; i < buttons.length; i++ )
      {
        Button b = buttons[ i ];
        Function f = b.getFunction();
        Function sf = b.getShiftedFunction();
        if ((( f != null ) && ( !map.isPresent( b ) || f.isExternal())) ||
            (( sf != null ) && ( sf.getHex() != null )))
        {
          hasKeyMoves = true;
          break;
        }
      }
      if ( hasKeyMoves )
      {
        deviceCode[ 0 ] = ( byte )( deviceCode[ 0 ] & 0xF7 );
        buff.append( "\nKeyMoves" );
        for ( ; i < buttons.length; i++ )
        {
          Button button = buttons[ i ];
          byte[] keyMoves = button.getKeyMoves( deviceCode, devType, remote );
          if (( keyMoves != null ) && keyMoves.length > 0 )
          {
            buff.append( "\n " );
            buff.append( Protocol.hex2String( keyMoves ));
          }
        }
      }

      buff.append( "\nEND" );
    }

    return buff.toString();
  }

  public void store()
    throws IOException
  {
    store( file );
  }

  public static String valueArrayToString( Value[] parms )
  {
    StringBuffer buff = new StringBuffer( 200 );
    for ( int i = 0; i < parms.length; i++ )
    {
      if ( i > 0 )
        buff.append( ' ' );
      buff.append( parms[ i ].getUserValue());
    }
    return buff.toString();
  }

  public Value[] stringToValueArray( String str )
  {
    StringTokenizer st = new StringTokenizer( str );
    Value[] parms = new Value[ st.countTokens()];
    for ( int i = 0; i < parms.length; i++ )
    {
      String token = st.nextToken();
      Integer val = null;
      if ( !token.equals( "null" ))
        val = new Integer( token );
      parms[ i ] = new Value( val, null );
    }
    return parms;
  }

  public void store( File file )
    throws IOException
  {
    this.file = file;
    Properties props = new Properties();
    props.setProperty( "Remote.name", remote.getName());
    props.setProperty( "Remote.signature", remote.getSignature());
    props.setProperty( "DeviceType", devType.getName());
    props.setProperty( "SetupCode", Integer.toString( setupCode ));
    props.setProperty( "Protocol", protocol.hex2String( protocol.getID()));
    Value[] parms = protocol.getDeviceParmValues();
    if (( parms != null ) && ( parms.length != 0 ))
      props.setProperty( "ProtocolParms", valueArrayToString( parms ));

    if ( notes != null )
      props.setProperty( "Notes", notes );
    int i = 0;
    for ( Enumeration e = functions.elements(); e.hasMoreElements(); i++ )
    {
      Function func = ( Function )e.nextElement();
      func.store( props, "Function." + i );
    }
    i = 0;
    for ( Enumeration e = extFunctions.elements(); e.hasMoreElements(); i++ )
    {
      ExternalFunction func = ( ExternalFunction )e.nextElement();
      func.store( props, "ExtFunction." + i );
    }
    Button[] buttons = remote.getUpgradeButtons();
    for ( i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      Function f = b.getFunction();

      String fstr;
      if ( f == null )
        fstr = "null";
      else
        fstr = f.getName();

      Function sf = b.getShiftedFunction();
      String sstr;
      if ( sf == null )
        sstr = "null";
      else
        sstr = sf.getName();
      if (( f != null ) || ( sf != null ))
      {
        props.setProperty( "Button." + Integer.toHexString( b.getKeyCode()),
                           fstr + '|' + sstr );
      }

    }
    FileOutputStream out = new FileOutputStream( file );
    props.store( out, null );
    out.close();
  }

  public void load( File file, Remote[] remotes, Vector protocols )
    throws Exception
  {
    System.err.println( "DeviceUpgrade.load()" );
    this.file = file;
    Properties props = new Properties();
    FileInputStream in = new FileInputStream( file );
    props.load( in );
    in.close();

    String str = props.getProperty( "Remote.name" );
    String sig = props.getProperty( "Remote.signature" );
    System.err.println( "Searching for remote " + str );
    int index = Arrays.binarySearch( remotes, str );
    if ( index < 0 )
    {
      JOptionPane.showMessageDialog( null,
                                     "No remote definition with name " + str + " was found!",
                                     "File Load Error", JOptionPane.ERROR_MESSAGE );
      return;
    }
    remote = remotes[ index ];
    str = props.getProperty( "DeviceType" );
    System.err.println( "Searching for device type " + str );
    devType = remote.getDeviceType( str );
    System.err.println( "Device type is " + devType );
    setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));
    byte[] bytes = Protocol.string2hex( props.getProperty( "Protocol" ));

    System.err.println( "Searching for protocol with id " + Protocol.hex2String( bytes ));
    boolean found = false;
    for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
    {
      protocol = ( Protocol )e.nextElement();
      byte[] pid = protocol.getID();
      if (( bytes[ 0 ] == pid[ 0 ] ) && ( bytes[ 1 ] == pid[ 1 ] ))
      {
        found = true;
        break;
      }
    }
    if ( !found )
    {
      JOptionPane.showMessageDialog( null,
                                     "No protocol with ID " + Protocol.hex2String( bytes ) + " was found!",
                                     "File Load Error", JOptionPane.ERROR_MESSAGE );
      return;
    }
    str = props.getProperty( "ProtocolParms" );
    if (( str != null ) && ( str.length() != 0 ))
      protocol.setDeviceParms( stringToValueArray( str ));

    notes = props.getProperty( "Notes" );

    System.err.println( "Loading functions" );
    functions.clear();
    int i = 0;
    while ( true )
    {
      Function f = new Function();
      f.load( props, "Function." + i );
      if ( f.getName() == null )
      {
        System.err.println( "name is null" );
        break;
      }
      System.err.println( "Adding to vector" );
      functions.add( f );
      i++;
    }

    System.err.println( "Loading external functions" );
    extFunctions.clear();
    i = 0;
    while ( true )
    {
      ExternalFunction f = new ExternalFunction();
      f.load( props, "ExtFunction." + i, remote );
      if ( f.getName() == null )
      {
        System.err.println( "name is null" );
        break;
      }
      System.err.println( "Adding to vector" );
      extFunctions.add( f );
      i++;
    }
    System.err.println( "Loading remote assignments." );
    Button[] buttons = remote.getUpgradeButtons();
    for ( i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      System.err.println( "Looking for functions assigned to " + b.getName());
      str = props.getProperty( "Button." + Integer.toHexString( b.getKeyCode()));
      if ( str == null )
      {
        System.err.println( "No button found" );
        continue;
      }
      StringTokenizer st = new StringTokenizer( str, "|" );
      str = st.nextToken();
      Function func = null;
      if ( !str.equals( "null" ))
      {
        func = getFunction( str );
        System.err.println( "Assigning function " + func + " to button " + b.getName()); 
        b.setFunction( func );
      }
      str = st.nextToken();
      if ( !str.equals( "null" ))
      {
        func = getFunction( str );
        System.err.println( "Assigning function " + func + " to shifted button " + b.getName()); 
        b.setShiftedFunction( func );
      }
    }
  }

  private int setupCode = 0;
  private Remote remote = null;
  private DeviceType devType = null;
  private Protocol protocol = null;
  private String notes = null;
  private Vector functions = new Vector();
  private Vector extFunctions = new Vector();
  private File file = null;

  private static final String[] defaultFunctionNames =
  {
    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
    "vol up", "vol down", "mute",
    "channel up", "channel down",
    "power", "enter", "tv/vcr",
    "last (prev ch)", "menu", "program guide", "up arrow", "down arrow",
    "left arrow", "right arrow", "select", "sleep", "pip on/off", "display",
    "pip swap", "pip move", "play", "pause", "rewind", "fast fwd", "stop",
    "record", "exit", "surround", "input toggle", "+100", "fav/scan",
    "device button", "next track", "prev track", "shift-left", "shift-right",
    "pip freeze", "slow", "eject", "slow+", "slow-", "X2", "center", "rear"
  };

}
