package com.hifiremote.jp1;

import java.awt.Dimension;
import java.util.StringTokenizer;

public class DeviceParmFactory
{
  public static DeviceParameter[] createParameters( String text )
  {
    DeviceParameter[] rc = null;

    StringTokenizer st = new StringTokenizer( text, "," );

    int count = st.countTokens();
    rc = new DeviceParameter[ count ];
    for ( int i = 0; i < count ; i++ )
    {
      String string = st.nextToken();
      StringTokenizer st2 = new StringTokenizer( string, ":=", true );
      Integer defaultValue = new Integer( 0 );
      int bits = -1;
      String name = st2.nextToken();
      int base = 10;
      String[] choices = null;
      Dimension d = null;
      DeviceParameter ref = null;
      while ( st2.hasMoreTokens())
      {
        String sep = st2.nextToken();
        if ( sep.equals( "=" ))
        {
          String token = st2.nextToken();
          if ( token.indexOf( '[' ) != -1 )
          {
            StringTokenizer st3 = new StringTokenizer( token, "[]" );
            ref = rc[ Integer.parseInt( st3.nextToken())];
          }
          else
            defaultValue = Integer.valueOf( token, base );
        }
        else if ( sep.equals( ":" ))
        {
          String str = st2.nextToken();
          if ( str.charAt( 0 ) == '$' )
          {
            base = 16;
            str = str.substring( 1 );
          }
          if ( str.indexOf( '|' ) != -1 )
          {
            StringTokenizer st3 = new StringTokenizer( str, "|" );
            int numChoices = st3.countTokens();
            choices = new String[ numChoices + 1 ];
            choices[ 0 ] = "";
            for ( int j = 0; j < numChoices; j++ )
              choices[ j + 1 ] = st3.nextToken();
          }
          else if ( str.indexOf( '-' ) != -1 )
          {
            StringTokenizer st3 = new StringTokenizer( str, "-" );
            d = new Dimension( Integer.parseInt( st3.nextToken(), base ),
                               Integer.parseInt( st3.nextToken(), base ));

          }
          else if ( str.length() > 0 )
          {
            bits = Integer.parseInt( str );
          }
        }
      }
      DeviceParameter parm = null;
      if ( choices != null )
        parm = new ChoiceDeviceParm( name, defaultValue, choices );
      else if ( bits != -1 )
      {
        parm = new NumberDeviceParm( name, defaultValue, bits ).setBase( base );
      }
      else if ( d != null )
        parm = new NumberDeviceParm( name, defaultValue, d.width, d.height ).setBase( base );
      else
        parm = new NumberDeviceParm( name, defaultValue ).setBase( base );

      if ( ref != null )
        parm.setDefaultReference( ref );

      rc[ i ] = parm;
    }
    return rc;
  }
}
