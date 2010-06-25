package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralPanel.
 */
public class GeneralPanel extends RMPanel implements ListSelectionListener, ActionListener
{

  /**
   * Instantiates a new general panel.
   */
  public GeneralPanel()
  {
    deviceButtonPanel = new JPanel( new BorderLayout() );

    deviceButtonPanel.setBorder( BorderFactory.createTitledBorder( "Device Buttons" ) );

    deviceButtonTable = new JP1Table( deviceModel );
    deviceButtonTable.getSelectionModel().addListSelectionListener( this );
    deviceButtonTable.initColumns( deviceModel );
    deviceButtonTable.addMouseListener( new MouseAdapter()
    {
      public void mouseClicked( MouseEvent e )
      {
        if ( e.getClickCount() != 2 )
          return;
        int row = deviceButtonTable.getSelectedRow();
        if ( row == -1 )
          return;
        if ( !deviceButtonTable.isCellEditable( row, deviceButtonTable.columnAtPoint( e.getPoint() ) ) )
          editUpgradeInRow( row );
      }
    } );

    deviceScrollPane = new JScrollPane( deviceButtonTable );
    deviceButtonPanel.add( deviceScrollPane, BorderLayout.CENTER );
    JPanel editPanel = new JPanel();
    editButton = new JButton( "Edit Device" );
    editButton.setEnabled( false );
    editPanel.add( editButton );
    editButton.addActionListener( this );
    deviceButtonPanel.add( editPanel, BorderLayout.PAGE_END );

    // deviceScrollPane.setPreferredSize( deviceButtonPanel.getPreferredSize() );

    // now the other settings table
    settingTable = new JP1Table( settingModel );
    settingTable.setCellEditorModel( settingModel );
    settingTable.initColumns( settingModel );

    settingsScrollPane = new JScrollPane( settingTable );
    settingsScrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory
        .createTitledBorder( "Other Settings" ), settingsScrollPane.getBorder() ) );
    // settingsScrollPane.setPreferredSize( settingTable.getPreferredSize() );
    upperPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, deviceButtonPanel, settingsScrollPane );
    upperPane.setResizeWeight( 0.5 );

    notes = new JTextArea( 6, 20 );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    notesScrollPane = new JScrollPane( notes );
    notesScrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "General Notes" ),
        notesScrollPane.getBorder() ) );

    mainPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT, upperPane, notesScrollPane );
    mainPane.setResizeWeight( 0.7 );

    add( mainPane, BorderLayout.CENTER );

    adjustPreferredViewportSizes();
  }

  private void adjustPreferredViewportSizes()
  {
    int rows = 8;
    if ( remoteConfig != null )
    {
      rows = Math.min( 8, remoteConfig.getRemote().getDeviceButtons().length );
    }
    Dimension d = deviceButtonTable.getPreferredSize();
    d.height = deviceButtonTable.getRowHeight() * rows;
    deviceButtonTable.setPreferredScrollableViewportSize( d );

    rows = 10;
    if ( remoteConfig != null )
    {
      rows = Math.min( 12, remoteConfig.getRemote().getSettings().length );
    }
    d = settingTable.getPreferredSize();
    d.height = 12 * settingTable.getRowHeight();
    settingTable.setPreferredScrollableViewportSize( d );

    upperPane.resetToPreferredSizes();
    mainPane.resetToPreferredSizes();
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    setInProgress = true;
    this.remoteConfig = remoteConfig;
    deviceModel.set( remoteConfig );
    deviceButtonTable.initColumns( deviceModel );

    settingModel.set( remoteConfig );
    settingTable.initColumns( settingModel );

    String text = remoteConfig.getNotes();
    if ( text == null )
    {
      text = "";
    }
    notes.setText( text );

    adjustPreferredViewportSizes();

    validate();
    setInProgress = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMPanel#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( listener != null )
    {
      if ( deviceModel != null )
        deviceModel.addPropertyChangeListener( listener );
      if ( settingModel != null )
        settingModel.addPropertyChangeListener( listener );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() && !setInProgress )
    {
      int selectedRow = deviceButtonTable.getSelectedRow();
      Remote remote = remoteConfig.getRemote();
      DeviceButton deviceButton = remote.getDeviceButtons()[ selectedRow ];
      selectedUpgrade = remoteConfig.getAssignedDeviceUpgrade( deviceButton );

      editButton.setEnabled( selectedUpgrade != null );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed( ActionEvent arg0 )
  {
    editUpgradeInRow( deviceButtonTable.getSelectedRow() );
  }

  public void editUpgradeInRow( int row )
  {
    if ( row == -1 )
      return;

    DeviceUpgrade newUpgrade = new DeviceUpgrade( selectedUpgrade );
    RemoteMaster rm = ( RemoteMaster )SwingUtilities.getAncestorOfClass( RemoteMaster.class, this );
    List< Remote > remotes = new ArrayList< Remote >( 1 );
    remotes.add( remoteConfig.getRemote() );
    DeviceUpgradeEditor editor = new DeviceUpgradeEditor( rm, newUpgrade, remotes );
    newUpgrade = editor.getDeviceUpgrade();
    if ( newUpgrade == null )
      return;

    ListIterator< DeviceUpgrade > upgrades = remoteConfig.getDeviceUpgrades().listIterator();
    while ( upgrades.hasNext() )
    {
      DeviceUpgrade upgrade = upgrades.next();
      if ( upgrade == selectedUpgrade )
      {
        upgrades.set( newUpgrade );
        deviceModel.setValueAt( newUpgrade.getDeviceType(), row, 2 );
        deviceModel.setValueAt( new SetupCode( newUpgrade.getSetupCode() ), row, 3 );
        deviceModel.fireTableRowsUpdated( row, row );
        break;
      }
    }
  }

  private RemoteConfiguration remoteConfig = null;

  private JSplitPane upperPane = null;
  private JSplitPane mainPane = null;

  private JPanel deviceButtonPanel = null;

  private JScrollPane deviceScrollPane = null;
  private JScrollPane settingsScrollPane = null;
  private JScrollPane notesScrollPane = null;

  /** The device model. */
  private JP1Table deviceButtonTable = null;
  private DeviceButtonTableModel deviceModel = new DeviceButtonTableModel();

  /** The setting model. */
  private JP1Table settingTable = null;
  private SettingsTableModel settingModel = new SettingsTableModel();

  /** The notes. */
  private JTextArea notes = null;

  private JButton editButton = null;
  private DeviceUpgrade selectedUpgrade = null;
  private boolean setInProgress = false;
}
