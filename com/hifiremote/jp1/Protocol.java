package com.hifiremote.jp1;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Vector;
import java.util.Enumeration;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class Protocol
{
  public Protocol( String name, byte[] id, Properties props )
  {
    this.name = name;
    this.id = id;
    this.fixedData = string2hex( props.getProperty( "FixedData", "" ));
    this.defaultCmd = string2hex( props.getProperty( "DefaultCmd", "00" ));
    this.cmdIndex = Integer.parseInt( props.getProperty( "CmdIndex", "0" ));

    String temp = props.getProperty( "DevParms", "" );
    if ( temp != null )
      devParms = DeviceParmFactory.createParameters( temp );

    temp = props.getProperty( "DeviceTranslator" );
    if ( temp != null )
    {
      System.err.println( "Protocol.Protocol("+ name +") got DeviceTranslator property." );
      deviceTranslators = TranslatorFactory.createTranslators( temp );
    }

    temp = props.getProperty( "CmdTranslator" );
    if ( temp != null )
    {
      System.err.println( "Protocol.Protocol("+ name +") got CmdTranslator property." );
      cmdTranslators = TranslatorFactory.createTranslators( temp );
    }

    temp = props.getProperty( "Code.S3C80" );
    if ( temp != null )
      code.put( "S3C80", string2hex( temp ));
    temp = props.getProperty( "Code.740" );
    if ( temp != null )
      code.put( "740", string2hex( temp ));
    temp = props.getProperty( "Code.6805" );
    if ( temp != null )
      code.put( "6805", string2hex( temp ));
    temp = props.getProperty( "CmdParms", "" );
    StringTokenizer st = new StringTokenizer( temp, "," );
    int count = st.countTokens();
    System.err.println( "Detected " + count + " cmdParms" );
    cmdParms = new CmdParameter[ count ];
    for ( int i = 0; i < count; i++ )
    {
      String str = st.nextToken();
      cmdParms[ i ] = CmdParmFactory.createParameter( str );
      if (cmdParms[i] == null)
        System.err.println( "Protocol.Protocol("+ name +") failed createParameter("+ str +")");
    }
    temp = props.getProperty( "CmdParmInit" );
    if ( temp != null )
    {
      System.err.println( "Protocol.Protocol("+ name +") got CmdParmInit property." );
      cmdParmInit = InitializerFactory.create( temp );
    }
  }

  public void initializeParms()
  {
    if ( cmdParmInit != null )
    {
      for ( int i = 0; i < cmdParmInit.length; i++ )
      {
        cmdParmInit[ i ].initialize( devParms, cmdParms );
      }
    }
  }

  public byte[] getCode( String processor )
  {
    return ( byte[] )code.get( processor );
  }

  public String getCodeText( String processor )
  {
    byte[] bytes = ( byte[] )code.get( processor );
    if ( bytes == null )
      return null;

    StringBuffer buff = new StringBuffer( 400 );
    buff.append( "Protocol code 0 = " );
    buff.append( hex2String( id ));
    buff.append( " (" );
    buff.append( processor );
    buff.append( ")\n " );
    buff.append( hex2String( bytes, 16 ));
    buff.append( "\nEnd" );

    return buff.toString();
  }

  public void setDeviceParms( Value[] parms )
  {
    for ( int i = 0; i < parms.length; i++ )
    {
      devParms[ i ].setValue( parms[ i ].getUserValue());
    }
  }

  public Value[] getDeviceParmValues()
  {
    Value[] rc = new Value[ devParms.length ];
    for ( int i = 0; i < rc.length; i++ )
    {
      DeviceParameter parm = devParms[ i ];
      rc[ i ] = new Value( parm.getValue(), parm.getDefaultValue());
    }
    return rc;
  }

  public static byte[] efc2hex( byte efc, byte[] hex, int index )
  {
    int temp = Translate.byte2int( efc ) + 156;
    temp = ( temp & 0xFF ) ^ 0xAE;
    temp = ( temp >> 3 ) | ( temp << 5 );
    hex[ index ] = ( byte )temp;
    return hex;
  }

  public byte[] efc2hex( byte efc, byte[] hex )
  {
    if ( hex == null )
      hex = getDefaultCmd();
    return efc2hex( efc, hex, cmdIndex );
  }

  public static byte hex2efc( byte[] hex, int index )
  {
    int temp = Translate.byte2int( hex[ index ]);
    temp = ( temp << 3 ) | ( temp >> 5 );
    temp = ( temp ^ 0xAE ) - 156;
    return ( byte )temp;
  }

  public byte hex2efc( byte[] hex )
  {
    return hex2efc( hex, cmdIndex );
  }

  public static String byte2String( byte data )
  {
    StringBuffer buff = new StringBuffer( 2 );
    String str = Integer.toHexString( Translate.byte2int( data ));
    int len = str.length();
    if ( len > 2 )
      str = str.substring( len - 2);
    else if ( len < 2 )
      buff.append( '0' );

    buff.append( str );
    return buff.toString();
  }

  public static String hex2String( byte[] data )
  {
    return hex2String( data, -1 );
  }

  public static String hex2String( byte[] data, int breakAt )
  {
    if ( data == null )
      return null;

    StringBuffer rc = new StringBuffer( 4 * data.length );
    int breakCount = breakAt;
    for ( int i = 0; i < data.length; i++ )
    {
      if ( breakCount == 0 )
      {
        rc.append( '\n' );
        breakCount = breakAt;
      }
      --breakCount;

      if ( i > 0 )
        rc.append( ' ' );

      String str = Integer.toHexString( Translate.byte2int( data[ i ]));
      int len = str.length();
      if ( len < 2  )
        rc.append( '0' );
      rc.append( str );
    }
    return rc.toString();
  }

  public static byte[] string2hex( String str )
  {
    StringTokenizer st = new StringTokenizer( str, " " );
    int length = st.countTokens();
    byte[] rc = new byte[ length ];
    for ( int i = 0; i < length; i++ )
      rc[ i ] = ( byte )Integer.parseInt( st.nextToken(), 16 );

    return rc;
  }

  public byte[] getDefaultCmd()
  {
    byte[] rc = ( byte[] )defaultCmd.clone();
    Value[] vals = new Value[ cmdParms.length ];

    for ( int i = 0; i < cmdParms.length; i++ )
      vals[ i ] = new Value( cmdParms[ i ].getDefaultValue(), null );

    for ( int i = 0; i < cmdTranslators.length; i++ )
      cmdTranslators[ i ].in( vals, rc, devParms, -1 );

    return rc;
  }

  public DeviceParameter[] getDeviceParameters()
  {
    return devParms;
  }

  public CmdParameter[] getCommandParameters()
  {
    return cmdParms;
  }

  // These methods allow adding columns to the Functions Panel
  public int getColumnCount()
  {
    return cmdParms.length;
  }

  public Class getColumnClass( int col )
  {
    return cmdParms[ col ].getValueClass();
  }

  public TableCellEditor getColumnEditor( int col )
  {
    return cmdParms[ col ].getEditor();
  }

  public TableCellRenderer getColumnRenderer( int col )
  {
    return cmdParms[ col ].getRenderer();
  }

  public String getColumnName( int col )
  {
    return cmdParms[ col ].getName();
  }

  public Integer getValueAt( int col, byte[] hex )
  {
    Value[] vals = new Value[ cmdParms.length ];
    for (int i=0; i<cmdTranslators.length; i++)
      cmdTranslators[ i ].out( hex, vals, devParms );
    Value v = vals[ col ];
    if ( v == null )
    {
      System.err.println( "Protocol.getValueAt("+ col +") failed" );
      return new Integer(0);
    }
    return v.getValue();
  }

  public void setValueAt( int col, byte[] hex, Object value )
  {
    Value[] vals = new Value[ cmdParms.length ];
    System.err.println( "Value is of class " + value.getClass() );
    vals[ col ] = new Value(( Integer )value, null );
    for ( int i = 0; i < cmdTranslators.length; i++ )
      cmdTranslators[ i ].in( vals, hex, devParms, col );
  }

  public boolean isEditable( int col ){ return true; }

  public String toString(){ return name; }
  public String getName(){ return name; }
  public byte[] getID(){ return id; }
  public byte[] getFixedData()
  {
    System.err.println( "Protocol.getFixedData()" );
    byte[] temp = ( byte[] )fixedData.clone();
    Value[] parms = getDeviceParmValues();
    if ( deviceTranslators != null )
    {
      System.err.println( "\tHave deviceTranslators!" );
      for ( int i = 0; i < deviceTranslators.length; i++ )
        deviceTranslators[ i ].in( parms, temp, devParms, -1 );
    }
    return temp;
  }

  // convert the functions defined in this protocol to the new Protocol
  public void convertFunctions( Vector funcs, Protocol newProtocol )
  {
    System.err.println( "Protocol.convertFunctions()" );
    CmdParameter[] newParms = newProtocol.cmdParms;

    // count the number of matching parameters
    int matchingParms = 0;
    for ( int i = 0; i < cmdParms.length; i++ )
    {
      String name = cmdParms[ i ].getName();
      System.err.println( "Checking " + name + " for a match." );
      for ( int j = 0; j < newParms.length; j++ )
      {
        if ( name.equals( newParms[ j ].getName()))
        {
          System.err.println( "Found a match!" );
          matchingParms++;
          break;
        }
      }
    }
    System.err.println( "There are " + matchingParms + " matches." );
    // create a map of command parameter indexs from this protocol to the new one
    int[] oldIndex = new int[ matchingParms ];
    int[] newIndex = new int[ matchingParms ];
    int match = 0;
    for ( int i = 0; i < cmdParms.length; i++ )
    {
      String name = cmdParms[ i ].getName();
      for ( int j = 0; j < newParms.length; j++ )
      {
        if ( name.equals( newParms[ j ].getName()))
        {
          System.err.println( "parm " + i + " matches new parm " + j );
          oldIndex[ match ] = i;
          newIndex[ match ] = j;
          match++;
          break;
        }
      }
    }

    // create Value arrays for holding the command parameters
    Value[] currValues = new Value[ cmdParms.length ];
    Value[] newValues = new Value[ newParms.length ];
    // initialize the contents of newValues with the defaultValues for the new protocol
    for ( int i = 0; i < newValues.length; i++ )
    {
      System.err.println( "Initializing newParm " + i + " to " + newParms[ i ].getDefaultValue() );
      newValues[ i ] = new Value( newParms[ i ].getDefaultValue(), null );
    }

    // now convert each defined function
    for ( Enumeration enum = funcs.elements(); enum.hasMoreElements(); )
    {
      Function f = ( Function )enum.nextElement();
      System.err.println( "Converting function " + f.getName());
      byte[] hex = f.getHex();
      System.err.println( "Staring hex is " + hex2String( hex ));
      byte[] newHex = newProtocol.getDefaultCmd();
      System.err.println( "Initial new hex is " + hex2String( newHex ));
      if ( hex != null )
      {
        // extract the command parms from the hex
        for ( int i = 0; i < cmdTranslators.length; i++ )
          cmdTranslators[ i ].out( hex, currValues, devParms );

        // copy the matching parameters to the new Values;
        for ( int i = 0; i < oldIndex.length; i++ )
        {
          System.err.println( "Setting newValue " + newIndex[ i ] + " to " + currValues[ oldIndex[ i ]].getValue());
          newValues[ newIndex[ i]] = currValues[ oldIndex[ i ]];
        }

        // generate the appropriate hex for the new protocol
        for ( int i = 0; i < newProtocol.cmdTranslators.length; i++ )
          newProtocol.cmdTranslators[ i ].in( newValues, newHex, devParms, -1 );

        // store the hex back into the function
        System.err.println( "Storing new hex " + hex2String( newHex ));
        f.setHex( newHex );
      }
    }
  }

  public int different(Properties props)
  //
  // This is intended to become a fuzzy comparison to help select the best protocol
  // when protocols.ini has been changed and there is no perfect fit.
  //
  // It returns the value tooDifferent in cases where this protocol wouldn't be
  // good enough even if it were the best found.  (It never returns any value greater
  // than tooDifferent).
  // It returns 0 for a perfect match or a larger value for a worse match.
  //
  // For now it absolutely requires a match of id.  I expect that won't always be
  // an absolute.
  {
    byte[] pid = string2hex( props.getProperty( "Protocol" ));
	if ( pid[0] != id[0] || pid[1] != id[1] )
	  return tooDifferent;
	int result = 0;
	String str = props.getProperty( "Protocol.name" );
	if (str!= null && ! str.equals(name))
	{
	  // I think we should use a fuzzy string compare here, but for now...
	  result += 1000;
	}
	str = props.getProperty( "FixedData" );
	if (str != null)
	{
	  byte[] data = string2hex( str );
	  if ( data.length != fixedData.length )
	  {
	    result += 2000;
	  }
	  else
	  {
	    // Ought to compare lengths and valid ranges of parms
	    // Ought to test translate Parms to see how closely they match
	  }
	}
	return result;
  }

  public final static int tooDifferent = 0x7FFFFFFF;

  private String name;
  private byte[] id = new byte[ 2 ];
  private byte[] fixedData = null;
  // private int[] parms = null;
  protected byte[] defaultCmd = null;
  protected int cmdIndex;
  private DeviceParameter[] devParms = null;
  private Translate[] deviceTranslators = null;
  private CmdParameter[] cmdParms = null;
  private Translate[] cmdTranslators = null;
  private HashMap code = new HashMap( 4 );
  private Initializer[] cmdParmInit = null;
}
