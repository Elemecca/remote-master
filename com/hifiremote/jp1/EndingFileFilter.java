package com.hifiremote.jp1;

import java.io.File;

public class EndingFileFilter
    extends javax.swing.filechooser.FileFilter
{
  public EndingFileFilter( String description, String[] endings )
  {
    this.description = description;
    this.endings = endings;
  }

  //Accept all directories and all files that end with one of the endings.
  public boolean accept( File f )
  {
    if ( f.isDirectory())
      return true;
    else
    {
      String fileName = f.getName();
      if ( ignoreCase )
        fileName = fileName.toLowerCase();

      for ( int i = 0; i < endings.length; ++i )
      {
        String ending = endings[ i ];
        if ( ignoreCase )
          ending = ending.toLowerCase();
        if ( fileName.endsWith( ending ))
          return true;
      }
    }
    return false;
  }

  private String description;
  public String getDescription()
  {
    return description;
  }

  public String[] getEndings(){ return endings; }
  private String[] endings = null;
  private boolean ignoreCase = true;
  public boolean getIgnoreCase(){ return ignoreCase; }
  public void setIgnoreCase( boolean ignoreCase )
  {
    this.ignoreCase = ignoreCase;
  }
}
