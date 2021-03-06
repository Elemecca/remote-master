package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.event.SwingPropertyChangeSupport;

import com.hifiremote.jp1.Activity.Assister;
import com.hifiremote.jp1.RemoteConfiguration.KeySpec;
import com.hifiremote.jp1.SetupPanel.AltPIDStatus;
import com.hifiremote.jp1.translate.Translate;
import com.hifiremote.jp1.translate.Translator;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceUpgrade.
 */
public class DeviceUpgrade extends Highlight
{

  /**
   * Instantiates a new device upgrade.
   */
  public DeviceUpgrade()
  {
    this( ( String[] )null );
  }

  /**
   * Instantiates a new device upgrade.
   * 
   * @param defaultNames
   *          the default names
   */
  public DeviceUpgrade( String[] defaultNames )
  {
    devTypeAliasName = deviceTypeAliasNames[ 0 ];
    initFunctions( defaultNames );
  }

  /**
   * Instantiates a new device upgrade.
   * 
   * @param base
   *          the base
   */
  public DeviceUpgrade( DeviceUpgrade base, DeviceButton newRestriction )
  {
    this.baseUpgrade = base;
    description = base.description;
    setupCode = base.setupCode;
    devTypeAliasName = base.devTypeAliasName;
    remote = base.remote;
    remoteConfig = base.remoteConfig;
    notes = base.notes;
    protocol = base.protocol;
    if ( newRestriction == null || newRestriction == DeviceButton.noButton )
    {
      buttonIndependent = base.buttonIndependent;
      buttonRestriction = base.buttonRestriction;
    }
    else
    {
      buttonIndependent = false;
      buttonRestriction = newRestriction;
    }
    protocolHighlight = base.protocolHighlight;
    sizeCmdBytes = base.sizeCmdBytes;
    sizeDevBytes = base.sizeDevBytes;
    setHighlight( base );

    // Copy assignment colors
    for ( int index : base.assignmentColors.keySet() )
    {
      assignmentColors.put( index, new Color[ 256 ] );
      for ( int i = 0; i < 256; i++ )
      {
        assignmentColors.get( index )[ i ] = base.assignmentColors.get( index )[ i ];
      }
    }

    // copy the device parameter values
    if ( base.protocol != null )
    {
      protocol.setDeviceParms( base.parmValues );
      parmValues = protocol.getDeviceParmValues();
    }

    // Copy the functions and their assignments
    swapList = new LinkedHashMap< Function, Function >();
    
    for ( Function f : base.functions )
    {
      Function f2 = new Function( f );
      f2.setUpgrade( this );
      swapList.put( f, f2 );
      functions.add( f2 );
      for ( Function.User user : f.getUsers() )
      {
        if ( user.db == null || user.db == DeviceButton.noButton )
        {
//          assignments.assign( user.button, f2, user.state );
          f2.addReference( user.button, user.state );
        }
        else if ( user.db.getUpgrade() == base )
        {
          f2.addReference( user.db, user.button );
        }
      }
    }
    for ( int i = 0; i < base.getAssignments().getAssignedFunctions().length; i++ )
    {
      Function f = null;
      if ( ( f = base.getAssignments().getAssignedFunctions()[ i ] ) != null )
      {
        assignments.getAssignedFunctions()[ i ] = swapList.get( f );
      }
    }

    // Copy the external functions and their assignments
    for ( ExternalFunction f : base.extFunctions )
    {
      ExternalFunction f2 = new ExternalFunction( f );
      extFunctions.add( f2 );
      for ( Function.User user : f.getUsers() )
      {
        assignments.assign( user.button, f2, user.state );
      }
    }
    if ( remote.usesEZRC() )
    {
      if ( base.getMacroMap() != null )
      {
        macroMap = new LinkedHashMap< Integer, Macro >();
        macroMap.putAll( base.getMacroMap() );
      }
      if ( base.getLearnedMap() != null )
      {
        learnedMap = new LinkedHashMap< Integer, LearnedSignal >();
        learnedMap.putAll( base.getLearnedMap() );
      }
      if ( base.getFunctionMap() != null )
      {
        functionMap = new LinkedHashMap< Integer, Function >();
        functionMap.putAll( base.getFunctionMap() );  // should these be mapped with corr?
      }
      if ( base.getSelectorMap() != null )
      {
        selectorMap = new LinkedHashMap< Integer, GeneralFunction >();
        selectorMap.putAll( base.getSelectorMap() );
      }
    }
  }

  /**
   * Instantiates a new device upgrade.
   *
   * @param base
   *          the base
   */
  public DeviceUpgrade( LearnedSignal[] signals, RemoteConfiguration remoteConfig, Protocol protocol, boolean convert )
  {
    Remote remote = remoteConfig.getRemote();
    LearnedSignalDecode d = signals[0].getDecodes().get(0);
    int device = d.device;
    int subDevice = d.subDevice;

    description = "Learned Signal Upgrade";
    notes =  "Device Upgrade automatically created by RemoteMaster from " + signals.length + " Learned Signals all with protocol " + d.protocolName;
    if ( device >= 0 )
    {
      notes += ", device " + device;
    }
    if ( subDevice >= 0 )
    {
      notes += ", subdevice " + subDevice;
    }
    notes += ".";
    this.protocol = protocol;
    setupCode = 2000;
    List<DeviceUpgrade> upgrades = remoteConfig.getDeviceUpgrades();
    boolean setupCodeNotAvail = true;
    while ( setupCodeNotAvail )
    {
      setupCodeNotAvail = false;
      for ( DeviceUpgrade u: upgrades )
      {
        if ( u.getDeviceTypeAliasName().equals( "Cable" ) && u.getSetupCode() == setupCode )
        {
          setupCode++;
          setupCodeNotAvail = true;
          break;
        }
      }
    }
    devTypeAliasName = "Cable";

    if ( remote.usesEZRC() && macroMap == null )
    {
      macroMap = new LinkedHashMap< Integer, Macro >();
      learnedMap = new LinkedHashMap< Integer, LearnedSignal >();
      functionMap = new LinkedHashMap< Integer, Function >();
      selectorMap = new LinkedHashMap< Integer, GeneralFunction >();
    }

    sizeCmdBytes = protocol.getDefaultCmd().length();
    sizeDevBytes = protocol.getFixedDataLength();
    
    setRemoteConfig( remoteConfig );

    // copy the device parameter values
    DeviceParameter[] protocolDevParms = protocol.getDeviceParameters();
    parmValues = new Value[protocolDevParms.length];
    for ( int i = 0; i < parmValues.length; i++ )
    {
      if ( protocolDevParms.length > i )
      {
        if ( protocolDevParms[i].getName().startsWith("Device") )
          parmValues[i] = new Value( d.device );
        else if ( protocolDevParms[i].getName().startsWith("Sub Device") )
        {
          System.err.println("New upgrade's subdevice is " + d.subDevice);
          parmValues[i] = new Value( d.subDevice );
        }
        else
          parmValues[i] = new Value( protocolDevParms[i].getValueOrDefault() );
      }
    }
    
    // Copy the functions and their assignments
    List< List< String >> failedToConvert = new ArrayList< List< String > >();
    for ( LearnedSignal s : signals )
    {
      d = s.getDecodes().get( 0 );
      Button b = remote.getButton( s.getKeyCode() );
      String name = s.getSignalName( remote );
      List< String > error = new ArrayList< String >( 2 );
      Hex hex = convert ? d.getProtocolHex( protocol, error ) : d.getSignalHex();
      if ( hex != null )
      {
        Function f = new Function( name, new Hex( hex ), s.getNotes() );
        f.setUpgrade( this );
        if ( remote.isSSD() )
        {
          f.icon = new RMIcon( 9 );
        }
        if ( remote.usesEZRC() )
        {
          f.setGid( Function.defaultGID );
        }         
        functions.add( f );
        assignments.assign( b, f );
      }
      else
      {
        error.set( 0, name );
        failedToConvert.add( error );
      }
    }
      
    if ( !failedToConvert.isEmpty() )
    {
      if ( !LearnedSignalDecode.displayErrors( protocol.getName(), failedToConvert ) )
      {
        this.protocol = null;
      }
    }
  }
  
  /**
   * Reset.
   */
  public void reset()
  {
    reset( defaultNames );
  }

  /**
   * Reset.
   * 
   * @param defaultNames
   *          the default names
   */
  public void reset( String[] defaultNames )
  {
    description = null;
    setupCode = 0;

    // remove all currently assigned functions
    if ( remote != null )
    {
      assignments.clear();
    }

    Collection< Remote > remotes = RemoteManager.getRemoteManager().getRemotes();
    if ( remote == null )
    {
      remote = remotes.iterator().next();
    }
    devTypeAliasName = deviceTypeAliasNames[ 0 ];

    if ( protocol != null )
    {
      protocol.reset();
    }
    if ( !remote.usesEZRC() )
    {
      ProtocolManager pm = ProtocolManager.getProtocolManager();
      java.util.List< String > names = pm.getNames();
      for ( String protocolName : names )
      {
        Protocol p = pm.findProtocolForRemote( remote, protocolName );
        if ( p != null )
        {
          protocol = p;
          break;
        }
      }
    }
    else
    {
      protocol = null;
    }

    if ( protocol != null )
    {
      DeviceParameter[] devParms = protocol.getDeviceParameters();
      for ( int i = 0; i < devParms.length; i++ )
      {
        devParms[ i ].setValue( null );
      }
      setProtocol( protocol, false );
    }

    notes = null;
    file = null;

    functions.clear();
    initFunctions( defaultNames );

    extFunctions.clear();
  }

  /**
   * Inits the functions.
   * 
   * @param names
   *          the names
   */
  private void initFunctions( String[] names )
  {
    defaultNames = names;
    if ( defaultNames == null )
    {
      defaultNames = defaultFunctionNames;
    }
    for ( int i = 0; i < defaultNames.length; i++ )
    {
      Function f = new Function( defaultNames[ i ] );
      f.setUpgrade( this );
      functions.add( f );
    }
  }

  /**
   * Sets the description.
   * 
   * @param text
   *          the new description
   */
  public void setDescription( String text )
  {
    description = text;
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription()
  {
    if ( ( description == null || description.trim().isEmpty() ) 
        && !( buttonRestriction == null  || buttonRestriction == DeviceButton.noButton ) )
    {
      return buttonRestriction.getName();
    }
    return description;
  }

  /**
   * Sets the setup code.
   * 
   * @param setupCode
   *          the new setup code
   */
  public void setSetupCode( int setupCode )
  {
    int oldSetupCode = this.setupCode;
    this.setupCode = setupCode;
    propertyChangeSupport.firePropertyChange( "setupCode", oldSetupCode, setupCode );
  }

  /**
   * Gets the setup code.
   * 
   * @return the setup code
   */
  public int getSetupCode()
  {
    return setupCode;
  }

  public int getSizeDevBytes()
  {
    return sizeDevBytes;
  }

  public void setSizeDevBytes( int sizeDevBytes )
  {
    this.sizeDevBytes = sizeDevBytes;
  }

  public int getSizeCmdBytes()
  {
    return sizeCmdBytes;
  }

  public void setSizeCmdBytes( int sizeCmdBytes )
  {
    this.sizeCmdBytes = sizeCmdBytes;
  }

  /**
   * Checks for defined functions.
   * 
   * @return true, if successful
   */
  public boolean hasDefinedFunctions()
  {
    for ( Function func : functions )
    {
      if ( func.getHex() != null )
      {
        return true;
      }
    }
    for ( Function func : extFunctions )
    {
      if ( func.getHex() != null )
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the remote.
   * 
   * @param newRemote
   *          the new remote
   */
  public void setRemote( Remote newRemote )
  {
    if ( newRemote == remote )
    {
      return;
    }
    Protocol p = protocol;
    ProtocolManager pm = ProtocolManager.getProtocolManager();
    java.util.List< Protocol > protocols = pm.getProtocolsForRemote( newRemote, false );
    if ( p == null )
    {
      if ( !newRemote.usesEZRC() ) 
      {
        protocol = protocols.get( 0 );
      }
    }
    else if ( !protocols.contains( p ) )
    {
      System.err.println( "DeviceUpgrade.setRemote(), protocol " + p.getDiagnosticName() + " is not built into remote "
          + newRemote.getName() );
      Protocol newp = pm.findProtocolForRemote( newRemote, p.getName() );

      if ( newp != null )
      {
        if ( newp != p )
        {
          System.err.println( "Testing if protocol " + newp.getDiagnosticName() + " can be used." );
          System.err.println( "\tChecking for matching dev. parms" );
          DeviceParameter[] parms = p.getDeviceParameters();
          DeviceParameter[] parms2 = newp.getDeviceParameters();

          int[] map = new int[ parms.length ];
          boolean parmsMatch = true;
          for ( int i = 0; i < parms.length; i++ )
          {
            String name = parms[ i ].getName();
            System.err.print( "\tchecking " + name );
            boolean nameMatch = false;
            for ( int j = 0; j < parms2.length; j++ )
            {
              if ( name.equals( parms2[ j ].getName() ) )
              {
                map[ i ] = j;
                nameMatch = true;
                System.err.print( " has a match!" );
                break;
              }
            }
            if ( !nameMatch )
            {
              Object v = parms[ i ].getValue();
              Object d = parms[ i ].getDefaultValue();
              if ( d != null )
              {
                d = ( ( DefaultValue )d ).value();
              }
              System.err.print( " no match!" );

              if ( v == null || v.equals( d ) )
              {
                nameMatch = true;
                map[ i ] = -1;
                System.err.print( " But there's no value anyway!  " );
              }
            }
            System.err.println();
            parmsMatch = nameMatch;
            if ( !parmsMatch )
            {
              break;
            }
          }
          if ( parmsMatch )
          {
            // copy parameters from p to p2!
            Value[] vals = new Value[ parms2.length ];
            System.err.println( "\tCopying dev. parms" );
            for ( int i = 0; i < map.length; i++ )
            {
              if ( map[ i ] == -1 )
              {
                continue;
              }
              System.err.println( "\tfrom index " + i + " (=" + parms[ i ].getValue() + ") to index " + map[ i ] );
              parms2[ map[ i ] ].setValue( parms[ i ].getValue() );
              vals[ map[ i ] ] = new Value( parms[ i ].getValue() );
            }
            newp.setDeviceParms( vals );
            System.err.println();
            System.err.println( "Protocol " + newp.getDiagnosticName() + " will be used." );
            p.convertFunctions( functions, newp, preserveOBC );
            protocol = newp;
            parmValues = vals;
          }
          if ( p instanceof DeviceCombiner && newp instanceof DeviceCombiner )
          {
            for ( CombinerDevice dev : ( ( DeviceCombiner )p ).getDevices() )
            {
              ( ( DeviceCombiner )newp ).add( dev );
            }
          }
        }
      }
      else if ( description == null && file == null && assignments.isEmpty() && !hasDefinedFunctions() )
      {
        remote = newRemote;
        protocol = null;
        reset();
      }
      else
      {
        System.err.println( "DeviceUpgrade.setRemote(), protocol " + p.getDiagnosticName()
            + "is not compatible with remote " + newRemote.getName() );
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "The selected protocol " + p.getDiagnosticName()
            + "\nis not compatible with the selected remote.\n" + "This upgrade will NOT function correctly.\n"
            + "Please choose a different protocol.", "Error", JOptionPane.ERROR_MESSAGE );
      }
    }
    if ( remote != null && remote != newRemote )
    {
      SetupCode.setMax( remote.getSegmentTypes() == null ? remote.usesTwoBytePID() ? 4095 : 2047 : 0x7FFF );
      Button[] buttons = remote.getUpgradeButtons();
      ButtonAssignments newAssignments = new ButtonAssignments();
      java.util.List< java.util.List< String >> unassigned = new ArrayList< java.util.List< String >>();
      for ( int i = 0; i < buttons.length; i++ )
      {
        Button b = buttons[ i ];
        for ( int state = Button.NORMAL_STATE; state <= Button.XSHIFTED_STATE; ++state )
        {
          Function f = assignments.getAssignment( b, state );
          if ( f != null )
          {
            assignments.assign( b, null, state );

            Button newB = newRemote.findByStandardName( b );
            java.util.List< String > temp = null;
            if ( f != null )
            {
              if ( newB != null && newB.allowsKeyMove( state ) )
              {
                newAssignments.assign( newB, f, state );
              }
              else
              {
                temp = new ArrayList< String >();
                temp.add( f.getName() );
                temp.add( b.getName() );
                unassigned.add( temp );
              }
            }
          }
        }
      }
      if ( !unassigned.isEmpty() )
      {
        String message = "<html>Some of the functions defined in the device upgrade were assigned to buttons<br>"
            + "that do not match buttons on the newly selected remote.  The functions and the<br>"
            + "corresponding button names from the original remote are listed below."
            + "<br><br>Use the Button or Layout panel to assign those functions properly.</html>";

        JPanel panel = new JPanel( new BorderLayout() );

        JLabel text = new JLabel( message );
        text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        panel.add( text, BorderLayout.NORTH );
        java.util.List< String > titles = new ArrayList< String >();
        titles.add( "Function name" );
        titles.add( "Button name" );
        Object[][] unassignedArray = new Object[ unassigned.size() ][];
        int i = 0;
        for ( java.util.List< String > l : unassigned )
        {
          unassignedArray[ i++ ] = l.toArray();
        }
        JTableX table = new JTableX( unassignedArray, titles.toArray() );
        Dimension d = table.getPreferredScrollableViewportSize();
        int showRows = 14;
        if ( unassigned.size() < showRows )
        {
          showRows = unassigned.size();
        }
        d.height = ( table.getRowHeight() + table.getRowMargin() ) * showRows;
        table.setPreferredScrollableViewportSize( d );
        panel.add( new JScrollPane( table ), BorderLayout.CENTER );

        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), panel, "Lost Function Assignments",
            JOptionPane.PLAIN_MESSAGE );
      }
      assignments = newAssignments;
    }
    remote = newRemote;
    if ( remote.usesEZRC() && macroMap == null )
    {
      macroMap = new LinkedHashMap< Integer, Macro >();
      learnedMap = new LinkedHashMap< Integer, LearnedSignal >();
      functionMap = new LinkedHashMap< Integer, Function >();
      selectorMap = new LinkedHashMap< Integer, GeneralFunction >();
    }
  }

