package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class ProtocolManager
{
  protected ProtocolManager()
  {
  }

  public static ProtocolManager getProtocolManager()
  {
    return protocolManager;
  }

  public void load( File f )
    throws Exception
  {
    if ( loaded )
      return;

    while ( !f.canRead() )
    {
      JOptionPane.showMessageDialog( null, "Couldn't read " + f.getName() + "!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      RMFileChooser chooser = new RMFileChooser( f.getParentFile() );
      chooser.setFileSelectionMode( RMFileChooser.FILES_ONLY );
      chooser.setDialogTitle( "Pick the file containing the protocol definitions" );
      int returnVal = chooser.showOpenDialog( null );
      if ( returnVal != RMFileChooser.APPROVE_OPTION )
        System.exit( -1 );
      else
        f = chooser.getSelectedFile();
    }
    LineNumberReader rdr = new LineNumberReader( new FileReader( f ));
    rdr.setLineNumber( 1 );
    Properties props = null;
    String name = null;
    Hex id = null;
    String type = null;

    while ( true )
    {
      String line = rdr.readLine();
      if ( line == null )
        break;

      line = line.trim();

      if (( line.length() == 0 ) || ( line.charAt( 0 ) == '#' ))
        continue;

      line = line.replaceAll( "\\\\n", "\n" );
      line = line.replaceAll( "\\\\t", "\t" );
      while ( line.endsWith( "\\" ))
      {
        String temp = rdr.readLine().trim();
        temp = temp.replaceAll( "\\\\n", "\n" );
        temp = temp.replaceAll( "\\\\t", "\t" );
        line = line.substring(0, line.length() - 1 ) + temp;
      }

      if ( line.charAt( 0 ) == '[' ) // begin new protocol
      {
        if ( name != null  )
        {
          Protocol protocol =
            ProtocolFactory.createProtocol( name, id, type, props );
          if ( protocol != null )
            add( protocol );
        }
        name = line.substring( 1, line.length() - 1 ).trim();
        props = new Properties();
        id = null;
        type = "Protocol";
      }
      else
      {
        StringTokenizer st = new StringTokenizer( line, "=", true );
        String parmName = st.nextToken().trim();
        String parmValue = null;
        st.nextToken(); // skip the =
        if ( !st.hasMoreTokens() )
          continue;
        else
          parmValue = st.nextToken( "" ); // .trim();

        if ( parmName.equals( "PID" ))
        {
          id = new Hex( parmValue );
        }
        else if ( parmName.equals( "Type" ))
        {
          type = parmValue;
        }
        else
        {
          props.setProperty( parmName, parmValue );
        }
      }
    }
    rdr.close();
    add( ProtocolFactory.createProtocol( name, id, type, props ));
    manualProtocol = new ManualProtocol( null, null );
    manualProtocol.setName( manualProtocol.getName());

    if ( byName.size() == 0 )
    {
      JOptionPane.showMessageDialog( null, "No protocols were loaded!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      System.exit( -1 );
    }

    // Sort the names array
    String[] temp = new String[ 0 ];
    temp = names.toArray( temp );
    Arrays.sort( temp );
    names = new ArrayList< String >( temp.length );
    for ( int i = 0; i < temp.length; i++ )
      names.add( temp[ i ]);

    loaded = true;
  }

  public void add( Protocol p )
  {
    if ( p.getClass() == ManualProtocol.class )
    {
      manualProtocol = ( ManualProtocol )p;
      return;
    }

    // Add the protocol to the byName hashtable
    String name = p.getName();
    List< Protocol > v = byName.get( name );
    if ( v == null )
    {
      v = new ArrayList< Protocol >();
      byName.put( name, v );
      names.add( name );
    }
    v.add( p );

    // add the protocol to the byPID hashtable
    Hex id = p.getID();
    v = byPID.get( id );
    if ( v == null )
    {
      v = new ArrayList< Protocol >();
      byPID.put( id, v );
    }
    v.add( p );

    id = p.getAlternatePID();
    if ( id != null )
    {
      v = byAlternatePID.get( id );
      if ( v == null )
      {
        v = new ArrayList< Protocol >();
        byAlternatePID.put( id, v );
      }
      v.add( p );
    }
  }

  public List< String > getNames(){ return names; }

  public List< Protocol > getProtocolsForRemote( Remote remote )
  {
    return getProtocolsForRemote( remote, true );
  }

  public List< Protocol > getProtocolsForRemote( Remote remote, boolean allowUpgrades )
  {
    List< Protocol > rc = new ArrayList< Protocol >();
    for ( String name : names )
    {
      Protocol p = findProtocolForRemote( remote, name, allowUpgrades );
      if ( p != null )
        rc.add( p );
    }
    if ( allowUpgrades && manualProtocol.hasCode( remote ))
      rc.add( manualProtocol );
    return rc;
  }

  public List< Protocol > findByName( String name )
  {
    List< Protocol > v = byName.get( name );
    if (( v == null ) && name.equals( manualProtocol.getName()))
    {
      v = new ArrayList< Protocol >();
      v.add( manualProtocol );
    }
    return v;
  }

  public List< Protocol > findByPID( Hex id )
  {
    List< Protocol > rc = byPID.get( id );
    if ( rc == null )
     rc = new ArrayList< Protocol >( 0 );
    return rc;
  }

  public List< Protocol > findByAlternatePID( Hex id )
  {
    return byAlternatePID.get( id );
  }

  public Protocol findProtocolForRemote( Remote remote, String name )
  {
    return findProtocolForRemote( remote, name, true );
  }

  public Protocol findProtocolForRemote( Remote remote, String name, boolean allowUpgrades )
  {
    Protocol protocol = null;
    Protocol tentative = null;

    List< Protocol > protocols = findByName( name );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols  )
    {
      if ( remote.supportsVariant( p.getID(), p.getVariantName()))
      {
        protocol = p;
        break;
      }

      if ( tentative == null )
      {
        if ( allowUpgrades && p.hasCode( remote ))
          tentative = p;
      }
    }
    if ( protocol == null )
      protocol = tentative;

    return protocol;
  }

  public Protocol findProtocolForRemote( Remote remote, Hex id, Hex fixedData )
  {
    List< Protocol > protocols = protocolManager.findByPID( id );
    for ( Protocol p : protocols )
    {
      if ( !remote.supportsVariant( id, p.getVariantName()))
        continue;
      Value[] vals = p.importFixedData( fixedData );
      Hex calculatedFixedData = p.getFixedData( vals );
      if ( calculatedFixedData.equals( fixedData ))
        return p;
    }
    return null;
  }

  public Protocol findProtocolForRemote( Remote remote, Hex id )
  {
    return findProtocolForRemote( remote, id, true );
  }

  public Protocol findProtocolForRemote( Remote remote, Hex id, boolean allowUpgrades )
  {
    Protocol protocol = null;
    Protocol tentative = null;
    List< Protocol > protocols = findByPID( id );
    if ( protocols == null )
      protocols = findByAlternatePID( id );

    if ( protocols == null )
      return null;

    for ( Protocol p : protocols )
    {
      if ( remote.supportsVariant( id , p.getVariantName()))
      {
        protocol = p;
        break;
      }
      if ( tentative == null )
      {
        if ( allowUpgrades && p.hasCode( remote ))
          tentative = p;
      }
    }
    if ( protocol == null )
      protocol = tentative;
    return protocol;
  }

  public Protocol findProtocolByOldName( Remote remote, String name, Hex pid )
  {
    Protocol matchByName = null;
    List< Protocol > protocols = getProtocolsForRemote( remote );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols )
    {
      for ( String oldName : p.getOldNames())
      {
        if ( name.equals( oldName ))
        {
          if ( matchByName == null )
            matchByName = p;
          if ( p.getID().equals( pid ))
            return p;
        }
      }
    }

    return matchByName;
  }

  public Protocol findProtocol( String name, Hex id, String variantName )
  {
    List< Protocol > protocols = findByPID( id );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols )
    {
      if ( p.getName().equals( name ) &&
           p.getVariantName().equals( variantName ))
      {
        return p;
      }
    }
    return null;
  }

  public Protocol findNearestProtocol( String name, Hex id, String variantName )
  {
    Protocol near = null;
    List< Protocol > protocols = findByPID( id );
    if ( protocols == null )
      protocols = findByAlternatePID( id );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols )
    {
      if (( variantName != null ) &&  p.getVariantName().equals( variantName ) )
      {
        if ( p.getName().equals( name ) )
          return p;
        near = p;
      }
      if ( p.getName().equals( name ) && near == null )
      {
        near = p;
      }
    }
    if ( near != null )
      return near;
    protocols = findByName( name );
    if ( protocols != null )
      return ( Protocol )protocols.get(0);
    return null;
  }

  public ManualProtocol getManualProtocol()
  {
    System.err.println( "ProtocolManager.getManualProtocol(): " + manualProtocol );
    return manualProtocol;
  }

  private static ProtocolManager protocolManager = new ProtocolManager();
  private static ManualProtocol manualProtocol = null;
  private boolean loaded = false;
  private List< String > names = new ArrayList< String >();
  private Hashtable< String, List< Protocol >> byName = new Hashtable< String, List< Protocol >>();
  private Hashtable< Hex, List< Protocol >> byPID = new Hashtable< Hex, List< Protocol >>();
  private Hashtable< Hex, List< Protocol >> byAlternatePID = new Hashtable< Hex, List< Protocol >>();
}
