package com.hifiremote.jp1;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class RMFileChooser
  extends JFileChooser
{
  public RMFileChooser()
  {
    super();
  }

  public RMFileChooser( File currentDirectory )
  {
    super( currentDirectory );
  }

  RMFileChooser( File currentDirectory, FileSystemView fsv )
  {
    super( currentDirectory, fsv );
  }

  RMFileChooser( FileSystemView fsv )
  {
    super( fsv );
  }

  RMFileChooser( String currentDirectoryPath )
  {
    super( currentDirectoryPath );
  }

  RMFileChooser( String currentDirectoryPath, FileSystemView fsv )
  {
    super( currentDirectoryPath, fsv );
  }

  public void updateUI()
  {
    putClientProperty( "FileChooser.useShellFolder", Boolean.FALSE );
    super.updateUI();
  }
}
