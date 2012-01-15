package com.hifiremote.jp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.JOptionPane;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoteManager.
 */
public class RemoteManager
{

  /**
   * Instantiates a new remote manager.
   */
  protected RemoteManager()
  {}

  protected class ExtensionFilter implements FilenameFilter
  {
    public ExtensionFilter( String match )
    {
      this.match = match;
    }

    public void setExtension( String extension )
    {
      match = extension;
    }

    public boolean accept( File dir, String name )
    {
      int dot = name.lastIndexOf( '.' );
      if ( dot < 0 )
        return false;
      return name.substring( dot ).toLowerCase().equals( match );
    }

    private String match;
  }

  protected File initPath( PropertyFile properties, String propName, String defaultName, String extension, String type )
  {
    File path = properties.getFileProperty( propName );
    if ( path == null )
      path = new File( System.getProperty( "user.dir" ), defaultName );

    while ( !path.exists() || !path.isDirectory() ) // GD:  was && but it seemed to me that it should be ||
      path = path.getParentFile();

    File[] files = new File[ 0 ];
    File dir = path;
    FilenameFilter filter = new ExtensionFilter( extension );

    while ( files.length == 0 )
    {
      files = dir.listFiles( filter );
      if ( files.length == 0 )
      {
        JOptionPane.showMessageDialog( null, "No " + type + " files were found!", "Error", JOptionPane.ERROR_MESSAGE );
        RMDirectoryChooser chooser = new RMDirectoryChooser( dir, extension, type );
        chooser.setAccessory( new ChoiceArea( chooser ) );
        chooser.setDialogTitle( "Choose the directory containing the " + type + " files" );
        int returnVal = chooser.showDialog( null, "OK" );
        if ( returnVal != RMDirectoryChooser.APPROVE_OPTION )
          return properties.getFileProperty( propName );
        else
          dir = chooser.getSelectedFile();
      }
      path = dir;
    }
    properties.setProperty( propName, path );
    return path;
  }

  /**
   * Load remotes.
   * 
   * @param properties
   *          the properties
   * @throws Exception
   *           the exception
   */
  public void loadRemotes( PropertyFile properties )
  {
    if ( loadPath != null )
      return;

    initPath( properties, "ImagePath", "Images", ".map", "Map" );

    loadPath = initPath( properties, "RDFPath", "RDF", ".rdf", "RDF" );

    File[] files = loadPath.listFiles( new ExtensionFilter( ".rdf" ) );
    for ( int i = 0; i < files.length; i++ )
    {
      File rdf = files[ i ];

      Remote r = new Remote( rdf );
      addRemote( r );
    }
  }

  private void addRemote( Remote remote )
  {
    remotes.put( remote.getName(), remote );
    List< Remote > list = remotesBySignature.get( remote.getSignature() );
    if ( list == null )
    {
      list = new ArrayList< Remote >();
      remotesBySignature.put( remote.getSignature(), list );
    }
    list.add( remote );
    for ( int j = 1; j < remote.getNameCount(); j++ )
    {
      Remote dupRemote = new Remote( remote, j );
      remotes.put( dupRemote.getName(), dupRemote );
      list.add( dupRemote );
    }
  }
  
  public void replaceRemote( Remote oldRemote, Remote newRemote )
  {
    List< Remote > list = remotesBySignature.get( oldRemote.getSignature() );
    if ( list != null )
    {
      list.remove( oldRemote );
    }
    addRemote( newRemote );
  }

  /**
   * Gets the remotes.
   * 
   * @return the remotes
   */
  public Collection< Remote > getRemotes()
  {
    return remotes.values();
  }

  /**
   * Gets the remote manager.
   * 
   * @return the remote manager
   */
  public static RemoteManager getRemoteManager()
  {
    return remoteManager;
  }

