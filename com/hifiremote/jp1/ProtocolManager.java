package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class ProtocolManager
{
  public ProtocolManager()
  {}

  private void loadProtocols()
    throws Exception
  {
    File f = new File( "protocols.ini" );
    while ( !f.canRead() )
    {
      JOptionPane.showMessageDialog( null, "Couldn't read " + f.getName() + "!",
                                     "Error", JOptionPane.ERROR_MESSAGE );
      JFileChooser chooser = new JFileChooser( System.getProperty( "user.dir" ));
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
      inOrder.add( v );
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

  public Vector findByName( String name )
  {
    return ( Vector )byName.get( name );
  }

  public Vector findByPID( Hex id )
  {
    return ( Vector )byPID.get( id );   
  }
  
  private Vector inOrder = new Vector();
  private Hashtable byName = new Hashtable();
  private Hashtable byPID = new Hashtable();
}
