package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

public class FavScanPanel extends RMTablePanel< FavScan > implements ActionListener
{

  public FavScanPanel()
  {
    super( new FavScanTableModel() );
    ( ( FavScanTableModel )getModel() ).setPanel( this );
    headerPanel = new JPanel( new BorderLayout() );
    deviceBoxPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    deviceBoxPanel.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
    macroLabelPanel = new JPanel( new BorderLayout() );
    deviceButtonBox = new JComboBox();
    deviceButtonBox.addActionListener( this );
    Dimension d = deviceButtonBox.getPreferredSize();
    d.width = 100;
    deviceButtonBox.setPreferredSize( d );

    this.add( headerPanel, BorderLayout.PAGE_START );
    headerPanel.add( deviceBoxPanel, BorderLayout.PAGE_START );
    headerPanel.add( macroLabelPanel, BorderLayout.PAGE_END );

    deviceBoxPanel.add( new JLabel( "Scan device:   " ) );
    deviceBoxPanel.add( deviceButtonBox );
    macroLabelPanel.add( new JLabel( "Scan macros:" ), BorderLayout.LINE_START );
    macroLabelPanel.setBorder( BorderFactory.createEmptyBorder( 10, 5, 5, 5 ) );
  }

  @Override
  protected FavScan createRowObject( FavScan baseFavScan )
  {
    FavScanTableModel favScanTableModel = ( FavScanTableModel )model;
    RemoteConfiguration config = favScanTableModel.getRemoteConfig();
    if ( baseFavScan == null && config.getRemote().getFavKey().isSegregated() && favScanTableModel.getRowCount() == 1 )
    {
      String message = "This remote does not support more than one Fav/Scan entry.";
      String title = "Fav/Scan";
      JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
      return null;
    }
    return FavScanDialog.showDialog( this, baseFavScan, config );
  }

  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( FavScanTableModel )model ).set( remoteConfig );
    table.initColumns( model );
    if ( remoteConfig != null )
    {
      Remote remote = remoteConfig.getRemote();
      if ( !remote.hasFavKey() )
      {
        return;
      }
      DefaultComboBoxModel comboModel = new DefaultComboBoxModel( remote.getDeviceButtons() );
      if ( remote.getFavKey() != null && !remote.getFavKey().isSegregated() )
      {
        // It appears from IR.exe that when Fav/Scan is segregated, there is no option for
        // "none", since there it has the same index, 0, as the first real device button.
        // So it seems better simply not to allow it.
        comboModel.insertElementAt( DeviceButton.noButton, 0 );
      }
      if ( remoteConfig.getFavKeyDevButton() == null )
      {
        remoteConfig.setFavKeyDevButton( ( DeviceButton )comboModel.getElementAt( 0 ) );
      }
      deviceButtonBox.setModel( comboModel );
      deviceButtonBox.setSelectedItem( remoteConfig.getFavKeyDevButton() );
    }
  }

  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    super.valueChanged( e );
    cloneButton.setEnabled( false );
    cloneItem.setEnabled( false );
  }

  @Override
  public void actionPerformed( ActionEvent event )
  {
    Object source = event.getSource();
    if ( source == deviceButtonBox )
    {
      DeviceButton deviceButton = ( DeviceButton )deviceButtonBox.getSelectedItem();
      RemoteConfiguration config = ( ( FavScanTableModel )model ).getRemoteConfig();
      if ( deviceButton != config.getFavKeyDevButton() )
      {
        config.setFavKeyDevButton( deviceButton );
      }
      return;
    }
    super.actionPerformed( event );
  }

  private JPanel deviceBoxPanel = null;
  private JPanel headerPanel = null;
  private JPanel macroLabelPanel = null;
  protected JComboBox deviceButtonBox = null;

}