  /**
   * Find remote by name.
   * 
   * @param name
   *          the name
   * @return the remote
   */
  public Remote findRemoteByName( String name )
  {
    System.err.println( "Searching for remote with name " + name );
    if ( name == null )
      return null;
    Remote remote = remotes.get( name );
    if ( remote == null )
    {
      remote = findRemoteByOldName( name );
      if ( remote != null )
        return remote;
      // build a list of similar remote names, and ask the user to pick a match.
      // First check if there is a slash in the name;
      String[] subNames = new String[ 0 ];
      int slash = name.indexOf( '/' );
      if ( slash != -1 )
      {
        int count = 2;
        while ( ( slash = name.indexOf( '/', slash + 1 ) ) != -1 )
          count++ ;
        subNames = new String[ count ];
        StringTokenizer nameTokenizer = new StringTokenizer( name, " /" );
        for ( int i = 0; i < count; i++ )
        {
          subNames[ i ] = nameTokenizer.nextToken();
        }
      }
      else
      {
        subNames = new String[ 1 ];
        StringTokenizer nameTokenizer = new StringTokenizer( name );
        subNames[ 0 ] = nameTokenizer.nextToken();
      }
      int mostMatches = 0;
      List< Remote > similarRemotes = new ArrayList< Remote >();
      for ( Remote r : remotes.values() )
      {
        int numMatches = 0;
        for ( int j = 0; j < subNames.length; j++ )
        {
          if ( r.getName().indexOf( subNames[ j ] ) != -1 )
          {
            System.err.println( "Remote '" + r.getName() + "' matches subName '" + subNames[ j ] + "'" );
            numMatches++ ;
          }
        }
        if ( numMatches > mostMatches )
        {
          mostMatches = numMatches;
          similarRemotes.clear();
        }
        if ( numMatches == mostMatches )
          similarRemotes.add( r );
      }

      Remote[] simRemotes = new Remote[ 0 ];
      if ( similarRemotes.size() == 0 )
        simRemotes = remotes.values().toArray( simRemotes );
      else if ( similarRemotes.size() == 1 )
        remote = similarRemotes.get( 0 );
      else
      {
        simRemotes = similarRemotes.toArray( simRemotes );
      }

      if ( remote == null )
      {
        String message = "The upgrade file you are loading is for the remote \"" + name
            + "\".\nThere is no remote with that exact name.  Please choose the best match from the list below:";

        Object rc = ( Remote )JOptionPane.showInputDialog( null, message, "Unknown Remote", JOptionPane.ERROR_MESSAGE,
            null, simRemotes, simRemotes[ 0 ] );
        if ( rc == null )
          return remote;
        else
          remote = ( Remote )rc;
      }
    }
    remote.load();
    return remote;
  }

  /**
   * Load old remote names.
   */
  public void loadOldRemoteNames()
  {
    try
    {
      File file = new File( loadPath, "OldRemoteNames.ini" );
      if ( file.exists() )
      {
        oldRemoteNames = new Hashtable< String, String >();
        BufferedReader rdr = new BufferedReader( new FileReader( file ) );
        String line = null;
        while ( ( line = rdr.readLine() ) != null )
        {
          if ( line.length() == 0 )
            continue;
          char ch = line.charAt( 0 );
          if ( ( ch == '#' ) || ( ch == '!' ) )
            continue;
          int equals = line.indexOf( '=' );
          if ( equals == -1 )
            continue;
          String oldName = line.substring( 0, equals );
          String newName = line.substring( equals + 1 );
          oldRemoteNames.put( oldName, newName );
        }
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  /**
   * Find remote by old name.
   * 
   * @param oldName
   *          the old name
   * @return the remote
   */
  public Remote findRemoteByOldName( String oldName )
  {
    if ( oldRemoteNames == null )
      loadOldRemoteNames();
    if ( oldRemoteNames == null )
      return null;
    String newName = oldRemoteNames.get( oldName );
    if ( newName == null )
      return null;
    return remotes.get( newName );
  }

  /**
   * Find remote by signature.
   * 
   * @param signature
   *          the signature
   * @return the remote[]
   */
  public List< Remote > findRemoteBySignature( String signature )
  {
    List< Remote > rc = new ArrayList< Remote >();
    List< Remote > list = remotesBySignature.get( signature );
    if ( list != null )
    {
      rc.addAll( list );
    }
    return rc;
  }

  public void reset()
  {
    remotes.clear();
    remotesBySignature.clear();
    loadPath = null;
    oldRemoteNames = null;
  }

  /** The remote manager. */
  private static RemoteManager remoteManager = new RemoteManager();

  /** The remotes. */
  private TreeMap< String, Remote > remotes = new TreeMap< String, Remote >();

  private HashMap< String, List< Remote >> remotesBySignature = new HashMap< String, List< Remote >>();

  /** The load path. */
  private File loadPath = null;

  /** The old remote names. */
  private Hashtable< String, String > oldRemoteNames = null;

}
