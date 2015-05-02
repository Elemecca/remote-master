package com.hifiremote.jp1.extinstall;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JOptionPane;

import com.hifiremote.jp1.Activity;
import com.hifiremote.jp1.ActivityGroup;
import com.hifiremote.jp1.AddressRange;
import com.hifiremote.jp1.Button;
import com.hifiremote.jp1.DeviceButton;
import com.hifiremote.jp1.DeviceUpgrade;
import com.hifiremote.jp1.Macro;
import com.hifiremote.jp1.ProtocolManager;
import com.hifiremote.jp1.Remote;
import com.hifiremote.jp1.RemoteConfiguration;
import com.hifiremote.jp1.RemoteManager;
import com.hifiremote.jp1.XorCheckSum;
import com.hifiremote.jp1.extinstall.UpgradeItem.Classification;
import com.hifiremote.jp1.io.JPS;

public class RMExtInstall extends ExtInstall
{
  public static RemoteConfiguration remoteConfig;
  private static String errorMsg = null;
  private static Remote extenderRemote = null;
  private static boolean extenderMerge = true;
  private JPS io = null;
  private static boolean isSimpleset = false;
  
  public RMExtInstall( String hexName, RemoteConfiguration remoteConfig )
  {
    super( hexName, null, null, remoteConfig.getRemote().getSigAddress() );
    RMExtInstall.remoteConfig = remoteConfig;
    this.hexName = hexName;
    this.sigAddr = remoteConfig.getRemote().getSigAddress();
    this.io = remoteConfig.getOwner().binLoaded();
  }

  private String hexName;
  private int sigAddr;
  private List< Integer > devUpgradeCodes = new ArrayList< Integer >();
  private List< Integer > protUpgradeIDs = new ArrayList< Integer >();

