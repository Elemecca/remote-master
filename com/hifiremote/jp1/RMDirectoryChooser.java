package com.hifiremote.jp1;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.l2fprod.common.swing.JDirectoryChooser;

public class RMDirectoryChooser extends JDirectoryChooser
{
  private String extension = null;
  private String type = null;
  
  public RMDirectoryChooser( File file, String extension, String type )
  {
    super( file );
    setShowingCreateDirectory( false );
    Dimension d = getPreferredSize();
    d.width = d.height;
    setPreferredSize( d );
    this.extension = extension;
    this.type = type;
  }
  
  FilenameFilter filter = new FilenameFilter()
  {
    @Override
    public boolean accept( File dir, String name )
    {
      int dot = name.lastIndexOf( '.' );
      if ( dot < 0 )
      {
        return false;
      }
      return name.substring( dot ).toLowerCase().equals( extension );
    }
  };
  
  @Override
  public void approveSelection() 
  {
    File[] files = getSelectedFile().listFiles( filter );
    if ( files.length == 0 )
    { 
      JOptionPane.showMessageDialog( null, 
          "There are no " + type + " files in this directory.  Please choose another.",
          "Error", JOptionPane.ERROR_MESSAGE );
      return; 
    } 
    else
    {
      super.approveSelection();
    }
  }
  
  @Override
  public JDialog createDialog( Component parent ) throws HeadlessException 
  {
    dialog = super.createDialog( parent );
    return dialog;
  }
  
  public JDialog getDialog()
  {
    return dialog;
  }

  private JDialog dialog = null;
};
