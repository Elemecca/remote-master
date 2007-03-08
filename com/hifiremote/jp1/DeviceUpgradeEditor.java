package com.hifiremote.jp1;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

public class DeviceUpgradeEditor
  extends JDialog
  implements ActionListener
{
  public DeviceUpgradeEditor( JFrame owner, DeviceUpgrade deviceUpgrade, Remote[] remotes )
  {
    super( owner, "Device Upgrade Editor", true );
    createGUI( owner, deviceUpgrade, remotes );
  }

  public DeviceUpgradeEditor( JDialog owner, DeviceUpgrade deviceUpgrade, Remote[] remotes )
  {
    super( owner, "Device Upgrade Editor", true );
    createGUI( owner, deviceUpgrade, remotes );
  }

  private void createGUI( Window owner, DeviceUpgrade deviceUpgrade, Remote[] remotes )
  {
    setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
    addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent event )
      {
        cancelButton.doClick();
      }
    });
    editorPanel = new DeviceEditorPanel( deviceUpgrade, remotes );
    add( editorPanel, BorderLayout.CENTER );

    Box buttonBox = Box.createHorizontalBox();
    buttonBox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
    add( buttonBox, BorderLayout.SOUTH );

    buttonBox.add( loadButton );
    buttonBox.add( Box.createHorizontalStrut( 5 ));
    buttonBox.add( importButton );
    buttonBox.add( Box.createHorizontalStrut( 5 ));
    buttonBox.add( saveAsButton );
    buttonBox.add( Box.createHorizontalGlue());
    buttonBox.add( okButton );
    buttonBox.add( Box.createHorizontalStrut( 5 ));
    buttonBox.add( cancelButton );

    loadButton.setToolTipText( "Load a device upgrade from a file." );
    importButton.setToolTipText( "Load a device upgrade from the clipboard." );
    saveAsButton.setToolTipText( "Save the device upgrade to a file." );
    okButton.setToolTipText( "Commit any changes and dismiss the dialog." );
    cancelButton.setToolTipText( "Dismiss the dialog without commiting changes." );

    loadButton.addActionListener( this );
    importButton.addActionListener( this );
    saveAsButton.addActionListener( this );
    okButton.addActionListener( this );
    cancelButton.addActionListener( this );
    pack();
    setLocationRelativeTo( owner );
    setVisible( true );
  }

  public DeviceUpgrade getDeviceUpgrade()
  {
    if ( cancelled )
      return null;

    return editorPanel.getDeviceUpgrade();
  }

  public void actionPerformed( ActionEvent e )
  {
    try
    {
      Object source = e.getSource();
      if ( source == cancelButton )
      {
        cancelled = true;
        setVisible( false );
      }
      else if ( source == okButton )
        setVisible( false );
      else if ( source == loadButton )
        load();
      else if ( source == importButton )
        importFromClipboard();
      else if ( source == saveAsButton )
        save();
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  public void load()
    throws Exception
  {
    System.err.println( "DeviceUpgradeEditor.load()" );
    File file = null;
    RMFileChooser chooser = new RMFileChooser();
    try
    {
      chooser.setAcceptAllFileFilterUsed( false );
    }
    catch ( Exception e )
    {
      e.printStackTrace( System.err );
    }
    String[] endings = { ".rmdu", ".txt" };
    chooser.setFileFilter( new EndingFileFilter( "All device upgrade files", endings ));
    endings = new String[ 1 ];
    endings[ 0 ] = ".txt";
    chooser.addChoosableFileFilter( new EndingFileFilter( "KeyMapMaster device upgrade files (*.txt)", endings ));
    endings[ 0 ] = ".rmdu";
    chooser.addChoosableFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files (*.rmdu)", endings ));

    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
    String dir = rm.getProperties().getProperty( "UpgradePath" );
    if ( dir != null )
      chooser.setCurrentDirectory( new File( dir ));
    while ( true )
    {
      if ( chooser.showOpenDialog( rm ) == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();

        int rc = JOptionPane.YES_OPTION;
        if ( !file.exists())
        {
          JOptionPane.showMessageDialog( rm,
                                         file.getName() + " doesn't exist.",
                                         "File doesn't exist.",
                                         JOptionPane.ERROR_MESSAGE );
        }
        else if ( file.isDirectory())
        {
          JOptionPane.showMessageDialog( rm,
                                         file.getName() + " is a directory.",
                                         "File doesn't exist.",
                                         JOptionPane.ERROR_MESSAGE );
        }
        else
          break;
      }
      else
        return;
    }

    System.err.println( "Opening " + file.getCanonicalPath() + ", last modified " +
                        DateFormat.getInstance().format( new Date( file.lastModified())));    DeviceUpgrade deviceUpgrade = editorPanel.getDeviceUpgrade();

                        Remote remote = deviceUpgrade.getRemote();
    deviceUpgrade.reset();
    deviceUpgrade.load( file );
    rm.getProperties().put( "UpgradePath", file.getParent());
    deviceUpgrade.setRemote( remote );
    editorPanel.refresh();
  }

  public void importFromClipboard()
  {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable clipData = clipboard.getContents( clipboard );
    if ( clipData != null )
    {
      try
      {
        if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ))
        {
          String s =
            ( String )( clipData.getTransferData( DataFlavor.stringFlavor ));
          BufferedReader in = new BufferedReader( new StringReader( s ));
          DeviceUpgrade deviceUpgrade = editorPanel.getDeviceUpgrade();
          Remote remote = deviceUpgrade.getRemote();
          deviceUpgrade.reset();
          deviceUpgrade.load( in );
          deviceUpgrade.setRemote( remote );
          editorPanel.refresh();
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace( System.err );
      }
    }
  }

  public void save()
    throws IOException
  {
    System.err.println( "DeviceUpgradeEditor.save()" );
    DeviceUpgrade deviceUpgrade = editorPanel.getDeviceUpgrade();
    RMFileChooser chooser = new RMFileChooser();
    String[] endings = { ".rmdu" };
    chooser.setFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files (*.rmdu)", endings ));
    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
    File f = deviceUpgrade.getFile();
    if ( f != null )
      chooser.setSelectedFile( f );
    else
    {
      String path = rm.getProperties().getProperty( "UpgradePath" );
      if ( path != null )
        chooser.setCurrentDirectory( new File( path ));
    }

    int returnVal = chooser.showSaveDialog( rm );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ".rmdu" ))
        name = name + ".rmdu";
      File file = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( file.exists())
      {
        rc = JOptionPane.showConfirmDialog( rm,
                                            file.getName() + " already exists.  Do you want to replace it?",
                                            "Replace existing file?",
                                            JOptionPane.YES_NO_OPTION );
      }
      if ( rc == JOptionPane.YES_OPTION )
        deviceUpgrade.store( file );
    }
  }

  private boolean cancelled = false;
  private DeviceEditorPanel editorPanel = null;
  private JButton loadButton = new JButton( "Load" );
  private JButton importButton = new JButton( "Import" );
  private JButton saveAsButton = new JButton( "Save" );
  private JButton okButton = new JButton( "OK" );
  private JButton cancelButton = new JButton( "Cancel" );
}
