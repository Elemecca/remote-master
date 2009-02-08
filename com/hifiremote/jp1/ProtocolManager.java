package com.hifiremote.jp1;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

// TODO: Auto-generated Javadoc
/**
 * The Class ProtocolManager.
 */
public class ProtocolManager
{

  /**
   * Instantiates a new protocol manager.
   */
  protected ProtocolManager()
  {}

  /**
   * Gets the protocol manager.
   * 
   * @return the protocol manager
   */
  public static ProtocolManager getProtocolManager()
  {
    return protocolManager;
  }

  /**
   * Load.
   * 
   * @param f
   *          the f
   * 
   * @throws Exception
   *           the exception
   */
  public void load( File f ) throws Exception
  {
    if ( loaded )
      return;

    while ( !f.canRead() )
    {
      JOptionPane.showMessageDialog( null, "Couldn't read " + f.getName() + "!", "Error",
          JOptionPane.ERROR_MESSAGE );
      RMFileChooser chooser = new RMFileChooser( f.getParentFile() );
      chooser.setFileSelectionMode( RMFileChooser.FILES_ONLY );
      chooser.setDialogTitle( "Pick the file containing the protocol definitions" );
      int returnVal = chooser.showOpenDialog( null );
      if ( returnVal != RMFileChooser.APPROVE_OPTION )
        System.exit( -1 );
      else
        f = chooser.getSelectedFile();
    }
    LineNumberReader rdr = new LineNumberReader( new FileReader( f ) );
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

      if ( ( line.length() == 0 ) || ( line.charAt( 0 ) == '#' ) )
        continue;

      line = line.replaceAll( "\\\\n", "\n" );
      line = line.replaceAll( "\\\\t", "\t" );
      while ( line.endsWith( "\\" ) )
      {
        String temp = rdr.readLine().trim();
        temp = temp.replaceAll( "\\\\n", "\n" );
        temp = temp.replaceAll( "\\\\t", "\t" );
        line = line.substring( 0, line.length() - 1 ) + temp;
      }

      if ( line.charAt( 0 ) == '[' ) // begin new protocol
      {
        if ( name != null )
        {
          Protocol protocol = ProtocolFactory.createProtocol( name, id, type, props );
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

        if ( parmName.equals( "PID" ) )
        {
          id = new Hex( parmValue );
        }
        else if ( parmName.equals( "Type" ) )
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
    add( ProtocolFactory.createProtocol( name, id, type, props ) );
    ManualProtocol manualProtocol = new ManualProtocol( new Hex( "FF FF" ), null );
    manualProtocol.setName( manualProtocol.getName() );
    add( manualProtocol );

    if ( byName.size() < 2 )
    {
      JOptionPane.showMessageDialog( null, "No protocols were loaded!", "Error",
          JOptionPane.ERROR_MESSAGE );
      System.exit( -1 );
    }

    // Sort the names array
    String[] temp = new String[ 0 ];
    temp = names.toArray( temp );
    Arrays.sort( temp );
    names = new ArrayList< String >( temp.length );
    for ( int i = 0; i < temp.length; i++ )
      names.add( temp[ i ] );

    loaded = true;
  }

  /**
   * Adds the.
   * 
   * @param p
   *          the p
   */
  public void add( Protocol p )
  {
    /*
     * if ( p.getClass() == ManualProtocol.class ) { manualProtocol = ( ManualProtocol )p; return; }
     */

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
    else
    {
      String pvName = p.getVariantName();
      for ( Protocol tryit : v )
      {
        String tryName = tryit.getVariantName();
        if ( ( ( pvName == null ) && ( tryName == null ) ) || pvName.equals( tryName ) )
        {
          System.err.println( "**** Warning: multiple protocols with PID " + id
              + " and variantName " + pvName );
          break;
        }
      }
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

  /**
   * Gets the names.
   * 
   * @return the names
   */
  public List< String > getNames()
  {
    return names;
  }

  /**
   * Gets the protocols for remote.
   * 
   * @param remote
   *          the remote
   * 
   * @return the protocols for remote
   */
  public List< Protocol > getProtocolsForRemote( Remote remote )
  {
    return getProtocolsForRemote( remote, true );
  }

  /**
   * Gets the protocols for remote.
   * 
   * @param remote
   *          the remote
   * @param allowUpgrades
   *          the allow upgrades
   * 
   * @return the protocols for remote
   */
  public List< Protocol > getProtocolsForRemote( Remote remote, boolean allowUpgrades )
  {
    List< Protocol > rc = new ArrayList< Protocol >();
    for ( String name : names )
    {
      Protocol p = findProtocolForRemote( remote, name, allowUpgrades );
      if ( p != null )
        rc.add( p );
    }
    /*
     * if ( allowUpgrades && manualProtocol.hasCode( remote )) rc.add( manualProtocol );
     */
    return rc;
  }

  /**
   * Find by name.
   * 
   * @param name
   *          the name
   * 
   * @return the list< protocol>
   */
  public List< Protocol > findByName( String name )
  {
    List< Protocol > v = byName.get( name );
    /*
     * if (( v == null ) && name.equals( manualProtocol.getName())) { v = new ArrayList< Protocol
     * >(); v.add( manualProtocol ); }
     */
    return v;
  }

  /**
   * Find by pid.
   * 
   * @param id
   *          the id
   * 
   * @return the list< protocol>
   */
  public List< Protocol > findByPID( Hex id )
  {
    List< Protocol > rc = null;
    List< Protocol > list = byPID.get( id );
    if ( list == null )
      rc = new ArrayList< Protocol >( 0 );
    else
    {
      rc = new ArrayList< Protocol >( list.size() );
      rc.addAll( list );
    }
    return rc;
  }

  /**
   * Find by alternate pid.
   * 
   * @param id
   *          the id
   * 
   * @return the list< protocol>
   */
  public List< Protocol > findByAlternatePID( Hex id )
  {
    return byAlternatePID.get( id );
  }

  /**
   * Find protocol for remote.
   * 
   * @param remote
   *          the remote
   * @param name
   *          the name
   * 
   * @return the protocol
   */
  public Protocol findProtocolForRemote( Remote remote, String name )
  {
    return findProtocolForRemote( remote, name, true );
  }

  /**
   * Find protocol for remote.
   * 
   * @param remote
   *          the remote
   * @param name
   *          the name
   * @param allowUpgrades
   *          the allow upgrades
   * 
   * @return the protocol
   */
  public Protocol findProtocolForRemote( Remote remote, String name, boolean allowUpgrades )
  {
    Protocol protocol = null;
    Protocol tentative = null;

    List< Protocol > protocols = findByName( name );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols )
    {
      if ( remote.supportsVariant( p.getID(), p.getVariantName() ) )
      {
        protocol = p;
        break;
      }

      if ( tentative == null )
      {
        if ( allowUpgrades && p.hasCode( remote ) )
          tentative = p;
      }
    }
    if ( protocol == null )
      protocol = tentative;

    return protocol;
  }

  /**
   * Find protocol for remote.
   * 
   * @param remote
   *          the remote
   * @param id
   *          the id
   * @param fixedData
   *          the fixed data
   * 
   * @return the protocol
   */
  public Protocol findProtocolForRemote( Remote remote, Hex id, Hex fixedData )
  {
    List< Protocol > protocols = protocolManager.findByPID( id );
    for ( Protocol p : protocols )
    {
      if ( !remote.supportsVariant( id, p.getVariantName() ) )
        continue;
      Value[] vals = p.importFixedData( fixedData );
      Hex calculatedFixedData = p.getFixedData( vals );
      if ( calculatedFixedData.equals( fixedData ) )
        return p;
    }
    return null;
  }

  /**
   * Find protocol for remote.
   * 
   * @param remote
   *          the remote
   * @param id
   *          the id
   * 
   * @return the protocol
   */
  public Protocol findProtocolForRemote( Remote remote, Hex id )
  {
    return findProtocolForRemote( remote, id, true );
  }

  /**
   * Find protocol for remote.
   * 
   * @param remote
   *          the remote
   * @param id
   *          the id
   * @param allowUpgrades
   *          the allow upgrades
   * 
   * @return the protocol
   */
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
      if ( remote.supportsVariant( id, p.getVariantName() ) )
      {
        protocol = p;
        break;
      }
      if ( tentative == null )
      {
        if ( allowUpgrades && p.hasCode( remote ) )
          tentative = p;
      }
    }
    if ( protocol == null )
      protocol = tentative;
    return protocol;
  }

  /**
   * Find protocol by old name.
   * 
   * @param remote
   *          the remote
   * @param name
   *          the name
   * @param pid
   *          the pid
   * 
   * @return the protocol
   */
  public Protocol findProtocolByOldName( Remote remote, String name, Hex pid )
  {
    Protocol matchByName = null;
    List< Protocol > protocols = getProtocolsForRemote( remote );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols )
    {
      for ( String oldName : p.getOldNames() )
      {
        if ( name.equals( oldName ) )
        {
          if ( matchByName == null )
            matchByName = p;
          if ( p.getID().equals( pid ) )
            return p;
        }
      }
    }

    return matchByName;
  }

