package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating CmdParm objects.
 */
public class CmdParmFactory
{
  
  /**
   * Creates a new CmdParm object.
   * 
   * @param string the string
   * @param devParms the dev parms
   * @param cmdParms the cmd parms
   * 
   * @return the cmd parameter
   */
  public static CmdParameter createParameter( String string, DeviceParameter[] devParms, CmdParameter[] cmdParms )
  {
    CmdParameter rc = null;

    StringTokenizer st = new StringTokenizer( string, ":=", true );
    DefaultValue defaultValue = null;
    int bits = -1;
    int base = 10;
    boolean optional = false;
    
    String name = st.nextToken();
    if ( name.charAt( 0 ) == '[' )
    {
      name = name.substring( 1, name.length() - 2 );
      optional = true;
    }
    List< String > choices = null;
    while ( st.hasMoreTokens())
    {
      String sep = st.nextToken();
      if ( sep.equals( "=" ))
      {
        String token = st.nextToken();
        if ( token.indexOf( '{' ) != -1 )
        {
          StringTokenizer st3 = new StringTokenizer( token, "{}" );
          String indexStr = st3.nextToken();
          int dash = indexStr.indexOf( '-' );
          if ( dash != -1 )
            indexStr = indexStr.substring( 1 );
          int index = Integer.parseInt( indexStr );

          IndirectDefaultValue def = new IndirectDefaultValue( index, devParms[ index ] );
          def.setIsComplement( dash != -1 );
          defaultValue = def;
        }
        else if ( token.indexOf( '[' ) != -1 )
        {
          StringTokenizer st3 = new StringTokenizer( token, "[]" );
          String indexStr = st3.nextToken();
          int dash = indexStr.indexOf( '-' );
          if ( dash != -1 )
            indexStr = indexStr.substring( 1 );
          int index = Integer.parseInt( indexStr );

          IndirectDefaultValue def = new IndirectDefaultValue( index, cmdParms[ index ] );
          def.setIsComplement( dash != -1 );
          defaultValue = def;
        }
        else
        {
          defaultValue = new DirectDefaultValue( new Integer( token ) );
        }
      }
      else if ( sep.equals( ":" ))
      {
        String str = st.nextToken();
        if ( str.charAt( 0 ) == '$' )
        {
          base = 16;
          str = str.substring( 1 );
        }
        if ( str.indexOf( '|' ) != -1 )
        {
          StringTokenizer st2 = new StringTokenizer( str, "|", true );
          choices = new ArrayList< String >();
          while ( st2.hasMoreTokens())
          {
            String val = st2.nextToken();
            if ( val.equals( "|" ))
              val = null;
            else if ( st2.hasMoreTokens())
              st2.nextToken();
            choices.add( val );
          }
        }
        else if ( str.length() > 0 )
        {
          bits = Integer.parseInt( str );
        }
      }
    }
    if ( choices != null )
      rc = new ChoiceCmdParm( name, defaultValue, choices );
    else
    {
      if ( bits == -1 )
        bits = 8;
      rc = new NumberCmdParm( name, defaultValue, bits, base );
    }

    rc.setOptional( optional );
    return rc;
  }
}
