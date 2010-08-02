/**
 * 
 */
package com.hifiremote.jp1;

import java.util.List;

/**
 * @author Greg
 */
public class MacroCodingType extends RDFParameter
{
  private int type = 1;
  private boolean timedMacros = false;
  private int timedMacroCountAddress = 0;

  public void parse( String text, Remote remote ) throws Exception
  {
    List< String > settings = ParameterTokenizer.getTokens( text );
    type = RDFReader.parseNumber( settings.get( 0 ) );

    if ( type == 2 && settings.size() > 1 )
    {
      String setting = settings.get( 1 );
      if ( setting != null )
      {
        timedMacros = RDFReader.parseFlag( setting );
      }
      if ( settings.size() > 2 )
      {
        setting = settings.get( 2 );
        if ( setting != null )
        {
          timedMacroCountAddress = RDFReader.parseNumber( setting );
        }
      }
    }
  }

  public int getType()
  {
    return type;
  }

  public void setTimedMacros( boolean timedMacros )
  {
    this.timedMacros = timedMacros;
  }

  public boolean hasTimedMacros()
  {
    return timedMacros;
  }

  public void setTimedMacroCountAddress( int address )
  {
    timedMacroCountAddress = address;
  }

  public int getTimedMacroCountAddress()
  {
    return timedMacroCountAddress;
  }
}
