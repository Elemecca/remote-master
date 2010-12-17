package com.hifiremote.jp1;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkListener;

import com.hifiremote.LibraryLoader;
import com.hifiremote.jp1.io.IO;
import com.hifiremote.jp1.io.JP12Serial;
import com.hifiremote.jp1.io.JP1Parallel;
import com.hifiremote.jp1.io.JP1USB;
import com.l2fprod.common.swing.JDirectoryChooser;

/**
 * Description of the Class.
 * 
 * @author Greg
 * @created November 30, 2006
 */

public class RemoteMaster extends JP1Frame implements ActionListener, PropertyChangeListener, HyperlinkListener,
    ChangeListener
{
  public static final int MAX_RDF_SYNC = 4;
  public static final int MIN_RDF_SYNC = 3;
  
  public static final Color AQUAMARINE = new Color( 127, 255, 212 );

  /** The frame. */
  private static JP1Frame frame = null;

  /** Description of the Field. */
  public final static String version = "v2.01 alpha";

  /** The dir. */
  private File dir = null;

  /** Description of the Field. */
  public File file = null;

  /** The remote config. */
  private RemoteConfiguration remoteConfig = null;

  private RMAction newAction = null;
  private RMAction newUpgradeAction = null;

  private RMAction codesAction = null;

  /** The open item. */
  private RMAction openAction = null;

  /** The save item. */
  private RMAction saveAction = null;

  /** The save as item. */
  private RMAction saveAsAction = null;

  private RMAction openRdfAction = null;
  
  protected RMAction highlightAction = null;
  
  private JMenuItem rdfPathItem = null;
  
  private JMenuItem mapPathItem = null;

  /** The recent files. */
  private JMenu recentFiles = null;

  /** The exit item. */
  private JMenuItem exitItem = null;

  // Remote menu items
  /** The interfaces. */
  private ArrayList< IO > interfaces = new ArrayList< IO >();

  /** The download action. */
  private RMAction downloadAction = null;

  /** The upload action. */
  private RMAction uploadAction = null;

  /** The raw download item */
  private JMenuItem downloadRawItem = null;

  /** The verify upload item */
  private JCheckBoxMenuItem verifyUploadItem = null;

  /** The upload wav item. */
  private JMenuItem uploadWavItem = null;

  /** The look and feel items. */
  private JRadioButtonMenuItem[] lookAndFeelItems = null;
  
  private JCheckBoxMenuItem highlightItem = null;

  // Help menu items
  private JMenuItem readmeItem = null;

  private JMenuItem tutorialItem = null;

  private JMenuItem homePageItem = null;

  private JMenuItem learnedSignalItem = null;

  private JMenuItem wikiItem = null;

  private JMenuItem forumItem = null;

  /** The about item. */
  private JMenuItem aboutItem = null;

  /** The tabbed pane. */
  private JTabbedPane tabbedPane = null;

  private RMPanel currentPanel = null;

  /** The general panel. */
  private GeneralPanel generalPanel = null;

  /** The key move panel. */
  private KeyMovePanel keyMovePanel = null;

  /** The macro panel. */
  private MacroPanel macroPanel = null;

  /** The special function panel. */
  private SpecialFunctionPanel specialFunctionPanel = null;

  private TimedMacroPanel timedMacroPanel = null;

  private FavScanPanel favScanPanel = null;

  /** The device panel. */
  private DeviceUpgradePanel devicePanel = null;

  /** The protocol panel. */
  private ProtocolUpgradePanel protocolPanel = null;

  /** The learned panel. */
  private LearnedSignalPanel learnedPanel = null;

  /** The raw data panel. */
  private RawDataPanel rawDataPanel = null;

  /** The adv progress bar. */
  private JProgressBar advProgressBar = null;

  /** The upgrade progress bar. */
  private JProgressBar upgradeProgressBar = null;
  private JProgressBar devUpgradeProgressBar = null;

  private JPanel upgradeProgressPanel = null;

  /** The learned progress bar. */
  private JProgressBar learnedProgressBar = null;

  private boolean hasInvalidCodes = false;

  private CodeSelectorDialog codeSelectorDialog = null;
  
  private JDialog colorDialog = null;

  public JDialog getColorDialog()
  {
    return colorDialog;
  }
  
  private JColorChooser colorChooser = null;

  public JColorChooser getColorChooser()
  {
    return colorChooser;
  }

  private TextFileViewer rdfViewer = null;

  public class Preview extends JPanel
  {
    Preview()
    {
      super();
      sample.setPreferredSize( new Dimension(90, 30) );
      sample.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
      JPanel p = new JPanel();
      p.add(  sample  );
      add( p );
      add( Box.createHorizontalStrut( 20 ) );
      ButtonGroup grp = new ButtonGroup();
      grp.add( devices );
      grp.add( protocols );
      devices.setSelected( true );
      devices.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( ActionEvent e )
        {
          if ( colorCol != 0 )
          {
            Color color = getInitialHighlight( devicePanel.table, 0 );
            colorChooser.setColor( color );
            sample.setBackground( color );
            colorCol = 0;
          }
        }
      } );
      protocols.addActionListener( new ActionListener()
      {
        @Override
        public void actionPerformed( ActionEvent e )
        {
          if ( colorCol != 1 )
          {
            Color color = getInitialHighlight( devicePanel.table, 1 );
            colorChooser.setColor( color );
            sample.setBackground( color );
            colorCol = 1;
          }
        }
      } );
      selectors.add( devices );
      selectors.add( protocols );
      add( selectors );
    }
    
    public void reset( boolean disableProtocol )
    {
      colorCol = 0;
      devices.setSelected( true );
      protocols.setEnabled( !disableProtocol );
    }
    
    public Color getColor()
    {
      return result;
    }
    
    public JPanel getSelectors()
    {
      return selectors;
    }

    private JPanel sample = new JPanel();
    private Color result = null;
    private JPanel selectors = new JPanel( new GridLayout( 2, 1 ) );
    private int colorCol = 0;
    private JRadioButton devices = new JRadioButton( "Device" );
    private JRadioButton protocols = new JRadioButton( "Protocol" );
  }
  
  protected class RMAction extends AbstractAction
  {
    public RMAction( String text, String action, ImageIcon icon, String description, Integer mnemonic )
    {
      super( text, icon );
      putValue( ACTION_COMMAND_KEY, action );
      putValue( SHORT_DESCRIPTION, description );
      putValue( MNEMONIC_KEY, mnemonic );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed( ActionEvent event )
    {
      try
      {
        String command = event.getActionCommand();
        if ( command.equals( "NEW" ) )
        {
          Remote remote = RMNewDialog.showDialog( RemoteMaster.this );
          remote.load();
          ProtocolManager.getProtocolManager().reset();
          remoteConfig = new RemoteConfiguration( remote, RemoteMaster.this );
          remoteConfig.initializeSetup();
          remoteConfig.updateImage();
          remoteConfig.setDateIndicator();
          remoteConfig.setSavedData();
          update();
          saveAction.setEnabled( false );
          saveAsAction.setEnabled( true );
          openRdfAction.setEnabled( true );
          uploadAction.setEnabled( !interfaces.isEmpty() );
        }
        else if ( command.equals( "NEWDEVICE" ) )
        {
          new KeyMapMaster( properties );
        }
        else if ( command.equals( "OPEN" ) )
        {
          openFile();
        }
        else if ( command.equals( "SAVE" ) )
        {
          boolean validConfiguration = updateUsage();
          if ( !allowSave( Remote.SetupValidation.WARN ) )
          {
            return;
          }
          if ( !validConfiguration )
          {
            String title = "Invalid Configuration";
            String message = "This configuration is not valid, but it can be saved and then\n" +
                             "re-loaded to give again this same invalid configuration.\n\n" +
                             "Do you wish to continue?";
            if ( JOptionPane.showConfirmDialog( RemoteMaster.this, message, title, 
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.NO_OPTION )
            {
              return;
            }
          }
          remoteConfig.save( file );
        }
        else if ( command.equals( "SAVEAS" ) )
        {
          if ( !allowSave( Remote.SetupValidation.WARN ) )
          {
            return;
          }
          saveAs();
        }
        else if ( command.equals( "DOWNLOAD" ) )
        {
          System.err.println( "Starting normal download" );
          IO io = getOpenInterface();
          if ( io == null )
          {
            JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
            return;
          }
          System.err.println( "Interface opened successfully" );

          // See comment in Hex.getRemoteSignature( short[] ) for why the line below was not safe
          // String sig = io.getRemoteSignature();
          short[] sigData = new short[ 10 ];
          int baseAddress = io.getRemoteEepromAddress();
          System.err.println( "Base address = $" + Integer.toHexString( baseAddress ).toUpperCase() );
          int count = io.readRemote( baseAddress, sigData );
          System.err.println( "Read first " + count + " bytes: " + Hex.toString( sigData ) );

          String sig = Hex.getRemoteSignature( sigData );

          Remote currentRemote = null;
          Remote remote = null;
          if ( remoteConfig != null )
          {
            currentRemote = remoteConfig.getRemote();
          }
          if ( currentRemote == null || !currentRemote.getSignature().equals( sig ) )
          {
            System.err.println( "Searching for RDF" );
            List< Remote > remotes = null;
            String sig2 = null;
            for ( int i = 0; i < 5; i++ )
            {
              sig2 = sig.substring( 0, sig.length() - i );
              remotes = RemoteManager.getRemoteManager().findRemoteBySignature( sig2 );
              if ( !remotes.isEmpty() )
              {
                break;
              }
            }
            sig = sig2;
            System.err.println( "Final signature sought = " + sig );
            if ( remotes.isEmpty() )
            {
              System.err.println( "No matching RDF found" );
              JOptionPane.showMessageDialog( RemoteMaster.this, "No RDF matches signature starting " + sig );
              io.closeRemote();
              return;
            }
            else if ( remotes.size() == 1 )
            {
              remote = remotes.get( 0 );
            }
            else
            {// ( remotes.length > 1 )
              int maxFixedData = 0;
              for ( Remote r : remotes )
              {
                r.load();
                for ( FixedData fixedData : r.getRawFixedData() )
                {
                  maxFixedData = Math.max( maxFixedData, fixedData.getAddress() + fixedData.getData().length );
                }
              }

              int eepromSize = io.getRemoteEepromSize();
              if ( eepromSize > 0 && maxFixedData > eepromSize )
              {
                maxFixedData = eepromSize;
              }
              short[] buffer = new short[ maxFixedData ];
              if ( maxFixedData > 0 )
              {
                io.readRemote( baseAddress, buffer );
              }
              Remote[] choices = FixedData.filter( remotes, buffer );
              if ( choices.length == 0 )
              {
                // None of the remotes match on fixed data, so offer whole list
                choices = remotes.toArray( choices );
              }
              if ( choices.length == 1 )
              {
                remote = remotes.get( 0 );
              }
              else
              {
                String message = "Please pick the best match to your remote from the following list:";
                Object rc = JOptionPane.showInputDialog( null, message, "Ambiguous Remote", JOptionPane.ERROR_MESSAGE,
                    null, choices, choices[ 0 ] );
                if ( rc == null )
                {
                  io.closeRemote();
                  return;
                }
                else
                {
                  remote = ( Remote )rc;
                }
              }
            }
            System.err.println( "Remote identified as: " + remote.getName() );
          }
          else
          {
            remote = currentRemote;
          }
          remote.load();
          remoteConfig = new RemoteConfiguration( remote, RemoteMaster.this );
          count = io.readRemote( remote.getBaseAddress(), remoteConfig.getData() );
          System.err.println( "Number of bytes read  = $" + Integer.toHexString( count ).toUpperCase() );
          io.closeRemote();
          System.err.println( "Ending normal download" );
          remoteConfig.parseData();
          remoteConfig.updateImage();
          saveAction.setEnabled( false );
          saveAsAction.setEnabled( true );
          openRdfAction.setEnabled( true );
          uploadAction.setEnabled( true );
          update();
        }
        else if ( command.equals( "UPLOAD" ) )
        {
          boolean validConfiguration = updateUsage();
          if ( !validConfiguration )
          {
            String title = "Invalid Configuration";
            String message = "This configuration is not valid.  It cannot be uploaded as it\n" +
                             "could cause the remote to crash.";
            JOptionPane.showMessageDialog( RemoteMaster.this, message, title, JOptionPane.WARNING_MESSAGE );
            return;
          }
          
          Remote remote = remoteConfig.getRemote();
          if ( !allowSave( remote.getSetupValidation() ) )
          {
            return;
          }
          IO io = getOpenInterface();
          if ( io == null )
          {
            JOptionPane.showMessageDialog( RemoteMaster.this, "No remotes found!" );
            return;
          }
          String sig = io.getRemoteSignature();
          if ( !sig.equals( remote.getSignature() ) )
          {
            Object[] options =
            {
                "Upload to the remote", "Cancel the upload"
            };
            int rc = JOptionPane
                .showOptionDialog(
                    RemoteMaster.this,
                    "The signature of the attached remote does not match the signature you are trying to upload.  The image\n"
                        + "you are trying to upload may not be compatible with attached remote, and uploading it may damage the\n"
                        + "remote.  Copying the contents of one remote to another is only safe when the remotes are identical.\n\n"
                        + "This message will be displayed when installing an extender in your remote, which is the only time it is\n"
                        + "safe to upload to a remote when the signatures do not match.\n\n"
                        + "How would you like to proceed?", "Upload Signature Mismatch", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, options, options[ 1 ] );
            if ( rc == 1 || rc == JOptionPane.CLOSED_OPTION )
            {
              io.closeRemote();
              return;
            }
          }

          AutoClockSet autoClockSet = remote.getAutoClockSet();
          short[] data = remoteConfig.getData();
          if ( autoClockSet != null )
          {
            autoClockSet.saveTimeBytes( data );
            autoClockSet.setTimeBytes( data );
            remoteConfig.updateCheckSums();
          }

          int rc = io.writeRemote( remote.getBaseAddress(), data );

          if ( rc != data.length )
          {
            io.closeRemote();
            JOptionPane.showMessageDialog( RemoteMaster.this, "writeRemote returned " + rc );
            return;
          }
          if ( verifyUploadItem.isSelected() )
          {
            short[] readBack = new short[ data.length ];
            rc = io.readRemote( remote.getBaseAddress(), readBack );
            io.closeRemote();
            if ( rc != data.length )
            {
              JOptionPane.showMessageDialog( RemoteMaster.this, "Upload verify failed: read back " + rc
                  + " byte, but expected " + data.length );

            }
            else if ( !Hex.equals( data, readBack ) )
            {
              JOptionPane.showMessageDialog( RemoteMaster.this,
                  "Upload verify failed: data read back doesn't match data written." );
            }
          }
          else
          {
            io.closeRemote();
            JOptionPane.showMessageDialog( RemoteMaster.this, "Upload complete!" );
          }
          if ( autoClockSet != null )
          {
            autoClockSet.restoreTimeBytes( data );
            remoteConfig.updateCheckSums();
          }
        }
        else if ( command == "OPENRDF" )
        {
          String title = "View/Edit RDF";
          rdfViewer = TextFileViewer.showFile( RemoteMaster.this, remoteConfig.getRemote().getFile(), title, false );
        }
        else if ( command == "OPENCODES" )
        {
          codeSelectorDialog = CodeSelectorDialog.showDialog( RemoteMaster.this );
          codeSelectorDialog.enableAssign( currentPanel == generalPanel );
        }
        else if ( command == "HIGHLIGHT" )
        {
          JP1Table table = null;
          JP1TableModel< ? > model = null;
          TableSorter sorter = null;
          if ( currentPanel instanceof RMTablePanel< ? > )
          { 
            RMTablePanel< ? > panel = ( RMTablePanel< ? > )currentPanel;
            table = panel.table;
            model = panel.model;
            sorter = panel.sorter;
          }
          else if ( currentPanel == generalPanel )
          {
            table = generalPanel.getActiveTable();
            model = ( JP1TableModel< ? > )table.getModel();
          }
          Color color = getInitialHighlight( table, 0 );
          Preview preview = ( Preview )colorChooser.getPreviewPanel();
          preview.reset( ( currentPanel == devicePanel ) && ( getInitialHighlight( table, 1 ) == null ) );
          preview.selectors.setVisible( currentPanel == devicePanel );
          colorChooser.setColor( color );
          colorDialog.pack();
          colorDialog.setVisible( true );
          color = preview.result;
          if ( table != null && color != null )
          {
            for ( int i : table.getSelectedRows() )
            {
              if ( currentPanel == keyMovePanel )
              {
                // Special case needed to handle attached keymoves
                model.setValueAt( color, sorter.modelIndex( i ), 9 );
              }
              else if ( currentPanel == devicePanel && preview.colorCol == 1 )
              {
                DeviceUpgrade du = devicePanel.getRowObject( i );
                if ( du.needsProtocolCode() )
                {
                  // Special case needed to handle consequential highlights
                  model.setValueAt( color, sorter.modelIndex( i ), model.getColumnCount() - 1 );
                }
              }
              else
              {
                Highlight rowObject = getTableRow( table, i );
                rowObject.setHighlight( color );
              }
            }
            model.fireTableDataChanged();
            model.propertyChangeSupport.firePropertyChange( "data", null, null );
            highlightAction.setEnabled( false );
          }
        }
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
    }
  }
  
  private Highlight getTableRow( JP1Table table, int row )
  {
    Object obj;
    if ( row == -1 )
    {
      return null;
    }
    if ( currentPanel instanceof RMTablePanel< ? > )
    {
      obj = ( ( RMTablePanel< ? > )currentPanel ).getRowObject( row );
    }
    else
    {
      obj = ( ( JP1TableModel< ? > )table.getModel() ).getRow( row );
    }
    if ( obj instanceof Highlight )
    {
      return ( Highlight )obj;
    }
    return null;
  }
  
  private Color getInitialHighlight( JP1Table table, int colorCol )
  {
    Color color = null;
    if ( table != null )
    {
      int[] rows = table.getSelectedRows();
      if ( rows.length > 0 &&  getTableRow( table, rows[ 0 ] ) != null )
      {
        if ( currentPanel == devicePanel && colorCol == 1 )
        {
          for ( int i : rows )
          {
            DeviceUpgrade du = devicePanel.getRowObject( i );
            if ( !du.needsProtocolCode() )
            {
              continue;
            }
            if ( color == null )
            {
              color = du.getProtocolHighlight();
            }
            else if ( !du.getProtocolHighlight().equals( color ) )
            {
              return Color.WHITE;
            }
          }
        }
        else
        {
          color = getTableRow( table, rows[ 0 ] ).getHighlight();
          for ( int i : rows )
          {
            if ( !getTableRow( table, i ).getHighlight().equals( color ) )
            {
              return Color.WHITE;
            }
          }
        }
      }
    }
    return color;
  }

  /**
   * Constructor for the RemoteMaster object.
   * 
   * @param workDir
   *          the work dir
   * @param prefs
   *          the prefs
   * @throws Exception
   *           the exception
   * @exception Exception
   *              Description of the Exception
   */
  public RemoteMaster( File workDir, PropertyFile prefs ) throws Exception
  {
    super( "RM IR", prefs );
    dir = properties.getFileProperty( "IRPath", workDir );

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable( false );

    createMenus( toolBar );

    setDefaultCloseOperation( DISPOSE_ON_CLOSE );
    setDefaultLookAndFeelDecorated( true );

    final Preview preview = new Preview();
    // If a non-empty border is not set then the preview panel does not appear.  This sets
    // an invisible but non-empty border.
    preview.setBorder( BorderFactory.createLineBorder( preview.getBackground() ) );
    
    colorChooser = new JColorChooser();
    colorChooser.setPreviewPanel( preview );
    colorChooser.getSelectionModel().addChangeListener( new ChangeListener() 
    {
      @Override
      public void stateChanged( ChangeEvent evt ) 
      {
        ColorSelectionModel model = ( ColorSelectionModel ) evt.getSource();
        preview.sample.setBackground( model.getSelectedColor() );
      }
    } );

    colorDialog = JColorChooser.createDialog( this, "Highlight Color", true, colorChooser, 
    new ActionListener() 
    { // OK button listener
      @Override
      public void actionPerformed(ActionEvent event) 
      {
        preview.result = colorChooser.getColor();
      } 
    },  
    new ActionListener() 
    { // Cancel button listener
      @Override
      public void actionPerformed(ActionEvent event)
      {
        preview.result = null;
      } 
    } );

    addWindowListener( new WindowAdapter()
    {
      @Override
      public void windowClosing( WindowEvent event )
      {
        try
        {
          for ( int i = 0; i < recentFiles.getItemCount(); ++i )
          {
            JMenuItem item = recentFiles.getItem( i );
            properties.setProperty( "RecentIRs." + i, item.getActionCommand() );
          }
          int state = getExtendedState();
          if ( state != Frame.NORMAL )
          {
            setExtendedState( Frame.NORMAL );
          }
          Rectangle bounds = getBounds();
          properties
              .setProperty( "RMBounds", "" + bounds.x + ',' + bounds.y + ',' + bounds.width + ',' + bounds.height );

          properties.save();

          if ( generalPanel.getDeviceUpgradeEditor() != null )
          {
            generalPanel.getDeviceUpgradeEditor().dispose();
          }
          if ( devicePanel.getDeviceUpgradeEditor() != null )
          {
            devicePanel.getDeviceUpgradeEditor().dispose();
          }

        }
        catch ( Exception exc )
        {
          exc.printStackTrace( System.err );
        }
      }
    } );

    Container mainPanel = getContentPane();

    mainPanel.add( toolBar, BorderLayout.PAGE_START );

    // Set color for text on Progress Bars
    UIManager.put( "ProgressBar.selectionBackground", new javax.swing.plaf.ColorUIResource( Color.BLUE ) );
    UIManager.put( "ProgressBar.selectionForeground", new javax.swing.plaf.ColorUIResource( Color.BLUE ) );
    UIManager.put( "ProgressBar.foreground", new javax.swing.plaf.ColorUIResource( AQUAMARINE ) );

    tabbedPane = new JTabbedPane();
    mainPanel.add( tabbedPane, BorderLayout.CENTER );

    generalPanel = new GeneralPanel();
    tabbedPane.addTab( "General", generalPanel );
    generalPanel.addPropertyChangeListener( this );

    keyMovePanel = new KeyMovePanel();
    tabbedPane.addTab( "Key Moves", keyMovePanel );
    keyMovePanel.addPropertyChangeListener( this );

    macroPanel = new MacroPanel();
    tabbedPane.addTab( "Macros", macroPanel );
    macroPanel.addPropertyChangeListener( this );

    specialFunctionPanel = new SpecialFunctionPanel();
    tabbedPane.add( "Special Functions", specialFunctionPanel );
    specialFunctionPanel.addPropertyChangeListener( this );

    timedMacroPanel = new TimedMacroPanel();
    tabbedPane.add( "Timed Macros", timedMacroPanel );
    timedMacroPanel.addPropertyChangeListener( this );

    favScanPanel = new FavScanPanel();
    tabbedPane.addTab( "Fav/Scan", favScanPanel );
    favScanPanel.addPropertyChangeListener( this );

    devicePanel = new DeviceUpgradePanel();
    tabbedPane.addTab( "Devices", devicePanel );
    devicePanel.addPropertyChangeListener( this );

    protocolPanel = new ProtocolUpgradePanel();
    tabbedPane.addTab( "Protocols", protocolPanel );
    protocolPanel.addPropertyChangeListener( this );

    try
    {
      LearnedSignal.getDecodeIR();
      learnedPanel = new LearnedSignalPanel();
      tabbedPane.addTab( "Learned Signals", learnedPanel );
      learnedPanel.addPropertyChangeListener( this );
    }
    catch ( NoClassDefFoundError ncdfe )
    {
      System.err.println( "DecodeIR class not found!" );
    }
    catch ( NoSuchMethodError nsme )
    {
      System.err.println( "DecodeIR class is wrong version!" );
    }
    catch ( UnsatisfiedLinkError ule )
    {
      System.err.println( "DecodeIR JNI interface not found!" );
    }

    rawDataPanel = new RawDataPanel();
    tabbedPane.addTab( "Raw Data", rawDataPanel );
    rawDataPanel.addPropertyChangeListener( this );

    tabbedPane.addChangeListener( this );

    JPanel statusBar = new JPanel();

    mainPanel.add( statusBar, BorderLayout.SOUTH );

    statusBar.add( new JLabel( "Move/Macro:" ) );

    advProgressBar = new JProgressBar();
    advProgressBar.setStringPainted( true );
    advProgressBar.setString( "N/A" );
    statusBar.add( advProgressBar );

    statusBar.add( Box.createHorizontalStrut( 5 ) );
    JSeparator sep = new JSeparator( SwingConstants.VERTICAL );
    Dimension d = sep.getPreferredSize();
    d.height = advProgressBar.getPreferredSize().height;
    sep.setPreferredSize( d );
    statusBar.add( sep );

    statusBar.add( new JLabel( "Upgrade:" ) );

    upgradeProgressBar = new JProgressBar();
    upgradeProgressBar.setStringPainted( true );
    upgradeProgressBar.setString( "N/A" );

    upgradeProgressPanel = new JPanel();
    upgradeProgressPanel.setPreferredSize( advProgressBar.getPreferredSize() );
    upgradeProgressPanel.setLayout( new BorderLayout() );

    devUpgradeProgressBar = new JProgressBar();
    devUpgradeProgressBar.setStringPainted( true );
    devUpgradeProgressBar.setString( "N/A" );
    devUpgradeProgressBar.setVisible( false );

    upgradeProgressPanel.add( upgradeProgressBar, BorderLayout.NORTH );
    upgradeProgressPanel.add( devUpgradeProgressBar, BorderLayout.SOUTH );

    statusBar.add( upgradeProgressPanel );

    statusBar.add( Box.createHorizontalStrut( 5 ) );
    sep = new JSeparator( SwingConstants.VERTICAL );
    sep.setPreferredSize( d );
    statusBar.add( sep );

    statusBar.add( new JLabel( "Learned:" ) );

    learnedProgressBar = new JProgressBar();
    learnedProgressBar.setStringPainted( true );
    learnedProgressBar.setString( "N/A" );
    statusBar.add( learnedProgressBar );

    String temp = properties.getProperty( "RMBounds" );
    if ( temp != null )
    {
      Rectangle bounds = new Rectangle();
      StringTokenizer st = new StringTokenizer( temp, "," );
      bounds.x = Integer.parseInt( st.nextToken() );
      bounds.y = Integer.parseInt( st.nextToken() );
      bounds.width = Integer.parseInt( st.nextToken() );
      bounds.height = Integer.parseInt( st.nextToken() );
      setBounds( bounds );
    }
    else
    {
      pack();
    }
    currentPanel = generalPanel;
    setVisible( true );
  }

  /**
   * Gets the frame attribute of the RemoteMaster class.
   * 
   * @return The frame value
   */
  public static JP1Frame getFrame()
  {
    return frame;
  }

  public static ImageIcon createIcon( String imageName )
  {
    String imgLocation = "toolbarButtonGraphics/general/" + imageName + ".gif";
    URLClassLoader sysloader = ( URLClassLoader )ClassLoader.getSystemClassLoader();

    java.net.URL imageURL = sysloader.getResource( imgLocation );

    if ( imageURL == null )
    {
      System.err.println( "Resource not found: " + imgLocation );
      return null;
    }
    else
    {
      return new ImageIcon( imageURL );
    }
  }

  /**
   * Description of the Method.
   */
  private void createMenus( JToolBar toolBar )
  {
    JMenuBar menuBar = new JMenuBar();
    setJMenuBar( menuBar );

    JMenu menu = new JMenu( "File" );
    menu.setMnemonic( KeyEvent.VK_F );
    menuBar.add( menu );

    JMenu newMenu = new JMenu( "New" );
    newMenu.setMnemonic( KeyEvent.VK_N );
    menu.add( newMenu );

    newAction = new RMAction( "Remote Image...", "NEW", createIcon( "RMNew24" ), "Create new file", KeyEvent.VK_R );
    newMenu.add( newAction ).setIcon( null );
    toolBar.add( newAction );

    newUpgradeAction = new RMAction( "Device Upgrade", "NEWDEVICE", null, "Create new Device Upgrade", KeyEvent.VK_D );
    newMenu.add( newUpgradeAction );

    openAction = new RMAction( "Open...", "OPEN", createIcon( "RMOpen24" ), "Open a file", KeyEvent.VK_O );
    menu.add( openAction ).setIcon( null );
    toolBar.add( openAction );

    saveAction = new RMAction( "Save", "SAVE", createIcon( "Save24" ), "Save to file", KeyEvent.VK_S );
    saveAction.setEnabled( false );
    menu.add( saveAction ).setIcon( null );
    toolBar.add( saveAction );

    saveAsAction = new RMAction( "Save as...", "SAVEAS", createIcon( "SaveAs24" ), "Save to a different file",
        KeyEvent.VK_A );
    saveAsAction.setEnabled( false );
    JMenuItem menuItem = menu.add( saveAsAction );
    menuItem.setDisplayedMnemonicIndex( 5 );
    menuItem.setIcon( null );
    toolBar.add( saveAsAction );

    // revertItem = new JMenuItem( "Revert to saved" );
    // revertItem.setMnemonic( KeyEvent.VK_R );
    // revertItem.addActionListener( this );
    // menu.add( revertItem );

    menu.addSeparator();
    
    JMenu menuSetDirectory = new JMenu( "Set Directory" );
    menuSetDirectory.setMnemonic( KeyEvent.VK_D );
    menu.add( menuSetDirectory );
    
    rdfPathItem = new JMenuItem( "RDF Path..." );
    rdfPathItem.setMnemonic( KeyEvent.VK_R );
    rdfPathItem.addActionListener( this );
    menuSetDirectory.add( rdfPathItem );
    
    mapPathItem = new JMenuItem( "Image Path..." );
    mapPathItem.setMnemonic( KeyEvent.VK_I );
    mapPathItem.addActionListener( this );
    menuSetDirectory.add( mapPathItem );
    
    menu.addSeparator();
    toolBar.addSeparator();

    recentFiles = new JMenu( "Recent" );
    menu.add( recentFiles );
    recentFiles.setEnabled( false );
    for ( int i = 0; i < 10; i++ )
    {
      String propName = "RecentIRs." + i;
      String temp = properties.getProperty( propName );
      if ( temp == null )
      {
        break;
      }
      properties.remove( propName );
      File f = new File( temp );
      if ( f.canRead() )
      {
        JMenuItem item = new JMenuItem( temp );
        item.setActionCommand( temp );
        item.addActionListener( this );
        recentFiles.add( item );
      }
    }
    if ( recentFiles.getItemCount() > 0 )
    {
      recentFiles.setEnabled( true );
    }
    menu.addSeparator();

    exitItem = new JMenuItem( "Exit", KeyEvent.VK_X );
    exitItem.addActionListener( this );
    menu.add( exitItem );

    menu = new JMenu( "Remote" );
    menu.setMnemonic( KeyEvent.VK_R );
    menuBar.add( menu );

    File userDir = new File( System.getProperty( "user.dir" ) );
    try
    {
      JP1Parallel jp1Parallel = new JP1Parallel( userDir );
      interfaces.add( jp1Parallel );
      System.err.println( "    JP1Parallel version " + jp1Parallel.getInterfaceVersion() );
      System.err.println( "    EEPROM size returns " + jp1Parallel.getRemoteEepromSize() );
      System.err.println( "    EEPROM address returns " + jp1Parallel.getRemoteEepromAddress() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP1Parallel object: " + le.getMessage() );
    }

    try
    {
      JP12Serial jp12Serial = new JP12Serial( userDir );
      interfaces.add( jp12Serial );
      System.err.println( "    JP12Serial version " + jp12Serial.getInterfaceVersion() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP12Serial object: " + le.getMessage() );
    }

    try
    {
      JP1USB jp1usb = new JP1USB( userDir );
      interfaces.add( jp1usb );
      System.err.println( "    JP1USB version " + jp1usb.getInterfaceVersion() );
      System.err.println( "    EEPROM size returns " + jp1usb.getRemoteEepromSize() );
      System.err.println( "    EEPROM address returns " + jp1usb.getRemoteEepromAddress() );
    }
    catch ( LinkageError le )
    {
      System.err.println( "Unable to create JP1USB object: " + le.getMessage() );
    }

    ActionListener interfaceListener = new ActionListener()
    {
      public void actionPerformed( ActionEvent event )
      {
        String command = event.getActionCommand();
        if ( command.equals( "autodetect" ) )
        {
          properties.remove( "Interface" );
          properties.remove( "Port" );
          return;
        }

        for ( IO io : interfaces )
        {
          if ( io.getInterfaceName().equals( command ) )
          {
            String defaultPort = null;
            if ( command.equals( properties.getProperty( "Interface" ) ) )
            {
              defaultPort = properties.getProperty( "Port" );
            }

            String[] availablePorts = io.getPortNames();

            PortDialog d = new PortDialog( RemoteMaster.this, availablePorts, defaultPort );
            d.setVisible( true );
            if ( d.getUserAction() == JOptionPane.OK_OPTION )
            {
              String port = d.getPort();
              properties.setProperty( "Interface", io.getInterfaceName() );
              if ( port == null || port.equals( PortDialog.AUTODETECT ) )
              {
                properties.remove( "Port" );
              }
              else
              {
                properties.setProperty( "Port", port );
              }
            }

            break;
          }
        }
      }
    };

    if ( !interfaces.isEmpty() )
    {
      JMenu subMenu = new JMenu( "Interface" );
      menu.add( subMenu );
      subMenu.setMnemonic( KeyEvent.VK_I );
      ButtonGroup group = new ButtonGroup();
      String preferredInterface = properties.getProperty( "Interface" );
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( "Auto-detect" );
      item.setActionCommand( "autodetect" );
      item.setSelected( preferredInterface == null );
      subMenu.add( item );
      group.add( item );
      item.setMnemonic( KeyEvent.VK_A );
      item.addActionListener( interfaceListener );

      ListIterator< IO > it = interfaces.listIterator();
      while ( it.hasNext() )
      {
        IO io = it.next();
        try
        {
          String ioName = io.getInterfaceName();
          item = new JRadioButtonMenuItem( ioName + "..." );
          item.setActionCommand( ioName );
          item.setSelected( ioName.equals( preferredInterface ) );
          subMenu.add( item );
          group.add( item );
          item.addActionListener( interfaceListener );
        }
        catch ( UnsatisfiedLinkError ule )
        {
          it.remove();
          String className = io.getClass().getName();
          int dot = className.lastIndexOf( '.' );
          if ( dot != -1 )
          {
            className = className.substring( dot + 1 );
          }
          JOptionPane.showMessageDialog( this, "An incompatible version of the " + className
              + " driver was detected.  You will not be able to download or upload using that driver.",
              "Incompatible Driver", JOptionPane.ERROR_MESSAGE );
          ule.printStackTrace( System.err );
        }
      }
    }

    downloadAction = new RMAction( "Download from Remote", "DOWNLOAD", createIcon( "Import24" ),
        "Download from the attached remote", KeyEvent.VK_D );
    downloadAction.setEnabled( !interfaces.isEmpty() );
    menu.add( downloadAction ).setIcon( null );
    toolBar.add( downloadAction );

    uploadAction = new RMAction( "Upload to Remote", "UPLOAD", createIcon( "Export24" ),
        "Upload to the attached remote", KeyEvent.VK_U );
    uploadAction.setEnabled( false );
    menu.add( uploadAction ).setIcon( null );
    toolBar.add( uploadAction );

    toolBar.addSeparator();
    openRdfAction = new RMAction( "Open RDF...", "OPENRDF", createIcon( "RMOpenRDF24" ), "Open RDF to view or edit",
        null );
    openRdfAction.setEnabled( false );
    toolBar.add( openRdfAction );

    codesAction = new RMAction( "Code Selector...", "OPENCODES", createIcon( "RMCodes24" ), "Open Code Selector", null );
    codesAction.setEnabled( false );
    toolBar.add( codesAction );

    highlightAction = new RMAction( "Highlight...", "HIGHLIGHT", createIcon( "RMHighlight24" ), "Select highlight color", null );
    highlightAction.setEnabled( false );
    toolBar.add( highlightAction );
    
    uploadWavItem = new JMenuItem( "Create WAV", KeyEvent.VK_W );
    uploadWavItem.setEnabled( false );
    uploadWavItem.addActionListener( this );
    menu.add( uploadWavItem );

    menu.addSeparator();
    downloadRawItem = new JMenuItem( "Raw download", KeyEvent.VK_R );
    downloadRawItem.setEnabled( true );
    downloadRawItem.addActionListener( this );
    menu.add( downloadRawItem );

    menu.addSeparator();
    verifyUploadItem = new JCheckBoxMenuItem( "Verify after upload" );
    verifyUploadItem.setMnemonic( KeyEvent.VK_V );
    verifyUploadItem.setSelected( Boolean.parseBoolean( properties.getProperty( "verifyUpload", "true" ) ) );
    verifyUploadItem.addActionListener( this );
    menu.add( verifyUploadItem );

    menu = new JMenu( "Options" );
    menu.setMnemonic( KeyEvent.VK_O );
    menuBar.add( menu );

    JMenu subMenu = new JMenu( "Look and Feel" );
    subMenu.setMnemonic( KeyEvent.VK_L );
    menu.add( subMenu );
    
    highlightItem = new JCheckBoxMenuItem( "Highlighting" );
    highlightItem.setMnemonic( KeyEvent.VK_H );
    highlightItem.setSelected( Boolean.parseBoolean( properties.getProperty( "highlighting", "false" ) ) );
    highlightItem.addActionListener( this );
    menu.add( highlightItem );

    ActionListener al = new ActionListener()
    {
      public void actionPerformed( ActionEvent e )
      {
        try
        {
          JRadioButtonMenuItem item = ( JRadioButtonMenuItem )e.getSource();
          String lf = item.getActionCommand();
          UIManager.setLookAndFeel( lf );
          SwingUtilities.updateComponentTreeUI( RemoteMaster.this );
          RemoteMaster.this.pack();
          properties.setProperty( "LookAndFeel", lf );
        }
        catch ( Exception x )
        {
          x.printStackTrace( System.err );
        }
      }
    };

    ButtonGroup group = new ButtonGroup();
    String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
    lookAndFeelItems = new JRadioButtonMenuItem[ info.length ];
    for ( int i = 0; i < info.length; i++ )
    {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem( info[ i ].getName() );
      lookAndFeelItems[ i ] = item;
      item.setMnemonic( item.getText().charAt( 0 ) );
      item.setActionCommand( info[ i ].getClassName() );
      group.add( item );
      subMenu.add( item );
      if ( item.getActionCommand().equals( lookAndFeel ) )
      {
        item.setSelected( true );
      }
      item.addActionListener( al );
    }

    menu = new JMenu( "Help" );
    menu.setMnemonic( KeyEvent.VK_H );
    menuBar.add( menu );

    if ( desktop != null )
    {
      readmeItem = new JMenuItem( "Readme", KeyEvent.VK_R );
      readmeItem.addActionListener( this );
      menu.add( readmeItem );

      tutorialItem = new JMenuItem( "Tutorial", KeyEvent.VK_T );
      tutorialItem.addActionListener( this );
      menu.add( tutorialItem );

      learnedSignalItem = new JMenuItem( "Interpreting Decoded IR Signals", KeyEvent.VK_I );
      learnedSignalItem.addActionListener( this );
      menu.add( learnedSignalItem );

      menu.addSeparator();

      homePageItem = new JMenuItem( "Home Page", KeyEvent.VK_H );
      homePageItem.addActionListener( this );
      menu.add( homePageItem );

      wikiItem = new JMenuItem( "Wiki", KeyEvent.VK_W );
      wikiItem.addActionListener( this );
      menu.add( wikiItem );

      forumItem = new JMenuItem( "Forums", KeyEvent.VK_F );
      forumItem.addActionListener( this );
      menu.add( forumItem );

      menu.addSeparator();
    }

    aboutItem = new JMenuItem( "About...", KeyEvent.VK_A );
    aboutItem.addActionListener( this );
    menu.add( aboutItem );
  }

  /**
   * Gets the fileChooser attribute of the RemoteMaster object.
   * 
   * @return The fileChooser value
   */
  public RMFileChooser getFileChooser()
  {
    RMFileChooser chooser = new RMFileChooser( dir );
    EndingFileFilter irFilter = new EndingFileFilter( "All supported files", allEndings );
    chooser.addChoosableFileFilter( irFilter );
    chooser.addChoosableFileFilter( new EndingFileFilter( "RM IR files (*.rmir)", rmirEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "IR files (*.ir)", irEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "RM Device Upgrades (*.rmdu)", rmduEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "KM Device Upgrades (*.txt)", txtEndings ) );
    chooser.addChoosableFileFilter( new EndingFileFilter( "Sling Learned Signals (*.xml)", slingEndings ) );
    chooser.setFileFilter( irFilter );

    return chooser;
  }

  public RMFileChooser getFileSaveChooser( boolean validConfiguration )
  {
    RMFileChooser chooser = new RMFileChooser( dir );
    chooser.setAcceptAllFileFilterUsed( false );
    EndingFileFilter rmirFilter = new EndingFileFilter( "RM Remote Image (*.rmir)", rmirEndings );
    if ( validConfiguration )
    {
      chooser.addChoosableFileFilter( new EndingFileFilter( "IR file (*.ir)", irEndings ) );
    }
    chooser.setFileFilter( rmirFilter );
    return chooser;
  }
  
  private File getRDFPathChoice()
  {
    File result = null;
    File dir = properties.getFileProperty( "RDFPath" );
    if ( dir == null )
    {
      dir = new File( System.getProperty( "user.dir" ), "RDF" );
    }
    while ( !dir.exists() || !dir.isDirectory() )
    {
      dir = dir.getParentFile();
    }
    
    JDirectoryChooser chooser = new JDirectoryChooser( dir )
    { 
      FilenameFilter filter = new FilenameFilter()
      {
        @Override
        public boolean accept( File dir, String name )
        {
          int dot = name.lastIndexOf( '.' );
          if ( dot < 0 )
          {
            return false;
          }
          return name.substring( dot ).toLowerCase().equals( ".rdf" );
        }
      };
      
      @Override
      public void approveSelection() 
      {
        File[] files = getSelectedFile().listFiles( filter );
        if ( files.length == 0 )
        { 
          JOptionPane.showMessageDialog( null, 
              "There are no RDF files in this directory.  Please choose another.",
              "Error", JOptionPane.ERROR_MESSAGE );
          return; 
        } 
        else
        {
          super.approveSelection();
        }
      } 
    }; 

    chooser.setDialogTitle( "Select Directory for RDF Files" );
    if ( chooser.showDialog( this, "OK" ) == JDirectoryChooser.APPROVE_OPTION )
    {
      result = chooser.getSelectedFile();
    }
    return result;
  }
  
  private File getMapPathChoice()
  {
    File result = null;
    File dir = properties.getFileProperty( "ImagePath" );
    if ( dir == null )
    {
      dir = new File( System.getProperty( "user.dir" ), "Images" );
    }
    while ( !dir.exists() || !dir.isDirectory() )
    {
      dir = dir.getParentFile();
    }
    
    JDirectoryChooser chooser = new JDirectoryChooser( dir )
    { 
      FilenameFilter filter = new FilenameFilter()
      {
        @Override
        public boolean accept( File dir, String name )
        {
          int dot = name.lastIndexOf( '.' );
          if ( dot < 0 )
          {
            return false;
          }
          return name.substring( dot ).toLowerCase().equals( ".map" );
        }
      };
      
      @Override
      public void approveSelection() 
      {
        File[] files = getSelectedFile().listFiles( filter );
        if ( files.length == 0 )
        { 
          JOptionPane.showMessageDialog( null, 
              "There are no Map and Image files in this directory.  Please choose another.",
              "Error", JOptionPane.ERROR_MESSAGE );
          return; 
        } 
        else
        {
          super.approveSelection();
        }
      } 
    }; 

    chooser.setDialogTitle( "Select Directory for Map Files" );
    if ( chooser.showDialog( this, "OK" ) == JDirectoryChooser.APPROVE_OPTION )
    {
      result = chooser.getSelectedFile();
    }
    return result;
  }
  
  /**
   * Description of the Method.
   * 
   * @return Description of the Return Value
   * @throws Exception
   *           the exception
   * @exception Exception
   *              Description of the Exception
   */
  public File openFile() throws Exception
  {
    return openFile( null );
  }

  /**
   * Description of the Method.
   * 
   * @param file
   *          the file
   * @return Description of the Return Value
   * @throws Exception
   *           the exception
   * @exception Exception
   *              Description of the Exception
   */
  public File openFile( File file ) throws Exception
  {
    while ( file == null )
    {
      RMFileChooser chooser = getFileChooser();
      int returnVal = chooser.showOpenDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        file = chooser.getSelectedFile();

        if ( !file.exists() )
        {
          JOptionPane.showMessageDialog( this, file.getName() + " doesn't exist.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
        else if ( file.isDirectory() )
        {
          JOptionPane.showMessageDialog( this, file.getName() + " is a directory.", "File doesn't exist.",
              JOptionPane.ERROR_MESSAGE );
        }
      }
      else
      {
        return null;
      }
    }

    System.err.println( "Opening " + file.getCanonicalPath() + ", last modified "
        + DateFormat.getInstance().format( new Date( file.lastModified() ) ) );

    String ext = file.getName().toLowerCase();
    int dot = ext.lastIndexOf( '.' );
    ext = ext.substring( dot );

    dir = file.getParentFile();
    properties.setProperty( "IRPath", dir );

    if ( ext.equals( ".rmdu" ) || ext.equals( ".rmir" ) )
    {
      updateRecentFiles( file );

    }

    if ( ext.equals( ".rmdu" ) || ext.equals( ".txt" ) )
    {
      KeyMapMaster km = new KeyMapMaster( properties );
      km.loadUpgrade( file );
      return null;
    }

    if ( ext.equals( ".xml" ) )
    {
      List< Remote > remotes = RemoteManager.getRemoteManager().findRemoteBySignature( "XMLLEARN" );
      if ( remotes.isEmpty() )
      {
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(),
            "The RDF for XML Slingbox Learns was not found.  Please place it in the RDF folder and try again.",
            "Missing RDF File", JOptionPane.ERROR_MESSAGE );
        return null;
      }
      Remote remote = remotes.get( 0 );
      remote.load();
      remoteConfig = new RemoteConfiguration( remote, this );
      remoteConfig.initializeSetup();
      remoteConfig.updateImage();
      remoteConfig.setDateIndicator();
      remoteConfig.setSavedData();
      SlingLearnParser.parse( file, remoteConfig );
      remoteConfig.updateImage();
      update();
      saveAction.setEnabled( false );
      saveAsAction.setEnabled( true );
      uploadAction.setEnabled( !interfaces.isEmpty() );
      openRdfAction.setEnabled( true );
      return null;
    }

    if ( ext.equals( ".rmir" ) )
    {
      saveAction.setEnabled( true );
      saveAsAction.setEnabled( true );
      openRdfAction.setEnabled( true );
    }
    else
    // ext.equals( ".ir" )
    {
      saveAction.setEnabled( false );
      saveAsAction.setEnabled( true );
      openRdfAction.setEnabled( true );
    }
    uploadAction.setEnabled( !interfaces.isEmpty() );
    remoteConfig = new RemoteConfiguration( file, this );
    update();
    setTitleFile( file );
    this.file = file;
    return file;
  }

  /**
   * Description of the Method.
   * 
   * @param file
   *          the file
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @exception IOException
   *              Description of the Exception
   */
  private void updateRecentFiles( File file ) throws IOException
  {
    JMenuItem item = null;
    String path = file.getCanonicalPath();
    for ( int i = 0; i < recentFiles.getItemCount(); ++i )
    {
      File temp = new File( recentFiles.getItem( i ).getText() );

      if ( temp.getCanonicalPath().equals( path ) )
      {
        item = recentFiles.getItem( i );
        recentFiles.remove( i );
        break;
      }
    }
    if ( item == null )
    {
      item = new JMenuItem( path );
      item.setActionCommand( path );
      item.addActionListener( this );
    }
    recentFiles.insert( item, 0 );
    while ( recentFiles.getItemCount() > 10 )
    {
      recentFiles.remove( 10 );
    }
    recentFiles.setEnabled( true );
    dir = file.getParentFile();
    properties.setProperty( "IRPath", dir );
  }

  /**
   * Description of the Method.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @exception IOException
   *              Description of the Exception
   */
  public void saveAs() throws IOException
  {
    boolean validConfiguration = updateUsage();
    if ( !validConfiguration )
    {
      String title = "Invalid Configuration";
      String message = "This configuration is not valid.  It can be saved as a .rmir file\n" +
                       "which can be re-loaded to give again this same invalid configuration,\n" +
                       "but it cannot be saved as a .ir file as it could cause the remote\n" +
                       "to crash if it were uploaded to it by another application.";
      JOptionPane.showMessageDialog( RemoteMaster.this, message, title, JOptionPane.WARNING_MESSAGE );
    }
    RMFileChooser chooser = getFileSaveChooser( validConfiguration );
    if ( file != null )
    {
      String name = file.getName().toLowerCase();
      if ( name.endsWith( ".ir" ) || name.endsWith( ".txt" ) )
      {
        int dot = name.lastIndexOf( '.' );
        name = name.substring( 0, dot ) + ".rmir";
        file = new File( name );
      }
      chooser.setSelectedFile( file );
    }
    int returnVal = chooser.showSaveDialog( this );
    if ( returnVal == RMFileChooser.APPROVE_OPTION )
    {
      String ending = ( ( EndingFileFilter )chooser.getFileFilter() ).getEndings()[ 0 ];
      String name = chooser.getSelectedFile().getAbsolutePath();
      if ( !name.toLowerCase().endsWith( ending ) )
      {
        if ( name.toLowerCase().endsWith( ".rmir" ) )
        {
          int dot = name.lastIndexOf( '.' );
          name = name.substring( 0, dot );
        }
        name = name + ending;
      }
      File newFile = new File( name );
      int rc = JOptionPane.YES_OPTION;
      if ( newFile.exists() )
      {
        rc = JOptionPane.showConfirmDialog( this, newFile.getName() + " already exists.  Do you want to replace it?",
            "Replace existing file?", JOptionPane.YES_NO_OPTION );
      }

      if ( rc != JOptionPane.YES_OPTION )
      {
        return;
      }

      dir = newFile.getParentFile();
      properties.setProperty( "IRPath", dir );

      file = newFile;
      if ( ending == irEndings[ 0 ] )
      {
        remoteConfig.exportIR( file );
      }
      else
      {
        remoteConfig.save( file );
        setTitleFile( file );
        updateRecentFiles( file );
        saveAction.setEnabled( true );
      }
      uploadAction.setEnabled( !interfaces.isEmpty() );
    }
  }

  /**
   * Sets the titleFile attribute of the RemoteMaster object.
   * 
   * @param file
   *          the file
   */
  private void setTitleFile( File file )
  {
    if ( file == null )
    {
      setTitle( "RM IR" );
    }
    else
    {
      setTitle( "RM IR: " + file.getName() + " - " + remoteConfig.getRemote().getName() );
    }
  }

  /**
   * Gets the open interface.
   * 
   * @return the open interface
   */
  public IO getOpenInterface()
  {
    String interfaceName = properties.getProperty( "Interface" );
    System.err.println( "Interface Name = " + ( interfaceName == null ? "NULL" : interfaceName ) );
    String portName = properties.getProperty( "Port" );
    System.err.println( "Port Name = " + ( portName == null ? "NULL" : portName ) );
    if ( interfaceName != null )
    {
      for ( IO temp : interfaces )
      {
        String tempName = temp.getInterfaceName();
        System.err.println( "Testing interface: " + ( tempName == null ? "NULL" : tempName ) );
        if ( tempName.equals( interfaceName ) )
        {
          System.err.println( "Interface matched.  Trying to open remote." );
          if ( temp.openRemote( portName ) != null )
          {
            System.err.println( "Opened" );
            return temp;
          }
          else
          {
            System.err.println( "Failed to open" );
          }
        }
      }
    }
    else
    {
      for ( IO temp : interfaces )
      {
        String tempName = temp.getInterfaceName();
        System.err.println( "Testing interface: " + ( tempName == null ? "NULL" : tempName ) );
        portName = temp.openRemote();
        System.err.println( "Port Name = " + ( portName == null ? "NULL" : portName ) );
        if ( portName != null )
        {
          return temp;
        }
      }
    }
    return null;
  }

  /**
   * Description of the Method.
   * 
   * @param e
   *          the e
   */
  public void actionPerformed( ActionEvent e )
  {
    try
    {
      Object source = e.getSource();
      if ( source == exitItem )
      {
        dispatchEvent( new WindowEvent( this, WindowEvent.WINDOW_CLOSING ) );
      }
      else if ( source == downloadRawItem )
      {
        RawDataDialog dlg = new RawDataDialog( this );
        dlg.setVisible( true );
      }
      else if ( source == verifyUploadItem )
      {
        properties.setProperty( "verifyUpload", Boolean.toString( verifyUploadItem.isSelected() ) );
      }
      else if ( source == highlightItem )
      {
        properties.setProperty( "highlighting", Boolean.toString( highlightItem.isSelected() ) );
      }
      else if ( source == rdfPathItem )
      {
        File path = getRDFPathChoice();
        if ( path == null )
        {
          return;
        }
        
        int opt = JOptionPane.NO_OPTION;
        if ( remoteConfig != null )
        {
          String message = "Do you want to apply this directory change immediately?\n\n" +
          "Yes = the present setup will be reinterpreted with an RDF from the new directory;\n" +
          "No = the change will take place when you next open a remote, even within this session;\n" +
          "Cancel = the change will be cancelled.\n\n" +
          "Note that if you answer Yes, the setup will still have been loaded with the old RDF.\n" +
          "You can achieve a similar result by answering No, using File/Save As to save the setup\n" +
          "with the old RDF and then opening the saved file, which will open with the new RDF.\n" +
          "The best choice between these two methods can depend on how different the RDFs are,\n" +
          "and what you are trying to achieve.";

          String title = "Change of RDF Directory";
          opt = JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
        }
        if ( opt == JOptionPane.CANCEL_OPTION )
        {
          return;
        }
        properties.setProperty( "RDFPath", path );
        RemoteManager mgr = RemoteManager.getRemoteManager();
        mgr.reset();
        mgr.loadRemotes( properties );
        if ( opt == JOptionPane.NO_OPTION )
        {
          return;
        }
        String rmTitle = getTitle();
        remoteConfig.setSavedData();
        Remote oldRemote = remoteConfig.getRemote();
        Remote newRemote = RemoteManager.getRemoteManager().findRemoteByName( oldRemote.getName() );
        remoteConfig.setRemote( newRemote );
        SetupCode.setMax( newRemote.usesTwoBytePID() ? 4095 : 2047 );
        remoteConfig.updateImage();
        RemoteConfiguration.resetDialogs();
        update();
        int index = rmTitle.lastIndexOf( oldRemote.getName() );
        setTitle( rmTitle.substring( 0, index ) + newRemote.getName() );
      }
      else if ( source == mapPathItem )
      {
        File path = getMapPathChoice();
        if ( path == null )
        {
          return;
        }
        int opt = JOptionPane.NO_OPTION;
        if ( remoteConfig != null )
        {
          String message = "Do you want to apply this directory change immediately?\n\n" +
          "Yes = a map and image from the new directory will be used in the present setup;\n" +
          "No = the change will take place when you next open a remote, even within this session;\n" +
          "Cancel = the change will be cancelled.\n\n";

          String title = "Change of Map and Image Directory";
          opt = JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
        }
        if ( opt == JOptionPane.CANCEL_OPTION )
        {
          return;
        }
        properties.setProperty( "ImagePath", path );
        if ( remoteConfig == null )
        {
          return;
        }
        Remote remote = remoteConfig.getRemote();
        remote.resetImageMaps( path );
      }
      else if ( source == aboutItem )
      {
        StringBuilder sb = new StringBuilder( 1000 );
        sb.append( "<html><b>RemoteMaster " );
        sb.append( version );
        sb.append( "</b>" );
        sb.append( "<p>Written primarily by <i>Greg Bush</i> (now accepting donations at " );
        sb
            .append( "<a href=\"http://sourceforge.net/donate/index.php?user_id=735638\">http://sourceforge.net/donate/index.php?user_id=735638</a>)</p>" );
        sb.append( "<p>Other contributors include:<blockquote>" );
        sb
            .append( "Graham&nbsp;Dixon, John&nbsp;S&nbsp;Fine, Nils&nbsp;Ekberg, Jon&nbsp;Armstrong, Robert&nbsp;Crowe, " );
        sb.append( "Mark&nbsp;Pauker, Mark&nbsp;Pierson, Mike&nbsp;England</blockquote></p>" );

        sb.append( "<p>RDFs loaded from <b>" );
        sb.append( properties.getProperty( "RDFPath" ) );
        sb.append( "</b></p>" );

        sb.append( "</p><p>Images and Maps loaded from <b>" );
        sb.append( properties.getProperty( "ImagePath" ) );
        sb.append( "</b></p>" );
        try
        {
          String v = LearnedSignal.getDecodeIR().getVersion();
          sb.append( "<p>DecodeIR version " );
          sb.append( v );
          sb.append( "</p>" );
        }
        catch ( LinkageError le )
        {
          sb.append( "<p><b>DecodeIR is not available!</b></p>" );
        }

        if ( !interfaces.isEmpty() )
        {
          sb.append( "<p>Interfaces:<ul>" );
          for ( IO io : interfaces )
          {
            sb.append( "<li>" );
            sb.append( io.getInterfaceName() );
            sb.append( " version " );
            sb.append( io.getInterfaceVersion() );
            sb.append( "</li>" );
          }
          sb.append( "</ul></p>" );
        }

        String[] propertyNames =
        {
            "java.version", "java.vendor", "os.name", "os.arch"
        };

        sb.append( "<p>System Properties:<ul>" );
        for ( String name : propertyNames )
        {
          sb.append( "<li>" );
          sb.append( name );
          sb.append( " = \"" );
          sb.append( System.getProperty( name ) );
          sb.append( "\"</li>" );
        }
        sb.append( "</ul>" );

        sb.append( "<p>Libraries loaded from " );
        sb.append( LibraryLoader.getLibraryFolder() );
        sb.append( "</p></html>" );

        JEditorPane pane = new JEditorPane( "text/html", sb.toString() );
        pane.addHyperlinkListener( this );
        pane.setEditable( false );
        pane.setBackground( getContentPane().getBackground() );
        new TextPopupMenu( pane );
        JScrollPane scroll = new JScrollPane( pane );
        Dimension d = pane.getPreferredSize();
        d.height = d.height * 5 / 4;
        d.width = d.width * 2 / 3;
        scroll.setPreferredSize( d );

        JOptionPane.showMessageDialog( this, scroll, "About Java IR", JOptionPane.INFORMATION_MESSAGE, null );
      }
      else if ( source == readmeItem )
      {
        File readme = new File( "Readme.html" );
        desktop.browse( readme.toURI() );
      }
      else if ( source == tutorialItem )
      {
        File file = new File( "tutorial/tutorial.html" );
        desktop.browse( file.toURI() );
      }
      else if ( source == learnedSignalItem )
      {
        File file = new File( "DecodeIR.html" );
        desktop.browse( file.toURI() );
      }
      else if ( source == homePageItem )
      {
        URL url = new URL( "http://controlremote.sourceforge.net/" );
        desktop.browse( url.toURI() );
      }
      else if ( source == wikiItem )
      {
        URL url = new URL( "https://sourceforge.net/apps/mediawiki/controlremote/index.php?title=Main_Page" );
        desktop.browse( url.toURI() );
      }
      else if ( source == forumItem )
      {
        URL url = new URL( "http://www.hifi-remote.com/forums/" );
        desktop.browse( url.toURI() );
      }
      else
      {
        JMenuItem item = ( JMenuItem )source;
        File file = new File( item.getActionCommand() );
        recentFiles.remove( item );
        if ( file.canRead() )
        {
          openFile( file );
        }
      }
    }
    catch ( Exception ex )
    {
      ex.printStackTrace( System.err );
    }
  }

  /**
   * Description of the Method.
   */
  public void update()
  {
    if ( remoteConfig != null )
    {
      setTitle( "RM IR - " + remoteConfig.getRemote().getName() );
    }
    else
    {
      setTitle( "RM IR" );
    }

    Remote remote = remoteConfig.getRemote();

    generalPanel.set( remoteConfig );
    keyMovePanel.set( remoteConfig );
    macroPanel.set( remoteConfig );

    int index = getTabIndex( specialFunctionPanel );
    if ( remote.getSpecialProtocols().isEmpty() )
    {
      if ( index >= 0 )
      {
        tabbedPane.remove( index );
      }
    }
    else if ( index < 0 )
    {
      tabbedPane.insertTab( "Special Functions", null, specialFunctionPanel, null, getTabIndex( macroPanel ) + 1 );
    }

    index = getTabIndex( timedMacroPanel );
    if ( remote.hasTimedMacroSupport() )
    {
      if ( index < 0 )
      {
        index = getTabIndex( specialFunctionPanel );
        if ( index < 0 )
        {
          index = getTabIndex( macroPanel );
        }
        tabbedPane.insertTab( "Timed Macros", null, timedMacroPanel, null, index + 1 );
      }
    }
    else if ( index > 0 )
    {
      tabbedPane.remove( index );
    }

    index = getTabIndex( favScanPanel );
    if ( remote.hasFavKey() )
    {
      if ( index < 0 )
      {
        tabbedPane.insertTab( "Fav/Scan", null, favScanPanel, null, getTabIndex( devicePanel ) );
      }
    }
    else if ( index >= 0 )
    {
      tabbedPane.remove( index );
    }

    specialFunctionPanel.set( remoteConfig );
    timedMacroPanel.set( remoteConfig );
    favScanPanel.set( remoteConfig );

    devicePanel.set( remoteConfig );
    protocolPanel.set( remoteConfig );

    if ( learnedPanel != null )
    {
      learnedPanel.set( remoteConfig );
    }

    codesAction.setEnabled( remote.getSetupCodes().size() > 0 );
    if ( codeSelectorDialog != null )
    {
      if ( codeSelectorDialog.isDisplayable() )
      {
        codeSelectorDialog.dispose();
      }
      codeSelectorDialog = null;
    }

    if ( rdfViewer != null )
    {
      if ( rdfViewer.isDisplayable() )
      {
        rdfViewer.dispose();
      }
      rdfViewer = null;
    }

    updateUsage();

    rawDataPanel.set( remoteConfig );
  }

  private boolean updateUsage( JProgressBar bar, AddressRange range )
  {
    if ( range != null )
    {
      int used = range.getFreeStart() - range.getStart() + range.getEnd() - range.getFreeEnd();
      int available = range.getSize();
      bar.setMinimum( 0 );
      bar.setMaximum( available );
      bar.setValue( used );
      bar.setString( Integer.toString( available - used ) + " free" );
      if ( range == remoteConfig.getRemote().getDeviceUpgradeAddress() )
      {
        // Device Upgrade area is filled from top down, so freeEnd != end in normal use
        bar.setForeground( AQUAMARINE );
      }
      else
      {
        bar.setForeground( ( range.getFreeEnd() == range.getEnd() ) ? AQUAMARINE : Color.YELLOW );
      }

      return available >= used;
    }
    else
    {
      bar.setMinimum( 0 );
      bar.setMaximum( 0 );
      bar.setValue( 0 );
      bar.setString( "N/A" );
      bar.setForeground( AQUAMARINE );

      return true;
    }
  }

  /**
   * Updates the progress bars and returns a boolean specifying whether the configuration
   * is valid, i.e. whether all sections fit in their available space.
   */
  private boolean updateUsage()
  {
    boolean valid = true;
    Remote remote = remoteConfig.getRemote();
    Dimension d = advProgressBar.getPreferredSize();
    Font font = advProgressBar.getFont();
    if ( remote.getDeviceUpgradeAddress() == null )
    {
      upgradeProgressBar.setVisible( false );
      upgradeProgressBar.setPreferredSize( d );
      upgradeProgressBar.setFont( font );
      upgradeProgressBar.setVisible( true );
      devUpgradeProgressBar.setVisible( false );
    }
    else
    {
      d.height /= 2;
      Font font2 = font.deriveFont( ( float )font.getSize() * 0.75f );
      upgradeProgressBar.setVisible( false );
      upgradeProgressBar.setPreferredSize( d );
      upgradeProgressBar.setFont( font2 );
      upgradeProgressBar.setVisible( true );
      devUpgradeProgressBar.setVisible( false );
      devUpgradeProgressBar.setPreferredSize( d );
      devUpgradeProgressBar.setFont( font2 );
      devUpgradeProgressBar.setVisible( true );
    }

    String title = "Available Space Exceeded";
    String message = "";
    AddressRange range = remote.getAdvancedCodeAddress();
    if ( !updateUsage( advProgressBar, range ) )
    {
      valid = false;
      if ( range.getFreeEnd() == range.getEnd() )
      {
        message = "The defined advanced codes (keymoves, macros, special functions etc.) use more space than is available.  Please remove some.";
      }
      else
      {
        message = "There is insufficient space in the advanced codes section for both the defined\n" +
                  "advanced codes (keymoves, macros, special functions etc.) and the device\n" +
                  "upgrades that have overflowed from their own section.  Please remove some entries.";
      }
      showErrorMessage( message, title );
    }
    if ( !updateUsage( timedMacroPanel.timedMacroProgressBar, remote.getTimedMacroAddress() ) )
    {
      valid = false;
      message = "The defined timed macros use more space than is available.  Please remove some.";
      showErrorMessage( message, title );
    }
    if ( !updateUsage( upgradeProgressBar, remote.getUpgradeAddress() ) )
    {
      // Note that this section can only be full if there are no sections that can take overflow from it.
      // Otherwise, excessive device upgrades cause the overflow section, not this one, to be full.
      valid = false;
      message = "The defined device upgrades use more space than is available. Please remove some.";
      showErrorMessage( message, title );
    }
    if ( !updateUsage( devUpgradeProgressBar, remote.getDeviceUpgradeAddress() ) )
    {
      valid = false;
      message = "The defined button-dependent device upgrades use more space than is available. Please remove some.";
      showErrorMessage( message, title );
    }
    range = remote.getLearnedAddress();
    if ( !updateUsage( learnedProgressBar, range ) )
    {
      valid = false;
      if ( range.getFreeEnd() == range.getEnd() )
      {
        message = "The defined learned signals use more space than is available.  Please remove some.";
      }
      else
      {
        message = "There is insufficient space in the learned signals section for both the defined\n" +
                  "learned signals and the device upgrades that have overflowed from their own\n" +
                  "section.  Please remove some entries.";
      }
      showErrorMessage( message, title );
    }
    return valid;
  }
  
  private void showErrorMessage( String message, String title )
  {
    JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
  }

  /**
   * Description of the Method.
   * 
   * @param event
   *          the event
   */
  public void propertyChange( PropertyChangeEvent event )
  {
    // No need to check unassigned upgrades as keymoves are saved in .rmir file
    // remoteConfig.checkUnassignedUpgrades();
    if ( currentPanel == keyMovePanel )
    {
      ( ( KeyMoveTableModel )keyMovePanel.getModel() ).resetKeyMoves();
    }
    remoteConfig.updateImage();
    updateUsage();
    hasInvalidCodes = generalPanel.setWarning();
  }

  private boolean allowSave( Remote.SetupValidation setupValidation )
  {
    if ( !hasInvalidCodes )
    {
      return true;
    }
    String title = "Setup codes";
    if ( setupValidation == Remote.SetupValidation.WARN )
    {
      String message = "The current setup contains invalid device codes.\n" + "Are you sure you wish to continue?";
      return JOptionPane.showConfirmDialog( this, message, title, JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION;
    }
    else if ( setupValidation == Remote.SetupValidation.ENFORCE )
    {
      String message = "The current setup contains invalid device codes\n"
          + "which would cause this remote to malfunction.\n" + "Please correct these codes and try again.";
      JOptionPane.showMessageDialog( this, message, title, JOptionPane.ERROR_MESSAGE );
    }
    return false;
  }

  /**
   * Description of the Method.
   * 
   * @param args
   *          the args
   */
  private static void createAndShowGUI( ArrayList< String > args )
  {
    try
    {
      File workDir = new File( System.getProperty( "user.dir" ) );
      File propertiesFile = null;
      File fileToOpen = null;
      boolean launchRM = true;
      for ( int i = 0; i < args.size(); ++i )
      {
        String parm = args.get( i );
        if ( parm.equalsIgnoreCase( "-ir" ) )
        {
          launchRM = true;
        }
        else if ( parm.equalsIgnoreCase( "-rm" ) )
        {
          launchRM = false;
        }
        else if ( "-home".startsWith( parm ) )
        {
          String dirName = args.get( ++i );
          System.err.println( parm + " applies to \"" + dirName + '"' );
          workDir = new File( dirName );
          System.setProperty( "user.dir", workDir.getCanonicalPath() );
        }
        else if ( "-properties".startsWith( parm ) )
        {
          String fileName = args.get( ++i );
          System.err.println( "Properties file name is \"" + fileName + '"' );
          propertiesFile = new File( fileName );
        }
        else
        {
          fileToOpen = new File( parm );
        }
      }

      try
      {
        System.setErr( new PrintStream( new FileOutputStream( new File( workDir, "rmaster.err" ) ) ) );
      }
      catch ( Exception e )
      {
        e.printStackTrace( System.err );
      }

      System.err.println( "RemoteMaster " + RemoteMaster.version );
      String[] propertyNames =
      {
          "java.version", "java.vendor", "os.name", "os.arch"
      };

      System.err.println( "System Properties:" );
      for ( String name : propertyNames )
      {
        System.err.println( "   " + name + " = " + System.getProperty( name ) );
      }

      ClassPathAdder.addFile( workDir );

      FilenameFilter filter = new FilenameFilter()
      {
        public boolean accept( File dir, String name )
        {
          String temp = name.toLowerCase();
          return temp.endsWith( ".jar" ) && !temp.endsWith( "remotemaster.jar" ) && !temp.endsWith( "setup.jar" );
        }
      };

      File[] jarFiles = workDir.listFiles( filter );
      ClassPathAdder.addFiles( jarFiles );

      if ( propertiesFile == null )
      {
        File dir = workDir;
        propertiesFile = new File( dir, "RemoteMaster.properties" );
        if ( !propertiesFile.exists() )
        {
          if ( System.getProperty( "os.name" ).startsWith( "Windows" )
              && Float.parseFloat( System.getProperty( "os.version" ) ) >= 6.0f )
          {
            String baseFolderName = System.getenv( "APPDATA" );
            if ( baseFolderName == null || "".equals( baseFolderName ) )
            {
              baseFolderName = System.getProperty( "user.home" );
            }

            dir = new File( baseFolderName, "RemoteMaster" );
            if ( !dir.exists() )
            {
              dir.mkdirs();
            }
          }

          propertiesFile = new File( dir, "RemoteMaster.properties" );
        }
      }
      PropertyFile properties = new PropertyFile( propertiesFile );

      String lookAndFeel = properties.getProperty( "LookAndFeel", UIManager.getSystemLookAndFeelClassName() );
      try
      {
        UIManager.setLookAndFeel( lookAndFeel );
      }
      catch ( UnsupportedLookAndFeelException ulafe )
      {
        ulafe.printStackTrace( System.err );
      }

      RemoteManager.getRemoteManager().loadRemotes( properties );

      ProtocolManager.getProtocolManager().load( new File( workDir, "protocols.ini" ) );

      DigitMaps.load( new File( workDir, "digitmaps.bin" ) );

      if ( launchRM )
      {
        RemoteMaster rm = new RemoteMaster( workDir, properties );
        if ( fileToOpen != null )
        {
          rm.openFile( fileToOpen );
        }
        frame = rm;
      }
      else
      {
        KeyMapMaster km = new KeyMapMaster( properties );
        km.loadUpgrade( fileToOpen );
        frame = km;
      }
    }
    catch ( Exception e )
    {
      System.err.println( "Caught exception in RemoteMaster.main()!" );
      e.printStackTrace( System.err );
      System.err.flush();
      System.exit( 0 );
    }
    System.err.flush();
  }

  /**
   * The main program for the RemoteMaster class.
   * 
   * @param args
   *          the args
   */
  public static void main( String[] args )
  {
    JDialog.setDefaultLookAndFeelDecorated( true );
    JFrame.setDefaultLookAndFeelDecorated( true );
    Toolkit.getDefaultToolkit().setDynamicLayout( true );

    for ( String arg : args )
    {
      if ( "-version".startsWith( arg ) )
      {
        System.out.println( version );
        return;
      }
      else
      {
        parms.add( arg );
      }
    }
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run()
      {
        createAndShowGUI( parms );
      }
    } );
  }

  /** The parms. */
  private static ArrayList< String > parms = new ArrayList< String >();

  /** The Constant rmirEndings. */
  private final static String[] rmirEndings =
  {
    ".rmir"
  };

  /** The Constant rmduEndings. */
  private final static String[] rmduEndings =
  {
    ".rmdu"
  };

  /** The Constant irEndings. */
  private final static String[] irEndings =
  {
    ".ir"
  };

  /** The Constant txtEndings. */
  private final static String[] txtEndings =
  {
    ".txt"
  };

  private final static String[] slingEndings =
  {
    ".xml"
  };

  private final static String[] allEndings =
  {
      ".rmir", ".ir", ".rmdu", ".txt", ".xml"
  };

  @Override
  public void stateChanged( ChangeEvent event )
  {
    RMPanel newPanel = ( RMPanel )tabbedPane.getSelectedComponent();
    if ( newPanel != currentPanel )
    {
      newPanel.set( remoteConfig );
      currentPanel = newPanel;
      highlightAction.setEnabled( false );
    }
    if ( codeSelectorDialog != null )
    {
      codeSelectorDialog.enableAssign( currentPanel == generalPanel );
    }
  }

  private int getTabIndex( Component c )
  {
    for ( int i = 0; i < tabbedPane.getTabCount(); i++ )
    {
      if ( tabbedPane.getComponentAt( i ).equals( c ) )
      {
        return i;
      }
    }
    return -1;
  }

  public RemoteConfiguration getRemoteConfiguration()
  {
    return remoteConfig;
  }

  public GeneralPanel getGeneralPanel()
  {
    return generalPanel;
  }

  public DeviceUpgradePanel getDeviceUpgradePanel()
  {
    return devicePanel;
  }

}