  /**
   * Gets the remote.
   * 
   * @return the remote
   */
  public Remote getRemote()
  {
    return remote;
  }

  public void setNewRemote( Remote newRemote )
  {
    remote = newRemote;
  }

  /**
   * Sets the device type alias name.
   * 
   * @param name
   *          the new device type alias name
   */
  public void setDeviceTypeAliasName( String name )
  {
    String oldName = devTypeAliasName;
    if ( name != null )
    {
      if ( remote.getDeviceTypeByAliasName( name ) != null )
      {
        devTypeAliasName = name;
      }
      else
      {
        devTypeAliasName = deviceTypeAliasNames[ 0 ];
        System.err.println( "Unable to find device type with alias name " + name );
      }
    }
    propertyChangeSupport.firePropertyChange( "deviceTypeAliasName", oldName, devTypeAliasName );
  }

  /**
   * Gets the device type alias name.
   * 
   * @return the device type alias name
   */
  public String getDeviceTypeAliasName()
  {
    return devTypeAliasName;
  }

  /**
   * Gets the device type.
   * 
   * @return the device type
   */
  public DeviceType getDeviceType()
  {
    return remote.getDeviceTypeByAliasName( devTypeAliasName );
  }

  public boolean setProtocol( Protocol newProtocol )
  {
    return setProtocol( newProtocol, true );
  }
  
  private DeviceUpgrade protocolInUse()
  {
    if ( remoteConfig != null )
    {
      for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
      {
        if ( du == this ) continue;
        if ( du.getProtocol() == protocol ) return du;
      }
    }
    return null;
  }
  
  public AltPIDStatus testAltPID( )
  {
    AltPIDStatus status = new AltPIDStatus();
    if ( protocol == null )
    {
      return status;
    }
    int officialPID = protocol.getID( remote, false ).get( 0 );
    if ( remoteConfig != null )
    {
      for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
      {
        if ( du == baseUpgrade || du.getProtocol() == null || baseUpgrade != null && du.getProtocol() == baseUpgrade.getProtocol() ) continue;
        if ( du.getProtocol() == protocol )
        {
          // Selected protocol is already used by a device upgrade.  Must use same alternate
          status.value = du.getProtocol().getID( remote ).get( 0 );
          status.hasValue = true;
          status.editable = false;
          status.msgIndex = 0x100;
          break;
        }
        else if ( !du.getProtocol().getID( remote ).equals( protocol.getID( remote, false ) ) )
        {
          // Protocol of device upgrade has different PID from selected protocol, so no conflict
          continue;
        }
        else if ( getCode() == null || du.getCode() == null )
        {
          // If both are null then the two protocols are using the same built-in code and there is no conflict.
          // If only one is null then we cannot test if both use the same code.  Even if one has custom code, it
          // may still be the official executor.  To avoid forcing an unnecessary alternate PID, treat as
          // no (known) conflict.
          continue;
        }
        
        if ( du.getCode().equals( getCode() ) )
        {
          // A different protocol with same PID and same code is already used by a device upgrade.
          // Must use same alternate
          status.value = du.getProtocol().getID( remote ).get( 0 );
          status.hasValue = true;
          status.editable = false;
          status.msgIndex = 0x400;
          break;
        }
        else
        {
          // A different protocol with same PID but different code is already used by a device upgrade. 
          //  Alternate required.
          status.required = true;
          status.msgIndex = 0x200;
          // Do not break as we must still look for identical protocol
        }
      }
    }
    Hex currentAltPID = protocol.getRemoteAltPID().get( remote.getSignature() );
    if ( !status.hasValue && currentAltPID != null && currentAltPID.length() > 0 )
    {
      status.hasValue = true;
      status.value = currentAltPID.get( 0 );
    }
 
    ProtocolUpgrade pu = protocol.getCustomUpgrade( remoteConfig, false );
    // If pu not null, there is an existing configuration, pu is an unused protocol upgrade present in it
    // that has the same pid as is returned by protocol.getID(remote), which finds an alternate
    // PID if one is specified.
    Hex puCode = null;
    List< Protocol > builtIn = ProtocolManager.getProtocolManager().getBuiltinProtocolsForRemote( remote,
        protocol.getID( remote, false ) );
    
    if ( pu != null && ( puCode = pu.getCode() ) != null && puCode.length() != 0 )
    {
      // Translate fully, as getCode() also translates fully
      puCode = remote.getProcessor().translate( puCode, remote );
      translateCode( puCode );
    }
    
    if ( status.msgIndex == 0 && puCode != null && !puCode.equals( getCode() ) )
    {
      status.msgIndex = 0x300;
    }

    if ( officialPID > 0x1FF && !remote.usesTwoBytePID() )
    {
      // The standard PID cannot be used for this remote so an alternate PID is required.
      // If already present in configuration then current PID is required.
      status.required = true;
      status.msgIndex |= 1;
    }
    else if ( protocol instanceof ManualProtocol )
    {
      status.msgIndex |= 2;
    }
    else if ( !builtIn.isEmpty() && !builtIn.contains( protocol ) )
    {
      // This is a standard protocol that is not built in but whose standard PID clashes with
      // a built-in protocol, so it will override that protocol in a device upgrade.
      // Allow an alternate PID.
      status.msgIndex |= 3;
    }
    // We now know that this is a standard protocol that is either (a) built in or 
    // (b) is not built in and does not clash with a built-in protocol.  In case (a)
    // we cannot allow an alternate PID, in case (b) it is not necessary, so we do not
    // offer it, unless there is a clash with an existing upgrade or it already has
    // an alternate. If there is a clash then by now, status.msgIndex != 0.
    else if ( builtIn.contains( protocol ) || status.msgIndex == 0 && !status.hasValue )
    {
      status.visible = false;
    }
    // Now a standard protocol, not built in, either clashes with existing upgrade or
    // already has a value.
    else if ( status.msgIndex == 0 && status.hasValue && status.value != officialPID )
    {
      status.msgIndex = 5;
    }
    // else nothing.  This is for standard protocol, not built in, clashes with
    // existing upgrade (msgIndex = 0x200) and/or has value assigned that is official PID.
    
    // If status.hasValue = true and value is official value and protocol is not a manual one 
    // then we cannot have an alternate, but if it is a manual protocol then we can change the
    // PID and it will apply to the other upgrades that share this protocol
    if ( status.hasValue && status.value == officialPID )
    {
      if ( ( status.msgIndex & 0xFF ) != 2 )
      {
        // not a manual protocol
        if ( status.visible )
        {
          // Explain why alternate not available
          status.msgIndex |= 0x800;
        }
        status.hasValue = false;
        status.visible = false;
      }
      else
      {
        status.editable = true;
        if ( ( status.msgIndex & 0xF00 ) == 0x100 )
        {
          status.msgIndex |= 0x400;
        }
      }
    }
    
    if ( protocol.getCustomCode( remote.getProcessor() ) != null )
    {
      // If it has custom code. notify this
      status.msgIndex |= 0x1000;
    }
    
    if ( !status.visible )
    {
      if ( status.required )
      {
        status.msgIndex = 4;
      }
      else
      {
        status.msgIndex &= 0x1000 | ( ( ( status.msgIndex & 0x800 ) > 0 ) ? 0xF00 : 0 );
      }
    }
    return status;
  }

  public boolean isCustom()
  {
    return protocol.getCustomCode( remote.getProcessor() ) != null;
  }
  
