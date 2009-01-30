package com.hifiremote.jp1;

import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Class EndingFileFilter.
 */
public class EndingFileFilter
    extends javax.swing.filechooser.FileFilter
{
  
  /**
   * Instantiates a new ending file filter.
   * 
   * @param description the description
   * @param endings the endings
   */
  public EndingFileFilter( String description, String[] endings )
  {
    this.description = description;
    this.endings = endings;
  }

  //Accept all directories and all files that end with one of the endings.
  /* (non-Javadoc)
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
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

  /** The description. */
  private String description;
  
  /* (non-Javadoc)
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Gets the endings.
   * 
   * @return the endings
   */
  public String[] getEndings(){ return endings; }
  
  /** The endings. */
  private String[] endings = null;
  
  /** The ignore case. */
  private boolean ignoreCase = true;
  
  /**
   * Gets the ignore case.
   * 
   * @return the ignore case
   */
  public boolean getIgnoreCase(){ return ignoreCase; }
  
  /**
   * Sets the ignore case.
   * 
   * @param ignoreCase the new ignore case
   */
  public void setIgnoreCase( boolean ignoreCase )
  {
    this.ignoreCase = ignoreCase;
  }
}
