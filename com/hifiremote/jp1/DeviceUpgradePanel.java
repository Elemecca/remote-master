package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
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
  
  public int getRow( DeviceUpgrade upg )
  {
    int row = -1;
    if ( remoteConfig != null )
    {
      row = remoteConfig.getDeviceUpgrades().indexOf( upg );
      row = sorter.viewIndex( row );
    }
    return row;
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
    if ( remoteConfig == null )
    {
      String msg = "Cannot create upgrade: No remote selected.";
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Upgrade error",
          JOptionPane.ERROR_MESSAGE );
      return;
    }
    Remote remote = remoteConfig.getRemote();
    DeviceButton defaultDev = null;
    DeviceUpgrade upgrade = null;
    if ( rowOut == null && remote.hasDeviceDependentUpgrades() == 1 )
    {
      for ( DeviceButton db : remote.getDeviceButtons() )
      {
        if ( db.getSegment() == null || db.getDeviceSlot( db.getSegment().getHex().getData() ) == 0xFFFF )
        {
          String message = "The new upgrade being created will be assigned automatically\n" +
          		             "to an unassigned device.  What name do you want to give to\n" +
                           "this device?";
          String name = JOptionPane.showInputDialog( RemoteMaster.getFrame(), message, "New device" );
          if ( name == null )
          {
            return;
          }
          defaultDev = db;
          if ( db.getSegment() == null )
          {
            db.setSegment( new Segment( 0, 0xFF, new Hex( 15 ) ) );
          }
          defaultDev.setName( name );
          break;
        }
      }
      if ( defaultDev == null )
      {
        String msg = "You already have the maximum number of assigned devices.  You\n" +
        		         "cannot add a new upgrade as there is no device to which it can\n" +
                     "be assigned.";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "New device", JOptionPane.WARNING_MESSAGE );
        return;
      }
    }
    
    if ( baseUpgrade == null )
    {
      upgrade = new DeviceUpgrade();;
      upgrade.setRemote( remote );
      if ( remote.hasDeviceDependentUpgrades() == 2 )
      {
        defaultDev = DeviceButton.noButton;
        String msg =  "<html>This remote has device upgrades that are available on<br>"
            + "all device buttons and ones that are only available on a<br>"
            + "specified button.  The same upgrade can even be in both<br>"
            + "categories.  This new upgrade will be created as being in<br>"
            + "neither category.  After pressing OK, edit the new table<br>"
            + "entry to set the availability as required.</html>";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Creating a new device upgrade",
            JOptionPane.PLAIN_MESSAGE );
      }
    }
    else
    {
      upgrade = new DeviceUpgrade( baseUpgrade, defaultDev );
      processorName = remoteConfig.getRemote().getProcessor().getEquivalentName();
      Protocol baseProtocol = baseUpgrade.getProtocol();
      if ( baseProtocol != null )
      {
        baseProtocol.oldCustomCode = baseProtocol.customCode.get(  processorName );
        baseProtocol.newCustomCode = null;
      }
    }
    oldUpgrade = baseUpgrade;
    if ( defaultDev != null )
    {
      if ( upgrade.getSetupCode() < 0 )
      {
        upgrade.setSetupCode( 0 );
      }
      if ( defaultDev != DeviceButton.noButton )
      {
        defaultDev.setUpgrade( upgrade );
        upgrade.setButtonRestriction( defaultDev );
        upgrade.setButtonIndependent( false );
      }
    }

    List< Remote > remotes = new ArrayList< Remote >( 1 );
    remotes.add( remoteConfig.getRemote() );
    upgrade.setRemoteConfig( remoteConfig );
    editor = new DeviceUpgradeEditor( remoteConfig.getOwner(), upgrade, remotes, rowOut, this );
  }

  private DeviceUpgrade createRowObjectB( DeviceUpgradeEditor editor )
  {
    this.editor = null;
    Remote remote = remoteConfig.getRemote();
    DeviceUpgrade newUpgrade = editor.getDeviceUpgrade();
    if ( remote.getSegmentTypes() != null && !remote.isSSD() && !remote.getSegmentTypes().contains( 0x10 ) 
        && !remote.getSegmentTypes().contains( 0x0F ) && newUpgrade != null && newUpgrade.needsProtocolCode() )
    {
      // Such remotes do not support device upgrades that need a protocol upgrade
      newUpgrade.doCancel( rowOut == null );
      newUpgrade = null;
      String message = "This device upgrade needs a protocol upgrade and protocol upgrades\n"
          + "are not supported by this remote, so unfortunately this edit is\n"
          + "being cancelled.";
      String title = "Forced Cancellation of Edit";
      JOptionPane.showMessageDialog( null, message, title, JOptionPane.INFORMATION_MESSAGE );
    }
    if ( remoteConfig.hasSegments() && newUpgrade != null && newUpgrade.getProtocol() != null )
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
  public void editRowObject( int row )
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
    DeviceUpgrade du = model.getRow( row );
    Protocol p = du.getProtocol().editProtocol( remoteConfig.getRemote(), this );
    if ( p != null )
    {
      du.setProtocol( p );
      model.propertyChangeSupport.firePropertyChange( "data", null, null );
    }
//    model.getRow( row ).getProtocol().editProtocol( remoteConfig.getRemote(), this );
    model.fireTableDataChanged();
  }

  public void endEdit( DeviceUpgradeEditor editor, Integer row )
  {
    DeviceUpgrade newUpgrade = createRowObjectB( editor );
    Remote remote = remoteConfig.getRemote();
    if ( newUpgrade == null )
    {
      // Edit has been cancelled
      if ( oldUpgrade != null && oldUpgrade.getProtocol() != null )
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
      remoteConfig.swapFunctions( newUpgrade );
    }
    newUpgrade.setSwapList( null );
    if ( row != null )
    {
      // Edit
      model.setRow( sorter.modelIndex( row ), newUpgrade );
      Protocol pOld = oldUpgrade.getProtocol();
      Protocol pNew = newUpgrade.getProtocol();
      if ( pOld != null && pOld == pNew && pOld.newCustomCode != null && pOld.oldCustomCode != null
          && ! pOld.newCustomCode.equals( pOld.oldCustomCode ) )
      {
        ProtocolManager.getProtocolManager().remove( pOld.newCustomCode.getProtocol() );
        remoteConfig.getProtocolUpgrades().remove( pOld.newCustomCode );
        pOld.saveCode( remoteConfig, pOld.oldCustomCode );
      }
      if ( pOld != pNew )
      {
        ( ( DeviceUpgradeTableModel )getModel() ).checkProtocolRemoval( oldUpgrade, false );
      }
           
      DeviceButtonTableModel deviceModel = remoteConfig.getOwner().getGeneralPanel().getDeviceButtonTableModel();

      if ( rowBound != null )
      {
        deviceModel.setValueAt( newUpgrade.getDeviceType(), rowBound, 2 );
        deviceModel.setValueAt( new SetupCode( newUpgrade.getSetupCode() ), rowBound, 3 );
//        deviceModel.fireTableRowsUpdated( rowBound, rowBound );
        deviceModel.fireTableDataChanged();
      }
      KeyMovePanel kmp = remoteConfig.getOwner().getKeyMovePanel();
      if ( kmp != null )
      {
        kmp.endEditUpgrade();
      }
    }
    else
    {
      // New, Clone
      int dbi = 0;
      DeviceButton devButton = null;
      if ( ( dbi = remoteConfig.findBoundDeviceButtonIndex( newUpgrade ) ) == -1
          || remote.hasDeviceDependentUpgrades() == 1 )
      {
        if ( dbi == -1 )
        {
          // upgrade isn't bound to a device button.
          DeviceButton[] devButtons = remote.getDeviceButtons();
          devButton = ( DeviceButton )JOptionPane.showInputDialog( RemoteMaster.getFrame(),
              "The device upgrade \"" + newUpgrade.toString()
              + "\" is not assigned to a device button.\nDo you want to assign it now?\n"
              + "To do so, select the desired device button and press OK.\n" + "Otherwise please press Cancel.\n",
              "Unassigned Device Upgrade", JOptionPane.QUESTION_MESSAGE, null, devButtons, null );
        }
        else
        {
          // upgrade is assigned to a device button via a button restriction
          devButton = newUpgrade.getButtonRestriction();
        }
        
        if ( devButton == null && remote.usesEZRC() )
        {
          // This case, which only concerns XSight remotes, should not occur.
          newUpgrade.doCancel( true );
          return;
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
      
      if ( devButton != null )
      {
        DeviceType devType = remote.getDeviceTypeByAliasName( newUpgrade.getDeviceTypeAliasName() );
        SetupCode setupCode = new SetupCode( newUpgrade.getSetupCode() );
        boolean valid = true;
        if ( !remote.usesEZRC() )
        {
          // Test device for validity if upgrade contains keymoves.
          // Remotes that use EZ-RC do not support keymoves.
          DeviceButtonTableModel dum = remoteConfig.getOwner().getGeneralPanel().getDeviceButtonTableModel();
          int dbRow = Arrays.asList( remote.getDeviceButtons() ).indexOf( devButton );
          valid = dum.isValidDevice( dbRow, devType, setupCode );
        }

        if ( valid )
        {
          short[] data = remoteConfig.getData();
          if ( remoteConfig.hasSegments() )
          {
            data = devButton.getSegment().getHex().getData();
          }

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
          remoteConfig.getOwner().getGeneralPanel().getDeviceButtonTableModel().propertyChangeSupport.firePropertyChange( "value", null, null );
        }
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
