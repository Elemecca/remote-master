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
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import javax.swing.JList;
import java.awt.Component;

public class DeviceUpgrade
{
  public DeviceUpgrade()
  {
    initFunctions();
  }

  public void reset( Remote[] remotes, Vector protocols )
  {
    description = null;
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

  public void setDescription( String text )
  {
    description = text;
  }

  public String getDescription()
  {
    return description;
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
    Function rc = null;
    for ( Enumeration e = funcs.elements(); e.hasMoreElements(); )
    {
      Function func = ( Function )e.nextElement();
      if ( func.getName().equals( name ))
      {
        rc = func;
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
      byte[] id = protocol.getID().getData();
      int temp = devType.getNumber() * 0x1000 +
                 ( id[ 0 ] & 1 ) * 0x08 +
                 setupCode - remote.getDeviceCodeOffset();

      byte[] deviceCode = new byte[2];
      deviceCode[ 0 ] = ( byte )(temp >> 8 );
      deviceCode[ 1 ] = ( byte )temp;

      buff.append( Hex.toString( deviceCode ));
      buff.append( " (" );
      buff.append( devType.getName());
      buff.append( '/' );
      DecimalFormat df = new DecimalFormat( "0000" );
      buff.append( df.format( setupCode ));
      buff.append( ")\n " );
      buff.append( Hex.toString( id[ 1 ]));
      buff.append( " 00" );  // Digit Map??

      buff.append( ' ' );
      ButtonMap map = devType.getButtonMap();
      buff.append( Hex.toString( map.toBitMap()));

      buff.append( ' ' );
      buff.append( protocol.getFixedData().toString());

      byte[] data = map.toCommandList();
      if (( data != null ) && ( data.length != 0 ))
      {
        buff.append( "\n " );
        buff.append( Hex.toString( data, 16 ));
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
            buff.append( Hex.toString( keyMoves ));
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
    if ( description != null )
      props.setProperty( "Description", description );
    props.setProperty( "Remote.name", remote.getName());
    props.setProperty( "Remote.signature", remote.getSignature());
    props.setProperty( "DeviceType", devType.getName());
    DeviceType[] types = remote.getDeviceTypes();
    for ( int i = 0; i < types.length; i++ )
    { 
      if ( devType == types[ i ] )
      {
        props.setProperty( "DeviceIndex", Integer.toHexString( i ));
        break;
      }
    }
    props.setProperty( "SetupCode", Integer.toString( setupCode ));
    props.setProperty( "Protocol", protocol.getID().toString());
    props.setProperty( "Protocol.name", protocol.getName());
    Value[] parms = protocol.getDeviceParmValues();
    if (( parms != null ) && ( parms.length != 0 ))
      props.setProperty( "ProtocolParms", valueArrayToString( parms ));
    props.setProperty( "FixedData", protocol.getFixedData().toString());

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

    String str = props.getProperty( "Description" );
    if ( str != null )  
      description = str;
    str = props.getProperty( "Remote.name" );
    String sig = props.getProperty( "Remote.signature" );
    System.err.println( "Searching for remote " + str );
    int index = Arrays.binarySearch( remotes, str );
    if ( index < 0 )
    {
      // build a list of similar remote names, and ask the user to pick a match.
      Vector similarRemotes = new Vector();
      for ( int i = 0; i < remotes.length; i++ )
      {
        if ( remotes[ i ].getName().indexOf( str ) != -1 )
          similarRemotes.add( remotes[ i ]);
      }

      Object[] simRemotes = null;
      if ( similarRemotes.size() > 0 )
        simRemotes = similarRemotes.toArray();
      else
        simRemotes = remotes;
      
      String message = "Could not find an exact match for the remote \"" + str + "\".  Choose one from the list below:";
      
      Object rc = ( Remote )JOptionPane.showInputDialog( null, 
                                                         message,
                                                         "Upgrade Load Error",
                                                         JOptionPane.ERROR_MESSAGE,
                                                         null, 
                                                         simRemotes,
                                                         simRemotes[ 0 ]);
      if ( rc == null )
        return;
      else
        remote = ( Remote )rc;
    }
    else
      remote = remotes[ index ];
    index = -1;
    str = props.getProperty( "DeviceIndex" );
    if ( str != null )
      index = Integer.parseInt( str, 16 );
    str = props.getProperty( "DeviceType" );
    System.err.println( "Searching for device type " + str );
    devType = remote.getDeviceType( str, index );
    System.err.println( "Device type is " + devType );
    setupCode = Integer.parseInt( props.getProperty( "SetupCode" ));

    System.err.println( "Searching for protocol with id " + props.getProperty( "Protocol" ));
    int leastDifferent = Protocol.tooDifferent;
    for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
    {
      Protocol tentative = ( Protocol )e.nextElement();
      int difference = tentative.different( props );
      if (difference < leastDifferent)
      {
        protocol = tentative;
        leastDifferent = difference;
        if ( difference == 0 )
          break;
      }
    }
    if ( leastDifferent == Protocol.tooDifferent )
    {
      JOptionPane.showMessageDialog( null,
                                     "No matching protocol for ID " + props.getProperty( "Protocol" ) + " was found!",
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
      if ( f.isEmpty())
      {
        break;
      }
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
      if ( f.isEmpty())
      {
        break;
      }
      extFunctions.add( f );
      i++;
    }
    System.err.println( "Assigning functions to buttons" );
    Button[] buttons = remote.getUpgradeButtons();
    for ( i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      str = props.getProperty( "Button." + Integer.toHexString( b.getKeyCode()));
      if ( str == null )
      {
        continue;
      }
      StringTokenizer st = new StringTokenizer( str, "|" );
      str = st.nextToken();
      Function func = null;
      if ( !str.equals( "null" ))
      {
        func = getFunction( str );
        b.setFunction( func );
      }
      str = st.nextToken();
      if ( !str.equals( "null" ))
      {
        func = getFunction( str );
        b.setShiftedFunction( func );
      }
    }
  }

  private String description = null;
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
