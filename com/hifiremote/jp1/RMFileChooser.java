package com.hifiremote.jp1;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Class RMFileChooser.
 */
public class RMFileChooser
  extends JFileChooser
{
  
  /**
   * Instantiates a new rM file chooser.
   */
  public RMFileChooser()
  {
    super();
  }

  /**
   * Instantiates a new rM file chooser.
   * 
   * @param currentDirectory the current directory
   */
  public RMFileChooser( File currentDirectory )
  {
    super( currentDirectory );
  }

  /**
   * Instantiates a new rM file chooser.
   * 
   * @param currentDirectory the current directory
   * @param fsv the fsv
   */
  RMFileChooser( File currentDirectory, FileSystemView fsv )
  {
    super( currentDirectory, fsv );
  }

  /**
   * Instantiates a new rM file chooser.
   * 
   * @param fsv the fsv
   */
  RMFileChooser( FileSystemView fsv )
  {
    super( fsv );
  }

  /**
   * Instantiates a new rM file chooser.
   * 
   * @param currentDirectoryPath the current directory path
   */
  RMFileChooser( String currentDirectoryPath )
  {
    super( currentDirectoryPath );
  }

  /**
   * Instantiates a new rM file chooser.
   * 
   * @param currentDirectoryPath the current directory path
   * @param fsv the fsv
   */
  RMFileChooser( String currentDirectoryPath, FileSystemView fsv )
  {
    super( currentDirectoryPath, fsv );
  }

  /* (non-Javadoc)
   * @see javax.swing.JFileChooser#updateUI()
   */
  public void updateUI()
  {
    if ( System.getProperty( "os.name" ).startsWith( "Windows" ) && 
         ( System.getProperty( "java.version" ).compareTo( "1.6.0" ) < 0 ))
    putClientProperty( "FileChooser.useShellFolder", Boolean.FALSE );
    super.updateUI();
  }
}
