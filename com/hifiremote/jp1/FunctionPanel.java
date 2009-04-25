package com.hifiremote.jp1;

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

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.TablePanel#canDelete(java.lang.Object)
   */
  protected boolean canDelete( Object o )
  {
    Function f = ( Function )o;
    return !f.assigned();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.TablePanel#doNotDelete(java.lang.Object)
   */
  protected void doNotDelete( Object o )
  {
    String message = "Function is assigned to a button, it can not be deleted.";
    JP1Frame.showMessage( message, this );
    throw new IllegalArgumentException( message );
  }
}
