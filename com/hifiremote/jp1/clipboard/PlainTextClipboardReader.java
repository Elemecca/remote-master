/**
 * 
 */
package com.hifiremote.jp1.clipboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class PlainTextClipboardReader implements ClipboardReader
{
  public PlainTextClipboardReader( Reader in )
  {
    rdr = new BufferedReader( in );
  }

  private BufferedReader rdr = null;

  @Override
  public String[] readNextLine() throws IOException
  {
    String line = rdr.readLine();
    if ( line == null )
    {
      return null;
    }
    while ( line.trim().length() == 0 )
    {
      line = rdr.readLine();
      if ( line == null )
      {
        return null;
      }
    }

    return line.split( "\t" );
  }

  public void close() throws IOException
  {
    rdr.close();
  }
}
