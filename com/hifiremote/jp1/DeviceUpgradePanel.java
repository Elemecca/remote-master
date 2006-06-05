package com.hifiremote.jp1;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import java.beans.PropertyChangeListener;

public class DeviceUpgradePanel
  extends RMTablePanel
  implements ListSelectionListener, ChangeListener, ActionListener
{
  public DeviceUpgradePanel()
  {
    super( new DeviceUpgradeTableModel(), BorderLayout.LINE_START );
    table.getSelectionModel().addListSelectionListener( this );
    tabbedPane = new JTabbedPane();
    tabbedPane.setBorder( BorderFactory.createTitledBorder( "Device Information" ));
    tabbedPane.addChangeListener( this );
    add( tabbedPane, BorderLayout.CENTER );
    
    loadButton = new JButton( "Load" );
    loadButton.setMnemonic( KeyEvent.VK_L );
    loadButton.setToolTipText( "Load a device upgrade from a file." );
    loadButton.addActionListener( this );
    super.buttonPanel.add( loadButton );
    
    importButton = new JButton( "Import" );
    importButton.setMnemonic( KeyEvent.VK_I );
    importButton.setToolTipText( "Import a device upgrade from the clipboard." );
    importButton.addActionListener( this );
    super.buttonPanel.add( importButton );
    
    exportButton = new JButton( "Export" );
    exportButton.setMnemonic( KeyEvent.VK_X );
    exportButton.setToolTipText( "Save the current device upgrade to a file." );
    exportButton.addActionListener( this );
    super.buttonPanel.add( exportButton );
    
    setupPanel = new SetupPanel( null );
    tabbedPane.add( "Setup", setupPanel );

    functionPanel = new FunctionPanel( null );
    tabbedPane.add( "Functions", functionPanel );
    
    buttonPanel = new ButtonPanel( null );
    tabbedPane.add( "Buttons", buttonPanel );
    
    layoutPanel = new LayoutPanel( null );
    tabbedPane.add( "Layout", layoutPanel );
    
    keyMapPanel = new KeyMapPanel( null );
    tabbedPane.add( "Key Map", keyMapPanel );
    currPanel = ( KMPanel )tabbedPane.getSelectedComponent();
  }

  public void set( RemoteConfiguration remoteConfig )
  {
    (( DeviceUpgradeTableModel )model ).set( remoteConfig );
    this.remoteConfig = remoteConfig;
//    buttonPanel.setDeviceUpgrade( remoteConfig.getDeviceUpgrade());
  }
  
  public Object createRowObject()
  {
    System.err.println( "DeviceUpgradePanel.createRowObject()" );
    DeviceUpgrade upgrade = new DeviceUpgrade();
    upgrade.reset();
    upgrade.setRemote( remoteConfig.getRemote());
    return upgrade;
  }

  // Interface ListSelectionListener
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() )
    {
      int row = table.getSelectedRow();
      DeviceUpgrade upgrade = null;
      if ( row != -1 )
        upgrade = ( DeviceUpgrade )getRowObject( row );
      exportButton.setEnabled( row != -1 );
      loadButton.setEnabled( row != -1 );
      importButton.setEnabled( row != -1 );
      setupPanel.setDeviceUpgrade( upgrade );
      functionPanel.setDeviceUpgrade( upgrade );
      buttonPanel.setDeviceUpgrade( upgrade );
      layoutPanel.setDeviceUpgrade( upgrade );
      keyMapPanel.setDeviceUpgrade( upgrade );
      currPanel.update();
    }
  }
  
  private KMPanel currPanel = null;
  public void stateChanged( ChangeEvent e )
  {
    if ( currPanel != null )
      currPanel.commit();
    currPanel = ( KMPanel )(( JTabbedPane )e.getSource()).getSelectedComponent();
    currPanel.update();
    // SwingUtilities.updateComponentTreeUI( currPanel );
    // validateUpgrade();
  }
  
  public void actionPerformed( ActionEvent e )
  {
    try
    {
      Object source = e.getSource();
      int row = table.getSelectedRow();
      DeviceUpgrade deviceUpgrade = ( DeviceUpgrade )getRowObject( row ); 
      if ( source == exportButton )
        export( deviceUpgrade );
      else if ( source == loadButton )
      {
        load( deviceUpgrade );
        model.fireTableDataChanged();
        table.setRowSelectionInterval( row, row );
      }
      else if ( source == importButton )
      {
//        importFromClipboard( deviceUpgrade );
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
    RMFileChooser chooser = new RMFileChooser();
    String[] endings = { ".rmdu" };
    chooser.setFileFilter( new EndingFileFilter( "RemoteMaster device upgrade files (*.rmdu)", endings ));
    File f = deviceUpgrade.getFile();
    if ( f != null )
      chooser.setSelectedFile( f );
    JFrame frame = ( JFrame )SwingUtilities.getAncestorOfClass( JFrame.class, this );
    int returnVal = chooser.showSaveDialog( frame );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ".rmdu" ))
        name = name + ".rmdu";
      File file = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( file.exists())
      {
        rc = JOptionPane.showConfirmDialog( frame,
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

    JFrame frame = ( JFrame )SwingUtilities.getAncestorOfClass( JFrame.class, this );
    
    while ( true )
    {
      if ( chooser.showOpenDialog( frame ) == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();
  
        int rc = JOptionPane.YES_OPTION;
        if ( !file.exists())
        {
          JOptionPane.showMessageDialog( frame,
                                         file.getName() + " doesn't exist.",
                                         "File doesn't exist.",
                                         JOptionPane.ERROR_MESSAGE );
        }
        else if ( file.isDirectory())
        {
          JOptionPane.showMessageDialog( frame,
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
    deviceUpgrade.setRemote( remoteConfig.getRemote());
  }
  
  private RemoteConfiguration remoteConfig;
  private JTabbedPane tabbedPane = null;
  private SetupPanel setupPanel = null;
  private FunctionPanel functionPanel = null;
  private ButtonPanel buttonPanel = null;
  private LayoutPanel layoutPanel = null;
  private KeyMapPanel keyMapPanel = null;
  private JButton loadButton = null;
  private JButton importButton = null;
  private JButton exportButton = null;
}
  
