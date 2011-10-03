package com.hifiremote.jp1;

import java.util.ArrayList;
import java.util.List;

import com.hifiremote.jp1.Function.User;

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
    Remote r = deviceUpgrade.getRemote();
    ( ( FunctionTableModel )model ).setProtocol( p, r );
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
    return new Function();
  }

  protected void delete( Function f )
  {
    List< User > users = new ArrayList< User >( f.getUsers() );
    for ( User user : users )
    {
      deviceUpgrade.setFunction( user.button, null, user.state );
    }
  }
}
