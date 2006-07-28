package com.hifiremote.jp1;

import java.io.*;
import java.util.*;
import javax.swing.*;

public class PropertyFile
  extends Properties
{
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
  
  public void save()
    throws IOException
  {
    FileOutputStream out = new FileOutputStream( file );
    store( out, null );
    out.close();
  }
  
  public void setProperty( String name, File file )
  {
    setProperty( name, file.getAbsolutePath());
  }
  
  public File getFileProperty( String name )
  {
    String value = getProperty( name );
    if ( value == null )
      return null;
    return new File( value );
  }
  
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
  
  public void updateFileMenu( JMenu menu, String prefix )
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
          menu.add( new FileAction( f ));
        else
          remove( name );
      }
    } while ( f != null );
    menu.setEnabled( menu.getItemCount() > 0 );
  }
  
  public File getFile(){ return file; }

  private File file = null;
}
