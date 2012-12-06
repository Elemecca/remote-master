package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceUpgradePanel.
 */
public class DeviceUpgradePanel extends RMTablePanel< DeviceUpgrade >
{

  /**
   * Instantiates a new device upgrade panel.
   */
  public DeviceUpgradePanel()
  {
    super( new DeviceUpgradeTableModel() );
    footerPanel.add( upgradeBugPane, BorderLayout.PAGE_START );
    Font font = upgradeBugPane.getFont();
    Font font2 = font.deriveFont( Font.BOLD, 12 );
    upgradeBugPane.setFont( font2 );
    upgradeBugPane.setBackground( Color.RED );
    upgradeBugPane.setForeground( Color.YELLOW );
    String bugText = "NOTE:  This remote contains a bug that prevents device upgrades from working "
        + "if they use upgraded protocols.\nWorkaround:  Set up devices that use upgraded protocols "
        + "as \"Device Button Restricted\"";
    upgradeBugPane.setText( bugText );
    upgradeBugPane.setEditable( false );
    upgradeBugPane.setVisible( false );  
    editProtocolItem.setVisible( true );
    editProtocolButton.setVisible( true );
  }

  /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
  @Override
  public void set( RemoteConfiguration remoteConfig )
  {
    ( ( DeviceUpgradeTableModel )model ).set( remoteConfig );
    this.remoteConfig = remoteConfig;
    table.initColumns( model );
    upgradeBugPane.setVisible( remoteConfig != null && remoteConfig.getRemote().hasUpgradeBug() );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  @Override
  public DeviceUpgrade createRowObject( DeviceUpgrade baseUpgrade )
  {
    // Never used, the methods that call it are overridden separately.
    return null;
  }

  private void createRowObjectA( DeviceUpgrade baseUpgrade )
  {
    System.err.println( "DeviceUpgradePanel.createRowObject()" );
    DeviceUpgrade upgrade = null;
    if ( baseUpgrade == null )
    {
      if ( remoteConfig == null )
      {
        String msg = "Cannot create upgrade: No remote selected.";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Upgrade error",
            JOptionPane.ERROR_MESSAGE );
        return;
      }
      upgrade = new DeviceUpgrade();
      Remote remote = remoteConfig.getRemote();
      upgrade.setRemote( remote );
      if ( remote.hasDeviceDependentUpgrades() > 0 )
      {
        DeviceButton defaultDev = remote.hasDeviceDependentUpgrades() == 2 || remote.getDeviceButtons().length == 0 ?
            DeviceButton.noButton : remote.getDeviceButtons()[ 0 ];
        upgrade.setButtonIndependent( false );
        upgrade.setButtonRestriction( defaultDev );
        String msg;
        if ( remote.hasDeviceDependentUpgrades() == 2 )
        {
          msg =  "<html>This remote has device upgrades that are available on<br>"
            + "all device buttons and ones that are only available on a<br>"
            + "specified button.  The same upgrade can even be in both<br>"
            + "categories.  This new upgrade will be created as being in<br>"
            + "neither category.  After pressing OK, edit the new table<br>"
            + "entry to set the availability as required.</html>";
        }
        else
        {
          msg = "<html>This remote requires there to be exactly one device upgrade<br>"
            + "assigned to each active device button.  After pressing OK, edit<br>"
            + "the new table entry to assign this upgrade appropriately.";
        }
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Creating a new device upgrade",
            JOptionPane.PLAIN_MESSAGE );
      }
    }
    else
    {
      upgrade = new DeviceUpgrade( baseUpgrade );
      processorName = remoteConfig.getRemote().getProcessor().getEquivalentName();
      Protocol baseProtocol = baseUpgrade.getProtocol();
      baseProtocol.oldCustomCode = baseProtocol.customCode.get(  processorName );
      baseProtocol.newCustomCode = null;
    }
    oldUpgrade = baseUpgrade;

