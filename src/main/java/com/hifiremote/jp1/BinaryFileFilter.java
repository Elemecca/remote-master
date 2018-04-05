package com.hifiremote.jp1;

import java.io.File;
  
// TODO: Auto-generated Javadoc
/**
 * The Class BinaryFileFilter.
 */
public class BinaryFileFilter
    extends javax.swing.filechooser.FileFilter
{
  
  /**
   * Instantiates a new binary file filter.
   */
  public BinaryFileFilter(){}
  
  /**
   * Instantiates a new binary file filter.
   * 
   * @param tag the tag
   */
  public BinaryFileFilter( String tag )
  {
    ending = "_" + tag + ending;
  }

  //Accept all directories and all .km/.rmdu files.
  /* (non-Javadoc)
   * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
   */
  public boolean accept( File f )
  {
    boolean rc = false;
    if ( f.isDirectory())
      rc = true;
    else
    {
      String lowerName = f.getName().toLowerCase();
      if ( lowerName.endsWith( ending.toLowerCase()))
        rc = true;
    }
    return rc;
  }

  //The description of this filter
  /* (non-Javadoc)
   * @see javax.swing.filechooser.FileFilter#getDescription()
   */
  public String getDescription()
  {
    return "Binary upgrade files (*" + ending + ")";
  }

  /**
   * Gets the ending.
   * 
   * @return the ending
   */
  public String getEnding(){ return ending; }

  /** The ending. */
  private String ending = ".bin";
}