  /**
   * Sets the protocol.
   * 
   * @param newProtocol
   *          the new protocol
   * @return true, if successful
   */
  public boolean setProtocol( Protocol newProtocol, boolean messages )
  { 
    Protocol oldProtocol = protocol;
    // Convert device parameters to the new protocol
    if ( protocol != null )
    {
      if ( protocol == newProtocol )
      {
        return false;
      }

      newProtocol.reset();

      if ( newProtocol.getFixedDataLength() == protocol.getFixedDataLength() )
      {
        newProtocol.importFixedData( protocol.getFixedData( parmValues ) );
      }

      DeviceParameter[] parms = protocol.getDeviceParameters();
      DeviceParameter[] parms2 = newProtocol.getDeviceParameters();

      int[] map = new int[ parms.length ];
      for ( int i = 0; i < map.length; i++ )
      {
        map[ i ] = -1;
      }
      boolean parmsMatch = true;
      for ( int i = 0; i < parms.length; i++ )
      {
        String name = parms[ i ].getName();
        boolean nameMatch = false;
        for ( int j = 0; j < parms2.length; j++ )
        {
          if ( name.equals( parms2[ j ].getName() ) )
          {
            map[ i ] = j;
            nameMatch = true;
            break;
          }
        }
        if ( nameMatch )
        {
          parmsMatch = true;
        }
      }

      if ( parmsMatch )
      {
        // copy parameters from p to p2!
        System.err.println( "\tCopying dev. parms" );
        for ( int i = 0; i < map.length; i++ )
        {
          int mappedIndex = map[ i ];
          if ( mappedIndex != -1 )
          {
            System.err.println( "\tfrom index " + i + " to index " + map[ i ] );
            parms2[ mappedIndex ].setValue( parms[ i ].getValue() );
          }
        }
      }

      // convert the functions to the new protocol
      if ( !protocol.convertFunctions( functions, newProtocol, preserveOBC ) )
      {
        propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, oldProtocol );
        return false;
      }
    }
    protocol = newProtocol;
    if ( protocol != null )
    {
      parmValues = protocol.getDeviceParmValues();
      propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, protocol );
    }
    return true;
  }

  /**
   * Gets the protocol.
   * 
   * @return the protocol
   */
  public Protocol getProtocol()
  {
    return protocol;
  }

  /**
   * Sets the notes.
   * 
   * @param notes
   *          the new notes
   */
  public void setNotes( String notes )
  {
    this.notes = notes;
  }

  /**
   * Gets the functions.
   * 
   * @return the functions
   */
  public java.util.List< Function > getFunctions()
  {
    return functions;
  }

  /**
   * Gets the function.
   * 
   * @param name
   *          the name
   * @return the function
   */
  public Function getFunction( String name )
  {
    Function rc = getFunction( name, functions );
    if ( rc == null )
    {
      rc = getFunction( name, extFunctions );
    }
    return rc;
  }

  /**
   * Gets the function.
   * 
   * @param name
   *          the name
   * @param funcs
   *          the funcs
   * @return the function
   */
  public Function getFunction( String name, java.util.List< ? extends Function > funcs )
  {
    for ( Function func : funcs )
    {
      String funcName = func.getName();
      if ( funcName != null && funcName.equalsIgnoreCase( name ) )
      {
        return func;
      }
    }
    return null;
  }
  
  private Function getFunctionByRmirIndex( int rmirIndex )
  {
    for ( Function f : functions )
    {
      if ( f.getRmirIndex() == rmirIndex )
      {
        return f;
      }
    }
    return null;
  }

  /**
   * Gets the function.
   * 
   * @param hex
   *          the hex
   * @return the function
   */
  public Function getFunction( Hex hex )
  {
    if ( hex == null )
    {
      return null;
    }
    for ( Function f : functions )
    {
      if ( f.getHex() == null )
      {
        continue;
      }
      if ( hex.equals( f.getHex() ) )
      {
        return f;
      }
    }
    return null;
  }

  /**
   * Gets the external functions.
   * 
   * @return the external functions
   */
  public java.util.List< ExternalFunction > getExternalFunctions()
  {
    return extFunctions;
  }

  /*
   * public List< KeyMove > getKeyMoves() { return keymoves; }
   */

  /**
   * Sets the file.
   * 
   * @param file
   *          the new file
   */
  public void setFile( File file )
  {
    this.file = file;
  }

  /**
   * Gets the file.
   * 
   * @return the file
   */
  public File getFile()
  {
    return file;
  }

  /**
   * Find digit map index.
   * 
   * @return the short
   */
  private short findDigitMapIndex()
  {
    short[] digitMaps = remote.getDigitMaps();
    if ( digitMaps == null || digitMaps.length == 0 || remote.usesEZRC() || remote.usesSimpleset() )
    {
      return -1;
    }

    int cmdLength = protocol.getDefaultCmd().length();
    short[] digitKeyCodes = new short[ 10 * cmdLength ];
    Button[] buttons = remote.getUpgradeButtons();
    int offset = 0;
    for ( int i = 0; i < 10; i++ , offset += cmdLength )
    {
      Function f = assignments.getAssignment( buttons[ i ] );
      if ( f != null && !f.isExternal() )
      {
        Hex.put( f.getHex(), digitKeyCodes, offset );
      }
    }
    return DigitMaps.findDigitMapIndex( digitMaps, digitKeyCodes );
  }

  /**
   * Import raw upgrade.
   * 
   * @param hexCode
   *          the hex code
   * @param newRemote
   *          the new remote
   * @param newDeviceTypeAliasName
   *          the new device type alias name
   * @param pid
   *          the pid
   * @param pCode
   *          the code
   * @throws ParseException
   *           the parse exception
   */
  public void importRawUpgrade( Hex hexCode, Remote newRemote, String newDeviceTypeAliasName, Hex pid, Hex pCode )
      throws java.text.ParseException
  {
    reset();
    System.err.println( "DeviceUpgrade.importRawUpgrade" );
    System.err.println( "  hexCode=" + hexCode.toString() );
    System.err.println( "  newRemote=" + newRemote );
    System.err.println( "  newDeviceTypeAliasName=" + newDeviceTypeAliasName );
    System.err.println( "  pid=" + pid.toString() );
    System.err.println( "  pCode=" + pCode );
    int index = 1;
    if ( newRemote.usesTwoBytePID() )
    {
      index++ ;
    }

    short[] code = hexCode.getData();
    remote = newRemote;
    functions.clear();
    devTypeAliasName = newDeviceTypeAliasName;
    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    ButtonMap map = devType.getButtonMap();

    int digitMapIndex = -1;
    if ( !remote.getOmitDigitMapByte() && index < code.length )
    {
      digitMapIndex = code[ index++ ] - 1;
      if ( digitMapIndex >= remote.getDigitMaps().length )
      {
        // Illegal value so treat as absent
        digitMapIndex = -1;
      }
    }
    java.util.List< Button > buttons = null;
    if ( map != null && index < code.length )
    {
      buttons = map.parseBitMap( code, index, digitMapIndex != -1 );
    }
    else
    {
      buttons = new ArrayList< Button >();
    }

    while ( index < code.length && ( code[ index++ ] & 1 ) == 0 )
    {
      ; // skip over the bitMap
    }

    int fixedDataOffset = index;
    int fixedDataLength = 0;
    int cmdLength = 0;
    short[] fixedData = null;
    Hex fixedDataHex = null;
    // Determine the fixed and variable data lengths and fixed data bytes where the
    // raw upgrade on its own provides enough info to do so.
    if ( remote.getSegmentTypes() != null )
    {
      fixedDataLength = sizeDevBytes;
      cmdLength = sizeCmdBytes;
    }
    else if ( pCode != null && pCode.length() > 2 )
    {
      Processor proc = newRemote.getProcessor(); 
      fixedDataLength = Protocol.getFixedDataLengthFromCode( proc.getEquivalentName(), pCode );
      cmdLength = Protocol.getCmdLengthFromCode( proc.getEquivalentName(), pCode );
    }
    
    if ( cmdLength > 0 )
    {
      System.err.println( "fixedDataLength=" + fixedDataLength + " and cmdLength=" + cmdLength );
      fixedData = new short[ fixedDataLength ];
      System.arraycopy( code, fixedDataOffset, fixedData, 0, fixedDataLength );
      fixedDataHex = new Hex( fixedData );
    }
    Value[] vals = parmValues;
    java.util.List< Protocol > protocols = null;
    boolean isBuiltIn = true;
    if ( pCode == null )
    {
      // Only get protocol variants in protocols.ini that are built in to the remote.
      protocols = ProtocolManager.getProtocolManager().getBuiltinProtocolsForRemote( remote, pid );
    }
    
    // When pCode is null, this list can still be empty because either
    //   (a) there is no built-in protocol with this pid, or
    //   (b) there is a built-in protocol with this pid but either the protocol itself,
    //       or at least the built-in variant, is not in protocols.ini
    // These are both problem situations, but (a) is a problem with the remote and
    // (b) a problem with protocols.ini.
    //
    // These cases can be distinguished by testing if the remote supports any protocol
    // variant with this pid.  In case (a) the remote should have a protocol upgrade
    // with this pid but it is missing.  We treat this in the same way as when the protocol
    // upgrade is present, by testing all protocols with this pid that are in protocols.ini.
    // In case (b) we will need to create a new entry for ProtocolManager.
    if ( pCode != null || remote.getSupportedVariantNames( pid ) == null )
    {
      // Get all protocol variants, whether or not built in to the remote.
      isBuiltIn = false;
      protocols = ProtocolManager.getProtocolManager().findByPID( pid );
      // Add to this a list by Alternate PID, including user alternates
      List< Protocol > pList = ProtocolManager.getProtocolManager().findByAlternatePID( remote, pid, true );
      if ( pList != null ) protocols.addAll( pList );
    }
    
    Protocol tentative = null;
    Value[] tentativeVals = null;
    Protocol p = null;
    int matchLenCount = -1;
    for ( Protocol tryit : protocols )
    {
      Protocol pOld = p;
      p = tryit;
      System.err.println( "Checking protocol " + p.getDiagnosticName() );
      if ( !remote.supportsVariant( pid, p.getVariantName() ) && !p.hasCode( remote ) )
      {
        // p is not built in but protools.ini has no code appropriate
        // for this remote, so skip it.
        p = pOld;
        continue;
      }
      int tempLength = fixedDataLength;
      if ( cmdLength == 0 )
      {
        // Fixed data length not determined by raw upgrade so set it
        // from protocol under test.  Cmd length not needed here.
        tempLength = p.getFixedDataLength();
        fixedData = new short[ tempLength ];
        System.arraycopy( code, fixedDataOffset, fixedData, 0, tempLength );
        fixedDataHex = new Hex( fixedData );
      }
      else if ( cmdLength != p.getDefaultCmd().length() || fixedDataLength != p.getFixedDataLength() )
      {
        // This can only happen when length is determined by raw upgrade.
        if ( isBuiltIn )
        {
          // Possible only for segmented and XSight remotes
          // Before reporting error, check whether any other identified protocol matches these length values.
          if ( matchLenCount < 0 )
          {
            // Check not yet performed
            matchLenCount = 0;
            for ( Protocol pTest : protocols )
              if ( cmdLength == pTest.getDefaultCmd().length() && fixedDataLength == pTest.getFixedDataLength() )
                matchLenCount++;
          }
          if ( matchLenCount > 0 )
          {
            // There are matching protocols, so skip this one
            p = pOld;
            continue;
          }

          // No protocols match length data so report error
          String title = "Protocol Variant Error";
          String message = "Error in RDF.  Wrong variant specified for PID = " + 
              pid + ".  Number of fixed/command bytes\n" +
              "should be " + fixedDataLength + "/" + cmdLength +
              ", for specified variant it is " + p.getFixedDataLength() +
              "/" + p.getDefaultCmd().length() + ".";
          JOptionPane.showMessageDialog( null, message, title, JOptionPane.WARNING_MESSAGE );
        }
        System.err.println( "Command or fixed data length doesn't match!" );
        p = pOld;
        continue;
      }
      
      // At this point either:
      // (a) fixed and variable data lengths are determined by raw upgrade and p is
      //     compatible with them, or
      // (b) they are not so determined, and instead have been determined from p.
      System.err.println( "Imported fixedData is " + fixedDataHex );
      vals = p.importFixedData( fixedDataHex );
      System.err.print( "Imported device parms are:" );
      for ( Value v : vals )
      {
        System.err.print( " " + v.getValue() );
      }
      System.err.println();
      Hex calculatedFixedData = p.getFixedData( vals );
      System.err.println( "Calculated fixedData is " + calculatedFixedData );
      Hex mask = p.getFixedDataMask();
      Hex maskedCalculatedData = calculatedFixedData.applyMask( mask );
      Hex maskedImportedData = fixedDataHex.applyMask( mask );
      if ( maskedCalculatedData.equals( maskedImportedData ) )
      {
        // Either (a) or (b) above and in addition the fixed data of the device upgrade is
        // compatible with the translators for p
        System.err.println( "It's a match!" );

        // Get the code currently used by p, and by current tentative protocol if there is
        // one, which in both cases will be custom code if that has been set
        Hex tentativeCode = getCode( p );
        Hex oldTentativeCode = ( tentative == null ) ? null : getCode( tentative );

        if ( tentative == null || tempLength > tentative.getFixedDataLength() || pCode != null
            && pCode.equals( tentativeCode ) && !tentativeCode.equals( oldTentativeCode ) )
        {
          // Replace the tentative protocol since either
          // (a) one hasn't yet been set, or
          // (b) there is no protocol upgrade, both existing and new tentative protocols
          // are built in, but the new one matches on a longer set of fixed data, or
          // (c) there is a protocol upgrade and the current code of the new tentative
          // protocol (which may be custom) matches its code exactly but that of the
          // old tentative protocol does not. If more than one protocol matches
          // exactly then selection is made by the criterion below.
          if ( tentative != null )
          {
            if ( tempLength > tentative.getFixedDataLength() )
            {
              System.err.println( "And it matches on a longer set of fixed data!" );
            }

            else
            {
              System.err.println( "And its code matches the protocol upgrade code!" );
            }
          }
          tentative = p;
          tentativeVals = vals;
        }
        else if ( tentative != null && tentativeCode != null && tentativeCode.equals( oldTentativeCode )
            && p.getOEMParmVariance( vals ) < tentative.getOEMParmVariance( tentativeVals ) )
        {
          // If a further selection is required because there are two protocols with the same
          // code then test on values of OEM and Parm parameters from the fixed data.
          System.err.println( String.format( "And protocol code is identical but better match on OEM or Parm parameters "
              + "(variance %d instead of %d)", p.getOEMParmVariance( vals ),
              tentative.getOEMParmVariance( tentativeVals ) ) );
          tentative = p;
          tentativeVals = vals;
        }
      }
    }

    if ( cmdLength == 0 )
    {
      // Fixed and variable data lengths not determined by raw upgrade alone, so set
      // defaults from device hex alone, on the assumption (which will generally be true) that
      // the number of mapped buttons is greater than the number of fixed bytes. (This is
      // the way that IR.exe always determines these for built-in protocols since it does
      // not have access to the protocol code).  These defaults will be overridden if
      // protocol is located in protocols.ini.
      System.err.println( "Calculating default fixed data and command lengths" );
      int dataLength = hexCode.length() - fixedDataOffset;
      cmdLength = ( buttons.size() > 0 ) ? ( dataLength / buttons.size() ) : 1;
      fixedDataLength = dataLength - cmdLength * buttons.size();
      System.err.println( "Calculated: Fixed data length = " + fixedDataLength + ", Command length = " + cmdLength );  
    }
    ManualProtocol mp = null;

    if ( tentative != null ) // && (( pCode == null ) || pCode.equals( getCode( tentative ))))
    {
      // Found a match.
      p = tentative;
      System.err.println( "Using " + p.getDiagnosticName() );
      fixedDataLength = p.getFixedDataLength();
      cmdLength = p.getDefaultCmd().length();
      parmValues = tentativeVals;
      ProtocolUpgrade newProtocolUpgrade = null;
      isBuiltIn = ProtocolManager.getProtocolManager().getBuiltinProtocolsForRemote( remote, pid ).contains( p );
      
      if ( !isBuiltIn && !p.getID( remote, false ).equals( pid ) )
      {
        // PID must be in remote-specific list of alternate pids, so set as current alternate
        p.setAltPID( remote, pid );
      }
      
      if ( pCode != null && remote.doForceEvenStarts() )
      {
        // check for possibility that last byte(s) of pCode is/are spurious
        int pLen = pCode.length();
        int excess = pLen - getCode( p ).length();
        if ( excess > 0 && excess < remote.getForceModulus() 
            && pCode.subHex( 0, pLen - excess ).equals( getCode( p ) ) )
        {
          pCode = pCode.subHex( 0, pLen - excess );
        }
      }
      
      if ( pCode != null && ( !pCode.equals( getCode( p ) ) || isBuiltIn ) )
      {
        // Custom code always generates a protocol upgrade in the binary image, so if a protocol
        // upgrade is present for a built-in protocol we must always set it as custom code so that
        // it is generated on output. For a protocol that is not built in, there will always be
        // a protocol upgrade output, so then we only need to set custom code if it differs from
        // the standard code.
        if ( remoteConfig != null )
        {
          // Try to make sense of anomalous situations in which there is more than one protocol
          // upgrade for the same pid.
          if ( !isBuiltIn )
          {
            // See if the standard code for the protocol is actually present but is not the
            // first upgrade with that pid.
            for ( ProtocolUpgrade pu : remoteConfig.getProtocolUpgrades() )
            {
              if ( pu.getPid() == pid.get( 0 ) && pu.getCode().equals( getCode( p ) ) )
              {
                newProtocolUpgrade = pu;
                remoteConfig.protocolUpgradeUsed = pu;
                break;
              }
            }
          }

          if ( newProtocolUpgrade == null )
          {
            // There wasn't, or protocol is built in, so see if there is one that matches on fixed and
            // command lengths; this will be the present one if that matches, but it may not.
            String proc = remote.getProcessor().getEquivalentName();
            for ( ProtocolUpgrade pu : remoteConfig.getProtocolUpgrades() )
            {
              if ( pu.getPid() == pid.get( 0 )
                  && Protocol.getFixedDataLengthFromCode( proc, pu.getCode() ) == p.getFixedDataLength()
                  && Protocol.getCmdLengthFromCode( proc, pu.getCode() ) == p.getDefaultCmd().length() )
              {
                remoteConfig.protocolUpgradeUsed = pu;
                pCode = pu.getCode();
                break;
              }
            }
          }
        }
        if ( newProtocolUpgrade == null )
        {
          System.err.println( "Protocol code doesn't match or protocol is built-in. Setting custom code." );
          p.addCustomCode( remote.getProcessor(), pCode );
        }
      }
      else if ( pCode == null && !isBuiltIn )
      {
        // This is an error situation, in that there is no code for the protocol used by this device.
        // RMIR should not be able to generate it, but if it is found then it should not automatically
        // add that code. Use an empty custom code to signify this situation.
        p.addCustomCode( remote.getProcessor(), new Hex() );
      }
    }
    else if ( p != null && pCode == null )
    {
      // Found a matching PID, there's no protocol code, but couldn't
      // recreate the fixed data. Maybe there's some reason to use
      // non-standard fixed data, so we create a derived protocol.
      System.err.println( "Creating a derived protocol" );
      Properties props = new Properties();
      for ( Processor pr : ProcessorManager.getProcessors() )
      {
        Hex hCode = p.getCode( pr );
        if ( hCode != null )
        {
          props.put( "Code." + pr.getEquivalentName(), hCode.toString() );
        }
      }
      String variant = p.getVariantName();
      if ( variant != null && variant.length() > 0 )
      {
        props.put( "VariantName", variant );
      }
      p = ProtocolFactory.createProtocol( "pid: " + pid.toString(), pid, "Protocol", props );
      ProtocolManager.getProtocolManager().add( p );
      fixedDataLength = p.getFixedDataLength();
      cmdLength = p.getDefaultCmd().length();
      parmValues = p.importFixedData( fixedDataHex );
    }
    else if ( isBuiltIn )
    {
      // Protocol, or at least its supported variant, missing from protocols.ini
      // or there is a fixed/cmd length conflict with entry in protocols.ini.
      List< String > variants = remote.getSupportedVariantNames( pid );
      String variant = variants.get( 0 );
      if ( !protocols.isEmpty() )
      {
        // Protocol variant is in protocols.ini but doesn't match on fixed/variable
        // data lengths.  Treat as RDF error and mark variant as unknown.
        variants.remove( variant );
        variant = "???";
        variants.add( variant );
      }
      p = ProtocolManager.getProtocolManager().createMissingProtocol( pid, variant, fixedDataLength, cmdLength );
      fixedData = new short[ fixedDataLength ];
      System.arraycopy( code, fixedDataOffset, fixedData, 0, fixedDataLength );
      fixedDataHex = new Hex( fixedData );
      parmValues = p.importFixedData( fixedDataHex );
    }
    else
    {
      // Don't have anything we can use, so create a manual protocol
      if ( pCode == null )
      {
        // Protocol code is required but absent.
        pCode = new Hex(); // signifies missing code
      }

      System.err.println( "Using a Manual Protocol" );
      fixedData = new short[ fixedDataLength ];
      System.arraycopy( code, fixedDataOffset, fixedData, 0, fixedDataLength );
      int cmdType = ManualProtocol.ONE_BYTE;
      if ( cmdLength == 2 )
      {
        cmdType = ManualProtocol.AFTER_CMD;
      }
      if ( cmdLength > 2 )
      {
        cmdType = cmdLength << 4;
      }

      ArrayList< Value > parms = new ArrayList< Value >();
      for ( short temp : fixedData )
      {
        parms.add( new Value( temp & 0xFF ) );
      }
      parmValues = parms.toArray( new Value[ fixedDataLength ] );

      String pName = ManualProtocol.getDefaultName( pid );
      mp = new ManualProtocol( pName, pid, cmdType, "MSB", 8, parms, new short[ 0 ], 8 );
      mp.setCode( pCode, remote.getProcessor() );
      ProtocolManager.getProtocolManager().add( mp );
      p = mp;
    }

    if ( digitMapIndex != -1 )
    {
      int mapNum = remote.getDigitMaps()[ digitMapIndex ];
      Hex[] hexCmds = DigitMaps.getHexCmds( mapNum, cmdLength );
      for ( int i = 0; i < hexCmds.length; ++i )
      {
        Function f = new Function();
        String name = Integer.toString( i );
        f.setName( name );
        f.setUpgrade( this );
        Hex hex = hexCmds[ i ];
        if ( cmdLength < hex.length() )
        {
          hex = hex.subHex( 0, cmdLength );
        }
        f.setHex( hex );
        Button b = map.get( i );
        assignments.assign( b, f );
        functions.add( f );
      }
    }

    index += fixedDataLength;
    int index2 = index + cmdLength * buttons.size();

    protocol = p;
    int missingIndex = 1;
    for ( Button b : buttons )
    {
      if ( index >= code.length )
      {
        break;
      }
      short[] cmd = new short[ cmdLength ];
      for ( int i = 0; i < cmdLength; i++ )
      {
        cmd[ i ] = code[ index++ ];
      }
      String name = remote.usesEZRC() ?  getFunctionName( b ) : b.getName();
      if ( name == null )
      {
        name = "__missing" + missingIndex++;
      }
      Function f = new Function( name );
      f.setHex( new Hex( cmd ) );
      f.setUpgrade( this );
      functions.add( f );
      if ( index2 < code.length - 1 )
      {
        f.setGid( Hex.get( code, index2 ) );
        index2 += 2;
      }
      assignments.assign( b, f );
    }
    if ( buttonRestriction != null && buttonRestriction.getSoftButtonNames() != null )
    {
      for ( Button b : buttonRestriction.getSoftButtonNames().keySet() )
      {
        if ( buttons.contains( b ) )
        {
          continue;
        }
        Function f = new Function( getFunctionName( b ) );
        f.setUpgrade( this );
        functions.add( f );
        assignments.assign( b, f );
      }
    }
  }
  
  public String getFunctionName( Button btn )
  {
    String name = null;
    if ( buttonRestriction != null && buttonRestriction.getSoftButtonNames() != null
        && ( name = buttonRestriction.getSoftButtonNames().get( btn ) ) != null )
    {
      return name;
    }
    else if ( buttonRestriction != null && buttonRestriction.getSoftFunctionNames() != null
        && ( name = buttonRestriction.getSoftFunctionNames().get( btn ) ) != null )
    {
      return name;
    }
    else
    {
      return null;//btn.getName();
    }
  }

  /**
   * Gets the hex setup code.
   * 
   * @return the hex setup code
   */
  public short[] getHexSetupCode()
  {
    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    short[] id = protocol.getID( remote ).getData();
    short temp = ( short )( devType.getNumber() * 0x1000 + setupCode - remote.getDeviceCodeOffset() );
    if ( !remote.usesTwoBytePID() )
    {
      temp += ( id[ 0 ] & 1 ) * 0x0800;
    }

    short[] rc = new short[ 2 ];
    rc[ 0 ] = ( short )( temp >> 8 & 0xFF );
    rc[ 1 ] = ( short )( temp & 0xFF );
    return rc;
  }

  public int getHexSetupCodeValue()
  {
    short[] hexCode = getHexSetupCode();
    return ( hexCode[ 0 ] << 8 ) + hexCode[ 1 ];
  }

  /**
   * Gets the key moves.
   * 
   * @return the key moves
   */
  public java.util.List< KeyMove > getKeyMoves()
  {
    return getKeyMoves( -1 );
  }

  public java.util.List< KeyMove > getKeyMoves( int deviceButtonIndex )
  {
    java.util.List< KeyMove > keyMoves = new ArrayList< KeyMove >();
    if ( protocol == null )
    {
      return keyMoves;
    }
    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    for ( Button button : remote.getUpgradeButtons() )
    {
      Function f = assignments.getAssignment( button, Button.NORMAL_STATE );
      KeyMove keyMove = button.getKeyMove( f, 0, setupCode, devType, remote, protocol.getKeyMovesOnly() );
      if ( keyMove != null )
      {
        keyMoves.add( keyMove );
        keyMove.setNotes( f.getNotes() );
        if ( deviceButtonIndex >= 0 )
        {
          keyMove.setHighlight( getAssignmentColor( button.getKeyCode( Button.NORMAL_STATE ), deviceButtonIndex ) );
        }
      }

      f = assignments.getAssignment( button, Button.SHIFTED_STATE );
      if ( button.getShiftedButton() != null )
      {
        f = null;
      }
      keyMove = button.getKeyMove( f, remote.getShiftMask(), setupCode, devType, remote, protocol.getKeyMovesOnly() );
      if ( keyMove != null )
      {
        keyMoves.add( keyMove );
        keyMove.setNotes( f.getNotes() );
        if ( deviceButtonIndex >= 0 )
        {
          keyMove.setHighlight( getAssignmentColor( button.getKeyCode( Button.SHIFTED_STATE ), deviceButtonIndex ) );
        }
      }

      f = assignments.getAssignment( button, Button.XSHIFTED_STATE );
      if ( button.getXShiftedButton() != null )
      {
        f = null;
      }
      keyMove = button.getKeyMove( f, remote.getXShiftMask(), setupCode, devType, remote, protocol.getKeyMovesOnly() );
      if ( keyMove != null )
      {
        keyMoves.add( keyMove );
        keyMove.setNotes( f.getNotes() );
        if ( deviceButtonIndex >= 0 )
        {
          keyMove.setHighlight( getAssignmentColor( button.getKeyCode( Button.XSHIFTED_STATE ), deviceButtonIndex ) );
        }
      }
    }
    return keyMoves;
  }

  /**
   * Gets the upgrade text.
   * 
   * @param includeNotes
   *          the include notes
   * @return the upgrade text
   */
  public String getUpgradeText()
  {
    StringBuilder buff = new StringBuilder( 400 );
    if ( remote.usesTwoBytePID() )
    {
      buff.append( "Upgrade Code2 = " );
    }
    else
    {
      buff.append( "Upgrade Code 0 = " );
    }

    short[] deviceCode = getHexSetupCode();

    buff.append( Hex.toString( deviceCode ) );
    buff.append( " (" );
    buff.append( devTypeAliasName );
    buff.append( '/' );
    DecimalFormat df = new DecimalFormat( "0000" );
    buff.append( df.format( setupCode ) );
    buff.append( ")" );

    String descr = "";
    if ( description != null )
    {
      descr = description.trim();
    }
    if ( descr.length() != 0 )
    {
      buff.append( ' ' );
      buff.append( descr );
    }
    buff.append( " (RM " );
    buff.append( RemoteMaster.version + " build " + RemoteMaster.getBuild() );
    buff.append( ')' );

    try
    {
      BufferedReader rdr = new BufferedReader( new StringReader( Hex.toString( getUpgradeHex().getData(), 16 ) ) );
      String line = null;
      while ( ( line = rdr.readLine() ) != null )
      {
        buff.append( "\n " );
        buff.append( line );
      }
    }
    catch ( IOException ioe )
    {
      ioe.printStackTrace( System.err );
    }

    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    ButtonMap map = devType.getButtonMap();
    Button[] buttons = remote.getUpgradeButtons();
    boolean hasKeyMoves = false;
    int i;
    for ( i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      Function f = assignments.getAssignment( b, Button.NORMAL_STATE );
      Function sf = assignments.getAssignment( b, Button.SHIFTED_STATE );
      if ( b.getShiftedButton() != null )
      {
        sf = null;
      }
      Function xf = assignments.getAssignment( b, Button.XSHIFTED_STATE );
      if ( b.getXShiftedButton() != null )
      {
        xf = null;
      }
      if ( f != null && ( map == null || protocol.getKeyMovesOnly() || !map.isPresent( b ) || f.isExternal() )
          || sf != null && sf.getHex() != null || xf != null && xf.getHex() != null )
      {
        hasKeyMoves = true;
        break;
      }
    }
    if ( hasKeyMoves )
    {
      deviceCode[ 0 ] = ( short )( deviceCode[ 0 ] & 0xF7 );
      buff.append( "\nKeyMoves" );
      boolean first = true;
      for ( ; i < buttons.length; i++ )
      {
        Button button = buttons[ i ];

        Function f = assignments.getAssignment( button, Button.NORMAL_STATE );
        first = appendKeyMove( buff,
            button.getKeyMove( f, 0, deviceCode, devType, remote, protocol.getKeyMovesOnly() ), f, first );
        f = assignments.getAssignment( button, Button.SHIFTED_STATE );
        if ( button.getShiftedButton() != null )
        {
          f = null;
        }
        first = appendKeyMove( buff,
            button.getKeyMove( f, remote.getShiftMask(), deviceCode, devType, remote, protocol.getKeyMovesOnly() ), f,
            first );
        f = assignments.getAssignment( button, Button.XSHIFTED_STATE );
        if ( button.getXShiftedButton() != null )
        {
          f = null;
        }
        first = appendKeyMove( buff,
            button.getKeyMove( f, remote.getXShiftMask(), deviceCode, devType, remote, protocol.getKeyMovesOnly() ), f,
            first );
      }
    }

    buff.append( "\nEnd" );

    return buff.toString();
  }

  /**
   * Gets the upgrade length.
   * 
   * @return the upgrade length
   */
  public int getUpgradeLength()
  {
    int rc = 0;

    // add the 2nd byte of the PID
    rc++ ;

    if ( remote.usesTwoBytePID() )
    {
      rc++ ;
    }

    // add the digitMapIndex
    int digitMapIndex = -1;

    if ( !remote.getOmitDigitMapByte() )
    {
      rc++ ;
      digitMapIndex = findDigitMapIndex();
    }

    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    ButtonMap map = devType.getButtonMap();
    if ( map != null )
    {
      rc += map.toBitMap( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments ).length;
    }

    rc += protocol.getFixedData( parmValues ).length();

    if ( map != null )
    {
      short[] data = map.toCommandList( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments );
      if ( data != null )
      {
        rc += data.length;
        if ( remote.usesEZRC() )
        {
          rc += map.getIndexList().length;
        }
      }
    }
    return rc;
  }

  /**
   * Gets the upgrade hex.
   * 
   * @return the upgrade hex
   */
  public Hex getUpgradeHex()
  {
    java.util.List< short[] > work = new ArrayList< short[] >();

    // add the 2nd byte of the PID

    short[] data = null;
    if ( remote.usesTwoBytePID() )
    {
      data = protocol.getID( remote ).getData();
    }
    else
    {
      data = new short[ 1 ];
      data[ 0 ] = protocol.getID( remote ).getData()[ 1 ];
    }
    work.add( data );

    short digitMapIndex = -1;

    if ( !remote.getOmitDigitMapByte() )
    {
      data = new short[ 1 ];
      digitMapIndex = findDigitMapIndex();
      if ( digitMapIndex == -1 )
      {
        data[ 0 ] = 0;
      }
      else
      {
        data[ 0 ] = digitMapIndex;
      }

      work.add( data );
    }

    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    ButtonMap map = devType.getButtonMap();
    if ( map != null )
    {
      work.add( map.toBitMap( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments ) );
    }

    work.add( protocol.getFixedData( parmValues ).getData() );

    if ( map != null )
    {
      data = map.toCommandList( digitMapIndex != -1, protocol.getKeyMovesOnly(), assignments );
      if ( data != null && data.length != 0 )
      {
        work.add( data );
        if ( remote.usesEZRC() )
        {
          work.add( map.getIndexList() );
        }
      }
      mappedButtons = map.getButtonList();
    }

    int length = 0;
    for ( short[] temp : work )
    {
      length += temp.length;
    }

    int offset = 0;
    short[] rc = new short[ length ];
    for ( short[] source : work )
    {
      System.arraycopy( source, 0, rc, offset, source.length );
      offset += source.length;
    }
    return new Hex( rc );
  }

  /**
   * Append key move.
   * 
   * @param buff
   *          the buff
   * @param keyMove
   *          the key move
   * @param f
   *          the f
   * @param includeNotes
   *          the include notes
   * @param first
   *          the first
   * @return true, if successful
   */
  private boolean appendKeyMove( StringBuilder buff, short[] keyMove, Function f, boolean first )
  {
    if ( keyMove == null || keyMove.length == 0 )
    {
      return first;
    }

    if ( !first )
    {
      buff.append( '\u00a6' );
    }

    buff.append( "\n " );

    buff.append( Hex.toString( keyMove ) );

    buff.append( '\u00ab' );
    buff.append( f.getName() );
    String notes = f.getNotes();
    if ( notes != null && notes.length() != 0 )
    {
      buff.append( ": " );
      buff.append( notes );
    }
    buff.append( '\u00bb' );

    return false;
  }

  /**
   * Store.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void store() throws IOException
  {
    store( file );
  }

  /**
   * Value array to string.
   * 
   * @param parms
   *          the parms
   * @return the string
   */
  public static String valueArrayToString( Value[] parms )
  {
    StringBuilder buff = new StringBuilder( 200 );
    for ( int i = 0; i < parms.length; i++ )
    {
      if ( i > 0 )
      {
        buff.append( ' ' );
      }
      Value parm = parms[ i ];
      if ( parm == null )
      {
        buff.append( "null" );
      }
      else
      {
        buff.append( parms[ i ].getUserValue() );
      }
    }
    return buff.toString();
  }

  /**
   * String to value array.
   * 
   * @param str
   *          the str
   * @return the value[]
   */
  public static Value[] stringToValueArray( String str )
  {
    StringTokenizer st = new StringTokenizer( str );
    Value[] parms = new Value[ st.countTokens() ];
    for ( int i = 0; i < parms.length; i++ )
    {
      String token = st.nextToken();
      Object val = null;
      if ( !token.equals( "null" ) )
      {
        if ( token.equals( "true" ) )
        {
          val = new Integer( 1 );
        }
        else if ( token.equals( "false" ) )
        {
          val = new Integer( 0 );
        }
        else
        {
          val = Integer.parseInt( token );
        }
      }
      parms[ i ] = new Value( val, null );
    }
    return parms;
  }

  /**
   * Store.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void store( File file ) throws IOException
  {
    this.file = file;
    StringWriter sw = new StringWriter();
    PropertyWriter pw = new PropertyWriter( new PrintWriter( sw ) );
    // For XSight remotes, do not store functions with no hex data
    store( pw, remote.usesEZRC() );
    pw.close();
    baseline = sw.toString();
    FileWriter fw = new FileWriter( file );
    fw.write( baseline );
    fw.flush();
    fw.close();
  }

  public void store( PropertyWriter out )
  {
    store( out, false );
  }

  public void store( PropertyWriter out, boolean dataOnly )
  {
    if ( description != null )
    {
      out.print( "Description", description );
    }
    out.print( "Remote.name", remote.getName() );
    out.print( "Remote.signature", remote.getSignature() );
    super.store( out );
    out.print( "DeviceType", devTypeAliasName );
    DeviceType devType = remote.getDeviceTypeByAliasName( devTypeAliasName );
    out.print( "DeviceIndex", Integer.toHexString( devType.getNumber() ) );
    out.print( "SetupCode", Integer.toString( setupCode ) );
    if ( !buttonIndependent )
    {
      out.print( "ButtonIndependent", "false" );
    }
    if ( buttonRestriction.getButtonIndex() != DeviceButton.noButton.getButtonIndex() )
    {
      out.print( "ButtonIndex", buttonRestriction.getButtonIndex() );
    }
    // protocol.setDeviceParms( parmValues );
    try
    {
      if ( protocol != null )
      {
        protocol.store( out, parmValues, remote );
      }
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
    if ( notes != null )
    {
      out.print( "Notes", notes );
    }
    int i = 0;
    for ( Function func : functions )
    {
      if ( dataOnly && ( func.getData() == null || func.getSerial() >= 0 ) )
      {
        continue;
      }
      func.setRmirIndex( i );
      func.store( out, "Function." + i++ );
    }

    i = 0;
    for ( ExternalFunction func : extFunctions )
    {
      func.store( out, "ExtFunction." + i++ );
    }

    List< Button>  buttons = new ArrayList< Button >( Arrays.asList( remote.getUpgradeButtons() ) );
    if ( remote.usesEZRC() )
    {
      List< Button> sysBtns = remote.getButtonGroups().get( "System" );
      if ( sysBtns != null )
      {
        buttons.addAll( sysBtns );
      }
    }
    
    String regex = "\\|";
    String replace = "\\\\u007c";
    for ( i = 0; i < buttons.size(); i++ )
    {
      Button b = buttons.get( i );
      Function f = assignments.getAssignment( b, Button.NORMAL_STATE );

      String fstr;
      if ( f == null )
      {
        fstr = "null";
      }
      else if ( remote.usesEZRC() )
      {
        fstr = "Function." + f.getRmirIndex();
      }
      else
      {
        fstr = f.getName().replaceAll( regex, replace );
      }

      Function sf = assignments.getAssignment( b, Button.SHIFTED_STATE );
      String sstr;
      if ( sf == null )
      {
        sstr = "null";
      }
      else
      {
        sstr = sf.getName().replaceAll( regex, replace );
      }

      Function xf = assignments.getAssignment( b, Button.XSHIFTED_STATE );
      String xstr;
      if ( xf == null )
      {
        xstr = null;
      }
      else
      {
        xstr = xf.getName().replaceAll( regex, replace );
      }
      if ( f != null || sf != null || xf != null )
      {
        out.print( "Button." + Integer.toHexString( b.getKeyCode() ), fstr + '|' + sstr + '|' + xstr );
      }

    }
    try
    {
      out.flush();
    }
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }

  private String baseline = "";

  public void setBaseline()
  {
    StringWriter sw = new StringWriter();
    store( new PropertyWriter( new PrintWriter( sw ) ) );
    baseline = sw.toString();
  }

  /**
   * Checks for changed.
   * 
   * @param baseFile
   *          the base file
   * @return true, if successful
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public boolean hasChanged() throws IOException
  {
    StringWriter sw = new StringWriter();
    store( new PropertyWriter( new PrintWriter( sw ) ) );

    String latest = sw.toString();

    BufferedReader baseReader = new BufferedReader( new StringReader( baseline ) );
    BufferedReader tempReader = new BufferedReader( new StringReader( latest ) );
    String baseLine = null;
    String tempLine = null;
    do
    {
      baseLine = baseReader.readLine();
      while ( baseLine != null && baseLine.startsWith( "#" ) )
      {
        baseLine = baseReader.readLine();
      }

      tempLine = tempReader.readLine();
      while ( tempLine != null && tempLine.startsWith( "#" ) )
      {
        tempLine = tempReader.readLine();
      }
      System.err.println( "baseLine=" + baseLine );
      System.err.println( "tempLine=" + tempLine );
    }
    while ( baseLine != null && tempLine != null && baseLine.equals( tempLine ) );
    baseReader.close();
    tempReader.close();
    // tempFile.delete();

    if ( baseLine == tempLine )
    {
      return false;
    }

    return true;
  }

  /**
   * Load.
   * 
   * @param file
   *          the file
   * @throws Exception
   *           the exception
   */
  public void load( File file ) throws Exception
  {
    load( file, true );
  }

  /**
   * Load.
   * 
   * @param file
   *          the file
   * @param loadButtons
   *          the load buttons
   * @throws Exception
   *           the exception
   */
  public void load( File file, boolean loadButtons ) throws Exception
  {
    BufferedReader reader = new BufferedReader( new FileReader( file ) );
    load( reader, loadButtons );
    if ( file.getName().toLowerCase().endsWith( ".rmdu" ) )
    {
      this.file = file;
    }
  }

  /**
   * Load.
   * 
   * @param reader
   *          the reader
   * @throws Exception
   *           the exception
   */
  public void load( BufferedReader reader ) throws Exception
  {
    load( reader, true );
  }

  /**
   * Load.
   * 
   * @param reader
   *          the reader
   * @param loadButtons
   *          the load buttons
   * @throws Exception
   *           the exception
   */
  public void load( BufferedReader reader, boolean loadButtons ) throws Exception
  {
    reader.mark( 160 );
    String line = reader.readLine();
    reader.reset();
    if ( line.startsWith( "Name:" ) )
    {
      reset();
      importUpgrade( reader, loadButtons );
    }
    else
    {
      Properties props = new Properties();
      Property property = new Property();
      PropertyReader pr = new PropertyReader( reader );
      while ( ( property = pr.nextProperty() ) != null )
      {
        props.put( property.name, property.value );
      }
      reader.close();

      load( props, loadButtons );
    }
    setBaseline();
  }

  /**
   * Load.
   * 
   * @param props
   *          the props
   */
  public void load( Properties props, boolean loadButtons )
  {
    load( props, true, null, null );
  }

  /**
   * Load.
   * 
   * @param props
   *          the props
   * @param loadButtons
   *          the load buttons
   */
  public void load( Properties props, boolean loadButtons, Remote theRemote, LinkedHashMap< GeneralFunction, Integer > iconrefMap )
  {
    reset();
    String str = props.getProperty( "Description" );
    if ( str != null )
    {
      description = str;
    }
    str = props.getProperty( "Remote.name" );
    if ( str == null )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
          "The upgrade you are trying to import is not valid!  It does not contain a value for Remote.name",
          "Import Failure", JOptionPane.ERROR_MESSAGE );
      reset();
      return;
    }
    if ( theRemote != null )
    {
      remote = theRemote;
    }
    else
    {
      Remote.prelimLoad = true;
      theRemote = RemoteManager.getRemoteManager().findRemoteByName( str );
      Remote.prelimLoad = false;
      if ( theRemote == null )
      {
        reset();
        return;
      }
      remote = theRemote;
    }
    remote.load();
    if ( remote.usesEZRC() && macroMap == null )
    {
      macroMap = new LinkedHashMap< Integer, Macro >();
      learnedMap = new LinkedHashMap< Integer, LearnedSignal >();
      functionMap = new LinkedHashMap< Integer, Function >();
      selectorMap = new LinkedHashMap< Integer, GeneralFunction >();
    }
    // SegmentFlags is omitted if it is 0 (which it is for JP1.3 and earlier as it is not used by them )
    str = props.getProperty( "SegmentFlags" );
    setSegmentFlags( str == null ? 0 : Integer.parseInt( str ) );
    str = props.getProperty( "DeviceIndex" );
    if ( str != null )
    {}
    setDeviceTypeAliasName( props.getProperty( "DeviceType" ) );
    setupCode = Integer.parseInt( props.getProperty( "SetupCode" ) );

    // Restriction of an upgrade to a particular device button is a property
    // of a configuration, not of the upgrade itself.
    if ( remoteConfig != null )
    {
      if ( remote.hasDeviceDependentUpgrades() > 0 )
      {
        str = props.getProperty( "ButtonIndependent" );
        buttonIndependent = str != null ? Boolean.parseBoolean( str ) : true;
      }
      else
      {
        buttonIndependent = true;
      }
      buttonRestriction = DeviceButton.noButton;

      str = props.getProperty( "ButtonIndex" );
      if ( str != null )
      {
        try
        {
          int index = Integer.parseInt( str );
          buttonRestriction = remote.getDeviceButton( index );
          buttonRestriction.setUpgrade( this );
        }
        catch ( NumberFormatException nfe )
        {
          nfe.printStackTrace( System.err );
        }
      }
    }
    else
    {
      buttonIndependent = true;
      buttonRestriction = DeviceButton.noButton;
    }

    if ( props.getProperty( "Protocol" ) != null || !remote.usesEZRC() )
    {
      Hex pid = new Hex( props.getProperty( "Protocol", "0200" ) );
      String name = props.getProperty( "Protocol.name", "" );
      String variantName = props.getProperty( "Protocol.variantName", "" );

      ProtocolManager pm = ProtocolManager.getProtocolManager();
      if ( name.startsWith( "Manual Settings" ) || name.equals( "Manual" )
          || name.equalsIgnoreCase( "PID " + pid.toString() ) )
      {
        ManualProtocol mp = new ManualProtocol( pid, props );
        mp.setName( name );
        protocol = testManualProtocol( mp );
        if ( protocol == mp )
        {
          pm.add( protocol );
        }
        str = props.getProperty( "ProtocolParms" );
        System.err.println( "ProtocolParms='" + str + "'" );
        if ( str != null && str.length() != 0 )
        {
          protocol.setDeviceParms( stringToValueArray( str ) );
          parmValues = protocol.getDeviceParmValues();
        }
      }
      else
      {
        // Need to consider all protocol attributes, to handle things like "Acer Keyboard (01 11)" and
        // "TiVo (01 11)"
        protocol = pm.findNearestProtocol( remote, name, pid, variantName );
        if ( protocol == null )
        {
          if ( remote.supportsVariant( pid, variantName ) )
          {
            // Protocol missing from protocols.ini so add to protocol manager
            int cmdLength = 0;
            int fixedDataLength = 0;
            String temp = props.getProperty( "Function.0.hex" );
            if ( temp != null )
            {
              cmdLength = ( new Hex( temp ) ).length();
            }
            temp = props.getProperty( "ProtocolParms" );
            if ( temp != null )
            {
              fixedDataLength = stringToValueArray( temp ).length;
            }
            if ( cmdLength > 0 )
            {
              protocol = pm.createMissingProtocol( pid, variantName, fixedDataLength, cmdLength );
            }
          }
        }
        if ( protocol == null )
        { 
          JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "No protocol found with name=\"" + name + "\", ID="
              + pid.toString() + ", and variantName=\"" + variantName + "\"", "File Load Error",
              JOptionPane.ERROR_MESSAGE );
          reset();
          return;
        }

        str = props.getProperty( "ProtocolParms" );
        System.err.println( "ProtocolParms='" + str + "'" );
        if ( str != null && str.length() != 0 )
        {
          protocol.setDeviceParms( stringToValueArray( str ) );
          parmValues = protocol.getDeviceParmValues();
        }

        if ( protocolInUse() == null )
        {
          Hex altPID = new Hex( props.getProperty( "Protocol.altPID", "" ) );
          protocol.setAltPID( remote, altPID );
          protocol.setProperties( props, remote );
        }
        else
        {
          Protocol p = testStandardProtocol( protocol, props );
          if ( p != null )
          {
            protocol = p;
          }
          else
          {
            return;
          }
        }
      }

      if ( protocolInUse() == null && !protocol.getID( remote, false ).equals( pid ) )
      {
        protocol.setAltPID( remote, pid );
      }
      if ( remote.getSegmentTypes() != null )
      {
        sizeCmdBytes = protocol.getDefaultCmd().length();
        sizeDevBytes = protocol.getFixedDataLength();
      }
    }

    notes = props.getProperty( "Notes" );

    System.err.println( "Loading functions for device upgrade "
        + devTypeAliasName + "/" + ( new SetupCode( setupCode ).toString() ) );
    functions.clear();
    swapList = null;
    LinkedHashMap< Integer, Integer > macroXRef = new LinkedHashMap< Integer, Integer >();
    int i = 0;
    while ( true )
    {
      Function f = new Function();
      if ( remote.isSSD() )
      {
        f.icon = new RMIcon( 9 );
      }
      Integer macroref = f.load( props, "Function." + i );
      if ( macroref != null )
      {
        macroXRef.put( i, macroref );
      }
      if ( remote.usesEZRC() && f.getGid() == null && f.getData() != null )
      {
        f.setGid( Function.defaultGID );
      }
      String temp = null;
      if ( iconrefMap != null && ( temp = props.getProperty( "Function." + i + ".iconref" ) ) != null )
      {
        iconrefMap.put( f, Integer.parseInt( temp ) );
      }
      if ( f.isEmpty() )
      {
        break;
      }
      if ( getFunction( f.getName(), functions ) != null )
      {
        System.err.println( "Warning:  multiple functions with name " + f.getName() );
      }
      f.setRmirIndex( i );
      f.setUpgrade( this );
      functions.add( f );
      i++ ;
    }

    extFunctions.clear();
    i = 0;
    while ( true )
    {
      ExternalFunction f = new ExternalFunction();
      f.load( props, "ExtFunction." + i, remote );
      if ( f.isEmpty() )
      {
        break;
      }
      extFunctions.add( f );
      i++ ;
    }

    if ( loadButtons )
    {
      List< Button>  buttons = new ArrayList< Button >( Arrays.asList( remote.getUpgradeButtons() ) );
      List< Button> sysBtns = null;
      if ( remote.usesEZRC() )
      {
        sysBtns = remote.getButtonGroups().get( "System" );
        if ( sysBtns != null )
        {
          buttons.addAll( sysBtns );
        }
      }
      
      String regex = "\\\\u007c";
      String replace = "|";
      for ( i = 0; i < buttons.size(); i++ )
      {
        Button b = buttons.get( i );
        str = props.getProperty( "Button." + Integer.toHexString( b.getKeyCode() ) );
        if ( str == null )
        {
          continue;
        }
        StringTokenizer st = new StringTokenizer( str, "|" );
        str = st.nextToken();
        Function func = null;
        if ( !str.equals( "null" ) )
        {
          if ( remote.usesEZRC() && str.startsWith( "Function." ) )
          {
            int rmirIndex = Integer.parseInt( str.substring( 9 ) );
            func = getFunctionByRmirIndex( rmirIndex );
            Integer macroref = macroXRef.get( rmirIndex );
            if ( macroref != null )
            {
              for ( Macro macro : remoteConfig.getMacros() )
              {
                if ( macro.getSerial() == macroref )
                {
                  if ( macro.getUserItems() == null )
                  {
                    macro.setUserItems( new ArrayList< Integer >() );
                  }
                  int dbi = buttonRestriction.getButtonIndex();
                  int keyCode = b.getKeyCode();
                  macro.getUserItems().add( ( dbi << 16  ) | keyCode );
                  break;
                }
              }
            }
          }
          else
          {
            func = getFunction( str.replaceAll( regex, replace ) );
          }
          assignments.assign( b, func, Button.NORMAL_STATE );
          if ( sysBtns != null && sysBtns.contains( b ) )
          {
            func.removeReference( buttonRestriction, b );
          }
        }
        str = st.nextToken();
        if ( !str.equals( "null" ) && remote.getShiftEnabled() )
        {
          func = getFunction( str.replaceAll( regex, replace ) );
          assignments.assign( b, func, Button.SHIFTED_STATE );
        }
        if ( st.hasMoreTokens() )
        {
          str = st.nextToken();
          if ( !str.equals( "null" ) && remote.getXShiftEnabled() )
          {
            func = getFunction( str.replaceAll( regex, replace ) );
            assignments.assign( b, func, Button.XSHIFTED_STATE );
          }
        }
      }
    }
    setBaseline();
  }

  /**
   * Import upgrade.
   * 
   * @param in
   *          the in
   * @throws Exception
   *           the exception
   */
  public void importUpgrade( BufferedReader in ) throws Exception
  {
    importUpgrade( in, true );
  }
  
  private boolean useEquivalent( Protocol pNew, Protocol pOld )
  {
    String title = "Protocol Identification";
    String message = "The manual protocol being imported, with name \"" + pNew.getName() + "\" and PID = " + pNew.getID()
    + "\nhas same binary code and translators as the existing protocol with name " + pOld.getName() + "\nand PID = " + pOld.getID()
    + ".\n\nDo you want to use the existing protocol?  Unless there are special reasons not to do so,"
    + "\nyou should answer Yes.";
    return JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE )
        == JOptionPane.YES_OPTION;
  }
  
  private Protocol testStandardProtocol( Protocol protocol, Properties props )
  {
    Processor processor = remote.getProcessor();
    String str = ( props == null ) ? null : props.getProperty( "CustomCode." + processor.getEquivalentName() );
    Hex newCustomCode = ( str == null ) ? null : new Hex( str );
    Hex oldCustomCode = protocol.getCustomCode( processor );
    String title = "Custom Code";
    if ( newCustomCode == null && oldCustomCode != null )
    {
      String message = "The protocol of this upgrade is already in use with custom code\n"
                     + "by an existing upgrade, but this upgrade uses the standard version.\n\n"
                     + "To load this upgrade you must first convert the protocol of that\n"
                     + "upgrade to a Manual Protocol and then change its PID.  If you open\n"
                     + "that upgrade you will find a button that performs this conversion.";
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE );
      reset();
      return null;
    }
    else if ( newCustomCode != null && !newCustomCode.equals( oldCustomCode ) )
    {
      String message = "The protocol of this upgrade has custom code and is already in use\n"
                     + "by an existing upgrade with either no or different custom code.\n\n"
                     + "If you load this upgrade, the protocol will be loaded as an\n"
                     + "equivalent Manual Protocol.  Do you want to proceed?";
      if ( JOptionPane.showConfirmDialog( RemoteMaster.getFrame(), message, title, 
          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION )
      {
        originalProtocol = Protocol.blank;  // Actual original protocol is never imported, so this placeholder is used instead
        convertedProtocol = protocol.convertToManual( remote, parmValues, newCustomCode );
        protocol = convertedProtocol;
        ProtocolManager.getProtocolManager().add( protocol );
        parmValues = protocol.getDeviceParmValues();
      }
      else
      {
        reset();
        return null;
      }
    }
    return protocol;
  }
  
  private Protocol testManualProtocol( ManualProtocol mp )
  {
    ProtocolManager pm = ProtocolManager.getProtocolManager();
    Protocol equiv = null;
    boolean nameInUse = false;
    Hex pid = mp.getID();
    String name = mp.getName();
    for ( Protocol p : pm.getProtocolsForRemote( remote ) )
    {
      if ( !( p instanceof ManualProtocol ) )
      {
        continue;
      }
      if ( ( ( ManualProtocol )p ).equivalentForRemoteTo( mp, remote ) )
      {
        if ( p.getName().equals( name ) && p.getID().equals( pid ) )
        {
          return p;
        }
        else if ( equiv == null )
        {
          equiv = p;
        }
      }
      if ( p.getName().equals( name ) )
      {
        nameInUse = true;
      }
    }

    if ( equiv != null && useEquivalent( mp, equiv ) )
    {
      return equiv;
    }

    if ( nameInUse )
    {
      String newName = ManualProtocol.getDefaultName( pid );
      String title = "Protocol Identification";
      String message = "There is already a manual protocol with name \"" + name + "\" but with"
      + "\ndifferent binary code or translators, so the protocol being imported\nis being renamed as "
      + "\"" + newName + "\"";
      JOptionPane.showMessageDialog( null, message, title, JOptionPane.INFORMATION_MESSAGE );
      mp.setName( newName );
    }

    return mp;
  }
  
  
  /**
   * Parses the int.
   * 
   * @param str
   *          the str
   * @return the int
   */
  private static int parseInt( String str )
  {
    int base = 10;
    if ( str.charAt( 0 ) == '$' )
    {
      base = 16;
      str = str.substring( 1 );
    }
    else if ( str.charAt( str.length() - 1 ) == 'h' )
    {
      base = 16;
      str = str.substring( 0, str.length() - 1 );
    }
    return Integer.parseInt( str, base );
  }

  /**
   * Clean name.
   * 
   * @param name
   *          the name
   * @return the string
   */
  private String cleanName( String name )
  {
    if ( name != null && name.length() == 5 && name.toLowerCase().startsWith( "num " )
        && Character.isDigit( name.charAt( 4 ) ) )
    {
      return name.substring( 4 );
    }
    return name;
  }

  /**
   * Checks if is external function name.
   * 
   * @param name
   *          the name
   * @return true, if is external function name
   */
  private boolean isExternalFunctionName( String name )
  {
    if ( name == null )
    {
      return false;
    }
    char firstChar = name.charAt( 0 );
    int slash = name.indexOf( '/' );
    int space = name.indexOf( ' ' );
    if ( space == -1 )
    {
      space = name.length();
    }
    if ( firstChar == '=' && slash > 1 && space > slash )
    {
      @SuppressWarnings( "unused" )
      String devName = name.substring( 1, slash );
      String setupString = name.substring( slash + 1, space );
      if ( setupString.length() == 4 )
      {
        try
        {
          @SuppressWarnings( "unused" )
          int setupCode = Integer.parseInt( setupString );
          return true;
        }
        catch ( NumberFormatException nfe )
        {}
      }
    }
    return false;
  }

  /**
   * Import upgrade.
   * 
   * @param in
   *          the in
   * @param loadButtons
   *          the load buttons
   * @throws Exception
   *           the exception
   */
  public void importUpgrade( BufferedReader in, boolean loadButtons ) throws Exception
  {
    String line = in.readLine(); // line 1 "Name:"
    String token = line.substring( 0, 5 );
    if ( !token.equals( "Name:" ) )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "The upgrade you are trying to import is not valid!",
          "Import Failure", JOptionPane.ERROR_MESSAGE );
      return;
    }
    String delim = line.substring( 5, 6 );
    List< String > fields = LineTokenizer.tokenize( line, delim );
    description = fields.get( 1 );
    String kmVersion = fields.get( 5 );
    System.err.println( "KM version of imported file is '" + kmVersion + '\'' );

    String protocolLine = in.readLine(); // line 2 "Devices:"
    String manualLine = in.readLine(); // line 3 "Manual:"

    line = in.readLine(); // line 4 "Setup:"
    List< String > setupFields = LineTokenizer.tokenize( line, delim );
    token = setupFields.get( 1 );
    setupCode = Integer.parseInt( token );
    token = setupFields.get( 2 );
    String str = token.substring( 5 );

    Remote.prelimLoad = true;
    remote = RemoteManager.getRemoteManager().findRemoteByName( str );
    Remote.prelimLoad = false;
    if ( remote == null )
    {
      reset();
      return;
    }
    Hex pid = null;
    while ( true )
    {
      line = in.readLine();
      if ( line != null && line.length() > 0 && line.charAt( 0 ) == '\"' )
      {
        line = line.substring( 1 );
      }
      int equals = line.indexOf( '=' );
      if ( equals != -1 && line.substring( 0, equals ).toLowerCase().startsWith( "upgrade code" ) )
      {
        short[] id = new short[ 2 ];
        if ( !remote.usesTwoBytePID() )
        {
          short temp = Short.parseShort( line.substring( equals + 2, equals + 4 ), 16 );
          if ( ( temp & 8 ) != 0 )
          {
            id[ 0 ] = 1;
          }

          line = in.readLine();
          temp = Short.parseShort( line.substring( 0, 2 ), 16 );
          id[ 1 ] = temp;
          pid = new Hex( id );
        }
        else
        {
          line = in.readLine();
          pid = new Hex( line.substring( 0, 5 ) );
        }
        break;
      }
    }

    remote.load();
    token = setupFields.get( 3 );
    str = token.substring( 5 );

    if ( remote.getDeviceTypeByAliasName( str ) == null )
    {
      String rc = null;
      String msg = "Remote \"" + remote.getName() + "\" does not support the device type " + str
          + ".  Please select one of the supported device types below to use instead.\n";
      while ( rc == null )
      {
        rc = ( String )JOptionPane.showInputDialog( RemoteMaster.getFrame(), msg, "Unsupported Device Type",
            JOptionPane.ERROR_MESSAGE, null, remote.getDeviceTypeAliasNames(), null );
      }
      str = rc;
    }
    setDeviceTypeAliasName( str );

    String buttonStyle = setupFields.get( 4 );

    List< String > deviceFields = LineTokenizer.tokenize( protocolLine, delim );
    String protocolName = deviceFields.get( 1 ); // protocol name

    ProtocolManager protocolManager = ProtocolManager.getProtocolManager();
    if ( protocolName.equals( "Manual Settings" ) )
    {
      System.err.println( "protocolName=" + protocolName );
      System.err.println( "manualLine=" + manualLine );
      List< String > manualFields = LineTokenizer.tokenize( manualLine, delim );
      String pidStr = manualFields.get( 1 );
      System.err.println( "pid=" + pidStr );
      if ( pidStr != null )
      {
        int space = pidStr.indexOf( ' ' );
        if ( space != -1 )
        {
          pid = new Hex( pidStr );
        }
        else
        {
          short pidInt = Short.parseShort( pidStr, 16 );
          short[] data = new short[ 2 ];
          data[ 0 ] = ( short )( ( pidInt & 0xFF00 ) >> 8 );
          data[ 1 ] = ( short )( pidInt & 0xFF );
          pid = new Hex( data );
        }
      }
      int byte2 = Integer.parseInt( manualFields.get( 2 ).substring( 0, 1 ) );
      System.err.println( "byte2=" + byte2 );
      String signalStyle = manualFields.get( 3 );
      System.err.println( "SignalStyle=" + signalStyle );
      String bitsStr = manualFields.get( 4 );
      int devBits = 8;
      int cmdBits = 8;
      try
      {
        if ( bitsStr != null )
        {
          devBits = Integer.parseInt( bitsStr.substring( 0, 1 ), 16 );
          cmdBits = Integer.parseInt( bitsStr.substring( 1 ), 16 );
        }
      }
      catch ( NumberFormatException nfe )
      {}
      System.err.println( "devBits=" + devBits + " and cmdBits=" + cmdBits );
      if ( devBits == 0 )
      {
        devBits = 8;
      }
      if ( cmdBits == 0 )
      {
        cmdBits = 8;
      }

      java.util.List< Value > values = new ArrayList< Value >();

      str = deviceFields.get( 2 ); // Device 1
      if ( str != null )
      {
        values.add( new Value( parseInt( str ) ) );
      }

      str = deviceFields.get( 3 ); // Device 2
      if ( str != null )
      {
        values.add( new Value( parseInt( str ) ) );
      }

      str = deviceFields.get( 4 ); // Device 3
      if ( str != null )
      {
        values.add( new Value( parseInt( str ) ) );
      }

      str = deviceFields.get( 5 ); // Raw Fixed Data
      if ( str == null )
      {
        str = "";
      }
      short[] rawHex = Hex.parseHex( str );

//      // Use the default name for the protocol, which includes the PID, to prevent distinct
//      // manual protocols all being called simply "Manual Protocol" and so getting mis-identified
//      // with one another.
//      protocolName = ManualProtocol.getDefaultName( pid );
      
      protocol = new ManualProtocol( protocolName, pid, byte2, signalStyle, devBits, values, rawHex, cmdBits );
      setParmValues( protocol.getDeviceParmValues() );
      protocolManager.add( protocol );
      java.util.List< Protocol > v = protocolManager.findByPID( pid );
      ListIterator< Protocol > li = v.listIterator();
      while ( li.hasNext() )
      {
        Protocol p = li.next();
        if ( p.getFixedDataLength() != rawHex.length )
        {
          li.remove();
          continue;
        }
      }
      if ( v.size() != 0 )
      {
        Protocol p = v.get( 0 );
        Hex code = p.getCode( remote );
        if ( code != null )
        {
          ( ( ManualProtocol )protocol ).setCode( code, remote.getProcessor() );
        }
      }
    }
    else
    {
      // protocol = protocolManager.findProtocolForRemote( remote, protocolName );
      Protocol p = protocolManager.findNearestProtocol( remote, protocolName, pid, null );

      if ( p == null )
      {
        p = protocolManager.findProtocolByOldName( remote, protocolName, pid );

        if ( p == null )
        {
          JOptionPane.showMessageDialog( RemoteMaster.getFrame(), "No protocol found with name=\"" + protocolName
              + "\" for remote \"" + remote.getName() + "\".", "Import Failure", JOptionPane.ERROR_MESSAGE );
          reset();
          return;
        }
      }
      else
      {
        p = testStandardProtocol( p, null );
        if ( p == null )
        {
          reset();
          return;
        } 
      }
      protocol = p;

      Value[] importParms = new Value[ 8 ];
      for ( int i = 0; i < importParms.length && i + 2 < deviceFields.size(); i++ )
      {
        token = deviceFields.get( 2 + i );
        Object val = null;
        if ( token == null )
        {
          val = null;
        }
        else
        {
          if ( token.equals( "true" ) )
          {
            val = new Integer( 1 );
          }
          else if ( token.equals( "false" ) )
          {
            val = new Integer( 0 );
          }
          else
          {
            val = token;
            // val = new Integer( token );
          }
        }
        importParms[ i ] = new Value( val );
      }
      protocol.importDeviceParms( importParms );
      parmValues = protocol.getDeviceParmValues();     
      
      if ( !protocol.getID( remote, false ).equals( pid ) )
      {
        protocol.setAltPID( remote, pid );
      }
    }

    // compute cmdIndex
    boolean useOBC = false; // assume OBC???
    boolean useEFC = false;
    if ( buttonStyle.equals( "OBC" ) )
    {
      useOBC = true;
    }
    else if ( buttonStyle.equals( "EFC" ) )
    {
      useEFC = true;
    }

    int obcIndex = 0;
    CmdParameter[] cmdParms = protocol.getCommandParameters();
    for ( int i = 0; i < cmdParms.length; i++ )
    {
      if ( cmdParms[ i ].getName().equals( "OBC" ) )
      {
        obcIndex = i;
        break;
      }
    }

    String match1 = "fByte2" + delim + "bButtons" + delim + "bFunctions" + delim + "fNotes" + delim + "Device Combiner";
    String match2 = "byte2" + delim + "Buttons" + delim + "Functions" + delim;

    while ( true )
    {
      line = in.readLine();
      if ( line == null || line.indexOf( match1 ) != -1 || line.indexOf( match2 ) != -1 )
      {
        break;
      }
    }

    fields = LineTokenizer.tokenize( line, delim );

    int buttonCodeIndex = fields.indexOf( "bBtnCd" );

    functions.clear();

    DeviceCombiner combiner = null;
    if ( protocol.getClass() == DeviceCombiner.class )
    {
      combiner = ( DeviceCombiner )protocol;
    }

    // save the function definition/assignment lines for later parsing
    String[] lines = new String[ 128 ];
    for ( int i = 0; i < 128; ++i )
    {
      lines[ i ] = in.readLine();
    }

    // read in the notes, which may have the protocol code
    while ( ( line = in.readLine() ) != null )
    {
      fields = LineTokenizer.tokenize( line, delim );
      token = fields.get( 0 );
      if ( token != null )
      {
        if ( token.equals( "Line Notes:" ) || token.equals( "Notes:" ) )
        {
          StringBuilder buff = new StringBuilder();
          boolean first = true;
          String tempDelim = null;
          while ( ( line = in.readLine() ) != null )
          {
            if ( line.length() > 0 && line.charAt( 0 ) == '"' )
            {
              tempDelim = "\"";
            }
            else
            {
              tempDelim = delim;
            }
            StringTokenizer st = new StringTokenizer( line, tempDelim );
            if ( st.hasMoreTokens() )
            {
              token = st.nextToken();
              if ( token.startsWith( "EOF Marker" ) )
              {
                break;
              }
              if ( first )
              {
                first = false;
              }
              else
              {
                buff.append( "\n" );
              }
              buff.append( token.trim() );
            }
            else
            {
              buff.append( "\n" );
            }
          }
          notes = buff.toString().trim();
          if ( protocol.getClass() == ManualProtocol.class )
          {
            protocol.importUpgradeCode( notes );
          }
        }
      }
    }

    // Parse the function definitions
    ArrayList< ArrayList< String >> unassigned = new ArrayList< ArrayList< String >>();
    for ( int i = 0; i < 128; i++ )
    {
      line = lines[ i ];
      fields = LineTokenizer.tokenize( line, delim );
      String funcName = cleanName( fields.get( 0 ) );
      String code = fields.get( 1 );
      String byte2 = fields.get( 2 );
      @SuppressWarnings( "unused" )
      String buttonName = fields.get( 3 );
      @SuppressWarnings( "unused" )
      String assignedName = fields.get( 4 );
      String notes = fields.size() > 5 ? fields.get( 5 ) : null;
      String pidStr = fields.size() > 6 ? fields.get( 6 ) : null;
      String fixedDataStr = fields.size() > 7 ? fields.get( 7 ) : null;

      Function f = null;
      if ( code != null || byte2 != null || notes != null )
      {
        System.err.println( "Creating a new function, because:" );
        System.err.println( "code=" + code );
        System.err.println( "byte2=" + byte2 );
        System.err.println( "notes=" + notes );

        boolean isExternal = isExternalFunctionName( funcName );
        if ( isExternal )
        {
          f = new ExternalFunction();
          extFunctions.add( ( ExternalFunction )f );
        }
        else
        {
          f = new Function();
          functions.add( f );
        }
        System.err.println( "Creating function w/ name " + funcName );
        f.setName( funcName );
        f.setUpgrade( this );

        if ( notes != null )
        {
          f.setNotes( notes );
        }

        Hex hex = null;
        if ( f.isExternal() )
        {
          ExternalFunction ef = ( ExternalFunction )f;
          String name = ef.getName();
          int slash = name.indexOf( '/' );
          String devName = name.substring( 1, slash );
          String match = null;
          String[] names = remote.getDeviceTypeAliasNames();
          for ( int j = 0; j < names.length && match == null; j++ )
          {
            if ( devName.equalsIgnoreCase( names[ j ] ) )
            {
              match = names[ j ];
              break;
            }
          }
          if ( match == null )
          {
            String msg = "The Keymap Master device upgrade you are importing includes an\nexternal function that uses the unknown device type "
                + devName + ".\n\nPlease select one of the supported device types below to use instead.";
            while ( match == null )
            {
              match = ( String )JOptionPane.showInputDialog( RemoteMaster.getFrame(), msg, "Unsupported Device Type",
                  JOptionPane.ERROR_MESSAGE, null, names, null );
            }
          }
          ef.setDeviceTypeAliasName( match );
          int space = name.indexOf( ' ', slash + 1 );
          String codeString = null;
          if ( space == -1 )
          {
            codeString = name.substring( slash + 1 );
          }
          else
          {
            codeString = name.substring( slash + 1, space );
          }
          ef.setSetupCode( Integer.parseInt( codeString ) );
          if ( code.indexOf( 'h' ) != -1 || code.indexOf( '$' ) != -1 || code.indexOf( ' ' ) != -1 )
          {
            hex = new Hex( code );
            ef.setType( ExternalFunction.HexType );
          }
          else
          {
            hex = new Hex( 1 );
            EFC.toHex( Short.parseShort( code ), hex, 0 );
            ef.setType( ExternalFunction.EFCType );
          }
        }
        else if ( code != null ) // not external and there is a command code
        {
          if ( code.indexOf( 'h' ) != -1 || code.indexOf( '$' ) != -1 || code.indexOf( ' ' ) != -1 )
          {
            hex = new Hex( code );
          }
          else
          {
            hex = protocol.getDefaultCmd();
            protocol.importCommand( hex, code, useOBC, obcIndex, useEFC );
          }

          if ( byte2 != null )
          {
            protocol.importCommandParms( hex, byte2 );
          }
        }
        f.setHex( hex );
      }

      if ( combiner != null && pidStr != null && !pidStr.equals( "Protocol ID" ) )
      {
        Hex fixedData = new Hex();
        if ( fixedDataStr != null )
        {
          fixedData = new Hex( fixedDataStr );
        }

        Hex newPid = new Hex( pidStr );
        Protocol p = protocolManager.findProtocolForRemote( remote, newPid, fixedData );
        if ( p != null )
        {
          CombinerDevice dev = new CombinerDevice( p, fixedData );
          combiner.add( dev );
        }
        else
        {
          ManualProtocol mp = new ManualProtocol( newPid, new Properties() );
          mp.setRawHex( fixedData );
          combiner.add( new CombinerDevice( mp, null, null ) );
        }
      }
    }

    // Parse the button assignments
    for ( int i = 0; i < 128; i++ )
    {
      line = lines[ i ];
      fields = LineTokenizer.tokenize( line, delim );
      @SuppressWarnings( "unused" )
      String funcName = fields.get( 0 ); // the function being defined, if any (field 1)
      @SuppressWarnings( "unused" )
      String code = fields.get( 1 ); // the EFC or OBC, if any (field 2 )
      @SuppressWarnings( "unused" )
      String byte2 = fields.get( 2 ); // byte2, if any (field 3)
      String actualName = cleanName( fields.get( 3 ) ); // get assigned button name (field 4)
      System.err.println( "actualName='" + actualName + "'" );
      if ( actualName == null )
      {
        continue;
      }

      String assignedName = fields.size() > 4 ? fields.get( 4 ) : null; // get assigned functionName
      @SuppressWarnings( "unused" )
      String notes = fields.size() > 5 ? fields.get( 5 ) : null; // get function notes

      String shiftAssignedName = fields.size() > 12 ? fields.get( 12 ) : null;

      String buttonCode = null;
      if ( buttonCodeIndex != -1 )
      {
        buttonCode = fields.get( buttonCodeIndex );
        if ( buttonCode.length() < 2 )
        {
          buttonCode = null;
        }
      }

      String buttonName = null;
      if ( actualName != null )
      {
        if ( i < genericButtonNames.length )
        {
          buttonName = genericButtonNames[ i ];
        }
        else
        {
          System.err.println( "No generic name available!" );
          Button b = remote.getButton( actualName );
          if ( b == null )
          {
            b = remote.getButton( actualName.replace( ' ', '_' ) );
          }
          if ( b != null )
          {
            buttonName = b.getStandardName();
          }
        }
      }

      Button b = null;
      if ( buttonCode != null )
      {
        int keyCode = Integer.parseInt( buttonCode, 16 );
        b = remote.getButton( keyCode );
        if ( b != null )
        {
          System.err.println( "Found button " + b + " for keyCode " + keyCode );
          buttonName = b.getStandardName();
        }
      }
      if ( b == null && buttonName != null )
      {
        System.err.println( "Searching for button w/ name " + buttonName );
        b = remote.findByStandardName( new Button( buttonName, null, ( byte )0, remote ) );
        if ( b == null )
        {
          b = remote.getButton( buttonName );
        }
        System.err.println( "Found button " + b );
      }
      if ( b == null )
      {
        System.err.println( "No buttonName for actualName=" + actualName + " and i=" + i );
      }

      if ( buttonName != null && assignedName != null && Character.isDigit( assignedName.charAt( 0 ) )
          && Character.isDigit( assignedName.charAt( 1 ) ) && assignedName.charAt( 2 ) == ' '
          && assignedName.charAt( 3 ) == '-' && assignedName.charAt( 4 ) == ' ' )
      {
        String name = cleanName( assignedName.substring( 5 ) );
        if ( name.length() == 5 && name.startsWith( "num " ) && Character.isDigit( name.charAt( 4 ) ) )
        {
          name = name.substring( 4 );
        }

        Function func = null;
        if ( isExternalFunctionName( name ) )
        {
          func = getFunction( name, extFunctions );
        }
        else
        {
          func = getFunction( name, functions );
        }

        if ( func == null )
        {
          System.err.println( "Could not find function " + name );
          continue;
        }
        else
        {
          System.err.println( "Found function " + name );
        }

        if ( b == null )
        {
          ArrayList< String > temp = new ArrayList< String >( 2 );
          temp.add( name );
          temp.add( actualName );
          unassigned.add( temp );
          System.err.println( "Couldn't find button " + buttonName + " to assign function " + name );
        }
        else if ( loadButtons && func.getHex() != null )
        {
          System.err.println( "Setting function " + name + " on button " + b );
          assignments.assign( b, func, Button.NORMAL_STATE );
        }
      }

      if ( shiftAssignedName != null )
      {
        System.err.println( "shiftAssignedName=" + shiftAssignedName );
      }
      if ( shiftAssignedName != null && !shiftAssignedName.equals( "" ) )
      {
        String name = cleanName( shiftAssignedName.substring( 5 ) );
        Function func = null;
        if ( isExternalFunctionName( name ) )
        {
          func = getFunction( name, extFunctions );
        }
        else
        {
          func = getFunction( name, functions );
        }
        if ( b == null )
        {
          ArrayList< String > temp = new ArrayList< String >( 2 );
          temp.add( name );
          temp.add( "shift-" + buttonName );
          unassigned.add( temp );
        }
        else if ( loadButtons && func != null && func.getHex() != null )
        {
          assignments.assign( b, func, Button.SHIFTED_STATE );
        }
      }
    }

    if ( !unassigned.isEmpty() )
    {
      String message = Integer.toString( unassigned.size() ) + " functions defined in the imported device upgrade "
          + "were assigned to buttons that could not be matched by name. "
          + "The functions and the corresponding button names are listed below."
          + "\n\nPlease post this information in the \"JP1 - Software\" section of the "
          + "JP1 Forums at www.hifi-remote.com"
          + "\n\nUse the Button or Layout panel to assign those functions properly.";

      JFrame frame = new JFrame( "Import Failure" );
      frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
      Container container = frame.getContentPane();

      JTextArea text = new JTextArea( message );
      text.setEditable( false );
      text.setLineWrap( true );
      text.setWrapStyleWord( true );
      text.setBackground( container.getBackground() );
      text.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
      container.add( text, BorderLayout.NORTH );
      java.util.List< String > titles = new ArrayList< String >();
      titles.add( "Function name" );
      titles.add( "Button name" );
      Object[][] unassignedArray = new Object[ unassigned.size() ][];
      int i = 0;
      for ( java.util.List< String > l : unassigned )
      {
        unassignedArray[ i++ ] = l.toArray();
      }
      JTableX table = new JTableX( unassignedArray, titles.toArray() );
      container.add( new JScrollPane( table ), BorderLayout.CENTER );
      /*
       * Dimension d = table.getPreferredScrollableViewportSize(); d.height = table.getPreferredSize().height;
       * table.setPreferredScrollableViewportSize( d );
       */

      frame.pack();
      frame.setLocationRelativeTo( RemoteMaster.getFrame() );
      frame.setVisible( true );
    }
    Button[] buttons = remote.getUpgradeButtons();
    System.err.println( "Removing assigned functions with no hex!" );
    for ( int i = 0; i < buttons.length; i++ )
    {
      Button b = buttons[ i ];
      for ( int state = Button.NORMAL_STATE; state <= Button.XSHIFTED_STATE; ++state )
      {
        Function f = assignments.getAssignment( b, state );
        if ( f != null && f.getHex() == null )
        {
          assignments.assign( b, null, state );
        }
      }
    }
    
    // Check now to see if we have created a duplicate protocol, and if so, delete it and use
    // existing one.  We cannot do this before creating the duplicate as it may actually have
    // different ImportCmdTranslators.  Safe now as these have been used for last time.
    if ( protocol instanceof ManualProtocol && protocol.hasCode( remote ) )
    {
      protocolManager.remove( protocol );
      ManualProtocol mp = ( ManualProtocol )protocol;
      protocol = testManualProtocol( mp );
      protocolManager.add( protocol );
    }

    System.err.println( "Done!" );
  }

  /**
   * Gets the parm values.
   * 
   * @return the parm values
   */
  public Value[] getParmValues()
  {
    return parmValues.clone();
  }

  /**
   * Sets the parm values.
   * 
   * @param parmValues
   *          the new parm values
   */
  public void setParmValues( Value[] parmValues )
  {
    this.parmValues = parmValues.clone();
  }

  /**
   * Gets the device type alias names.
   * 
   * @return the device type alias names
   */
  public static final String[] getDeviceTypeAliasNames()
  {
    return deviceTypeAliasNames;
  }

  /**
   * Auto assign functions.
   */
  public void autoAssignFunctions()
  {
    autoAssignFunctions( functions );
    autoAssignFunctions( extFunctions );
  }

  /**
   * Auto assign functions.
   * 
   * @param funcs
   *          the funcs
   */
  private void autoAssignFunctions( java.util.List< ? extends Function > funcs )
  {
    Button[] buttons = remote.getUpgradeButtons();
    for ( Function func : funcs )
    {
      if ( func.getHex() != null )
      {
        for ( int i = 0; i < buttons.length; i++ )
        {
          Button b = buttons[ i ];
          if ( assignments.getAssignment( b ) == null )
          {
            if ( b.getName().equalsIgnoreCase( func.getName() )
                || b.getStandardName().equalsIgnoreCase( func.getName() ) )
            {
              assignments.assign( b, func );
              break;
            }
          }
        }
      }
    }
  }

  /**
   * Check size.
   * 
   * @return true, if successful
   */
  public boolean checkSize()
  {
    Integer protocolLimit = remote.getMaxProtocolLength();
    Integer upgradeLimit = remote.getMaxUpgradeLength();
    Integer combinedLimit = remote.getMaxCombinedUpgradeLength();

    if ( protocolLimit == null && upgradeLimit == null && combinedLimit == null )
    {
      return true;
    }

    int protocolLength = 0;
    Hex protocolCode = getCode();
    if ( protocolCode != null )
    {
      protocolLength = protocolCode.length();
    }

    if ( protocolLimit != null && protocolLength > protocolLimit.intValue() )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
          "The protocol upgrade exceeds the maximum allowed by the remote.", "Protocol Upgrade Limit Exceeded",
          JOptionPane.ERROR_MESSAGE );
      return false;
    }

    int upgradeLength = getUpgradeLength();
    if ( upgradeLimit != null && upgradeLength > upgradeLimit.intValue() )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
          "The device upgrade exceeds the maximum allowed by the remote.", "Device Upgrade Limit Exceeded",
          JOptionPane.ERROR_MESSAGE );
      return false;
    }

    int combinedLength = upgradeLength + protocolLength;
    if ( combinedLimit != null && combinedLength > combinedLimit.intValue() )
    {
      JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
          "The combined upgrade exceeds the maximum allowed by the remote.", "Combined Upgrade Limit Exceeded",
          JOptionPane.ERROR_MESSAGE );
      return false;
    }

    return true;
  }

  /**
   * A device upgrade needs protocol code if either: (a) its protocol needs code as it has custom code or is a variant
   * that is not built in to the remote, or (b) the protocol has code translators that modify the protocol in accordance
   * with device parameters, and the device parameters are other than their default values.
   */
  public boolean needsProtocolCode()
  {
    if ( protocol == null )
    {
      return false;
    }
    if ( protocol.needsCode( remote ) )
    {
      return true;
    }
    Translate[] translators = protocol.getCodeTranslators( remote );
    if ( translators != null )
    {
      for ( Translate translate : translators )
      {
        Translator translator = ( Translator )translate;
        int devParmIndex = translator.getIndex();
        Value parmVal = parmValues[ devParmIndex ];
        if ( parmVal.hasUserValue() && !parmVal.getUserValue().equals( parmVal.getDefaultValue() ) )
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the code for this remote and protocol (correctly for the Device Combiner when that is the protocol),
   * translated according to the processor and, where present, any code translators for this device upgrade.
   */
  public Hex getCode()
  {
    return getCode( protocol );
  }

  /**
   * Returns the code for this remote and the specified protocol (correctly for the Device Combiner when that is the
   * protocol), translated according to the processor and, where present, any code translators for this device upgrade.
   */
  public Hex getCode( Protocol p )
  {
    Hex code = p.getCode( remote );
    if ( code != null )
    {
      // The new code for S3C80Processor.translate now ensures that manual protocols
      // are not translated when they are already correct for the remote concerned.
      if ( code.length() != 0 )
      {
        code = remote.getProcessor().translate( code, remote );
      }

      // A code translator for a protocol modifies the protocol hex according to the
      // value of certain device parameters
      translateCode( code );
    }
    return code;
  }

  public String getStarredID()
  {
    if ( protocol == null )
    {
      return null;
    }
    String starredID = protocol.getID( remote ).toString();
    if ( needsProtocolCode() )
    {
      Hex code = getCode();
      if ( code == null || code.length() == 0 )
      {
        starredID += "-";
      }
      else
      {
        starredID += "*";
      }
    }
    return starredID;
  }

  /**
   * Translates the code according to the protocol and parameter values of the device upgrade. The code provided as
   * parameter is modified.
   */
  public void translateCode( Hex code )
  {
    if ( protocol == null )
    {
      return;
    }
    Translate[] xlators = protocol.getCodeTranslators( remote );
    if ( xlators != null )
    {
      Value[] values = getParmValues();
      for ( int i = 0; i < xlators.length; i++ )
      {
        xlators[ i ].in( values, code, null, -1 );
      }
    }
  }
  
  public void changeProtocol( Protocol oldProtocol, ManualProtocol newProtocol )
  {
    if ( oldProtocol == null || getProtocol() != oldProtocol )
    {
      return;
    }
    short[] fixedData = oldProtocol.getFixedData( getParmValues() ).getData();
    boolean preserve = preserveOBC;
    preserveOBC = false;
    setProtocol( newProtocol );
    preserveOBC = preserve;
    List< Value > parms = new ArrayList< Value >();
    for ( int i = 0; i < fixedData.length; i++ )
    {
      parms.add( new Value( fixedData[ i ] ) );
    }
    setParmValues( parms.toArray( new Value[0] ) );
  }

  /** The description. */
  private String description = null;

  /** The setup code. */
  protected int setupCode = 0;

  /** The remote. */
  private Remote remote = null;

  private RemoteConfiguration remoteConfig = null;
  
  // Only set when editing, specifies the upgrade being edited.
  private DeviceUpgrade baseUpgrade = null;

  public RemoteConfiguration getRemoteConfig()
  {
    return remoteConfig;
  }

  public void setRemoteConfig( RemoteConfiguration remoteConfig )
  {
    if ( remoteConfig == null )
    {
      return;
    }
    Remote remote = remoteConfig.getRemote();
    if ( remote != this.remote )
    {
      setRemote( remote );
    }
    this.remoteConfig = remoteConfig;
    if ( remote != null && remote.usesEZRC() )//remote.isSSD() )
    {
      List< Button > selectors = null;
      if ( remote.getButtonGroups() != null && ( selectors = remote.getButtonGroups().get( "LCD" ) ) != null )
      {
        for ( Button b : selectors )
        {
          GeneralFunction gf = new GeneralFunction( b.getName() );
          gf.addReference( buttonRestriction, b );
          selectorMap.put( ( int )b.getKeyCode(), gf );
        }
      }
    }
  }

  /** The dev type alias name. */
  private String devTypeAliasName = null;

  /** The protocol. */
  protected Protocol protocol = null;
  
  protected Protocol originalProtocol = null;
  
  protected ManualProtocol convertedProtocol = null;
  
  private int sizeDevBytes = 0; // only used for JP1.4/JP2 remotes
  
  private int sizeCmdBytes = 0; // only used for JP1.4/JP2 remotes

  /** The parm values. */
  private Value[] parmValues = new Value[ 0 ];

  /** The functions. */
  private java.util.List< Function > functions = new ArrayList< Function >();
  
  private Button[] mappedButtons = null;

  public Button[] getMappedButtons()
  {
    return mappedButtons;
  }

  /** The ext functions. */
  private java.util.List< ExternalFunction > extFunctions = new ArrayList< ExternalFunction >();
  // private java.util.List< KeyMove > keymoves = new ArrayList< KeyMove >();
  /** The file. */
  private File file = null;

  private DeviceButton buttonRestriction = DeviceButton.noButton;
  
  private List< Button > softButtons = null;
  private List< Button > hardButtons = null;
  
  public List< Button > getSoftButtons()
  {
    return softButtons;
  }

  public List< Button > getHardButtons()
  {
    return hardButtons;
  }

  public void classifyButtons()
  {
    if ( remote.getButtonGroups() == null )
    {
      return;
    }
    
    if ( softButtons == null )
    {
      softButtons = new ArrayList< Button >();
    }
    else
    {
      softButtons.clear();
    }
    
    if ( hardButtons == null )
    {
      hardButtons = new ArrayList< Button >();
    } 
    else
    {
      hardButtons.clear();
    }
    
    for ( Button b : remote.getButtons() )
    {
      GeneralFunction f = getGeneralFunction( b.getKeyCode(), true );
      if ( f == null || f.getName() == null || f.getName().startsWith( "__" )
          || ((selectorMap !=null) && (selectorMap.get( ( int )b.getKeyCode() ) != null)) )
      {
        continue;
      }
      f = getGeneralFunction( b.getKeyCode(), false );
      if ( remote.isSoftButton( b ) )
      {
        softButtons.add( b );
      }
      else if ( f != null && !( f instanceof LearnedSignal ) )
      {
        hardButtons.add( b );
      }
    }
    Collections.sort( softButtons, ButtonSorter );
    Collections.sort( hardButtons, ButtonSorter );
  }
  
  private LinkedHashMap< Integer, Function > functionMap = null;
  private LinkedHashMap< Integer, Macro > macroMap = null;
  private LinkedHashMap< Integer, LearnedSignal > learnedMap = null;
  private LinkedHashMap< Integer, GeneralFunction > selectorMap = null;

  public LinkedHashMap< Integer, Function > getFunctionMap()
  {
    return functionMap;
  }

  public LinkedHashMap< Integer, Macro > getMacroMap()
  {
    return macroMap;
  }

  public LinkedHashMap< Integer, LearnedSignal > getLearnedMap()
  {
    return learnedMap;
  }

  public LinkedHashMap< Integer, GeneralFunction > getSelectorMap()
  {
    return selectorMap;
  }
  
  private LinkedHashMap< GeneralFunction, List< User > > restoreOnCancelReferences = null;

  /**
   * The offset in raw data to the start of this upgrade in the Device Dependent section, when applicable. Value
   * irrelevant if not applicable.
   */
  private int dependentOffset = 0;

  /**
   * Get the offset in raw data to the start of this upgrade in the Device Dependent section, when applicable. Value
   * irrelevant if not applicable.
   */
  public int getDependentOffset()
  {
    return dependentOffset;
  }

  /**
   * Set the offset in raw data to the start of this upgrade in the Device Dependent section, when applicable. Value
   * irrelevant if not applicable.
   */
  public void setDependentOffset( int dependentOffset )
  {
    this.dependentOffset = dependentOffset;
  }

  private Boolean buttonIndependent = true;

  public DeviceButton getButtonRestriction()
  {
    return buttonRestriction;
  }

  public void setButtonRestriction( DeviceButton buttonRestriction )
  {
    this.buttonRestriction = buttonRestriction;
  }

  public Boolean getButtonIndependent()
  {
    return buttonIndependent;
  }

  public void setButtonIndependent( Boolean buttonIndependent )
  {
    this.buttonIndependent = buttonIndependent;
  }
  
  /** The property change support. */
  private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );

  /**
   * Adds the property change listener.
   * 
   * @param listener
   *          the listener
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( listener );
  }

  /**
   * Adds the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
  }

  /**
   * Removes the property change listener.
   * 
   * @param listener
   *          the listener
   */
  public void removePropertyChangeListener( PropertyChangeListener listener )
  {
    propertyChangeSupport.removePropertyChangeListener( listener );
  }

  /**
   * Removes the property change listener.
   * 
   * @param propertyName
   *          the property name
   * @param listener
   *          the listener
   */
  public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener )
  {
    propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
  }

  /** The assignments. */
  private ButtonAssignments assignments = new ButtonAssignments();

  public ButtonAssignments getAssignments()
  {
    return assignments;
  }

  protected HashMap< Integer, Color[] > assignmentColors = new HashMap< Integer, Color[] >();

  public void setAssignmentColor( int keyCode, int deviceButtonIndex, Color color )
  {
    if ( !assignmentColors.containsKey( deviceButtonIndex ) )
    {
      Color[] allWhite = new Color[ 256 ];
      for ( int i = 0; i < 256; i++ )
      {
        allWhite[ i ] = Color.WHITE;
      }
      assignmentColors.put( deviceButtonIndex, allWhite );
    }
    assignmentColors.get( deviceButtonIndex )[ keyCode ] = color;
  }

  public Color getAssignmentColor( int keyCode, int deviceButtonIndex )
  {
    Color[] colors = assignmentColors.get( deviceButtonIndex );
    if ( colors != null )
    {
      return colors[ keyCode ];
    }
    return Color.WHITE;
  }

  private Color protocolHighlight = Color.WHITE;
  private int protocolMemoryUsage = 0;

  public Color getProtocolHighlight()
  {
    return protocolHighlight;
  }

  public void setProtocolHighlight( Color color )
  {
    protocolHighlight = color;
  }

  public int getProtocolMemoryUsage()
  {
    return protocolMemoryUsage;
  }

  public void clearProtocolMemoryUsage()
  {
    protocolMemoryUsage = 0;
  }

  public void addProtocolMemoryUsage( int protocolMemoryUsage )
  {
    this.protocolMemoryUsage += protocolMemoryUsage;
  }
  
  public void setFunction( Button b, GeneralFunction f, int state )
  {
    if ( !remote.usesEZRC() )
    {
      if ( f == null || f instanceof Function )
      {
        assignments.assign( b, ( Function )f, state );
      }
      return;
    }
    
    // From here on, code is only for XSight remotes
    
    int keyCode = b.getKeyCode();
    DeviceUpgrade fnUpg = f == null ? null : f.getUpgrade( remote );
    Function bf = getFunction( keyCode );  // function currently on b
    if ( f == null || bf != null )
    {
      // Delete current assignment to b.  If assignment is a macro,
      // leave the underlying function, if any.
      Macro macro = macroMap.get( keyCode );
      if ( macro != null )
      {
        macroMap.remove( keyCode );
        backupReferences( macro );
        macro.removeReference( buttonRestriction, b );
        if ( macro.isSystemMacro() )
        {
          KeySpec ks = macro.getItems().get( 0 );
          if ( ks.fn != null )
          {
            backupReferences( ks.fn );
            ks.fn.removeReference( buttonRestriction, b );
          }
        }
        if ( bf != null )
        {
          if ( bf.data == null )
          {
            // If assignment was a macro with no underlying function data,
            // remove the function
            assignments.assign( b, null );
            functions.remove( bf );
            bf = null;
          }
          else if ( f instanceof Function && fnUpg == this || remote.isSoftButton( b ) )
          {
            // Underlying function has data and either
            // (a) new value is a function for this upgrade, or
            // (b) button is a soft button and new value is a Macro.
            // Both cases require a new base function, in (a) so as not to overwrite it
            // and in (b) since the base function must be given same name as the macro.
            bf = null;;
          }
          // else underlying function has data, new value is a macro, button is not
          // a soft one, so base function need not be renamed and macro can be
          // placed on it.
        }
      }
      else if ( bf != null && ( f == null || f instanceof Function && fnUpg == this || remote.isSoftButton( b ) ) )
      {
        // new function will replace base function
        assignments.assign( b, null );
        bf.removeReference( buttonRestriction, b );
        bf = null;
      }
      // else base is null or is a Function with new value being a Macro to go on a
      // hard button, in which case base can be used to hold the macro
      if ( f == null )
      {
        return;
      }
    }
    
    // At this point, f is not null.  Also bf is null unless b is a hard button and
    // either f is not a Function or fnUpg != this, so that a macro will get assigned to b.

    if ( fnUpg == this && f instanceof Function )
    {
      // bf is necessarily null, f is a Function for current upgrade, so assign it
      // and return
      assignments.assign( b, ( Function )f );
      return;
    }
    
    // Now bf is null if b is a soft button, but it may not be so otherwise.
    // Also either f is a macro or a function that must be assigned as a macro
    
    if ( bf == null && remote.isSSD() )
    {
      // Create new empty bf to hold the macro.  Non-SSD remotes do not use a base
      // function to hold macros
      bf = new Function( f.getName() );
      bf.icon = f.icon;
      bf.setUpgrade( this );
      functions.add( bf );
      assignments.assign( b, bf );
    }
    
    if ( f instanceof Macro )
    {
      Macro macro = macroMap.get( keyCode );
      if ( macro != null )
      {
        backupReferences( macro );
        macro.removeReference( buttonRestriction, b );
      }  
      macro = ( Macro )f;
      macroMap.put( keyCode, macro );
      backupReferences( macro );
      macro.addReference( buttonRestriction, b );
      if ( remote.isSSD() && remote.isSoftButton( b ) )
      {
        // For a macro on a soft button, the base function needs same name
        // as macro.
        bf.setName( f.getName() );
      }
    }
    else // fnUpg != this and f is a Function 
    {
      Function fn = ( Function )f;
      backupReferences( fn );
      fn.addReference( buttonRestriction, b );
      // Create a system macro to hold the ir function, removing current macro if present
      Macro macro = macroMap.get( keyCode );
      if ( macro != null )
      {
        backupReferences( macro );
        macro.removeReference( buttonRestriction, b );
        if ( macro.isSystemMacro() )
        {
          KeySpec ks = macro.getItems().get( 0 );
          backupReferences( ks.fn );
          ks.fn.removeReference( buttonRestriction, b );
        }
      }   
      macro = new Macro( b.getKeyCode(), null, null );
      backupReferences( macro );      // enables macro to be removed if edit cancelled
      remoteConfig.getMacros().add( macro );
      macro.setName( fn.getName() );
      macro.setSystemMacro( true );
      int serial = remoteConfig.getNewMacroSerial();
      macro.setSerial( serial );
      List< KeySpec > items = new ArrayList< KeySpec >();
      KeySpec ks = null;
      if ( remote.isSSD() )
      {
        LinkedHashMap< Integer, List<Assister> > assists = new LinkedHashMap< Integer, List<Assister> >();
        for ( int j = 0; j < 3; j++ )
        {
          assists.put( j , new ArrayList< Assister >() );
        }
        macro.setAssists( assists );
      }
      ks = new KeySpec( fnUpg.buttonRestriction, fn );
      macro.setDeviceButtonIndex( buttonRestriction.getButtonIndex() );
      macro.addReference( buttonRestriction, b );
      macro.setSegmentFlags( 0xFF );
      ks.duration = 0;
      ks.delay = 3;
      items.add( ks );
      macro.setItems( items );
      macroMap.put( keyCode, macro );
    }
    return;
  }
  
  public LinkedHashMap< Function, Function > combineFunctions()
  {
    LinkedHashMap< Function, Function > map = new LinkedHashMap< Function, Function >();
    for ( int i = 0; ; i++ )
    {
      if ( i >= functions.size() - 1 )
      {
        break;
      }
      Function f1 = functions.get( i );
      for ( int j = i + 1; ; j++ )
      {
        if ( j >= functions.size() )
        {
          break;
        }
        Function f2 = functions.get( j );
        if ( !f2.isEquivalent( f1 ) )
        {
          continue;
        }
        if ( f2.getSerial() >= 0 && f1.getSerial() < 0 )
        {
          f1.setSerial( f2.getSerial() );
        }
        map.put( f2, f1 );
        functions.remove( f2 );
        // after the remove operation, the next function to be looked at has
        // the same index as the removed one, so need to decrement j
        j -= 1;
      }
    }
    for ( Button b : remote.getButtons() )
    {
      Function f1 = assignments.getAssignment( b );
      if ( f1 != null )
      {
        Function f2 = map.get( f1 );
        if ( f2 != null )
        {
          assignments.assign( b, f2 );
          f2.removeReference( buttonRestriction, b );
          for ( User u : f1.getUsers() )
          {
            f2.addReference( u.db, u.button );
          }
        }
      }
    }
    return map;
  }
  
  public void clearBackupReferences()
  {
    restoreOnCancelReferences = new LinkedHashMap< GeneralFunction, List< User > >();
  }
  
  private void backupReferences( GeneralFunction gf )
  {
    if ( gf instanceof Function && gf.getUpgrade( remote ) == this )
    {
      return;
    }
    
    if ( restoreOnCancelReferences != null && restoreOnCancelReferences.get( gf ) == null )
    {
      restoreOnCancelReferences.put( gf, new ArrayList< User >(  gf.getUsers() ) );
    }
  }
  
  
  public int getNewFunctionSerial( Function fn, boolean systemOnly )
  {
    List< Integer > list = new ArrayList< Integer >();
    if ( remote.isSSD() )
    {
      for ( DeviceUpgrade upg : remoteConfig.getDeviceUpgrades() )
      {
        for ( Function f : upg.getFunctions() )
        {
          if ( f.getSerial() >= 0 )
          {
            list.add( f.getSerial() );
          }
        }
      }
      for ( int serial = 24; ; serial++ )
      {
        if ( !list.contains( serial ) )
        {
          return serial;
        }
      }
    }
    
    // XSight Lite/Plus
    for ( Function f : functions )
    {
      if ( f.getSerial() >= 0 )
      {
        list.add( f.getSerial() & 0xFF );
      }
    }
    
    List< Button > btnList = remote.getButtonGroups().get( "System" );
    if ( btnList != null )
    {
      for ( Button b : btnList )
      {
        if ( !list.contains( ( int )b.getKeyCode() ) )
        {
          return ( buttonRestriction.getButtonIndex() << 8 ) + b.getKeyCode();
        }
      }
    }
    if ( systemOnly )
    {
      return -1;
    }

    btnList = remote.getButtonGroups().get( "Soft" );
    String title = "Assignment of function to button";
    String message = "Functions used in macros or assists must also be assigned to a button.\n"
            + "Function \"" + fn + "\" is so used and is not currently so assigned.\n";
    for ( Button b : btnList )
    {
      if ( getGeneralFunction( b.getKeyCode(), true ) == null )
      {
        message += "There is no hidden system button available so it will be assigned to\n"
                + "the soft button \"" + b + "\".  The Device Upgrade Editor may be used\n"
                + "to change this assignment if required.";
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE );
        assignments.assign( b, fn );
        fn.addReference( buttonRestriction, b );
        return -1;
      }
    }
    message += "There is no unassigned system button or soft button available so this\n"
        + "function will not send an IR signal.  To correct this, use the Device\n"
        + "Upgrade Editor either to manually assign this function or free up a\n"
        + "soft button by deleting its current assignment.";
    JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.WARNING_MESSAGE );
    return -1;
  }

  private class ButtonState
  {
    public Button button;
    public int state;
  }

  private ButtonState getButtonState( int keyCode )
  {
    ButtonState bs = new ButtonState();
    bs.state = Button.NORMAL_STATE;
    bs.button = remote.getButton( keyCode );
    if ( bs.button == null )
    {
      int baseCode = keyCode & 0x3F;
      if ( baseCode != 0 )
      {
        bs.button = remote.getButton( baseCode );
        if ( ( baseCode | remote.getShiftMask() ) == keyCode )
        {
          bs.state = Button.SHIFTED_STATE;
        }
        if ( ( baseCode | remote.getXShiftMask() ) == keyCode )
        {
          bs.state = Button.XSHIFTED_STATE;
        }
      }
      else
      {
        baseCode = keyCode & ~remote.getShiftMask();
        bs.button = remote.getButton( baseCode );
        if ( bs.button != null )
        {
          bs.state = Button.SHIFTED_STATE;
        }
        else
        {
          baseCode = keyCode & ~remote.getXShiftMask();
          bs.button = remote.getButton( baseCode );
          if ( bs.button != null )
          {
            bs.state = Button.XSHIFTED_STATE;
          }
        }
      }
    }
    return bs;
  }

  public void setFunction( int keyCode, Function f )
  {
    ButtonState bs = getButtonState( keyCode );
    setFunction( bs.button, f, bs.state );
  }

  /**
   * Gets the function.
   * 
   * @param b
   *          the b
   * @param state
   *          the state
   * @return the function
   */
  public Function getFunction( Button b, int state )
  {
    return assignments.getAssignment( b, state );
  }

  public Function getFunction( int keyCode )
  {
    ButtonState bs = getButtonState( keyCode );
    return assignments.getAssignment( bs.button, bs.state );
  }
  
  public GeneralFunction getGeneralFunction( int keyCode, boolean includeMacros )
  {
    Button button = remote.getButton( keyCode );
    GeneralFunction gf = null;
    if ( remote.usesEZRC() )
    {
      gf = selectorMap.get( keyCode );
      if ( gf == null )
      {
        gf = learnedMap.get( keyCode );
      }
      if ( includeMacros && gf == null && !remote.isSSD() )
      {
        gf = macroMap.get( keyCode );
      }
    }
    if ( gf == null )
    {
      gf = getFunction( button, Button.NORMAL_STATE );
    }
    return gf;
  }
  
  public List< Function > getFunctionList()
  {
    List< Function > list = new ArrayList< Function >();
    for ( Function function : functions )
    {
      if ( function.accept() )
      {
        list.add( function );
      }
    }
    return list;
  }
  
  public List< GeneralFunction > getGeneralFunctionList()
  {
    List< GeneralFunction > list = new ArrayList< GeneralFunction >( getFunctionList() );
    if ( remote.usesEZRC() )
    {
      list.addAll( learnedMap.values() );
      list.addAll( selectorMap.values() );
    }
    return list;
  }
  
  public void doCancel( boolean isNewUpgrade )
  {
    Remote remote = remoteConfig.getRemote();
    if ( remote.usesEZRC() && isNewUpgrade )
    {
      DeviceButton db = buttonRestriction;
      if ( db != null && db != DeviceButton.noButton )
      {
        db.setUpgrade( null );
        db.setDefaultName();
        db.setConstructed( false );
      }
      if ( db != null && remote.isSSD() )
      {
        db.setSegment( null );
      }
    }
    Protocol p = convertedProtocol;
    if ( p != null )
    {
      ProtocolManager.getProtocolManager().remove( p );
    }
    p = originalProtocol;
    if ( p instanceof ManualProtocol )
    {
      ProtocolManager.getProtocolManager().add( p );
    }
    for ( GeneralFunction gf : restoreOnCancelReferences.keySet() )
    {
      gf.removeReferences();
      for ( User user : restoreOnCancelReferences.get( gf ) )
      {
        gf.addReference( user.db, user.button );
      }
      if ( gf instanceof Macro )
      {
        Macro macro = ( Macro )gf;
        List< Macro > macros = remoteConfig.getMacros();
        if ( macro.getUsers().isEmpty() )
        {
          macros.remove( macro );
        }
        else if ( !macros.contains( macro ) )
        {
          macros.add( macro );
        }
      }
    }
    restoreOnCancelReferences = null;
  }
  
  /**
   * For EZ-RC remotes only, ensures functionMap has irSerial values for precisely
   * those functions that are used in a macro or in an activity or macro assist and
   * which are not sent by any keypress.
   */
  public void filterFunctionMap()
  {
    if ( !remote.usesEZRC() )
    {
      return;
    }
    
    List< Integer > fnkeys = new ArrayList< Integer >();
    int fnky = 0;
            
    for ( KeySpec ks : remoteConfig.getAllKeySpecs() )
    {
      if ( ks.fn != null && ks.fn instanceof Function && ks.getButton() == null 
          && ks.fn.getSerial() < 0 && ks.db.getUpgrade() == this )
      {
        // Assign a serial number to functions that are used but not assigned to buttons.
        int serial = getNewFunctionSerial( ( Function )ks.fn, false );
        ks.fn.setSerial( serial );
        if ( serial >= 0 )
        {
          functionMap.put( serial, ( Function )ks.fn );
        }
      }
      // Create list of serial numbers of functions not assigned to buttons.
      // These must get assigned to system keys
      if ( ks.getButton() == null && ks.fn != null 
          && ( fnky = ks.fn.getSerial() ) >= 0 
          && ks.fn == functionMap.get( fnky ) 
          && !fnkeys.contains( fnky ) )
      {
        fnkeys.add( fnky );
      }
    }
    
    // Cancel serial numbers that are not in the list just created.
    Iterator< Integer > it = functionMap.keySet().iterator();
    while ( it.hasNext() )
    {
      int key = it.next();
      if ( !fnkeys.contains( key ) )
      {
        functionMap.get( key ).setSerial( -1 );
        it.remove();
      }
    }
  }

  public String toString()
  {
    return devTypeAliasName + '/' + setupCode + " (" + description + ')';
  }

  /** The Constant deviceTypeAliasNames. */
  private static final String[] deviceTypeAliasNames =
  {
      "Cable", "TV", "VCR", "CD", "Tuner", "DVD", "SAT", "Tape", "Laserdisc", "DAT", "Home Auto", "Misc Audio",
      "Phono", "Video Acc", "Amp", "PVR", "OEM Mode"
  };

  /** The default names. */
  private static String[] defaultNames = null;

  /** The Constant defaultFunctionNames. */
  private static final String[] defaultFunctionNames =
  {
      "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "vol up", "vol down", "mute", "channel up", "channel down",
      "power", "enter", "tv/vcr", "last (prev ch)", "menu", "program guide", "up arrow", "down arrow", "left arrow",
      "right arrow", "select", "sleep", "pip on/off", "display", "pip swap", "pip move", "play", "pause", "rewind",
      "fast fwd", "stop", "record", "exit", "surround", "input toggle", "+100", "fav/scan", "device button",
      "next track", "prev track", "shift-left", "shift-right", "pip freeze", "slow", "eject", "slow+", "slow-", "X2",
      "center", "rear"
  };

  /** The Constant genericButtonNames. */
  private final static String[] genericButtonNames =
  {
      "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "vol up", "vol down", "mute", "channel up", "channel down",
      "power", "enter", "tv/vcr", "prev ch", "menu", "guide", "up arrow", "down arrow", "left arrow", "right arrow",
      "select", "sleep", "pip on/off", "display", "pip swap", "pip move", "play", "pause", "rewind", "fast fwd",
      "stop", "record", "exit", "surround", "input", "+100", "fav/scan", "device button", "next track", "prev track",
      "shift-left", "shift-right", "pip freeze", "slow", "eject", "slow+", "slow-", "x2", "center", "rear", "phantom1",
      "phantom2", "phantom3", "phantom4", "phantom5", "phantom6", "phantom7", "phantom8", "phantom9", "phantom10",
      "setup", "light", "theater", "macro1", "macro2", "macro3", "macro4", "learn1", "learn2", "learn3", "learn4" // ,
  // "button85", "button86", "button87", "button88", "button89", "button90",
  // "button91", "button92", "button93", "button94", "button95", "button96",
  // "button97", "button98", "button99", "button100", "button101", "button102",
  // "button103", "button104", "button105", "button106", "button107", "button108",
  // "button109", "button110", "button112", "button113", "button114", "button115",
  // "button116", "button117", "button118", "button119", "button120", "button121",
  // "button122", "button123", "button124", "button125", "button126", "button127",
  // "button128", "button129", "button130", "button131", "button132", "button133",
  // "button134", "button135", "button136"
  };

  private boolean preserveOBC = true;

  public void setPreserveOBC( boolean flag )
  {
    preserveOBC = flag;
  }

  public boolean getPreserveOBC()
  {
    return preserveOBC;
  }

  public DeviceUpgrade getBaseUpgrade()
  {
    return baseUpgrade;
  }
  
  private Segment softButtonSegment = null;
  private Segment softFunctionSegment = null;
  
  public Segment getSoftButtonSegment()
  {
    return softButtonSegment;
  }

  public void setSoftButtonSegment( Segment softButtonSegment )
  {
    this.softButtonSegment = softButtonSegment;
  }

  public Segment getSoftFunctionSegment()
  {
    return softFunctionSegment;
  }

  public void setSoftFunctionSegment( Segment softFunctionSegment )
  {
    this.softFunctionSegment = softFunctionSegment;
  }
  
  private LinkedHashMap< Function, Function > swapList = null;

  public LinkedHashMap< Function, Function > getSwapList()
  {
    return swapList;
  }

  public void setSwapList( LinkedHashMap< Function, Function > swapList )
  {
    this.swapList = swapList;
  }

  public static Comparator< Button > ButtonSorter = new Comparator< Button >()
  {
    @Override
    public int compare( Button b1, Button b2 )
    {
      return ( new Short( b1.getKeyCode() ).compareTo( new Short( b2.getKeyCode()) ) );
    }    
  };
}