    List< Remote > remotes = new ArrayList< Remote >( 1 );
    remotes.add( remoteConfig.getRemote() );
    upgrade.setRemoteConfig( remoteConfig );
    editor = new DeviceUpgradeEditor( remoteConfig.getOwner(), upgrade, remotes, rowOut, this );
  }

  private DeviceUpgrade createRowObjectB( DeviceUpgradeEditor editor )
  {
    this.editor = null;
    DeviceUpgrade newUpgrade = editor.getDeviceUpgrade();
    if ( remoteConfig.hasSegments() && newUpgrade != null )
    {
      Protocol p = newUpgrade.getProtocol();
      newUpgrade.setSizeCmdBytes( p.getDefaultCmd().length() );
      newUpgrade.setSizeDevBytes( p.getFixedDataLength() );
      newUpgrade.setSegmentFlags( 0xFF );   // Default value
    }
    if ( newUpgrade == null || oldUpgrade == null )
    {
      return newUpgrade;
    }

    int boundDeviceButtonIndex = remoteConfig.findBoundDeviceButtonIndex( oldUpgrade );
    rowBound = boundDeviceButtonIndex;
    if ( rowBound == -1 )
    {
      rowBound = null;
      return newUpgrade;
    }

    java.util.List< KeyMove > upgradeKeyMoves = newUpgrade.getKeyMoves();
    java.util.List< KeyMove > keyMoves = remoteConfig.getKeyMoves();
    for ( KeyMove upgradeKeyMove : upgradeKeyMoves )
    {
      for ( ListIterator< KeyMove > li = keyMoves.listIterator(); li.hasNext(); )
      {
        KeyMove keyMove = li.next();
        if ( keyMove.getDeviceButtonIndex() != boundDeviceButtonIndex )
        {
          continue;
        }
        if ( keyMove.getKeyCode() == upgradeKeyMove.getKeyCode() )
        {
          li.remove();
          Remote remote = remoteConfig.getRemote();
          System.err.println( "Removed keyMove assigned to " + remote.getDeviceButtons()[ boundDeviceButtonIndex ]
              + ':' + remote.getButtonName( keyMove.getKeyCode() )
              + " since there is one assigned in the device upgrade" );
          break;
        }
      }
    }
    return newUpgrade;
  }

  @Override
  protected void editRowObject( int row )
  {
    rowOut = row;
    createRowObjectA( getRowObject( row ) );
  }

  @Override
  protected void newRowObject( DeviceUpgrade baseUpgrade, int row, int modelRow, boolean select )
  {
    rowOut = null;
    rowNew = row == -1 ? null : row;
    rowModel = modelRow == -1 ? null : modelRow;
    this.select = select;
    createRowObjectA( baseUpgrade );
  }
  
  @Override
  public void editRowProtocol( int row )
  {
    model.getRow( row ).getProtocol().editProtocol( remoteConfig.getRemote(), this );
    model.fireTableDataChanged();
  }

  public void endEdit( DeviceUpgradeEditor editor, Integer row )
  {
    DeviceUpgrade newUpgrade = createRowObjectB( editor );
    Remote remote = remoteConfig.getRemote();
    if ( newUpgrade == null )
    {
      if ( oldUpgrade != null )
      {
        Protocol baseProtocol = oldUpgrade.getProtocol(); 
        // Restore custom code in case it has been changed
        if ( baseProtocol.oldCustomCode == null )
        {
          baseProtocol.customCode.remove( processorName );
        }
        else
        {
          baseProtocol.customCode.put(processorName, baseProtocol.oldCustomCode );
          remoteConfig.getProtocolUpgrades().remove( baseProtocol.newCustomCode );
        }
      }
      return;
    }
    if ( remote.usesEZRC() && newUpgrade.getButtonRestriction() != DeviceButton.noButton )
    {
      newUpgrade.getButtonRestriction().setUpgrade( newUpgrade );
    }
    if ( row != null )
    {
      // Edit
      model.setRow( sorter.modelIndex( row ), newUpgrade );
      Protocol pOld = oldUpgrade.getProtocol();
      Protocol pNew = newUpgrade.getProtocol();
      if ( pOld == pNew && pOld.newCustomCode != null && pOld.oldCustomCode != null
          && ! pOld.newCustomCode.equals( pOld.oldCustomCode ) )
      {
        ProtocolManager.getProtocolManager().remove( pOld.newCustomCode.getProtocol() );
        remoteConfig.getProtocolUpgrades().remove( pOld.newCustomCode );
        pOld.saveCode( remoteConfig, pOld.oldCustomCode );
      }
      if ( oldUpgrade.needsProtocolCode() && pOld != pNew )
      {
        boolean pUsed = false;
        for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
        {
          if ( ( ( pOld instanceof ManualProtocol ) && ( du.getProtocol() == pOld ) )
                || ( du.getProtocol().getID( remote ) ==  pOld.getID( remote ) ) )
          {
            pUsed = true;
            break;
          }
        }
        if ( !pUsed )
        {
          // Old protocol now unused so save its code
          pOld.saveCode( remoteConfig, oldUpgrade.getCode() );
          // If the old protocol had custom code, this would now be in the protocol upgrades
          pOld.customCode.clear();
        }
      }
           
      DeviceButtonTableModel deviceModel = remoteConfig.getOwner().getGeneralPanel().getDeviceButtonTableModel();

      if ( rowBound != null )
      {
        deviceModel.setValueAt( newUpgrade.getDeviceType(), rowBound, 2 );
        deviceModel.setValueAt( new SetupCode( newUpgrade.getSetupCode() ), rowBound, 3 );
//        deviceModel.fireTableRowsUpdated( rowBound, rowBound );
        deviceModel.fireTableDataChanged();
      }
    }
    else
    {
      // New, Clone
      if ( remoteConfig.findBoundDeviceButtonIndex( newUpgrade ) == -1 )
      {
        // upgrade isn't bound to a device button.
        DeviceButton[] devButtons = remote.getDeviceButtons();
        DeviceButton devButton = ( DeviceButton )JOptionPane.showInputDialog( RemoteMaster.getFrame(),
            "The device upgrade \"" + newUpgrade.toString()
                + "\" is not assigned to a device button.\nDo you want to assign it now?\n"
                + "To do so, select the desired device button and press OK.\n" + "Otherwise please press Cancel.\n",
            "Unassigned Device Upgrade", JOptionPane.QUESTION_MESSAGE, null, devButtons, null );
        if ( devButton != null )
        {
          short[] data = remoteConfig.getData();
          if ( remoteConfig.hasSegments() )
          {
            data = devButton.getSegment().getHex().getData();
          }
          DeviceType devType = remote.getDeviceTypeByAliasName( newUpgrade.getDeviceTypeAliasName() );
          devButton.setSetupCode( ( short )newUpgrade.getSetupCode(), data );
          devButton.setDeviceTypeIndex( ( short )devType.getNumber(), data );
          devButton.setDeviceGroup( ( short )devType.getGroup(), data );
          if ( remote.hasDeviceDependentUpgrades() == 2 )
          {
            String message = "Remember to set the button-dependent and/or button-independent\n"
                + " settings in a manner consistent with your choice of button\n" + " assignment.";
            String title = "Creating a new device upgrade";
            JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE );
          }
          
        }
      }
      
      // See if there is custom code waiting to be assigned
      Processor processor = remote.getProcessor();
      Protocol p = newUpgrade.getProtocol();
      ProtocolUpgrade pu = p.getCustomUpgrade( remoteConfig, true );
      if ( ( p.getCustomCode( processor ) == null ) &&  pu != null && p.matched()
          && !p.getCode( remote ).equals( pu.getCode() ) )
      {
        // There is custom code waiting to be assigned, so assign it
        // and delete it from the list of unused protocol upgrades
        // and its manual protocol from ProtocolManager, since it has
        // served its purpose as custom code in waiting.
        p.addCustomCode( processor, pu.getCode() );
        if ( remoteConfig.getProtocolUpgrades().contains( pu ) )
        {
          // If custom code comes from another device upgrade rather than a manual protocol,
          // it will not have been added, so do not try to remove.
          remoteConfig.getProtocolUpgrades().remove( pu );
          ProtocolManager.getProtocolManager().remove( pu.getManualProtocol( remote ) );
        }
      }
      if ( rowNew == null )
      {
        model.addRow( newUpgrade );
        row = model.getRowCount();
      }
      else
      {
        model.insertRow( rowModel, newUpgrade );
      }

      if ( select )
      {
        table.setRowSelectionInterval( rowNew, rowNew );
      }
    }
    // If new upgrade uses an upgrade protocol not previously used, delete that protocol
    // from the list of unused protocols.

    ProtocolUpgrade puUsed = null;
    for ( ProtocolUpgrade pu : remoteConfig.getProtocolUpgrades() )
    {
      if ( pu.getProtocol() == newUpgrade.getProtocol() )
      {
        puUsed = pu;
        break;
      }
    }
    if ( puUsed != null )
    {
      remoteConfig.getProtocolUpgrades().remove( puUsed );
    }
//    if ( remoteConfig.getRemote().usesEZRC() )
//    {
//      remoteConfig.assignUpgrades();
//    }
  }

  public DeviceUpgradeEditor getDeviceUpgradeEditor()
  {
    return editor;
  }

  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  public DeviceUpgrade getOldUpgrade()
  {
    return oldUpgrade;
  }

  private Integer rowOut = null;
  private Integer rowBound = null;
  private Integer rowNew = null;
  private Integer rowModel = null;
  private Boolean select = null;
  private DeviceUpgrade oldUpgrade = null;
  private DeviceUpgradeEditor editor = null;

  /** The remote config. */
  private RemoteConfiguration remoteConfig;
  
  private String processorName = null;

  private JTextPane upgradeBugPane = new JTextPane();

}
