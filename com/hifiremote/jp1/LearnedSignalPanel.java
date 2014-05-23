package com.hifiremote.jp1;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;

import com.hifiremote.jp1.GeneralFunction.RMIcon;

// TODO: Auto-generated Javadoc
/**
 * The Class LearnedSignalPanel.
 */
public class LearnedSignalPanel extends RMTablePanel< LearnedSignal >
{

  /**
   * Instantiates a new learned signal panel.
   */
  public LearnedSignalPanel()
  {
    super( new LearnedSignalTableModel() );
    
    convertToUpgradeButton = new JButton( "Convert to Device Upgrade" );
    convertToUpgradeButton.addActionListener( this );
    convertToUpgradeButton.setToolTipText( "Convert the selected item to a Device Upgrade." );
    convertToUpgradeButton.setEnabled( false );
    convertToUpgradeButton.setVisible( Boolean.parseBoolean( RemoteMaster.getProperties().getProperty( "LearnUpgradeConversion", "false" ) ) );    
    buttonPanel.add( convertToUpgradeButton );

    timingSummaryButton = new JButton( "Timing Summary" );
    timingSummaryButton.addActionListener( this );
    timingSummaryButton.setToolTipText( "View the Timing Summary for all of the Learned Signals." );
    timingSummaryButton.setEnabled( true );
    timingSummaryButton.setVisible( Boolean.parseBoolean( RemoteMaster.getProperties().getProperty( "LearnedSignalTimingAnalysis", "false" ) ) );
    buttonPanel.add( timingSummaryButton );
  }

