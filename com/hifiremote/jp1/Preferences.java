package com.hifiremote.jp1;

import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

// TODO: Auto-generated Javadoc
/**
 * The Class Preferences.
 */
public class Preferences
{

  /**
   * Instantiates a new preferences.
   * 
   * @param file
   *          the file
   */
  public Preferences( PropertyFile file )
  {
    this.file = file;
  }

  /**
   * Load.
   * 
   * @param recentFileMenu
   *          the recent file menu
   * @param l
   *          the l
   */
  public void load( JMenu recentFileMenu, ActionListener l )
  {
    file.populateFileMenu( recentFileMenu, "RecentUpgrades.", l );
  }

  /**
   * Gets the custom names.
   * 
   * @return the custom names
   */
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

  /**
   * Sets the custom names.
   * 
   * @param customNames
   *          the new custom names
   */
  public void setCustomNames( String[] customNames )
  {
    if ( ( customNames == null ) || ( customNames.length == 0 ) )
      file.remove( "CustomNames" );
    else
    {
      StringBuilder value = new StringBuilder();
      for ( int i = 0; i < customNames.length; i++ )
      {
        if ( i != 0 )
          value.append( '|' );
        value.append( customNames[ i ] );
      }
      file.setProperty( "CustomNames", value.toString() );
    }
  }

  /**
   * Gets the show remotes.
   * 
   * @return the show remotes
   */
  public String getShowRemotes()
  {
    return file.getProperty( "ShowRemotes", "All" );
  }

  /**
   * Sets the show remotes.
   * 
   * @param str
   *          the new show remotes
   */
  public void setShowRemotes( String str )
  {
    file.setProperty( "ShowRemotes", str );
  }

  /**
   * Gets the preferred remotes.
   * 
   * @return the preferred remotes
   */
  public Collection< Remote > getPreferredRemotes()
  {
    RemoteManager rm = RemoteManager.getRemoteManager();
    java.util.List< Remote > preferredRemotes = new ArrayList< Remote >();
    for ( int i = 0; true; i++ )
    {
      String name = file.getProperty( "PreferredRemotes." + i );
      if ( name == null )
        break;
      Remote r = rm.findRemoteByName( name );
      if ( r != null )
        preferredRemotes.add( r );
    }
    return preferredRemotes;
  }

  /**
   * Sets the preferred remotes.
   * 
   * @param remotes
   *          the new preferred remotes
   */
  public void setPreferredRemotes( Collection< Remote > remotes )
  {
    for ( int i = 0; true; i++ )
    {
      String name = file.getProperty( "PreferredRemotes." + i );
      if ( name == null )
        break;
      file.remove( name );
    }
    int i = 0;
    for ( Remote remote : remotes )
    {
      file.setProperty( "PreferredRemotes." + i, remote.getName() );
      ++i;
    }
  }

  /**
   * Save.
   * 
   * @param recentFileMenu
   *          the recent file menu
   * @throws Exception
   *           the exception
   */
  public void save( JMenu recentFileMenu ) throws Exception
  {
    for ( int i = 0; i < recentFileMenu.getItemCount(); i++ )
    {
      JMenuItem item = recentFileMenu.getItem( i );
      file.setProperty( "RecentUpgrades." + i, item.getText() );
    }

    file.save();
  }

  /**
   * Gets the rDF path.
   * 
   * @return the rDF path
   */
  public File getRDFPath()
  {
    return file.getFileProperty( "RDFPath" );
  }

  /**
   * Gets the upgrade path.
   * 
   * @return the upgrade path
   */
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

  /**
   * Sets the upgrade path.
   * 
   * @param path
   *          the new upgrade path
   */
  public void setUpgradePath( File path )
  {
    file.setProperty( "UpgradePath", path );
  }

  /**
   * Gets the binary upgrade path.
   * 
   * @return the binary upgrade path
   */
  public File getBinaryUpgradePath()
  {
    File path = file.getFileProperty( "BinaryUpgradePath", getUpgradePath() );
    if ( path != null )
      return path;

    return getUpgradePath();
  }

  /**
   * Sets the binary upgrade path.
   * 
   * @param path
   *          the new binary upgrade path
   */
  public void setBinaryUpgradePath( File path )
  {
    file.setProperty( "BinaryUpgradePath", path );
  }

