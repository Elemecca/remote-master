package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class ProtocolManager
{
  public ProtocolManager()
  {}

  public void load( File f )
    throws Exception
  {
    while ( !f.canRead() )
    {
      JOptionPane.showMessageDialog( null, "Couldn't read " + f.getName() + "!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      JFileChooser chooser = new JFileChooser( f.getParentFile() );
      chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
      chooser.setDialogTitle( "Pick the file containing the protocol definitions" );
      int returnVal = chooser.showOpenDialog( null );
      if ( returnVal != JFileChooser.APPROVE_OPTION )
        System.exit( -1 );
      else
        f = chooser.getSelectedFile();
    }
    BufferedReader rdr = new BufferedReader( new FileReader( f ));
    Properties props = null;
    String name = null;
    Hex id = null;
    String type = null;

    while ( true )
    {
      String line = rdr.readLine();
      if ( line == null )
        break;

      if (( line.length() == 0 ) || ( line.charAt( 0 ) == '#' ))
        continue;

      line = line.replaceAll( "\\\\n", "\n" );
      while ( line.endsWith( "\\" ))
      {
        String temp = rdr.readLine().trim();
        temp = temp.replaceAll( "\\\\n", "\n" );
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
          parmValue = st.nextToken( "" ).trim();

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

    if ( byName.size() == 0 )
    {
      JOptionPane.showMessageDialog( null, "No protocols were loaded!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      System.exit( -1 );
    }
  }

  private void add( Protocol p )
  {
    // Add the protocol to the byName hashtable
    String name = p.getName();
    Vector v = ( Vector )byName.get( name );
    if ( v == null )
    {
      v = new Vector();
      byName.put( name, v );
      names.add( name );
    }
    v.add( p );

    // add the protocol to the byPID hashtable
    Hex id = p.getID();
    v = ( Vector )byPID.get( id );
    if ( v == null )
    {
      v = new Vector();
      byPID.put( id, v );
    }
    v.add( p );

  }

  public Vector getNames(){ return names; }

  public Vector getProtocolsForRemote( Remote remote )
  {
    Vector rc = remote.getProtocols();
    if ( rc == null )
    {
      rc = new Vector();
      for ( Enumeration e = names.elements(); e.hasMoreElements(); )
      {
        String name = ( String )e.nextElement();
        Protocol p = findProtocolForRemote( remote, name );
        if ( p != null )
          rc.add( p );
      }
      remote.setProtocols( rc );
    }
    return rc;
  }

  public Vector findByName( String name )
  {
    return ( Vector )byName.get( name );
  }

  public Vector findByPID( Hex id )
  {
    return ( Vector )byPID.get( id );
  }

  public Protocol findProtocolForRemote( Remote remote, String name )
  {
    System.err.println( "ProtocolManager.findProtocolForRemote()" +
                        " remote=" + remote.getName() + ", name=" + name );

    Protocol protocol = null;
    Protocol tentative = null;

    Vector protocols = findByName( name );
    for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
    {
      Protocol p = ( Protocol )e.nextElement();
      String supportedVariant = remote.getSupportedVariantName( p.getID());

      if ( p.getVariantName().equals( supportedVariant ))
      {
        protocol = p;
        break;
      }

      if ( tentative == null )
      {
        if ( p.getCode( remote.getProcessor()) != null )
          tentative = p;
      }
    }
    if ( protocol == null )
      protocol = tentative;

    System.err.println( "Returned " + protocol );

    return protocol;
  }

  public Protocol findProtocol( String name, Hex id, String variantName )
  {
    Vector protocols = findByPID( id );
    for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
    {
      Protocol p = ( Protocol )e.nextElement();
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
    Vector protocols = findByPID( id );
    for ( Enumeration e = protocols.elements(); e.hasMoreElements(); )
    {
      Protocol p = ( Protocol )e.nextElement();
      if ( p.getVariantName().equals( variantName ) )
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
    if (near != null)
      return near;
    protocols = findByName( name );
    if ( protocols != null )
      return ( Protocol )protocols.get(0);
    return null;
  }

  private Vector names = new Vector();
  private Hashtable byName = new Hashtable();
  private Hashtable byPID = new Hashtable();
}
