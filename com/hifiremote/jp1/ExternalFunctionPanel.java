package com.hifiremote.jp1;

public class ExternalFunctionPanel
  extends TablePanel
{
  public ExternalFunctionPanel( DeviceUpgrade devUpgrade )
  {
    super( devUpgrade, new ExternalFunctionTableModel( devUpgrade ));
    initColumns();
  }

  public void update()
  {
    (( ExternalFunctionTableModel ) model ).update();
  }

  protected Object createRowObject()
  {
    return new ExternalFunction();
  }

  protected boolean canDelete( Object o )
  {
    return !(( Function ) o).assigned();
  }

  protected void doNotDelete( Object o )
  {
    String message = "Function is assigned to a button, it can not be deleted.";
    KeyMapMaster.showMessage( message );
    throw new IllegalArgumentException( message );
  }
}
