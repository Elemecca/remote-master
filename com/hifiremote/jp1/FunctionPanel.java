package com.hifiremote.jp1;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;

import com.hifiremote.jp1.GeneralFunction.User;
import com.hifiremote.jp1.GeneralFunction.RMIcon;

// TODO: Auto-generated Javadoc
/**
 * The Class FunctionPanel.
 */
public class FunctionPanel extends TablePanel< Function >
{

  /**
   * Instantiates a new function panel.
   * 
   * @param devUpgrade
   *          the dev upgrade
   */
  public FunctionPanel( DeviceUpgrade devUpgrade )
  {
    super( "Functions", devUpgrade, new FunctionTableModel( devUpgrade ) );
    Remote remote = devUpgrade.getRemote();
    RemoteConfiguration remoteConfig = devUpgrade.getRemoteConfig();
    iconLabel = new JLabel( "   " );
    iconLabel.setPreferredSize( new Dimension( 100, 40 ) );
    iconLabel.setHorizontalTextPosition( SwingConstants.LEADING );
    iconLabel.setVisible( remote.isSSD() && remoteConfig != null );
    buttonPanel.add( Box.createVerticalStrut( iconLabel.getPreferredSize().height ) );
    buttonPanel.add( iconLabel );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.TablePanel#setDeviceUpgrade(com.hifiremote.jp1.DeviceUpgrade)
   */
  public void setDeviceUpgrade( DeviceUpgrade deviceUpgrade )
  {
    ( ( FunctionTableModel )model ).setDeviceUpgrade( deviceUpgrade );
    super.setDeviceUpgrade( deviceUpgrade );
    Remote remote = deviceUpgrade.getRemote();
    RemoteConfiguration config = deviceUpgrade.getRemoteConfig();
    iconLabel.setVisible( remote.isSSD() && config != null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.TablePanel#update()
   */
  public void update()
  {
    if ( deviceUpgrade == null )
      return;
    Protocol p = deviceUpgrade.getProtocol();
    p.initializeParms();
    ( ( FunctionTableModel )model ).setDeviceUpgrade( deviceUpgrade );
    initColumns();
    super.update();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.TablePanel#createRowObject()
   */
  protected Function createRowObject()
  {
    Function f = new Function();
    f.setUpgrade( deviceUpgrade );
    deviceUpgrade.getFunctions().add( f );
    if ( deviceUpgrade.getRemote().usesEZRC() )
    {
      f.setGid( Function.defaultGID );
    }
    if ( deviceUpgrade.getRemote().isSSD() )
    {
      f.icon = new RMIcon( 9 );
    }
    return f;
  }

  protected void delete( Function f )
  {
    List< User > users = new ArrayList< User >( f.getUsers() );
    for ( User user : users )
    {
      deviceUpgrade.setFunction( user.button, null, user.state );
    }
    deviceUpgrade.getFunctions().remove( f );
  }
  
  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    super.valueChanged( e );
    if ( !e.getValueIsAdjusting() )
    {
      if ( table.getSelectedRowCount() == 1 )
      {
        int row = table.getSelectedRow();
        TableSorter sorter = ( TableSorter )table.getModel();
        row = sorter.modelIndex( row );
        FunctionTableModel model = ( FunctionTableModel )sorter.getTableModel();
        Function f = model.getRow( row );
        RMIcon icon = f.icon;
        iconLabel.setIcon( icon == null ? null : icon.image );
      }
      else
      {
        iconLabel.setIcon( null );
      }
    }
  }
  
  private JLabel iconLabel = null;
}
