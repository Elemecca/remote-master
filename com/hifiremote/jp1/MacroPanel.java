package com.hifiremote.jp1;

import java.util.LinkedHashMap;
import java.util.List;

import com.hifiremote.jp1.GeneralFunction.User;

// TODO: Auto-generated Javadoc
/**
 * The Class MacroPanel.
 */
public class MacroPanel extends RMTablePanel< Macro >
{

  /**
   * Instantiates a new macro panel.
   */
  public MacroPanel()
  {
    super( new MacroTableModel() );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( MacroTableModel )model ).set( remoteConfig );
    table.initColumns( model );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  public Macro createRowObject( Macro baseMacro )
  {
    return MacroDialog.showDialog( this, baseMacro, ( ( MacroTableModel )model ).getRemoteConfig() );
  }
  
  @Override
  protected void editRowObject( int row )
  {
    RemoteConfiguration config = ( ( MacroTableModel )model ).getRemoteConfig();
    Remote remote = config.getRemote();
    List< Macro > macros = config.getMacros();
    Macro baseMacro = getRowObject( row );
    int ndx = macros.indexOf( baseMacro );
    User baseUser = new User( baseMacro.getDeviceButton( config ), remote.getButton( baseMacro.getKeyCode() ) );
    Macro newMacro = createRowObject( baseMacro );
    if ( newMacro != null )
    {
      baseUser.db.getUpgrade().getAssignments().assign( baseUser.button, null );
      macros.remove( ndx );
      macros.add( ndx, newMacro );
      LinkedHashMap< Integer, Macro > macroMap = null;
      for ( User user : baseMacro.getUsers() )
      {
        if ( user.db != null && user.db.getUpgrade() != null 
            && ( macroMap = user.db.getUpgrade().getMacroMap() ) != null )
        {
          macroMap.remove( ( int )user.button.getKeyCode() );
          if ( !user.equals( baseUser ) )
          {
            macroMap.put( ( int )user.button.getKeyCode(), newMacro );
            newMacro.addReference( user.db, user.button );
          }
        }
      }
      model.setRow( sorter.modelIndex( row ), newMacro );
    }
  }

}
