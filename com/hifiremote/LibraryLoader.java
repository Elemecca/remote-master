/**
 * 
 */
package com.hifiremote;

import java.io.File;
import java.util.HashMap;

import com.hifiremote.jp1.io.CommHID;

/**
 * @author Greg
 */
public class LibraryLoader
{
  public static void loadLibrary( File folder, String libraryName ) throws UnsatisfiedLinkError
  {
    if ( libraryName.equals( "hidapi" ) )
    {
      CommHID.LoadHIDLibrary();
      return;
    }
    
    if ( libraryFolder == null )
    {
      String osName = System.getProperty( "os.name" );
      if ( osName.startsWith( "Windows" ) )
      {
        osName = "Windows";
      }
      String folderName = osName + '-' + System.getProperty( "os.arch" ).toLowerCase();
      libraryFolder = new File( folder, folderName );
      System.err.println( "libraryFolder=" + libraryFolder.getAbsolutePath() );
    }

    if ( libraries.get( libraryName ) == null )
    {
      System.err.println( "LibraryLoader: Java version '" + System.getProperty( "java.version" ) + "' from '" + System.getProperty( "java.home" ) + "' running on '" + System.getProperty( "os.name" ) + "' (" + System.getProperty( "os.arch" ) + ")" );
      String mappedName = System.mapLibraryName( libraryName );
      File libraryFile = new File( libraryFolder, mappedName );
      System.err.println( "LibraryLoader: Attempting to load '" + libraryName + "' from '" + libraryFile.getAbsolutePath() + "'..." );
      try
      {
        System.load( libraryFile.getAbsolutePath() );
        System.err.println( "LibraryLoader: Loaded '" + libraryName + "' successfully from '" + libraryFile.getAbsolutePath() + "'" );
        libraries.put( libraryName, mappedName );
      }
      catch ( UnsatisfiedLinkError ule )
      {
        System.err.println( "LibraryLoader: Failed to load '" + libraryName + "' from '" + libraryFile.getAbsolutePath() + "'" );
        // second try just from standard library locations
        loadLibrary( libraryName );
      }
    }
  }

  public static void loadLibrary( String libraryName ) throws UnsatisfiedLinkError
  {
    if ( libraries.get( libraryName ) == null )
    {
      System.err.println( "LibraryLoader: Java version '" + System.getProperty( "java.version" ) + "' from '" + System.getProperty( "java.home" ) + "' running on '" + System.getProperty( "os.name" ) + "' (" + System.getProperty( "os.arch" ) + ")" );
      System.err.println( "LibraryLoader: Attempting to load '" + libraryName + "' from java library path..." );
      System.err.println( "LibraryLoader: Java library path is '" + System.getProperty( "java.library.path" ) + "'" );
      System.loadLibrary( libraryName );
      System.err.println( "LibraryLoader: Loaded '" + libraryName + "' successfully from somewhere in java library path.");
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