  /**
   * Find protocol.
   * 
   * @param name
   *          the name
   * @param id
   *          the id
   * @param variantName
   *          the variant name
   * 
   * @return the protocol
   */
  public Protocol findProtocol( String name, Hex id, String variantName )
  {
    List< Protocol > protocols = findByPID( id );
    if ( protocols == null )
      return null;
    for ( Protocol p : protocols )
    {
      if ( p.getName().equals( name ) && p.getVariantName().equals( variantName ) )
      {
        return p;
      }
    }
    return null;
  }

  /**
   * Find nearest protocol.
   * 
   * @param name
   *          the name
   * @param id
   *          the id
   * @param variantName
   *          the variant name
   * 
   * @return the protocol
   */
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
      if ( ( variantName != null ) && p.getVariantName().equals( variantName ) )
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
      return ( Protocol ) protocols.get( 0 );
    return null;
  }

  /*
   * public ManualProtocol getManualProtocol() { System.err.println(
   * "ProtocolManager.getManualProtocol(): " + manualProtocol ); return manualProtocol; }
   */
  /** The protocol manager. */
  private static ProtocolManager protocolManager = new ProtocolManager();
  // private static ManualProtocol manualProtocol = null;
  /** The loaded. */
  private boolean loaded = false;

  /** The names. */
  private List< String > names = new ArrayList< String >();

  /** The by name. */
  private Hashtable< String, List< Protocol >> byName = new Hashtable< String, List< Protocol >>();

  /** The by pid. */
  private Hashtable< Hex, List< Protocol >> byPID = new Hashtable< Hex, List< Protocol >>();

  /** The by alternate pid. */
  private Hashtable< Hex, List< Protocol >> byAlternatePID = new Hashtable< Hex, List< Protocol >>();
}
