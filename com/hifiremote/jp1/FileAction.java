package com.hifiremote.jp1;

import java.io.*;
import java.awt.event.*;
import javax.swing.*;

class FileAction
  extends AbstractAction
{
  private File file = null;
  public FileAction( File file )
  {
    super( file.getAbsolutePath());
    this.file = file;
  }

  public void actionPerformed( ActionEvent e )
  {
    try
    {
      KeyMapMaster km = KeyMapMaster.getKeyMapMaster();
      if ( km.promptToSaveUpgrade( KeyMapMaster.ACTION_LOAD ))
        km.loadUpgrade( file );
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  public File getFile()
  {
    return file;
  }
}
