package com.hifiremote.jp1;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;

public class RDFReader
{
  public RDFReader( File file )
    throws IOException
  {
    this.fileName = file.getName();
    in = new BufferedReader( new FileReader( file ));
  }

  public String readLine()
    throws IOException
  {
    String rc;
    while ( true )
    {
      rc = in.readLine();
      lineNumber++;

      if ( rc == null )
        break;

      rc = rc.trim();

      if (( rc.length() == 0 ) || ( rc.charAt( 0 ) != '#' ))
        break;
    }

    return rc;
  }

  public int parseNumber( String text )
    throws Exception
  {
    int rc = 0;
    try
    {
      if ( text.charAt( 0 ) == '$' )
        rc = Integer.parseInt( text.substring( 1 ), 16 );
      else
        rc = Integer.parseInt( text );
    }
    catch ( NumberFormatException e )
    {
      throw new Exception( "Syntax error at line " + lineNumber + " in " + fileName, e );
    }

    return rc;
  }

  public int getLineNumber(){ return lineNumber; }

  public void close()
    throws IOException
  {
    in.close();
  }

  private String fileName;
  private int lineNumber = 0;
  private BufferedReader in = null;
}