  @Override
  public void install() throws IOException, CloneNotSupportedException
  {
      CrudeErrorLogger Erl = new CrudeErrorLogger();

      IrHexConfig ExtHex = new IrHexConfig();
      if ( io != null )
      {
        File file = FindFile( hexName, ".txt" );
        BufferedReader rdr = new BufferedReader( new FileReader( file ) );
        if ( !ExtHex.Load( Erl, rdr ) )
        {
          rdr.close(); 
          System.exit( 1 );
        }
        String title = "Simpleset Patching";
        String message = "You are about to apply patches to a settings.bin file for a Simpleset\n"
                        +"remote.  If you continue, these patches will be applied immediately,\n"
                        +"without any Save step or any further opportunity to abort the process.\n\n"
                        +"You are strongly advised NOT to do so directly with the file on the\n"
                        +"external drive of the remote, but only on a copy on your PC.\n\n"
                        +"Do you wish to proceed?";
        if ( JOptionPane.showConfirmDialog( remoteConfig.getOwner(), message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION )
        {
          message = "Operation cancelled.";
          JOptionPane.showMessageDialog( remoteConfig.getOwner(), message, title, JOptionPane.INFORMATION_MESSAGE );
          return;
        }
        
        int start = 0;
        int len = 0;
//        int base = remoteConfig.getRemote().getBaseAddress();
        for ( int i = 0; i <= ExtHex.size(); i++ )
        {
          IrHex val = i < ExtHex.size() ? ExtHex.get( i ) : null;
          if ( val == null || !val.isValid() )
          {
            if ( len > 0 )
            {
              short[] buffer = new short[ len ];
              for ( int n = 0; n < len; n++ )
              {
                buffer[ n ] = ExtHex.Get( start + n );
//                if ( ( start + n ) >= base )
//                {
//                  remoteConfig.getData()[ start + n - base ] = buffer[ n ];
//                }
              }
              io.writeRemote( start, buffer );
            }
            start = i + 1;
            len = 0;
          }
          else
          {
            len++;
          }
        }
        XorCheckSum cs = new XorCheckSum( 0, new AddressRange( 2, io.getCodeSize() - 1 ), false );
        short[] code = new short[ io.getCodeSize() ];
        io.readRemote( io.getCodeAddress(), code );
        short c = cs.calculateCheckSum( code, 2, io.getCodeSize() - 1 );
        short[] buffer = new short[ 2 ];
        buffer[ 0 ] = c;
        buffer[ 1 ] = ( short )~c;
        io.writeRemote( io.getCodeAddress(), buffer );
        
        cs = new XorCheckSum( 0, new AddressRange( 2, io.getSigSize() - 1 ), false );
        code = new short[ io.getSigSize() ];
        io.readRemote( io.getSigAddress(), code );
        c = cs.calculateCheckSum( code, 2, io.getSigSize() - 1 );
        buffer[ 0 ] = c;
        buffer[ 1 ] = ( short )~c;
        io.writeRemote( io.getSigAddress(), buffer );
        message = "Patching complete.";
        JOptionPane.showMessageDialog( remoteConfig.getOwner(), message, title, JOptionPane.INFORMATION_MESSAGE );
        rdr.close();
        return;
      }
      AdvList ExtAdv = new AdvList();
      UpgradeList ExtUpgrade = new UpgradeList();
      Rdf ExtRdf = new Rdf();
      Remote newRemote = null;
      LoadHex( Erl,
               hexName,
               ExtHex,
               ExtAdv,
               ExtUpgrade,
               ExtRdf,
               0 );
      
      if ( isSimpleset )
      {
        return;
      }
      if ( ExtRdf.m_AdvCodeAddr.end < 0 || ExtRdf.m_UpgradeAddr.end < 0 )
      {
        remoteConfig = null;
        showError();
        return;
      }

      for ( UpgradeItem item : ExtUpgrade )
      {
        if ( item.Classify() == Classification.eDevice )
        {
          devUpgradeCodes.add( ( ( UpgradeDevice )item ).m_ID );
        }
        else if ( item.Classify() == Classification.eProtocol )
        {
          protUpgradeIDs.add( ( ( UpgradeProtocol )item ).m_ID );
        }
      }
      
      IrHexConfig OldHex = new IrHexConfig();
      AdvList OldAdv = new AdvList();
      UpgradeList OldUpgrade = new UpgradeList();
      Rdf OldRdf = new Rdf();
      LoadHex( Erl,
               null,
               OldHex,
               OldAdv,
               OldUpgrade,
               OldRdf,
               sigAddr );
      
      if ( OldRdf.m_AdvCodeAddr.end < 0 || OldRdf.m_UpgradeAddr.end < 0 )
      {
        remoteConfig = null;
        showError();
        return;
      }

      String generalComment;
      
      System.err.println( "Merging." );

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      if ( !extenderMerge )
      {
          // Install anything other than an extender by copying from the thing
          // being installed into the configuration
        
          newRemote = remoteConfig.getRemote(); // Unchanged by merge
          
          OldAdv.Merge( ExtAdv,
                        EnumSet.of( AdvItem.Flag.eMacroCollideNew,
                                    AdvItem.Flag.eKeyMoveCollideNew ) );
          OldUpgrade.Merge( Erl,
                            ExtUpgrade,
                            EnumSet.of( UpgradeList.Flag.eProtocolCollideNew,
                                        UpgradeList.Flag.eDeviceCollideNew ) );

          generalComment = OldHex.GetComment( 0x0000 ); // get the general comment from the IR file
          OldHex.RemoveComments(); // remove all comments from the IR configuration
          if ( generalComment != null && !generalComment.equals( "" ) )
          {
            OldHex.SetComment( 0x0000, generalComment );
          } // set the general comment

          OldHex.PostAdvList( Erl, OldAdv );
          OldHex.PostUpgradeList( Erl, OldUpgrade );
          
          OldRdf.DoCheckSums( OldHex );
          OldHex.Dump( pw );
      }
      else
      {
          // Install an extender by copying things from the configuration into
          // the extender

          newRemote = extenderRemote;
          
          ExtAdv.Merge( OldAdv,
                        EnumSet.of( AdvItem.Flag.eMacroCollideNew ) );
          ExtUpgrade.Merge( Erl,
                            OldUpgrade,
                            EnumSet.noneOf( UpgradeList.Flag.class ) );

          generalComment = OldHex.GetComment( 0x0000 ); // get the general comment from the IR file
          ExtHex.RemoveComments(); // remove all comments from Extender configuration
          if ( generalComment != null && !generalComment.equals( "" ) )
          {
              ExtHex.SetComment( 0x0000, generalComment );
          } // set the general comment

          ExtHex.PostAdvList( Erl, ExtAdv );
          ExtHex.PostUpgradeList( Erl, ExtUpgrade );

          // ExtAdv.Print(ExtAdv); //show the merged advance code list
          // ExtUpgrade.Print(OldUpgrade); //show the merged upgrade list
          // ExtHex.PrintComments(); //show the merged comment list

          ExtHex.Merge( OldHex );
          ExtRdf.DoCheckSums( ExtHex );
          ExtHex.Dump( pw );
      }
      String out = sw.toString();
      pw.close();
      ProtocolManager.getProtocolManager().reset( protUpgradeIDs );
      remoteConfig = new RemoteConfiguration( out, remoteConfig.getOwner(), newRemote );
  }
  
  public static void LoadHex( ErrorLogger Erl, String arg, IrHexConfig Config, AdvList Adv, UpgradeList Upgrade,
      Rdf rdf, int sigAddr ) throws IOException
  {
    Remote remote = null;
    short[] sigData = null;
    BufferedReader rdr = null;
    if ( arg != null )
    {
      File file = FindFile( arg, ".txt" );
      System.err.println( "Loading data from file." );
      rdr = new BufferedReader( new FileReader( file ) );
    }
    else
    {
      System.err.println( "Exporting present configuration as string in .ir file format." );
      String ir = remoteConfig.exportIR();
      System.err.println( "Loading data from exported string." );
      rdr = new BufferedReader( new StringReader( ir ) );
      remote = remoteConfig.getRemote();
    }

    if ( !Config.Load( Erl, rdr ) )
    {
      errorMsg = "Loading of ";
      errorMsg += ( arg == null ) ? "main" : "merge";
      errorMsg += " data failed.";
      rdr.close();
      return;
    }

    rdr.close();
    if ( remote == null )
    {
      int baseAddr = 0;
      if ( !Config.IsValid( 2 ) ) // If base address is 0 then signature starts at address 2
      {
        // If base address > 0 then it is multiple of 0x100 and signature starts at it.
        for ( ; baseAddr < Config.size() && !Config.IsValid( baseAddr ); baseAddr += 0x100 ){}
      }
      if ( baseAddr >= Config.size() )
      {
        errorMsg = "Unable to locate a valid signature.";
        return;
      }
      sigAddr = ( baseAddr == 0 ) ? 2 : baseAddr;    
      extenderMerge = ( baseAddr == 0 ) ? !Config.IsValid( 0 ) : !Config.IsValid( baseAddr + 8 );
      if ( Config.IsValid( sigAddr ) && Config.IsValid( sigAddr + 1 ) && Config.Get( sigAddr ) + Config.Get( sigAddr + 1 ) == 0xFF )
      {
        isSimpleset = true;
        extenderMerge = true;
        System.err.println( "Extender is for a Simpleset remote" );
        sigData = new short[ 64 ];
        for ( int i = 0; i < sigData.length; i++ )
        {
          if ( !Config.IsValid( sigAddr + i ) )
          {
            errorMsg = "Incomplete sig block in extender data.";
            return;
          }
          sigData[ i ] = Config.Get( sigAddr + i );
        }
        sigAddr += 6;
        // Increment base address past sig section to start of E2
        baseAddr += 0x100;
        for ( ; baseAddr < Config.size() && !Config.IsValid( baseAddr ); baseAddr += 0x100 ){}
        if ( baseAddr >= Config.size() )
        {
          errorMsg = "Unable to locate a valid EEPROM area.";
          return;
        }
      }
      int eepromSize = ( extenderMerge ) ? remoteConfig.getRemote().getEepromSize() : Config.size() - baseAddr;
      if ( Config.size() > baseAddr + eepromSize )
      {
        errorMsg = "Extender data extends beyond EEPROM size.";
        return;
      }
      if ( extenderMerge && baseAddr != remoteConfig.getRemote().getBaseAddress() )
      {
        errorMsg = "EEPROM area is located differently in extended and base remotes";
        return;
      }
      StringBuilder sb = new StringBuilder();
      int sigLen = isSimpleset ? 6 : 8;
      for ( int ndx = 0; Config.IsValid( ndx + sigAddr ) && ndx < sigLen; ndx++ )
      {
        sb.append( ( char )Config.Get( ndx + sigAddr ) );
      }
      String signature = sb.toString();
      String signature2 = null;
      RemoteManager rm = RemoteManager.getRemoteManager();
      List< Remote > remotes = null;
      for ( int len = signature.length(); len > 3; len-- )
      {
        signature2 = signature.substring( 0, len );
        remotes = rm.findRemoteBySignature( signature2 );
        if ( !remotes.isEmpty() ) break;
      }
      signature = signature2;
      short[] data = new short[ eepromSize ];
      for ( int i = 0; i < eepromSize; i++ )
      {
        data[ i ] = ( ( i < Config.size() - baseAddr ) && Config.IsValid( i + baseAddr ) ) ? Config.Get( i + baseAddr) : 0x100;
      }
      
      remote = RemoteConfiguration.filterRemotes( remotes, signature, eepromSize, data, sigData, false );
      if ( remote == null )
      {
        errorMsg = "No remote found that matches the merge file.";
        return;
      }
      remote.load();
      if ( baseAddr != remote.getBaseAddress() )
      {
        errorMsg = "Merge data and its RDF have conflicting base addresses.";
        return;
      }
      if ( remote.getOemSignature() != null && !remoteConfig.getRemote().getSignature().equals( remote.getOemSignature() )
         && !remoteConfig.getRemote().getSignature().equals( remote.getSignature() ) )
      {
        if ( remoteConfig.getRemote().getOemSignature() != null && remoteConfig.getRemote().getOemSignature().equals( remote.getOemSignature() ) )
        {
          String message = "This remote already has an extender installed which has a different signature from\n"
              + "that of the extender you are about to install.  This may mean that the new extender\n"
              + "is not compatible with the exiting one.  If you continue with the installation you\n"
              + "may finish up with a corrupt setup.\n\n"
              + "Are you sure that you wish to continue?";
          String title = "Apparent extender incompatibility";
          if ( JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.NO_OPTION )
          {
            errorMsg = "Aborting installation.";
            return;
          }
        }
        else
        {
          errorMsg = "Extender is not for this remote.";
          return;
        }
      }
      extenderRemote = remote;
    }
    if ( isSimpleset )
    {
      RemoteConfiguration extConfig = new RemoteConfiguration( remote, remoteConfig.getOwner() );
      extConfig.setSigData( sigData );
      int baseAddr = remote.getBaseAddress();
      for ( int i = 0; i < extConfig.getData().length; i++ )
      {
        extConfig.getData()[ i ] = ( ( i < Config.size() - baseAddr ) && Config.IsValid( i + baseAddr ) ) ? Config.Get( i + baseAddr) : 0xFF;
      }
      extConfig.loadSegments( true );
      extConfig.setDeviceButtonSegments();
      List< Integer > missingDev = new ArrayList< Integer >();
      for ( int i = 0; i < remote.getDeviceButtons().length; i++ )
      {
        DeviceButton db = remote.getDeviceButtons()[ i ];
        if ( remoteConfig.getRemote().getDeviceButton( db.getButtonIndex() ) == null )
        {
          remoteConfig.getSegments().get( 0 ).add( db.getSegment() );
          missingDev.add( i );
        }
      }
      remoteConfig.setRemote( remote );
      for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
      {
        du.setNewRemote( remote );
      }
      remoteConfig.setSigData( sigData );
      remoteConfig.setDeviceButtonSegments();
      List< Activity > list = new ArrayList< Activity >();
      LinkedHashMap< Button, Activity > activities = remoteConfig.getActivities();
      if ( activities != null )
      {
        for ( Activity activity : remoteConfig.getActivities().values() )
        {
          List< ActivityGroup > missingGroups = new ArrayList< ActivityGroup >();
          Button btn = remote.getButton( activity.getButton().getKeyCode() );
          Activity extActivity = extConfig.getActivities().get( btn );
          for ( ActivityGroup group : extActivity.getActivityGroups() )
          {
            if ( activity.getGroupMap().get( group.getIndex() ) == null )
            {
              missingGroups.add( group );
            }
          }
          if ( missingGroups.size() > 0 )
          {
            int oldLen = activity.getActivityGroups().length;
            int newLen = oldLen + missingGroups.size();
            ActivityGroup[] newGroups = Arrays.copyOf( activity.getActivityGroups(), newLen );
            for ( int i = 0; i < missingGroups.size(); i++ )
            {
              ActivityGroup group = missingGroups.get( i );
              newGroups[ oldLen + i ] = group;
              activity.getGroupMap().put( group.getIndex(), group );
            }
            activity.setActivityGroups( newGroups );
          }
          activity.set( remote );
          if ( activity.getMacro() != null )
          {
            Macro macro = activity.getMacro();
            remote.correctType04Macro( macro );
            for ( Integer i : missingDev )
            {
              macro.getData().set( ( short )remote.getDeviceButtons()[ i ].getButtonIndex(), 2*i );
            }
          }
          list.add( activity );
        }
        activities.clear();
        for ( Activity activity : list )
        {
          activities.put( activity.getButton(), activity );
        }
        for ( Button btn : extConfig.getActivities().keySet() )
        {
          if ( activities.get( btn ) == null )
          {
            activities.put( btn, extConfig.getActivities().get( btn ) );
          }
        }
      }
      remoteConfig.setDeviceButtonNotes( Arrays.copyOf( remoteConfig.getDeviceButtonNotes(), remote.getDeviceButtons().length ) );
      return;
    }

    File rdfFile = remote.getFile();
    try
    {
        rdf.rdr = new BufferedReader( new FileReader( rdfFile ) );
    }
    catch ( FileNotFoundException fnfe )
    {
      errorMsg = "Can't read file " + rdfFile.getAbsolutePath() + ".";
      return;
    }
    String message = "Loading RDF " + rdfFile.getCanonicalPath();
    message += " for " + ( arg == null ? "main" : "merge" ) + " file";
    System.err.println( message );
    rdf.Load();

    if ( rdf.m_AdvCodeAddr.end < 0 || rdf.m_UpgradeAddr.end < 0 )
    {
      errorMsg = "RDF file " + rdfFile + " not valid.";
      return;
    }

    Config.m_pRdf = rdf;
    Config.SetAdvMem( rdf.m_AdvCodeAddr.begin, rdf.m_AdvCodeAddr.end );
    Config.SetUpgradeMem( rdf.m_UpgradeAddr.begin, rdf.m_UpgradeAddr.end );
    Config.SetBaseAddr( rdf.m_BaseAddr );
    Config.SetAdvCodeType( rdf.m_AdvCodeType );
    Config.SetSectionTerminator( rdf.m_SectionTerminator );

    if ( rdf.m_LearnedAddr.begin != 0 )
    { // Learned memory is optional
      // Search for a gap in the Learn memory
      int last = rdf.m_LearnedAddr.begin;
      for ( ; last < rdf.m_LearnedAddr.end && Config.IsValid( last ); last++ )
      {
        ;
      }
      // Search for a nongap in the Learn memory
      for ( ; last < rdf.m_LearnedAddr.end && !Config.IsValid( last ); last++ )
      {
        ;
      }
      rdf.m_LearnedAddr.end = last;
    }

    // Learn memory ends at the first nonGap after a gap, or at 0x6FF if a nonGap after gap wasn't found
    Config.SetLearnMem( rdf.m_LearnedAddr.begin, rdf.m_LearnedAddr.end );
    Config.FillAdvList( Erl, Adv );
    Config.FillUpgradeList( Erl, Upgrade );
  }
  
  public void showError()
  {
    String title = "ExtInstall error";
    errorMsg += "\nExtInstall terminating.";
    JOptionPane.showMessageDialog( null, errorMsg, title, JOptionPane.ERROR_MESSAGE );
    System.err.println( errorMsg );
  }

  public List< Integer > getDevUpgradeCodes()
  {
    return devUpgradeCodes;
  }

  public List< Integer > getProtUpgradeIDs()
  {
    return protUpgradeIDs;
  }

  public boolean isExtenderMerge()
  {
    return extenderMerge;
  }

}
