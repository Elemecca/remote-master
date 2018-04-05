package com.hifiremote.jp1;

import java.util.List;

import com.hifiremote.jp1.FixedData.Location;

public class ExtenderVersionParm extends RDFParameter
{
  public enum ExtenderVersionFormat
  {
    HEX, ASC
  }

  private int majorVersionAddr = 0;
  private int minorVersionAddr = 0;
  private ExtenderVersionFormat versionFormat = ExtenderVersionFormat.HEX;
  private Location location = Location.E2;
  
  @Override
  public void parse( String text, Remote remote ) throws Exception
  {
    // ExtenderVersionAddr=[Location, ]Addr1, Format1[, Addr2]
    List< String > settings = ParameterTokenizer.getTokens( text );
    if ( settings.size() < 2 )
    {
      return;
    }
    if ( settings.get( 0 ).equals( "E2" ) )
    {
      location = Location.E2;
      settings.remove( 0 );
    }
    else if ( settings.get( 0 ).equals( "SIG" ) )
    {
      location = Location.SIGBLK;
      settings.remove( 0 );
    }
    
    if ( settings.size() < 2 )
    {
      return;
    }    
    
    majorVersionAddr = RDFReader.parseNumber( settings.get( 0 ) );
    if ( settings.get( 1 ).equalsIgnoreCase( "Asc" ) )
    {
      versionFormat = ExtenderVersionFormat.ASC;
    }
    if ( settings.size() > 2 )
    {
      minorVersionAddr = RDFReader.parseNumber( settings.get( 2 ) );
    }
  }
  
  public boolean displayExtenderVersion()
  {
    return majorVersionAddr > 0;
  }

  public String getExtenderVersion( RemoteConfiguration remoteConfig )
  {
    short[] data = ( location == Location.E2 ) ? remoteConfig.getData() : remoteConfig.getSigData();
    int majorVersionByte = -1;
    int minorVersionByte = -1;
    String version = null;
    
    if ( data != null && majorVersionAddr < data.length )
    {
      majorVersionByte = data[ majorVersionAddr ];
    }
    else
    {
      return "<unknown>";
    }
    
    if ( minorVersionAddr > 0 && minorVersionAddr < data.length )
    {
      minorVersionByte = data[ minorVersionAddr ];  
    }
    
    if ( versionFormat == ExtenderVersionFormat.ASC )
    {
      version = Character.toString( ( char )majorVersionByte );
      if ( minorVersionByte >= 0 )
      {
        version += Integer.toString( minorVersionByte );
      }
    }
    else
    {
      version = Integer.toString( majorVersionByte );
      if ( minorVersionByte >= 0 )
      {
        version += String.format( ".%02d", minorVersionByte );
      }
    }
    return version;
    
  }
  
}