  @Override
  protected void refresh()
  {
    convertToUpgradeButton.setVisible( Boolean.parseBoolean( RemoteMaster.getProperties().getProperty( "LearnUpgradeConversion", "false" ) ) );    
    timingSummaryButton.setVisible( Boolean.parseBoolean( RemoteMaster.getProperties().getProperty( "LearnedSignalTimingAnalysis", "false" ) ) );
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
    this.remoteConfig = remoteConfig;
    ( ( LearnedSignalTableModel )model ).set( remoteConfig );
    table.initColumns( model );
    newButton.setEnabled( remoteConfig != null && remoteConfig.getRemote().getLearnedAddress() != null );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.RMTablePanel#createRowObject(java.lang.Object)
   */
  @Override
  public LearnedSignal createRowObject( LearnedSignal learnedSignal )
  {
    LearnedSignal newSignal = null;
    if ( learnedSignal != null )
    {
      newSignal = new LearnedSignal( learnedSignal );
    }
    return LearnedSignalDialog.showDialog( SwingUtilities.getRoot( this ), newSignal, remoteConfig );
  }
  
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == convertToUpgradeButton )
    {
      finishEditing();
      int[] rows = table.getSelectedRows();
      ArrayList<LearnedSignal> signals = new ArrayList<LearnedSignal>();
      for ( int i =0; i < rows.length; i++ )
      {
        LearnedSignal s = getRowObject(rows[i]);
        if ( !s.getDecodes().isEmpty() )
          signals.add(s);
      }
      if ( !signals.isEmpty() )
        convertToDeviceUpgrade( signals.toArray(new LearnedSignal[signals.size()]) );
    }
    else if ( source == timingSummaryButton )
      LearnedSignalTimingSummaryDialog.showDialog( SwingUtilities.getRoot( this ), remoteConfig );
    else
      super.actionPerformed( e );
  }
  
  private boolean validateLearnedSignalsForUpgradeConversion( LearnedSignal[] signals )
  {
    LearnedSignalDecode d = signals[0].getDecodes().get( 0 );
    String protocolName = d.protocolName;
    if ( protocolName.startsWith("48-NEC") )
      protocolName = protocolName.substring(3);
    int device = d.device;
    int subDevice = d.subDevice;

    for ( LearnedSignal s: signals )
    {
      d = s.getDecodes().get( 0 );
      String p = d.protocolName;
      if ( p.startsWith("48-NEC") )
        p = p.substring(3);
      if ( !p.equals(protocolName) || device != d.device || subDevice != d.subDevice )
        return false;
    }

    return true;
  }
  private void convertToDeviceUpgrade( LearnedSignal[] signals )
  {
    if ( !validateLearnedSignalsForUpgradeConversion( signals ) )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "The Learned Signals you have selected do not all have the\nsame protocol, device, and subdevice so they cannot\nbe automatically converted to a Device Upgrade.", "Unable to convert Learned Signals to Device Upgrade", JOptionPane.ERROR_MESSAGE );
      return;
    }

    LearnedSignalDecode d = signals[0].getDecodes().get( 0 );
    String protocolName = d.protocolName;
    if ( protocolName.startsWith("48-NEC") )
      protocolName = protocolName.substring(3);
    int device = d.device;
    int subDevice = d.subDevice;
    
    boolean convert = false;
    Remote remote = remoteConfig.getRemote();
    Protocol protocol = null;
    List< Protocol > protocols = ProtocolManager.getProtocolManager().findByName( protocolName );
    if ( protocols != null )
    {
      protocol = protocols.get( 0 );
    }
    else
    {
      convert = true;
      String title = "Protocol Chooser";
      String message = 
            "There is no protocol with the precise name " + protocolName + ".\n"
          + "Please choose a compatible protocol from the list below.\n\n"
          + "Please note that conversion to the selected protocol may not\n"
          + "be possible, or may require manual adjustment to the device\n"
          + "or command parameters after conversion.  Use the Device Upgrade\n"
          + "Editor to check and to make any necessary adjustments.\n";
      protocols = ProtocolManager.getProtocolManager().getProtocolsForRemote( remote );
      protocol = ( Protocol )JOptionPane.showInputDialog( remoteConfig.getOwner(), message, title, JOptionPane.QUESTION_MESSAGE, null, 
          protocols.toArray(), protocols.get( 0 ) );
    }
    
    //System.err.println("Checking if can append for protocol " + protocolName + ", device " + device + ", subDevice " + subDevice + "...");
    DeviceUpgrade appendUpgrade = null;
    List<DeviceUpgrade> upgrades = remoteConfig.getDeviceUpgrades();
    for ( DeviceUpgrade u: upgrades )
    {
      if ( (
          ( u.getDescription() != null && u.getDescription().contains( "Learned Signal" )) || (u.getNotes() != null && u.getNotes().contains( "Learned Signal" ) )
          ) && protocol != null && protocol == u.protocol )
          // u.protocol.getName().equals( protocolName ) )
      {
        int uDevice = -1;
        int uSubDevice = -1;
        DeviceParameter[] protocolDevParms = u.protocol.getDeviceParameters();
        for ( int i = 0; i < protocolDevParms.length; i++ )
        {
          if ( protocolDevParms[i].getName().startsWith( "Device" ) )
            uDevice = ((Integer)u.getParmValues()[i].getValue()).intValue();
          if ( protocolDevParms[i].getName().startsWith( "Sub Device" ) )
            uSubDevice = ((Integer)u.getParmValues()[i].getValue()).intValue();
        }
        //System.err.println("Device Upgrade (" + u.getDescription() + ") has protocol " + protocolName + ", device " + uDevice + ", subDevice " + uSubDevice + "...");
        if ( uDevice == device && uSubDevice == subDevice )
        {
          //System.err.println( "Device Upgrade (" + u.getDescription() + ") is a match." );
          appendUpgrade = u;
          break;
        }
      }
    }

    if ( appendUpgrade == null )
    {
      DeviceUpgrade upgrade = null;
      if ( protocol != null )
      {
        upgrade = new DeviceUpgrade( signals, remoteConfig, protocol, convert );
        protocol = upgrade.getProtocol();
      }
 
      String msg = null;
      
      if ( protocol != null )
      {
        DeviceButton defaultDev = null;
        if ( remote.hasDeviceDependentUpgrades() == 1 )
        {
          for ( DeviceButton db : remote.getDeviceButtons() )
          {
            if ( db.getSegment() == null || db.getDeviceSlot( db.getSegment().getHex().getData() ) == 0xFFFF )
            {
              String message = "The new upgrade that has been created will be assigned automatically\n" +
                               "to an unassigned device.  What name do you want to give to this\n" +
                               "device?";
              String name = JOptionPane.showInputDialog( RemoteMaster.getFrame(), message, "New device" );
              if ( name == null )
              {
                protocol = null;
              }
              else
              {
                defaultDev = db;
                if ( db.getSegment() == null )
                {
                  db.setSegment( new Segment( 0, 0xFF, new Hex( 15 ) ) );
                }
                defaultDev.setName( name );
                defaultDev.setUpgrade( upgrade );
                upgrade.setButtonRestriction( defaultDev );
                upgrade.setButtonIndependent( false );
                short[] data = defaultDev.getSegment().getHex().getData();
                DeviceType devType = remote.getDeviceTypeByAliasName( upgrade.getDeviceTypeAliasName() );
                defaultDev.setSetupCode( ( short )upgrade.getSetupCode(), data );
                defaultDev.setDeviceTypeIndex( ( short )devType.getNumber(), data );
                defaultDev.setDeviceGroup( ( short )devType.getGroup(), data );
                if ( !remoteConfig.getDeviceButtonList().contains( defaultDev ) )
                {
                  remoteConfig.getDeviceButtonList().add( defaultDev );
                }
              }
              break;
            }
          }
          if ( defaultDev == null && upgrade != null && protocol != null )
          {
            msg = "You already have the maximum number of assigned devices.  You\n"
                +  "cannot add the new upgrade as there is no device to which it can\n"
                +  "be assigned.  The conversion is aborted.";
            upgrade = null;
          }
        }
      }
      if ( msg == null && protocol == null )
      {
        msg = "The conversion of Learned Signals to device upgrade\nhas been aborted.";
      }
      if ( msg == null )
      {
        if ( remote.usesEZRC() )
        {
          Function[] assignments = upgrade.getAssignments().getAssignedFunctions();
          for ( int keyCode = 0; keyCode < assignments.length; keyCode++ )
          {
            Function f = assignments[ keyCode ];
            if ( f != null )
            {
              Button b = remote.getButton( keyCode );
              f.removeReferences();
              f.addReference( upgrade.getButtonRestriction(), b );
            }
          }
        }
        remoteConfig.getDeviceUpgrades().add( upgrade );
        remoteConfig.getOwner().getDeviceUpgradePanel().model.fireTableDataChanged();
        if ( signals.length == upgrade.getFunctions().size() )
        {
          msg = "The " + signals.length + " selected Learned Signals" ;
        }
        else
        {
          msg = "Of the " + signals.length + " selected Learned Signals, " + upgrade.getFunctions().size();
        }
        msg += " have been converted\ninto a new Device Upgrade of type CBL\nwith the Setup Code " + upgrade.getSetupCode() + ".\n\nSwitch to the Devices tab to view/edit/etc this new Upgrade.";
      }
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Learned Signals converted to New Device Upgrade", JOptionPane.PLAIN_MESSAGE );
    }
    else
    {
      // Append the Learned Signals to the existing upgrade
      ArrayList<String> existingFunctions = new ArrayList<String>();
      ArrayList<String> renamedFunctions = new ArrayList<String>();
      ArrayList<String> shiftedFunctions = new ArrayList<String>();
      ArrayList<String> unassignedFunctions = new ArrayList<String>();
      List< List< String >> failedToConvert = new ArrayList< List< String > >();
      LinkedHashMap< LearnedSignal, Hex > converted = new LinkedHashMap< LearnedSignal, Hex >();
      for ( LearnedSignal s : signals )
      {
        d = s.getDecodes().get( 0 );
        List< String > error = new ArrayList< String >( 2 );
        Hex funcHex = convert ? d.getProtocolHex( protocol, error ) : d.getSignalHex();
        if ( funcHex == null )
        {
          error.set( 0, s.getSignalName( remote ) );
          failedToConvert.add( error );
        }
        else
        {
          converted.put( s, funcHex );
        }
      }
      String msg = null;
      if ( !failedToConvert.isEmpty() 
          && !LearnedSignalDecode.displayErrors( protocol.getName(), failedToConvert ) )
      {
        msg = "The appending of Learned Signals to an existing device upgrade\nhas been aborted.";
      }
      else
      {
        for ( LearnedSignal s : converted.keySet() )
        {
          String origName = s.getSignalName( remote );
          Button b = remoteConfig.getRemote().getButton( s.getKeyCode() );
          Hex funcHex = converted.get( s );
          Function f = appendUpgrade.getFunction( funcHex );
          if ( f != null )
          {
            existingFunctions.add( origName );
          }
          else
          {
            int i = 1;
            String name = origName;
            while ( appendUpgrade.getFunction( name ) != null )
            {
              i++;
              name = origName + "_" + i;
            }
            if ( i > 1 )
              renamedFunctions.add( origName );
            f = new Function( name, funcHex, s.getNotes() );
            if ( remote.isSSD() )
            {
              f.icon = new RMIcon( 9 );
            }
            if ( remote.usesEZRC() )
            {
              f.setGid( Function.defaultGID );
            }   
            f.setUpgrade( appendUpgrade );
            appendUpgrade.getFunctions().add( f );

            if ( appendUpgrade.getFunction( b, Button.NORMAL_STATE ) == null )
              appendUpgrade.setFunction( b, f, Button.NORMAL_STATE );
            else if ( remote.getShiftEnabled() && b.allowsKeyMove( Button.SHIFTED_STATE ) && appendUpgrade.getFunction( b, Button.SHIFTED_STATE ) == null )
            {
              appendUpgrade.setFunction( b, f, Button.SHIFTED_STATE );
              shiftedFunctions.add( origName );
            }
            else
              unassignedFunctions.add( origName );
          }
        }
        if ( failedToConvert.isEmpty() )
        {
          msg = "The " + signals.length + " selected Learned Signals";
        }
        else
        {
          msg = "Of the " + signals.length + " selected Learned Signals, " + ( signals.length - failedToConvert.size() );
        }
        msg += " were appended to existing\n"
            + "Device Upgrade (" + appendUpgrade.getDescription() + ") with protocol " + appendUpgrade.getProtocol().getName();
        if ( device >= 0 )
        {
          msg += ",\ndevice " + device;
        }
        if ( subDevice >= 0 )
        {
          msg += ", subdevice " + subDevice;
        }
        msg += ".\n";

        boolean comma;
        if ( !existingFunctions.isEmpty() )
        {
          msg = msg + "\nThe following functions were already present in the upgrade:\n   ";
          comma = false;
          for (String n: existingFunctions)
            if (comma)
              msg = msg + ", " + n;
            else
            {
              msg = msg + n;
              comma = true;
            }
          msg = msg + "\n";
        }
        if ( !renamedFunctions.isEmpty() )
        {
          msg = msg + "The following Functions were renamed to prevent duplicates:\n   ";
          comma = false;
          for (String n: renamedFunctions)
            if (comma)
              msg = msg + ", " + n;
            else
            {
              msg = msg + n;
              comma = true;
            }
          msg = msg + "\n";
        }
        if ( !shiftedFunctions.isEmpty() )
        {
          msg = msg + "\nThe following were assigned to shifted keys to prevent duplicates:\n   ";
          comma = false;
          for (String n: shiftedFunctions)
            if (comma)
              msg = msg + ", " + n;
            else
            {
              msg = msg + n;
              comma = true;
            }
          msg = msg + "\n";
        }
        if ( !unassignedFunctions.isEmpty() )
        {
          msg = msg + "\nThe following could not be assigned to a key due to duplicates:\n   ";
          comma = false;
          for (String n: unassignedFunctions)
            if (comma)
              msg = msg + ", " + n;
            else
            {
              msg = msg + n;
              comma = true;
            }
        }
      }
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Learned Signals appended to Existing Device Upgrade", JOptionPane.PLAIN_MESSAGE );
    }

    /*
    for ( LearnedSignal s: signals )
    {
      LearnedSignalDecode d = s.getDecodes().get( 0 );
      System.err.println( d.protocolName + ": device " + d.device + " with obc " + d.obc + " on key " + s.getKeyCode() + " on device " + remoteConfig.getRemote().getDeviceButton(s.getDeviceButtonIndex()).getName() );
    }
    if (!signal.getDecodes().isEmpty())
    {
      LearnedSignalDecode d = signal.getDecodes().get( 0 );
      System.err.println( d.protocolName + ": " + d.device + " " + d.obc );
    }
    else
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "Unable to convert the selected Learned Signal to a Device Upgrade since the signal cannot be decoded by DecodeIR.", "Unable to convert to Device Upgrade", JOptionPane.PLAIN_MESSAGE );
    }
    */
  }
  
  public void valueChanged( ListSelectionEvent e )
  {
    super.valueChanged( e );
    if ( !e.getValueIsAdjusting() )
      convertToUpgradeButton.setEnabled( table.getSelectedRowCount() >= 1 );
  }

  private RemoteConfiguration remoteConfig = null;
  
  private JButton convertToUpgradeButton = null;
  private JButton timingSummaryButton = null;
}
