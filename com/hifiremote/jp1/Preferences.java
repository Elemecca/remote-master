package com.hifiremote.jp1;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Preferences
{
  public Preferences( PropertyFile file )
  {
    this.file = file;
  }

  public void load( JMenu recentFileMenu, ActionListener l )
  {
    file.populateFileMenu( recentFileMenu, "RecentUpgrades.", l );
  }

  public String[] getCustomNames()
  {
    String[] customNames = null;
    String temp = file.getProperty( "CustomNames" );
    if ( temp != null )
    {
      StringTokenizer st = new StringTokenizer( temp, "|" );
      int count = st.countTokens();
      customNames = new String[ count ];
      for ( int i = 0; i < count; i++ )
        customNames[ i ] = st.nextToken();
    }
    return customNames;
  }

  public void setCustomNames( String[] customNames )
  {
    if (( customNames == null ) || ( customNames.length == 0 ))
      file.remove( "CustomNames" );
    else
    {
      StringBuilder value = new StringBuilder();
      for ( int i = 0; i < customNames.length; i++ )
      {
        if ( i != 0 )
          value.append( '|' );
        value.append( customNames[ i ]);
      }
      file.setProperty( "CustomNames", value.toString() );
    }
  }

  public String getShowRemotes()
  {
    return file.getProperty( "ShowRemotes", "All" );
  }

  public void setShowRemotes( String str )
  {
    file.setProperty( "ShowRemotes", str );
  }

  public Remote[] getPreferredRemotes()
  {
    RemoteManager rm = RemoteManager.getRemoteManager();
    java.util.List< Remote > work = new ArrayList< Remote >();
    for ( int i = 0; true; i++ )
    {
      String name = file.getProperty( "PreferredRemotes." + i );
      if ( name == null )
        break;
      Remote r = rm.findRemoteByName( name );
      if ( r != null )
        work.add( r );
    }
    Remote[] preferredRemotes = new Remote[ 0 ];
    preferredRemotes = work.toArray( preferredRemotes );
    return preferredRemotes;
  }

  public void setPreferredRemotes( Remote[] remotes )
  {
    for ( int i = 0; true; i++ )
    {
      String name = file.getProperty( "PreferredRemotes." + i );
      if ( name == null )
        break;
      file.remove( name );
    }
    for ( int i = 0; i < remotes.length; i++ )
    {
      file.setProperty( "PreferredRemotes." + i, remotes[ i ].getName());
    }
  }

  public void save( JMenu recentFileMenu )
    throws Exception
  {
    for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
    {
      JMenuItem item = recentFileMenu.getItem( i );
      file.setProperty( "RecentUpgrades." + i, item.getText());
    }

    file.save();
  }

  public File getRDFPath()
  {
    return file.getFileProperty( "RDFPath" );
  }

  public File getUpgradePath()
  {
    File path = file.getFileProperty( "UpgradePath" );
    if ( path != null )
      return path;
    path = file.getFileProperty( "KMPath" );
    if ( path != null )
    {
      file.remove( "KMPath" );
      setUpgradePath( path );
      return path;
    }

    path = new File( file.getFile().getParentFile(), upgradeDirectory );
    setUpgradePath( path );
    return path;
  }

  public void setUpgradePath( File path )
  {
    file.setProperty( "UpgradePath", path );
  }
  
  public File getBinaryUpgradePath()
  {
    File path = file.getFileProperty( "BinaryUpgradePath", getUpgradePath());
    if ( path != null )
      return path;

    return getUpgradePath();
  }

  public void setBinaryUpgradePath( File path )
  {
    file.setProperty( "BinaryUpgradePath", path );
  }

  public Rectangle getBounds()
  {
    String temp = file.getProperty( "KMBounds" );
    if ( temp == null )
      return null;
    Rectangle bounds = new Rectangle();
    StringTokenizer st = new StringTokenizer( temp, "," );
    bounds.x = Integer.parseInt( st.nextToken());
    bounds.y = Integer.parseInt( st.nextToken());
    bounds.width = Integer.parseInt( st.nextToken());
    bounds.height = Integer.parseInt( st.nextToken());
    return bounds;
  }

  public void setBounds( Rectangle bounds )
  {
    file.setProperty( "KMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );
  }

  public String getLastRemoteName()
  {
    return file.getProperty( "Remote.name" );
  }

  public void setLastRemoteName( String name )
  {
    file.setProperty( "Remote.name", name );
  }

  public String getLastRemoteSignature()
  {
    return file.getProperty( "Remote.signature" );
  }

  public void setLastRemoteSignature( String sig )
  {
    file.setProperty( "Remote.signature", sig );
  }

  public void setLookAndFeel( String lf )
  {
    file.setProperty( "LookAndFeel", lf );
  }

  public String getLookAndFeel()
  {
    return file.getProperty( "LookAndFeel" );
  }

  public void setFontSizeAdjustment( Float adjustment )
  {
    file.setProperty( "FontSizeAdjustment", Float.toString( adjustment ));
  }

  public float getFontSizeAdjustment()
  {
    float rc = 0.0f;
    String temp = file.getProperty( "FontSizeAdjustment" );
    if ( temp != null )
      rc = Float.parseFloat( temp );
    return rc;
  }

  public String getPromptToSave()
  {
    return file.getProperty( "PromptToSave", "0" );
  }

  public void setPromptToSave( String prompt )
  {
    file.setProperty( "PromptToSave", prompt );
  }

  public boolean getUseCustomNames()
  {
    return file.getProperty( "UseCustomNames" ) != null;
  }

  public void setUseCustomNames( boolean flag )
  {
    if ( flag )
      file.setProperty( "UseCustomNames" , "yes" );
    else
      file.remove( "UseCustomNames" );
  }

  private PropertyFile file;
  private float fontSizeAdjustment = 0f;
  private java.util.List< String > preferredRemoteNames = new ArrayList< String >();
  private static String[] customNames = null;

  private final static String upgradeDirectory = "Upgrades";
  private Remote[] preferredRemotes = new Remote[ 0 ];
}
