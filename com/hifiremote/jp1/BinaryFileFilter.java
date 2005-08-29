package com.hifiremote.jp1;

import java.io.File;
  
public class BinaryFileFilter
    extends javax.swing.filechooser.FileFilter
{
  public BinaryFileFilter(){}
  
  public BinaryFileFilter( String tag )
  {
    ending = "_" + tag + ending;
  }

  //Accept all directories and all .km/.rmdu files.
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
  public String getDescription()
  {
    return "Binary upgrade files (*" + ending + ")";
  }

  public String getEnding(){ return ending; }

  private String ending = ".bin";
}