  /**
   * Gets the bounds.
   * 
   * @return the bounds
   */
  public Rectangle getBounds()
  {
    String temp = file.getProperty( "KMBounds" );
    if ( temp == null )
      return null;
    Rectangle bounds = new Rectangle();
    StringTokenizer st = new StringTokenizer( temp, "," );
    bounds.x = Integer.parseInt( st.nextToken() );
    bounds.y = Integer.parseInt( st.nextToken() );
    bounds.width = Integer.parseInt( st.nextToken() );
    bounds.height = Integer.parseInt( st.nextToken() );
    return bounds;
  }

  /**
   * Sets the bounds.
   * 
   * @param bounds
   *          the new bounds
   */
  public void setBounds( Rectangle bounds )
  {
    file.setProperty( "KMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );
  }

  /**
   * Gets the last remote name.
   * 
   * @return the last remote name
   */
  public String getLastRemoteName()
  {
    return file.getProperty( "Remote.name" );
  }

  /**
   * Sets the last remote name.
   * 
   * @param name
   *          the new last remote name
   */
  public void setLastRemoteName( String name )
  {
    file.setProperty( "Remote.name", name );
  }

  /**
   * Gets the last remote signature.
   * 
   * @return the last remote signature
   */
  public String getLastRemoteSignature()
  {
    return file.getProperty( "Remote.signature" );
  }

  /**
   * Sets the last remote signature.
   * 
   * @param sig
   *          the new last remote signature
   */
  public void setLastRemoteSignature( String sig )
  {
    file.setProperty( "Remote.signature", sig );
  }

  /**
   * Sets the look and feel.
   * 
   * @param lf
   *          the new look and feel
   */
  public void setLookAndFeel( String lf )
  {
    file.setProperty( "LookAndFeel", lf );
  }

  /**
   * Gets the look and feel.
   * 
   * @return the look and feel
   */
  public String getLookAndFeel()
  {
    return file.getProperty( "LookAndFeel" );
  }

  /**
   * Sets the font size adjustment.
   * 
   * @param adjustment
   *          the new font size adjustment
   */
  public void setFontSizeAdjustment( Float adjustment )
  {
    file.setProperty( "FontSizeAdjustment", Float.toString( adjustment ) );
  }

  /**
   * Gets the font size adjustment.
   * 
   * @return the font size adjustment
   */
  public float getFontSizeAdjustment()
  {
    float rc = 0.0f;
    String temp = file.getProperty( "FontSizeAdjustment" );
    if ( temp != null )
      rc = Float.parseFloat( temp );
    return rc;
  }

  /**
   * Gets the prompt to save.
   * 
   * @return the prompt to save
   */
  public String getPromptToSave()
  {
    return file.getProperty( "PromptToSave", "Always" );
  }

  /**
   * Sets the prompt to save.
   * 
   * @param prompt
   *          the new prompt to save
   */
  public void setPromptToSave( String prompt )
  {
    file.setProperty( "PromptToSave", prompt );
  }

  /**
   * Gets the use custom names.
   * 
   * @return the use custom names
   */
  public boolean getUseCustomNames()
  {
    return file.getProperty( "UseCustomNames" ) != null;
  }

  /**
   * Sets the use custom names.
   * 
   * @param flag
   *          the new use custom names
   */
  public void setUseCustomNames( boolean flag )
  {
    if ( flag )
      file.setProperty( "UseCustomNames", "yes" );
    else
      file.remove( "UseCustomNames" );
  }

  public boolean getShowRemoteSignature()
  {
    return file.getProperty( "ShowRemoteSignature" ) != null;
  }

  public void setShowRemoteSignature( boolean flag )
  {
    if ( flag )
    {
      file.setProperty( "ShowRemoteSignature", "yes" );
    }
    else
    {
      file.remove( "ShowRemoteSignature" );
    }
  }

  public File getFileProperty( String name )
  {
    return file.getFileProperty( name );
  }

  public void setProperty( String name, File value )
  {
    file.setProperty( name, value );
  }

  /** The file. */
  private PropertyFile file;

  /** The Constant upgradeDirectory. */
  private final static String upgradeDirectory = "Upgrades";
}
