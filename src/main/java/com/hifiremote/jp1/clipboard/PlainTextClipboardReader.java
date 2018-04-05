/**
 * 
 */
package com.hifiremote.jp1.clipboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class PlainTextClipboardReader extends ClipboardReader
{
  public PlainTextClipboardReader( Reader in )
  {
    rdr = new BufferedReader( in );
  }

  private BufferedReader rdr = null;

  @Override
  public List< String > readNextLine() throws IOException
  {
    String line = rdr.readLine();
    if ( line == null )
    {
      return null;
    }
//    while ( line.trim().length() == 0 )
//    {
//      line = rdr.readLine();
//      if ( line == null )
//      {
//        return null;
//      }
//    }

    String[] tokens = line.split( "\t" );
    if ( tokens.length == 0 )
    {
      tokens = new String[]{""};
    }
    List< String > rc = new ArrayList< String >( tokens.length );
    for ( String token : tokens )
    {
      rc.add( token.trim() );
    }
    return rc;
  }

  public void close() throws IOException
  {
    rdr.close();
  }
}
