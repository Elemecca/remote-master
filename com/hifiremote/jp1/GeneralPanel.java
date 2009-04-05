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
    JPanel panel = new JPanel( new BorderLayout( 5, 0 ) );
    add( panel, BorderLayout.PAGE_START );

    JPanel deviceButtonPanel = new JPanel( new BorderLayout() );
    panel.add( deviceButtonPanel, BorderLayout.LINE_START );

    deviceButtonPanel.setBorder( BorderFactory.createTitledBorder( "Device Buttons" ) );

    // first the device button table.
    // JTableX table = new JTableX( deviceModel );
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

    JScrollPane scrollPane = new JScrollPane( deviceButtonTable );
    deviceButtonPanel.add( scrollPane, BorderLayout.CENTER );
    JPanel editPanel = new JPanel();
    editButton = new JButton( "Edit Device" );
    editButton.setEnabled( false );
    editPanel.add( editButton );
    editButton.addActionListener( this );
    deviceButtonPanel.add( editPanel, BorderLayout.PAGE_END );

    Dimension d = deviceButtonTable.getPreferredSize();
    d.height = 10 * deviceButtonTable.getRowHeight();
    deviceButtonTable.setPreferredScrollableViewportSize( d );

    // now the other settings table
    settingTable = new JP1Table( settingModel );
    settingTable.setCellEditorModel( settingModel );
    settingTable.initColumns( settingModel );

    scrollPane = new JScrollPane( settingTable );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Other Settings" ),
        scrollPane.getBorder() ) );
    panel.add( scrollPane, BorderLayout.CENTER );

    d = settingTable.getPreferredScrollableViewportSize();
    d.width = settingTable.getPreferredSize().width;
    d.height = 10 * settingTable.getRowHeight();
    settingTable.setPreferredScrollableViewportSize( d );

    notes = new JTextArea( 10, 20 );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    scrollPane = new JScrollPane( notes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "General Notes" ),
        scrollPane.getBorder() ) );

    add( scrollPane, BorderLayout.CENTER );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    this.remoteConfig = remoteConfig;
    deviceModel.set( remoteConfig );
    deviceButtonTable.initColumns( deviceModel );
    Dimension d = deviceButtonTable.getPreferredSize();
    int rows = Math.min( 12, remoteConfig.getRemote().getDeviceButtons().length );
    d.height = rows * deviceButtonTable.getRowHeight();
    deviceButtonTable.setPreferredScrollableViewportSize( d );

    settingModel.set( remoteConfig );
    settingTable.initColumns( settingModel );
    d = settingTable.getPreferredSize();
    rows = Math.min( remoteConfig.getRemote().getSettings().length, 12 );
    d.height = rows * settingTable.getRowHeight();
    settingTable.setPreferredScrollableViewportSize( d );

    String text = remoteConfig.getNotes();
    if ( text != null )
    {
      notes.setText( text );
    }
    validate();
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
    if ( !e.getValueIsAdjusting() )
    {
      int selectedRow = deviceButtonTable.getSelectedRow();
      Remote remote = remoteConfig.getRemote();
      DeviceButton deviceButton = remote.getDeviceButtons()[ selectedRow ];
      short[] data = remoteConfig.getData();
      DeviceType deviceType = remote.getDeviceTypeByIndex( deviceButton.getDeviceTypeIndex( data ) );
      int setupCode = deviceButton.getSetupCode( data );
      selectedUpgrade = null;
      for ( DeviceUpgrade deviceUpgrade : remoteConfig.getDeviceUpgrades() )
      {
        if ( deviceUpgrade.setupCode == setupCode && deviceUpgrade.getDeviceType() == deviceType )
        {
          selectedUpgrade = deviceUpgrade;
          break;
        }
      }

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
}
