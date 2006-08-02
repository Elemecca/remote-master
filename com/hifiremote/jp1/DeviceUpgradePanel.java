package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

public class DeviceUpgradePanel
  extends RMTablePanel< DeviceUpgrade >
  implements ListSelectionListener, ChangeListener, ActionListener, DocumentListener
{
  public DeviceUpgradePanel()
  {
    super( new DeviceUpgradeTableModel());
    table.getSelectionModel().addListSelectionListener( this );

    importButton = new JButton( "Import" );
    importButton.addActionListener( this );
    importButton.setMnemonic( KeyEvent.VK_L );
    importButton.setToolTipText( "Import a device upgrade from a file." );
    importButton.setEnabled( false );
    super.buttonPanel.add( importButton );

    exportButton = new JButton( "Export" );
    exportButton.addActionListener( this );
    exportButton.setMnemonic( KeyEvent.VK_X );
    exportButton.setToolTipText( "Export the current device upgrade to a file or the clipboard." );
    exportButton.setEnabled( false );
    super.buttonPanel.add( exportButton );
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( DeviceUpgradeTableModel )model ).set( remoteConfig );
    this.remoteConfig = remoteConfig;
  }
  
  public DeviceUpgrade createRowObject( DeviceUpgrade baseUpgrade )
  {
    System.err.println( "DeviceUpgradePanel.createRowObject()" );
    DeviceUpgrade upgrade = null;
    if ( baseUpgrade == null )
    {
      upgrade = new DeviceUpgrade();
      upgrade.setRemote( remoteConfig.getRemote());
    }
    else
      upgrade = new DeviceUpgrade( baseUpgrade );
      
    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, table );
    Remote[] remotes = new Remote[ 1 ];
    remotes[ 0 ] = remoteConfig.getRemote();
    DeviceUpgradeEditor editor = new DeviceUpgradeEditor( rm, upgrade, remotes );
    return editor.getDeviceUpgrade();
  }

  // Interface ListSelectionListener
  public void valueChanged( ListSelectionEvent e )
  {
    super.valueChanged( e );
    if ( !e.getValueIsAdjusting() )
    {
      DeviceUpgrade upgrade = null;
      int row = table.getSelectedRow();
      if ( row != -1 )
      {
        upgrade = ( DeviceUpgrade )getRowObject( row );
      }
      boolean enableFlag = row != -1;
      exportButton.setEnabled( enableFlag );
      importButton.setEnabled( enableFlag );
      exportButton.setEnabled( enableFlag );
    }
  }
  
  private KMPanel currPanel = null;
  public void stateChanged( ChangeEvent e )
  {
    if ( currPanel != null )
      currPanel.commit();
    currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
    currPanel.setEnabled( true );
    currPanel.update();
  }
  
  public void actionPerformed( ActionEvent e )
  {
    System.err.println( "DeviceUpgradePanel.actionPerformed()" );
    try
    {
      Object source = e.getSource();
      int row = table.getSelectedRow();
      DeviceUpgrade deviceUpgrade = ( DeviceUpgrade )getRowObject( row );
      if ( source == deviceType )
        deviceUpgrade.setDeviceTypeAliasName(( String )deviceType.getSelectedItem());
      else if ( source == exportButton )
        export( deviceUpgrade );
      else if ( source == importButton )
      {
        load( deviceUpgrade );
        model.fireTableDataChanged();
        table.setRowSelectionInterval( row, row );
      }
      else
        super.actionPerformed( e );
    }
    catch ( Exception exp )
    {
      exp.printStackTrace( System.err );
    }
  }
  
  public void export( DeviceUpgrade deviceUpgrade )
    throws IOException
  {
    System.err.println( "DeviceUpgradePanel.export()" );
    RMFileChooser chooser = new RMFileChooser();
    String[] endings = { ".rmdu" };
    chooser.setFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files (*.rmdu)", endings ));
    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
    File f = deviceUpgrade.getFile();
    if ( f != null )
      chooser.setSelectedFile( f );
    else
    {
      String path = rm.getPreferences().getProperty( "UpgradePath" );
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
  
  public void load( DeviceUpgrade deviceUpgrade )
    throws Exception
  {
    System.err.println( "DeviceUpgradePanel.load()" );
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
    String dir = rm.getPreferences().getProperty( "UpgradePath" );
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
    deviceUpgrade.reset();
    deviceUpgrade.load( file );
    rm.getPreferences().put( "UpgradePath", file.getParent());
    deviceUpgrade.setRemote( remoteConfig.getRemote());
  }

  // DocumentListener methods
  private void updateDescription()
  {
    int row = table.getSelectedRow();
    if ( row != -1 )
    {
      DeviceUpgrade upgrade = ( DeviceUpgrade )getRowObject( row );
      upgrade.setDescription( description.getText());
    }
  }
  
  public void changedUpdate( DocumentEvent e )
  {
    updateDescription();
  }

  public void insertUpdate( DocumentEvent e )
  {
    updateDescription();
  }

  public void removeUpdate( DocumentEvent e )
  {
    updateDescription();
  }

  private RemoteConfiguration remoteConfig;
  private JTextField description = new JTextField();
  private JComboBox deviceType = new JComboBox();
  private JTabbedPane tabbedPane = null;
  private SetupPanel setupPanel = null;
  private FunctionPanel functionPanel = null;
  private ButtonPanel buttonPanel = null;
  private LayoutPanel layoutPanel = null;
  private KeyMapPanel keyMapPanel = null;
  private JButton importButton = null;
  private JButton exportButton = null;
}
  
