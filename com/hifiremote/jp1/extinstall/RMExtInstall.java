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
import java.util.EnumSet;
import java.util.List;

import javax.swing.JOptionPane;

import com.hifiremote.jp1.Remote;
import com.hifiremote.jp1.RemoteConfiguration;
import com.hifiremote.jp1.RemoteManager;
import com.hifiremote.jp1.extinstall.UpgradeItem.Classification;

public class RMExtInstall extends ExtInstall
{
  public static RemoteConfiguration remoteConfig;
  private static String errorMsg = null;
  private static Remote extenderRemote = null;
  
  public RMExtInstall( String hexName, RemoteConfiguration remoteConfig )
  {
    super( hexName, null, null, remoteConfig.getRemote().getSigAddress() );
    RMExtInstall.remoteConfig = remoteConfig;
    this.hexName = hexName;
    this.sigAddr = remoteConfig.getRemote().getSigAddress();
  }

  private String hexName;
  private int sigAddr;
  private boolean extenderMerge = true;
  private List< Integer > devUpgradeCodes = new ArrayList< Integer >();
  private List< Integer > protUpgradeIDs = new ArrayList< Integer >();

  @Override
  public void install() throws IOException, CloneNotSupportedException
  {
      CrudeErrorLogger Erl = new CrudeErrorLogger();

      IrHexConfig ExtHex = new IrHexConfig();
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
               sigAddr );
      
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
      int baseAddr = sigAddr & 0xFFF0;
      if ( ( baseAddr == 0 && ExtHex.IsValid( 0 ) ) || ( baseAddr > 0 && ExtHex.IsValid( baseAddr + 8 ) ) )
      {
          // Install anything other than an extender by copying from the thing
          // being installed into the configuration
        
          extenderMerge = false;
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
          OldHex.SetComment( 0x0000, generalComment ); // set the general comment

          OldHex.PostAdvList( Erl, OldAdv );
          OldHex.PostUpgradeList( Erl, OldUpgrade );
          
          OldRdf.DoCheckSums( ExtHex );
          OldHex.Dump( pw );
      }
      else
      {
          // Install an extender by copying things from the configuration into
          // the extender

          extenderMerge = true;
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
      remoteConfig = new RemoteConfiguration( out, remoteConfig.getOwner(), newRemote );
  }
  
  public static void LoadHex( ErrorLogger Erl, String arg, IrHexConfig Config, AdvList Adv, UpgradeList Upgrade,
      Rdf rdf, int sigAddr ) throws IOException
  {
    Remote remote = null;
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
      return;
    }

    if ( remote == null )
    {
      remote = remoteConfig.getRemote();
      int eepromSize = remote.getEepromSize();
      int baseAddr = sigAddr & 0xFFF0;
      if ( Config.size() > baseAddr + eepromSize )
      {
        errorMsg = "Merge data extends beyond end of EEPROM.";
        return;
      }
      char[] Sig = new char[ remote.getSignature().length() ];

      for ( int ndx = 0; ndx < Sig.length; ndx++ )
      {
        Sig[ ndx ] = ( char )Config.Get( ndx + sigAddr );
      }

      String signature = new String( Sig );

      short[] data = new short[ eepromSize ];
      for ( int i = 0; i < eepromSize; i++ )
      {
        data[ i ] = ( i < Config.size() - baseAddr && Config.IsValid( i + baseAddr ) ) ? Config.Get( i + baseAddr) : 0x100;
      }
      RemoteManager rm = RemoteManager.getRemoteManager();
      List< Remote > remotes = rm.findRemoteBySignature( signature );
      remote = RemoteConfiguration.filterRemotes( remotes, signature, eepromSize, data, false );
      if ( remote == null )
      {
        errorMsg = "No remote found that matches the merge file.";
        return;
      }
      extenderRemote = remote;
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
