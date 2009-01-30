package com.hifiremote.jp1;

import java.util.StringTokenizer;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating DeviceParm objects.
 */
public class DeviceParmFactory
{
  
  /**
   * Creates a new DeviceParm object.
   * 
   * @param text the text
   * 
   * @return the device parameter[]
   */
  public static DeviceParameter[] createParameters( String text )
  {
    StringTokenizer st = new StringTokenizer( text, "," );
    int count = st.countTokens();
    DeviceParameter[] rc = new DeviceParameter[ count ];

    for ( int i = 0; i < count ; i++ )
    {
      String string = st.nextToken();
      StringTokenizer st2 = new StringTokenizer( string, ":=", true );
      // JSF28may03 Questionable design decision: DeviceParameter always has non null defaultValue
      DefaultValue defaultValue = new DirectDefaultValue( new Integer(0) );
      int bits = -1;
      boolean bool = false;
      String name = st2.nextToken();
      int base = 10;
      String[] choices = null;
//      Dimension d = null;
      while ( st2.hasMoreTokens())
      {
        String sep = st2.nextToken();
        if ( sep.equals( "=" ))
        {
          String token = st2.nextToken();
          if ( token.indexOf( '[' ) != -1 )
          {
            StringTokenizer st3 = new StringTokenizer( token, "[]" );
            String str = st3.nextToken();
            int dash = str.indexOf( '-' );
            if ( dash != -1 )
              str = str.substring( 1 );
            int index = Integer.parseInt( str );
            IndirectDefaultValue def = new IndirectDefaultValue( index, rc[ index ] );
            def.setIsComplement( dash != -1 );
            defaultValue = def;
          }
          else
            defaultValue = new DirectDefaultValue( Integer.valueOf( token, base ) );
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
//          else if ( str.indexOf( '-' ) != -1 )
//          {
//            StringTokenizer st3 = new StringTokenizer( str, "-" );
//            d = new Dimension( Integer.parseInt( st3.nextToken(), base ),
//                               Integer.parseInt( st3.nextToken(), base ));
//          }
          else if ( str.equals( "bool" ))
            bool = true;
          else if ( str.length() > 0 )
          {
            bits = Integer.parseInt( str );
          }
        }
      }
      DeviceParameter parm = null;
      if ( choices != null )
        parm = new ChoiceDeviceParm( name, defaultValue, choices );
      else if ( bool )
        parm = new FlagDeviceParm( name, defaultValue );
      else if ( bits != -1 )
        parm = new NumberDeviceParm( name, defaultValue, base, bits );
//      else if ( d != null )
//        parm = new NumberDeviceParm( name, defaultValue, base, d.width, d.height );
      else
        parm = new NumberDeviceParm( name, defaultValue, base );

      rc[ i ] = parm;
    }
    return rc;
  }
}
