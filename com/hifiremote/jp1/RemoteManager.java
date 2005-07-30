package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class RemoteManager
{
  protected RemoteManager(){}

  public File loadRemotes( File loadPath )
    throws Exception
  {
    this.loadPath = loadPath;
    File[] files = new File[ 0 ];
    File dir = loadPath;
    FilenameFilter filter = new FilenameFilter()
    {
      public boolean accept( File dir, String name )
      {
        return name.toLowerCase().endsWith( ".rdf" );
      }
    };

    while ( files.length == 0 )
    {
      files = dir.listFiles( filter );
      if ( files.length == 0 )
      {
        JOptionPane.showMessageDialog( null, "No RDF files were found!",
                                       "Error", JOptionPane.ERROR_MESSAGE );
        JFileChooser chooser = new JFileChooser( dir );
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
//        chooser.setFileFilter( new KMDirectoryFilter());
        chooser.setDialogTitle( "Choose the directory containing the RDFs" );
        int returnVal = chooser.showOpenDialog( null );
        if ( returnVal != JFileChooser.APPROVE_OPTION )
          System.exit( -1 );
        else
          dir = chooser.getSelectedFile();
      }
    }

    Vector work = new Vector();
    for ( int i = 0; i < files.length; i++ )
    {
      File rdf = files[ i ];

      Remote r = new Remote( rdf );
      work.add( r );
      for ( int j = 1; j < r.getNameCount(); j++ )
        work.add( new Remote( r, j ));
    }
    remotes = ( Remote[] )work.toArray( remotes );
    Arrays.sort( remotes );

    return dir;
  }

  public Remote[] getRemotes(){ return remotes; }

  public static RemoteManager getRemoteManager()
  {
    return remoteManager;
  }

  public Remote findRemoteByName( String name )
    throws Exception
  {
    System.err.println( "Searching for remote with name " + name );
    if ( name == null )
      return null;
    Remote remote = null;
    int index = Arrays.binarySearch( remotes, name );
    if ( index < 0 )
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
        while (( slash = name.indexOf( '/', slash + 1 )) != -1 )
          count++;
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
      Vector similarRemotes = new Vector();
      for ( int i = 0; i < remotes.length; i++ )
      {
        int numMatches = 0;
        Remote r = remotes[ i ];
        for ( int j = 0; j < subNames.length; j++ )
        {
          if ( r.getName().indexOf( subNames[ j ]) != -1 )
          {
            System.err.println( "Remote '" + r.getName() + "' matches subName '" + subNames[ j ] + "'" );
            numMatches++;
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

      Object[] simRemotes = null;
      if ( similarRemotes.size() == 0 )
        simRemotes = remotes;
      else if ( similarRemotes.size() == 1 )
        remote = ( Remote )similarRemotes.firstElement();
      else
      {
        simRemotes = new Object[ similarRemotes.size() ];
        simRemotes = similarRemotes.toArray( simRemotes );
      }
      
      if ( remote == null )
      {
        String message = "The upgrade file you are loading is for the remote \"" + name + "\".\nThere is no remote with that exact name.  Please choose the best match from the list below:";

        Object rc = ( Remote )JOptionPane.showInputDialog( null,
                                                           message,
                                                           "Unknown Remote",
                                                           JOptionPane.ERROR_MESSAGE,
                                                           null,
                                                           simRemotes,
                                                           simRemotes[ 0 ]);
        if ( rc == null )
          return remote;
        else
          remote = ( Remote )rc;
      }
    }
    else
      remote = remotes[ index ];
    remote.load();
    return remote;
  }

  public void loadOldRemoteNames()
  {
    try
    {
      File file = new File( loadPath, "OldRemoteNames.ini" );
      if ( file.exists())
      {
        oldRemoteNames = new Hashtable();
        BufferedReader rdr = new BufferedReader( new FileReader( file ));
        String line = null;
        while (( line = rdr.readLine() ) != null )
        {
          if ( line.length() == 0 )
            continue;
          char ch = line.charAt( 0 );
          if (( ch == '#' ) || ( ch == '!' ))
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

  public Remote findRemoteByOldName( String oldName )
  {
    if ( oldRemoteNames == null )
      loadOldRemoteNames();
    if ( oldRemoteNames == null )
      return null;
    String newName = ( String )oldRemoteNames.get( oldName );
    if ( newName == null )
      return null;
    int index = Arrays.binarySearch( remotes, newName );
    if ( index < 0 )
      return null;
    return remotes[ index ];
  }

  public Remote[] findRemoteBySignature( String signature ){ return null; }

  private static RemoteManager remoteManager = new RemoteManager();

  private Remote[] remotes = new Remote[ 0 ];
  private File loadPath = null;
  private Hashtable oldRemoteNames = null;

}
