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
            defaultValue = new Integer( token );
        }
        else if ( sep.equals( ":" ))
        {
          String str = st2.nextToken();
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
            d = new Dimension( Integer.parseInt( st3.nextToken()),
                               Integer.parseInt( st3.nextToken()));

          }
          else
          {
            bits = Integer.parseInt( str );
          }
        }
      }
      DeviceParameter parm = null;
      if ( choices != null )
        parm = new ChoiceDeviceParm( name, defaultValue, choices );
      else if ( bits != -1 )
        parm = new NumberDeviceParm( name, defaultValue, bits );
      else if ( d != null )
        parm = new NumberDeviceParm( name, defaultValue, d.width, d.height );
      else
        parm = new NumberDeviceParm( name, defaultValue );

      if ( ref != null )
        parm.setDefaultReference( ref );

      rc[ i ] = parm;
    }
    return rc;
  }
}
