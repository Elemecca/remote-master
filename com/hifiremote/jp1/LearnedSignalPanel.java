package com.hifiremote.jp1;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;

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
    //System.err.println("Checking if can append for protocol " + protocolName + ", device " + device + ", subDevice " + subDevice + "...");

    DeviceUpgrade appendUpgrade = null;
    List<DeviceUpgrade> upgrades = remoteConfig.getDeviceUpgrades();
    for ( DeviceUpgrade u: upgrades )
    {
      if ( (
          (u.getDescription() != null && u.getDescription().contains( "Learned Signal" )) || (u.getNotes() != null && u.getNotes().contains( "Learned Signal" ))
          ) && u.protocol.getName().equals( protocolName ) )
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

    if (appendUpgrade == null)
    {
      DeviceUpgrade upgrade = new DeviceUpgrade( signals, remoteConfig );
      remoteConfig.getDeviceUpgrades().add( upgrade );
      remoteConfig.getOwner().getDeviceUpgradePanel().model.fireTableDataChanged();
      String msg = "The " + signals.length + " selected Learned Signals have been converted\ninto a new Device Upgrade of type CBL\nwith the Setup Code " + upgrade.getSetupCode() + ".\n\nSwitch to the Devices tab to view/edit/etc this new Upgrade.";
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), msg, "Learned Signals converted to New Device Upgrade", JOptionPane.PLAIN_MESSAGE );
    }
    else
    {
      // Append the Learned Signals to the existing upgrade
      ArrayList<String> existingFunctions = new ArrayList<String>();
      ArrayList<String> renamedFunctions = new ArrayList<String>();
      ArrayList<String> shiftedFunctions = new ArrayList<String>();
      ArrayList<String> unassignedFunctions = new ArrayList<String>();
      for ( LearnedSignal s : signals )
      {
        d = s.getDecodes().get( 0 );
        String origName = s.getNotes();
        Button b = remoteConfig.getRemote().getButton( s.getKeyCode() );
        if ( origName == null || origName.isEmpty() )
          origName = b.getName();

        short[] hex = new short[d.hex.length];
        for ( int i=0; i < d.hex.length; i++ )
          hex[i] = (short)d.hex[i];
        Hex funcHex = new Hex( hex );

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
            name = name + "_" + i;
          }
          if (i > 1)
            renamedFunctions.add( origName );
          f = new Function( name, funcHex, s.getNotes() );

          appendUpgrade.getFunctions().add( f );
        }

        if ( appendUpgrade.getFunction( b, Button.NORMAL_STATE ) == null )
          appendUpgrade.setFunction( b, f, Button.NORMAL_STATE );
        else if ( b.allowsKeyMove( Button.SHIFTED_STATE ) && appendUpgrade.getFunction( b, Button.SHIFTED_STATE ) == null )
        {
          appendUpgrade.setFunction( b, f, Button.SHIFTED_STATE );
          shiftedFunctions.add( origName );
        }
        else
          unassignedFunctions.add( origName );
      }
      String msg = "The " + signals.length + " selected Learned Signals were append to existing\n"
          + "Device Upgrade (" + appendUpgrade.getDescription() + " with protocol " + appendUpgrade.getProtocol().getName() + ",\n" 
          + "device " + device + ", and subDevice " + subDevice + ".\n";

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
        msg = msg + "\nThe following could not be assinged to a key due to duplicates:\n   ";
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
      //String msg = "The " + signals.length + " selected Learned Signals have been converted<br>into a new Device Upgrade of type CBL<br>with the Setup Code " + upgrade.getSetupCode() + ".<br><br>Switch to the Devices tab to view/edit/etc this new Upgrade.";      
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
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "Unable to convert the selected Learned Signal to a Device Upgrade since the signal cannot be decoded by DecodeIR.", "Unable to conver to Device Upgrade", JOptionPane.PLAIN_MESSAGE );
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
