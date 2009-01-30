package com.hifiremote.jp1;

import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.*;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertyFile.
 */
public class PropertyFile
  extends Properties
{
  
  /**
   * Instantiates a new property file.
   * 
   * @param file the file
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public PropertyFile( File file )
    throws IOException
  {
    this.file = file;
    if ( file.canRead())
    {
      FileInputStream in = new FileInputStream( file );
      load( in );
      in.close();
      updatePropertyNames( "Bounds", "KMBounds" );
      updatePropertyNames( "RecentFiles.", "RecentUpgrades." );
    }
  }

  /**
   * Update property names.
   * 
   * @param oldBase the old base
   * @param newBase the new base
   */
  private void updatePropertyNames( String oldBase, String newBase )
  {
    int i = 0;
    String value;
    String propertyName;
    if ( oldBase.endsWith( "." ))
    {
      do
      {
        propertyName = oldBase + i;
        value = getProperty( propertyName );
        if ( value != null )
        {
          remove( propertyName );
          setProperty( newBase + i, value );
        }
        ++i;
      } while ( value != null );
    }
    else
    {
      value = getProperty( oldBase );
      if ( value != null )
      {
        remove( oldBase );
        setProperty( newBase, value );
      }
    }
  }

  /**
   * Save.
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void save()
    throws IOException
  {
    FileOutputStream out = new FileOutputStream( file );
    store( out, null );
    out.close();
  }

  /**
   * Sets the property.
   * 
   * @param name the name
   * @param file the file
   */
  public void setProperty( String name, File file )
  {
    setProperty( name, file.getAbsolutePath());
  }

  /**
   * Gets the file property.
   * 
   * @param name the name
   * 
   * @return the file property
   */
  public File getFileProperty( String name )
  {
    String value = getProperty( name );
    if ( value == null )
      return null;
    return new File( value );
  }

  /**
   * Gets the file property.
   * 
   * @param name the name
   * @param defaultFile the default file
   * 
   * @return the file property
   */
  public File getFileProperty( String name, File defaultFile )
  {
    File file = getFileProperty( name );
    if ( file == null )
    {
      setProperty( name, defaultFile );
      return defaultFile;
    }
    return file;
  }

  /**
   * Populate file menu.
   * 
   * @param menu the menu
   * @param prefix the prefix
   * @param l the l
   */
  public void populateFileMenu( JMenu menu, String prefix, ActionListener l )
  {
    File f = null;
    int i = 0;
    do
    {
      String name = prefix + i++;
      f = getFileProperty( name );
      if ( f != null )
      {
        if ( f.canRead() )
        {
          JMenuItem item = new JMenuItem( f.getAbsolutePath());
          menu.add( item );
          item.addActionListener( l );
        }
        else
          remove( name );
      }
    } while ( f != null );
    menu.setEnabled( menu.getItemCount() > 0 );
  }

  /**
   * Gets the file.
   * 
   * @return the file
   */
  public File getFile(){ return file; }

  /** The file. */
  private File file = null;
}
