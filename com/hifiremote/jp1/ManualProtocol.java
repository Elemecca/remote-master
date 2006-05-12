package com.hifiremote.jp1;

import java.io.*;
import java.util.*;

public class ManualProtocol
  extends Protocol
{
  public final static int ONE_BYTE = 0;
  public final static int BEFORE_CMD = 1;
  public final static int AFTER_CMD = 2;

  public ManualProtocol( Hex id, Properties props )
  {
    super( null, id, props );
    if ( props != null )
    {
      notes = props.getProperty( "Protocol.notes" );
    }
  }

  public ManualProtocol( ManualProtocol p )
  {
    super( p.getName(), p.id, null );
    if ( p.fixedData != null )
      fixedData = new Hex( p.fixedData );
  }

  public ManualProtocol( String name, Hex id, int cmdType, String signalStyle,
                         int devBits, Vector parms, short[] rawHex, int cmdBits )
  {
    super( name, id, new Properties());

    boolean lsb = false;
    boolean comp = false;
    if ( signalStyle.startsWith( "LSB" ))
      lsb = true;
    if ( signalStyle.endsWith( "COMP" ))
      comp = true;

    DirectDefaultValue defaultValue = new DirectDefaultValue( new Integer( 0 ));

    devParms = new DeviceParameter[ parms.size() ];
    deviceTranslators = new Translator[ parms.size() ];

    for ( int i = 0; i < parms.size(); i++ )
    {
      devParms[ i ] = new NumberDeviceParm( "Device " + ( i + 1 ), defaultValue, 10, devBits );
      devParms[ i ].setValue( parms.elementAt( i ));
      deviceTranslators[ i ] = new Translator( lsb, comp, i, devBits, i * 8 );
    }

    int offset = parms.size();
    short[] fixedBytes = new short[ offset + rawHex.length ];
    for ( int i = 0 ; i < rawHex.length; i++ )
      fixedBytes[ i + offset ] = rawHex[ i ];

    fixedData = new Hex( fixedBytes );

    int byte2Index = 0;
    switch ( cmdType )
    {
      case ONE_BYTE:
        defaultCmd = new Hex( new short[ 1 ]);
        cmdIndex = 0;
        break;
      case BEFORE_CMD:
        defaultCmd = new Hex( new short[ 2 ]);
        cmdIndex = 1;
        byte2Index = 0;
        break;
      case AFTER_CMD:
        defaultCmd = new Hex( new short[ 2 ]);
        cmdIndex = 0;
        byte2Index = 1;
        break;
    }

    cmdParms = new CmdParameter[ defaultCmd.length() ];
    cmdParms[ 0 ] = new NumberCmdParm( "OBC", null, cmdBits );
    cmdTranslators = new Translator[ defaultCmd.length() ];
    cmdTranslators[ 0 ] = new Translator( lsb, comp, 0, cmdBits, cmdIndex * 8 );
    if ( defaultCmd.length() > 1 )
    {
      cmdParms[ 1 ] = new NumberCmdParm( "Byte 2", defaultValue, 8, 16 );
      cmdTranslators[ 1 ] = new Translator( false, false, 1, 8, byte2Index * 8 );
      importCmdTranslators = new Translator[ 1 ];
      importCmdTranslators[ 0 ] = new Translator( false, false, 0, 8, byte2Index * 8 );
    }
  }

  public String getName()
  {
    if ( name != null )
      return name;
    else if ( id != null )
      return "PID " + id.toString();
    else
      return "Manual Settings";
  }

  public void setDeviceParms( Vector v )
  {
    devParms = new DeviceParameter[ v.size()];
    int i = 0;
    for ( Enumeration e = v.elements(); e.hasMoreElements(); )
    {
      devParms[ i++ ] = ( DeviceParameter )e.nextElement();
    }
  }

  public void setDeviceTranslators( Vector v )
  {
    deviceTranslators = new Translator[ v.size()];
    int i = 0;
    for ( Enumeration e = v.elements(); e.hasMoreElements(); )
    {
      deviceTranslators[ i++ ] = ( Translator )e.nextElement();
    }
  }

  public void setCommandParms( Vector v )
  {
    cmdParms = new CmdParameter[ v.size()];
    int i = 0;
    for ( Enumeration e = v.elements(); e.hasMoreElements(); )
    {
      cmdParms[ i++ ] = ( CmdParameter )e.nextElement();
    }
  }

  public void setCommandTranslators( Vector v )
  {
    cmdTranslators = new Translator[ v.size()];
    int i = 0;
    for ( Enumeration e = v.elements(); e.hasMoreElements(); )
    {
      cmdTranslators[ i++ ] = ( Translator )e.nextElement();
    }
  }

  public void importCommand( Hex hex, String text, boolean useOBC, int obcIndex, boolean useEFC )
  {
    if (( text.indexOf( ' ' ) != -1 ) || ( text.indexOf( 'h' ) != -1 ))
    {
      Hex newHex = new Hex( text );
      if ( newHex.length() > hex.length())
        setDefaultCmd( newHex );
      hex = newHex;
    }
    else
      super.importCommand( hex, text, useOBC, obcIndex, useEFC );
  }

  // for importing byte2 values from a KM upgrade.
  public void importCommandParms( Hex hex, String text )
  {
    if ( cmdParms.length == 1 )
      return;
    StringTokenizer st = new StringTokenizer( text );
    Value[] values = new Value[ st.countTokens() ];
    int index = 0;
    while ( st.hasMoreTokens())
      values[ index++ ] = new Value( Integer.valueOf( st.nextToken(), 16 ));

    for ( index = 0; index < values.length; index++ )
    {
      for ( int i = 0; i < importCmdTranslators.length; i++ )
        importCmdTranslators[ i ].in( values, hex, devParms, index );
    }
  }

  public void store( PropertyWriter out )
  {
    if ( devParms.length > 0 )
    {
      StringBuffer buff = new StringBuffer();
      for ( int i = 0; i < devParms.length; i++ )
      {
        if ( i > 0 )
          buff.append( ',' );
        DeviceParameter devParm = devParms[ i ];
        buff.append( devParm.toString() );
      }
      out.print( "DevParms", buff.toString());
    }
    if (( deviceTranslators != null ) && ( deviceTranslators.length > 0 ))
    {
      StringBuffer buff = new StringBuffer();
      for ( int i = 0; i < deviceTranslators.length; i++ )
      {
        if ( i > 0 )
          buff.append( ' ' );
        buff.append( deviceTranslators[ i ].toString());
      }
      out.print( "DeviceTranslator", buff.toString());
    }
    if ( cmdParms.length > 0 )
    {
      StringBuffer buff = new StringBuffer();
      for ( int i = 0; i < cmdParms.length; i++ )
      {
        if ( i > 0 )
          buff.append( ',' );
        buff.append( cmdParms[ i ]);
      }
      out.print( "CmdParms", buff.toString());
    }
    if ( cmdTranslators.length > 0 )
    {
      StringBuffer buff = new StringBuffer();
      for ( int i = 0; i < cmdTranslators.length; i++ )
      {
        if ( i > 0 )
          buff.append( ' ' );
        buff.append( cmdTranslators[ i ]);
      }
      out.print( "CmdTranslator", buff.toString());
    }
    out.print( "DefaultCmd", defaultCmd.toString());
    out.print( "CmdIndex", Integer.toString( cmdIndex ));
    out.print( "FixedData", fixedData.toString());
    for ( Iterator i = code.keySet().iterator(); i.hasNext(); )
    {
      Object key = i.next();
      out.print( "Code." + key, (( Hex )code.get( key )).toRawString());
    }
    if ( notes != null )
      out.print( "Protocol.notes", notes );
  }

  public void store( PropertyWriter out, Value[] vals )
    throws IOException
  {
    System.err.println( "ManualProtocol.store" );
    super.store( out, vals );
    store( out );
  }

  public void setDefaultCmd( Hex cmd )
  {
    defaultCmd = cmd;
  }

  public void setRawHex( Hex rawHex )
  {
    fixedData = rawHex;
  }

  public void setCode( Hex pCode, Processor p )
  {
    code.put( p.getFullName(), pCode );
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public void setID( Hex newID )
  {
    id = newID;
  }
}
