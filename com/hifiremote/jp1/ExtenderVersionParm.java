package com.hifiremote.jp1;

import java.util.List;

public class ExtenderVersionParm extends RDFParameter
{
  public enum ExtenderVersionFormat
  {
    HEX, ASC
  }

  private int majorVersionAddr = 0;
  private int minorVersionAddr = 0;
  private ExtenderVersionFormat versionFormat = ExtenderVersionFormat.HEX;
  
  @Override
  public void parse( String text, Remote remote ) throws Exception
  {
    // ExtenderVersionAddr=Addr1, Format1[, Addr2]
    List< String > settings = ParameterTokenizer.getTokens( text );
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
    short[] data = remoteConfig.getData();
    int majorVersionByte = data[ majorVersionAddr ];
    int minorVersionByte = 0;
    String version = null;
    if ( minorVersionAddr > 0 )
    {
      minorVersionByte = data[ minorVersionAddr ];  
    }
    if ( versionFormat == ExtenderVersionFormat.ASC )
    {
      version = Character.toString( ( char )majorVersionByte );
      if ( minorVersionAddr > 0 )
      {
        version += Integer.toString( minorVersionByte );
      }
    }
    else
    {
      version = Integer.toString( majorVersionByte );
      if ( minorVersionAddr > 0 )
      {
        version += String.format( ".%02d", minorVersionByte );
      }
    }
    return version;
    
  }
  
}
