package com.hifiremote.jp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class RDFReader.
 */
public class RDFReader
{
  private static RDFReader currentReader = null;

  /**
   * Instantiates a new rDF reader.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public RDFReader( File file ) throws IOException
  {
    this.fileName = file.getName();
    in = new BufferedReader( new FileReader( file ) );

    currentReader = this;
  }

  /**
   * Read line.
   * 
   * @return the string
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public String readLine() throws IOException
  {
    String rc;
    while ( true )
    {
      rc = in.readLine();

      if ( rc == null )
        break;

      lineNumber++ ;

      rc = rc.trim();

      if ( ( rc.length() == 0 ) || ( rc.charAt( 0 ) != '#' ) )
        break;
    }

    return rc;
  }

  /**
   * Parses the number.
   * 
   * @param text
   *          the text
   * @return the int
   * @throws Exception
   *           the exception
   */
  public static int parseNumber( String text ) throws Exception
  {
    int rc = 0;
    try
    {
      boolean negate = false;
      if ( text.charAt( 0 ) == '-' )
      {
        negate = true;
        text = text.substring( 1 );
      }
      if ( text.charAt( 0 ) == '$' )
        rc = Integer.parseInt( text.substring( 1 ), 16 );
      else
        rc = Integer.parseInt( text );
      if ( negate )
        rc = ( short )( 0 - rc );
    }
    catch ( NumberFormatException e )
    {
      throw new Exception( "Syntax error at line " + currentReader.lineNumber + " in " + currentReader.fileName, e );
    }

    return rc;
  }

  public static boolean parseFlag( String flag )
  {
    if ( flag.equalsIgnoreCase( "Y" ) || flag.equalsIgnoreCase( "Yes" ) || flag.equalsIgnoreCase( "T" )
        || flag.equalsIgnoreCase( "True" ) || flag.equalsIgnoreCase( "1" ) )
    {
      return true;
    }
    return false;
  }

  /**
   * Gets the line number.
   * 
   * @return the line number
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * Close.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void close() throws IOException
  {
    in.close();

    currentReader = null;
  }

  /** The file name. */
  private String fileName;

  /** The line number. */
  private int lineNumber = 0;

  /** The in. */
  private BufferedReader in = null;
}
