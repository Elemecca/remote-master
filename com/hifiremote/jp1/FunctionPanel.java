package com.hifiremote.jp1;

public class FunctionPanel
  extends TablePanel
{
  public FunctionPanel( DeviceUpgrade devUpgrade )
  {
    super( "Functions", devUpgrade,
           new FunctionTableModel( devUpgrade.getFunctions(), devUpgrade.getRemote()));
  }

  public void update()
  {
    Protocol p = deviceUpgrade.getProtocol();
    p.initializeParms();
    Remote r = deviceUpgrade.getRemote();
    (( FunctionTableModel )model ).setProtocol( p, r );
    if (( p != savedProtocol ) || ( r != savedRemote ))
    {
      initColumns();
      savedProtocol = p;
      savedRemote = r;
    }
    super.update();
  }

  protected Object createRowObject()
  {
    return new Function();
  }

  protected boolean canDelete( Object o )
  {
    Function f = ( Function )o;
    return !f.assigned();
  }

  protected void doNotDelete( Object o )
  {
    String message = "Function is assigned to a button, it can not be deleted.";
    KeyMapMaster.showMessage( message );
    throw new IllegalArgumentException( message );
  }

  private Protocol savedProtocol = null;
  private Remote savedRemote = null;
}
