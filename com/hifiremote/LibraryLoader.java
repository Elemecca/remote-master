/**
 * 
 */
package com.hifiremote;

import java.io.File;
import java.util.HashMap;

/**
 * @author Greg
 */
public class LibraryLoader
{
  public static void loadLibrary( File folder, String libraryName ) throws UnsatisfiedLinkError
  {
    if ( libraryFolder == null )
    {
      String folderName = "";
      String osName = System.getProperty( "os.name" );
      if ( osName.startsWith( "Windows" ) )
      {
        folderName = "Windows";
      }
      else
      {
        folderName = osName + '-' + System.getProperty( "os.arch" );
      }
      libraryFolder = new File( folder, folderName );
      System.err.println( "libraryFolder=" + libraryFolder.getAbsolutePath() );
    }

    if ( libraries.get( libraryName ) == null )
    {
      String mappedName = System.mapLibraryName( libraryName );
      File libraryFile = new File( libraryFolder, mappedName );
      System.err.println( "Loading " + libraryFile.getAbsolutePath() );
      System.load( libraryFile.getAbsolutePath() );
      System.err.println( "Loaded " + libraryFile.getAbsolutePath() );
      libraries.put( libraryName, mappedName );
    }
  }

  public static void loadLibrary( String libraryName ) throws UnsatisfiedLinkError
  {
    if ( libraries.get( libraryName ) == null )
    {
      System.err.println( "Loading " + libraryName );
      System.loadLibrary( libraryName );
      System.err.println( "Loaded " + libraryName );
      libraries.put( libraryName, libraryName );
    }
  }

  public static String getLibraryFolder()
  {
    return libraryFolder.getAbsolutePath();
  }

  protected static HashMap< String, String > libraries = new HashMap< String, String >();

  protected static File libraryFolder = null;
}
