package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Document;

import com.hifiremote.jp1.assembler.CommonData;

// TODO: Auto-generated Javadoc
/**
 * The Class ManualSettingsDialog.
 */
public class ManualSettingsDialog extends JDialog implements ActionListener, PropertyChangeListener, DocumentListener,
    ChangeListener, ListSelectionListener, ItemListener
{

  /**
   * Instantiates a new manual settings dialog.
   * 
   * @param owner
   *          the owner
   * @param protocol
   *          the protocol
   */
  public ManualSettingsDialog( JDialog owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    createGui( owner, protocol );
  }

  /**
   * Instantiates a new manual settings dialog.
   * 
   * @param owner
   *          the owner
   * @param protocol
   *          the protocol
   */
  public ManualSettingsDialog( JFrame owner, ManualProtocol protocol )
  {
    super( owner, "Manual Settings", true );
    createGui( owner, protocol );
  }

  public class CodeCellRenderer extends DefaultTableCellRenderer
  {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int col ) 
    {
      Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
      CodeTableModel model = ( CodeTableModel )table.getModel();
      if ( isSelected )
      {
        c.setForeground( ( Boolean )model.getValueAt( row, 2 ) ? Color.YELLOW : Color.WHITE );
      }
      else
      {
        c.setForeground( ( Boolean )model.getValueAt( row, 2 ) ? Color.GRAY : Color.BLACK );
      }
      return c;
    }
  }
  
  private class DisplayArea extends JTextArea
  {
    public DisplayArea( String text, List< JTextArea > areas )
    {
      super( text );
      JLabel label = new JLabel();
      setLineWrap( true );
      setWrapStyleWord( true );
      setFont( label.getFont() );
      setBackground( label.getBackground() );
      setEditable( false );
      areas.add( this );
    }
  }
  
  /**
   * Creates the gui.
   * 
   * @param owner
   *          the owner
   * @param protocol
   *          the protocol
   */
  private void createGui( Component owner, ManualProtocol protocol )
  {
    setLocationRelativeTo( owner );
    Container contentPane = getContentPane();
    double scale = 0.75;
    
    JPanel leftPanel = new JPanel( new BorderLayout() );
    JPanel rightPanel = new JPanel( new BorderLayout() );
    rightPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

    leftPanel.addComponentListener( new ComponentAdapter() {
      public void componentResized(ComponentEvent e)
      { 
        setPFPanel();
        setPDPanel();
        setFunctionPanel();
      } 
    } );

    this.protocol = protocol;
    System.err.println( "protocol=" + protocol );

    double b = 5; // space between rows and around border
    double c = 10; // space between columns
    double pr = TableLayout.PREFERRED;
    double pf = TableLayout.FILL;
    double size1[][] =
    {
        {
            b, pr, c, pf, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b
        }  // rows
    };
    
    double size2[][] =
    {
        {
            b, pr, c, pf, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b, pr, b
        }  // rows
    };
    
    double size3[][] =
    {
        {
            b, pr, c, pf, b, pr, b
        },    // cols
        null  // rows set later
    };
    
    outerPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel );
    outerPane.setResizeWeight( 0 );
    contentPane.add( outerPane, BorderLayout.CENTER );
    
    JPanel mainPanel = new JPanel( new TableLayout( size1 ) );
    leftPanel.add( mainPanel, BorderLayout.PAGE_START );
    
    JLabel label = new JLabel( "Name:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 1" );
    name = new JTextField( protocol.getName() );
    name.setEditable( false );
    name.setEnabled( false );
    name.getDocument().addDocumentListener( this );
    mainPanel.add( name, "3, 1" );

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 3" );

    pid = new JFormattedTextField( new HexFormat( 2, 2 ) );
    new TextPopupMenu( pid );
    pid.addPropertyChangeListener( "value", this );
    mainPanel.add( pid, "3, 3" );

    // Protocol Code Table
    JPanel tablePanel = new JPanel( new BorderLayout() );
    mainPanel.add( tablePanel, "1, 5, 3, 5" );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Protocol code" ) );
    codeModel = new CodeTableModel();
    codeTable = new JTableX( codeModel );
    tablePanel.add( new JScrollPane( codeTable ), BorderLayout.CENTER );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )codeTable.getDefaultRenderer( String.class );
    r.setHorizontalAlignment( SwingConstants.CENTER );
    codeTable.setDefaultEditor( Hex.class, new HexCodeEditor() );   
    codeTable.getSelectionModel().addListSelectionListener( this );//new ListSelectionListener()

    JLabel l = ( JLabel )codeTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent( codeTable,
        colNames[ 0 ], false, false, 0, 0 );

    TableColumnModel columnModel = codeTable.getColumnModel();
    TableColumn column = columnModel.getColumn( 0 );
    int width = l.getPreferredSize().width;

    procs = ProcessorManager.getProcessors();
    int count = 0;
    for ( int i = 0; i < procs.length; i++ )
    {
      Processor proc = procs[ i ];
      if ( proc.getEquivalentName().equals( proc.getFullName() ) )
        ++count;
    }
    Processor[] uProcs = new Processor[ count ];
    count = 0;
    for ( int i = 0; i < procs.length; i++ )
    {
      Processor proc = procs[ i ];
      if ( proc.getEquivalentName().equals( proc.getFullName() ) )
        uProcs[ count++ ] = proc;
    }
    procs = uProcs;
    for ( int i = 0; i < procs.length; i++ )
    {
      l.setText( procs[ i ].getFullName() );
      width = Math.max( width, l.getPreferredSize().width );
    }
    for ( int i = 0; i < procs.length; i++ )
    {
      column.setMinWidth( width );
      column.setMaxWidth( width );
      column.setPreferredWidth( width );
    }
    
    columnModel.getColumn( 1 ).setCellRenderer( new CodeCellRenderer() );
    
    codeTable.doLayout();    
    Dimension d = codeTable.getPreferredSize();
    d.width = (int)(d.width * scale );
    codeTable.setPreferredScrollableViewportSize( d );
    
    upperPanel = mainPanel;

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    importButton = new JButton( "Import Protocol Upgrade" );
    importButton.addActionListener( this );
    importButton.setToolTipText( "Import Protocol Upgrades(s) from the Clipboard" );
    importButton.setEnabled( false );
    buttonPanel.add( importButton );
    
    JPanel messagePanel = new JPanel( new FlowLayout( FlowLayout.LEFT) );
    messagePanel.add( messageLabel );
    setMessage( 2 );
    
    JPanel midPanel = new JPanel( new BorderLayout() );
    midPanel.add( buttonPanel, BorderLayout.CENTER );
    midPanel.add( messagePanel, BorderLayout.PAGE_END );
    tablePanel.add( midPanel, BorderLayout.PAGE_END) ;
  
    // Create lower part of left panel as tabbed pane
    mainPanel = new JPanel( new TableLayout( size2 ) );
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab( "Device Data", mainPanel );
    tabbedPane.addChangeListener( this );
    leftPanel.add( tabbedPane, BorderLayout.CENTER);
    
    // Device Parameter Table on Device Data tab
    deviceModel = new ParameterTableModel( protocol, ParameterTableModel.Type.DEVICE );
    deviceTable = new JTableX( deviceModel );
    SpinnerCellEditor editor = new SpinnerCellEditor( 0, 8, 1 );
    new TextPopupMenu( ( JTextField )( ( DefaultCellEditor )deviceTable.getDefaultEditor( String.class ) )
        .getComponent() );
    deviceTable.setDefaultEditor( Integer.class, editor );
    JScrollPane scrollPane = new JScrollPane( deviceTable );
    tablePanel = new JPanel( new BorderLayout() );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Device Parameters" ) );
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 1, 3, 1" );
    d = deviceTable.getPreferredScrollableViewportSize();
    d.height = deviceTable.getRowHeight() * 4;
    d.width = (int)(d.width * scale );
    deviceTable.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Default Fixed Data:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 3" );
    rawHexData = new JTextField();
    rawHexData.getDocument().addDocumentListener( this );
    new TextPopupMenu( rawHexData );
    mainPanel.add( rawHexData, "3, 3" );

    // Command Parameter table on Device Data tab
    commandModel = new ParameterTableModel( protocol, ParameterTableModel.Type.COMMAND );

    commandTable = new JTableX( commandModel );
    commandTable.setDefaultEditor( Integer.class, editor );
    new TextPopupMenu( ( JTextField )( ( DefaultCellEditor )commandTable.getDefaultEditor( String.class ) )
        .getComponent() );
    scrollPane = new JScrollPane( commandTable );
    tablePanel = new JPanel( new BorderLayout() );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Command Parameters" ) );
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 5, 3, 5" );
    d = commandTable.getPreferredScrollableViewportSize();
    d.height = commandTable.getRowHeight() * 4;
    d.width = (int)(d.width * scale );
    commandTable.setPreferredScrollableViewportSize( d );

    label = new JLabel( "Command Index:", SwingConstants.RIGHT );
    mainPanel.add( label, "1, 7" );
    cmdIndex = new JSpinner( new SpinnerNumberModel( protocol.getCmdIndex(), 0, protocol.getDefaultCmd().length() - 1,
        1 ) );
    cmdIndex.addChangeListener( this );
    mainPanel.add( cmdIndex, "3, 7" );
    
    // Protocol Data tab of lower left panel
    setTableLayout( size3, dataLabels, false );
    mainPanel = new JPanel( new TableLayout( size3 ) );
    scrollPane = new JScrollPane( mainPanel );
    scrollPane.setPreferredSize( scrollPane.getPreferredSize() ); // needed to limit height of pane
    tabbedPane.addTab( "Protocol Data", scrollPane );
    populateComboBox( devBytes, CommonData.to15 );
    populateComboBox( cmdBytes, CommonData.to15 );
    
    for ( int i = 0; i < dataComponents.length; i++ )
    {
      if ( dataComponents[ i ] != null )
      {
        if ( dataComponents[ i ].length > 1 )
        {
          label = ( JLabel )dataComponents[ i ][ 1 ];
        }
        else
        {
          label = new JLabel();
        }
        label.setText( dataLabels[ i ][ 0 ] );
        mainPanel.add( label, "1, " + ( i + 1 ) );
        mainPanel.add( dataComponents[ i ][ 0 ], "3, " + ( i + 1 ) );
        if ( dataLabels[ i ].length > 1 )
        {
          mainPanel.add( new JLabel( dataLabels[ i ][ 1 ] ), "5, " + ( i + 1 ) );
        }
      }
    }
    
    // PF Details tab of lower left panel (added to tabbed pane by valueChanged() when a protocol is selected)
    pfMainPanel = new JPanel( new BorderLayout() );
    pfPanel = new JPanel();
    pfScrollPane = new JScrollPane( pfPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
    pfScrollPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    pfMainPanel.add( pfScrollPane, BorderLayout.CENTER );

    JPanel headerPanel = new JPanel( new BorderLayout() );
    headerPanel.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
    JPanel pfChoice = new JPanel( new GridLayout( 1, CommonData.pfData.length ) );
    headerPanel.add( pfChoice, BorderLayout.PAGE_START );
    String text = "Bits per byte, current protocol values selected";
    headerPanel.add( new JLabel( text, SwingConstants.CENTER ), BorderLayout.PAGE_END );
    pfMainPanel.add( headerPanel, BorderLayout.PAGE_START );
    ButtonGroup grp = new ButtonGroup();
    pfButtons = new JRadioButton[ CommonData.pfData.length ];
    pfValues = new Integer[ CommonData.pfData.length ];
    for ( int i = 0; i < pfButtons.length; i++ )
    {
      pfButtons[ i ] = new JRadioButton( "PF" + i, false );
      pfButtons[ i ].addItemListener( this );
      pfChoice.add( pfButtons[ i ] );
      grp.add(  pfButtons[ i ] );
    }
    
    // PD Details tab of lower left panel (added to tabbed pane by valueChanged() when a protocol is selected)
    pdMainPanel = new JPanel( new BorderLayout() );
    pdPanel = new JPanel();
    pdScrollPane = new JScrollPane( pdPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
    pdScrollPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    pdMainPanel.add( pdScrollPane, BorderLayout.CENTER );
    pdHeaderPanel = new JPanel( new BorderLayout() );
    pdMainPanel.add(  pdHeaderPanel, BorderLayout.PAGE_START );
    int n = 0;
    for ( int i = 0; i < CommonData.pdData.length; i++)
    {
      n += Integer.parseInt( CommonData.pdData[ i ][ 0 ] );
    }
    pdValues = new Integer[ n ];
    
    // Function tab of lower left panel (added to tabbed pane by valueChanged() when a protocol is selected)
    fnMainPanel = new JPanel( new BorderLayout() );
    fnPanel = new JPanel();
    fnScrollPane = new JScrollPane( fnPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
    fnScrollPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    fnMainPanel.add( fnScrollPane, BorderLayout.CENTER );
    fnHeaderPanel = new JPanel( new BorderLayout() );
    fnMainPanel.add(  fnHeaderPanel, BorderLayout.PAGE_START );
    tabbedPane.add( "Functions", fnMainPanel );
    
    // Disassembly on right pane   
    assemblerTable = new JP1Table( assemblerModel );
    assemblerTable.initColumns( assemblerModel );
    assemblerModel.dialog = this;
    scrollPane = new JScrollPane( assemblerTable );
    scrollPane.setBorder( BorderFactory.createTitledBorder( "Disassembly" ) );
    rightPanel.add( scrollPane, BorderLayout.CENTER );
    
    // Disassembly options
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout( new BoxLayout( optionsPanel, BoxLayout.PAGE_AXIS ) );
    optionsPanel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Disassembly options" ),
        BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) ) );
    rightPanel.add( optionsPanel, BorderLayout.PAGE_END );
    JPanel optionPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
    useRegisterConstants.addItemListener( this );
    useFunctionConstants.addItemListener( this );
    optionPanel.add( new JLabel( "Use predefined constants for: ") );
    optionPanel.add( useRegisterConstants );
    optionPanel.add( useFunctionConstants );
    optionsPanel.add( optionPanel );
    optionPanel = new JPanel( new GridLayout( 1, 4 ) );
    asCodeButton.addItemListener( this );
    rcButton.addItemListener( this );
    wButton.addItemListener( this );
    grp = new ButtonGroup();
    grp.add( asCodeButton );
    grp.add( rcButton );
    grp.add( wButton );
    setOptionButtons();
    optionPanel.add(  new JLabel( "S3C80 only:" ) );
    optionPanel.add( asCodeButton );
    optionPanel.add( rcButton );
    optionPanel.add( wButton );
    optionsPanel.add( optionPanel );
    
    // Button Panel
    JPanel mainButtonPanel = new JPanel( new BorderLayout() );
    buttonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    mainButtonPanel.add( buttonPanel, BorderLayout.LINE_START );

    codeButton = new JButton( "Expand" );
    codeButton.setToolTipText( "<HTML>Expand/Collapse the lower tabbed panel.  When expanded<BR>it hides the upper panel of protocol codes.</HTML>" );
    codeButton.addActionListener( this );
    buttonPanel.add( codeButton, BorderLayout.CENTER );
    
    buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    mainButtonPanel.add( buttonPanel, BorderLayout.CENTER );
    view = new JButton( "View Ini" );
    view.setToolTipText( "View the protocols.ini entry for this protocol." );
    view.addActionListener( this );
    view.setEnabled( false );
    buttonPanel.add( view );

    buttonPanel.add( Box.createHorizontalGlue() );

    ok = new JButton( "OK" );
    ok.addActionListener( this );
    buttonPanel.add( ok );

    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    buttonPanel.add( cancel );

    leftPanel.add( mainButtonPanel, BorderLayout.SOUTH );

    Hex id = protocol.getID();
    pid.setValue( id );
    rawHexData.setText( protocol.getFixedData( new Value[ 0 ] ).toString() );

    d = rightPanel.getPreferredSize();
    d.width = (int)(leftPanel.getPreferredSize().width * 0.95 / scale );
    rightPanel.setPreferredSize( d );
    
    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );
    
    isEmpty = new boolean[ procs.length ];
    for ( int i = 0; i < procs.length; i++ )
    {
      Hex hex = protocol.getCode( procs[ i ] );
      isEmpty[ i ] = ( hex == null || hex.length() == 0 );
    }
  }
  
  public void setForCustomCode()
  {
//    pid.setEditable( false );
//    pid.setEnabled( false );
    
    deviceTable.setEnabled( false );
    deviceTable.setForeground( Color.GRAY );

    commandTable.setEnabled( false );
    commandTable.setForeground( Color.GRAY );

    rawHexData.setEnabled( false );
    cmdIndex.setEnabled( false );
    
    tabbedPane.setSelectedIndex( 1 );
  
    enableButtons();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    if ( source == importButton )
    {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable clipData = clipboard.getContents( clipboard );
      if ( clipData != null )
      {
        try
        {
          if ( clipData.isDataFlavorSupported( DataFlavor.stringFlavor ) )
          {
            String s = ( String )( clipData.getTransferData( DataFlavor.stringFlavor ) );
            importProtocolCode( s );
          }
        }
        catch ( Exception ex )
        {
          ex.printStackTrace( System.err );
        }
      }
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        assemblerModel.disassemble( protocol, procs[ codeTable.getSelectedRow() ] );
      }
    }
    else if ( source == view )
    {
      JTextArea ta = new JTextArea( protocol.getIniString( false, pid.isEnabled() ), 10, 70 );
      new TextPopupMenu( ta );
      ta.setEditable( false );
      JOptionPane.showMessageDialog( this, new JScrollPane( ta ), "Protocol.ini entry text", JOptionPane.PLAIN_MESSAGE );
    }
    else if ( source == ok )
    {
      userAction = JOptionPane.OK_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == cancel )
    {
      userAction = JOptionPane.CANCEL_OPTION;
      setVisible( false );
      dispose();
    }
    else if ( source == codeButton )
    {
      upperPanel.setVisible( !upperPanel.isVisible() );
      codeButton.setText( upperPanel.isVisible() ? "Expand" : "Collapse" );
    }
  }

  /**
   * Gets the protocol.
   * 
   * @return the protocol
   */
  public ManualProtocol getProtocol()
  {
    if ( userAction != JOptionPane.OK_OPTION )
      return null;

    // protocol.setDeviceParms( deviceParms );
    // protocol.setDeviceTranslators( deviceTranslators );
    // protocol.setCommandParms( cmdParms );
    // protocol.setCommandTranslators( cmdTranslators );
    protocol.setRawHex( new Hex( rawHexData.getText() ) );

    return protocol;
  }

  // PropertyChangeListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent e )
  {
    Object source = e.getSource();
    if ( source == pid )
    {
      Hex id = ( Hex )pid.getValue();
      boolean inDeviceUpgrade = false;
      Protocol p = null;
      DeviceUpgrade du = null;
      if ( id != null && id.length() != 0 && remoteConfig != null )
      {
        Remote remote = remoteConfig.getRemote();
        for ( DeviceUpgrade temp : remoteConfig.getDeviceUpgrades() )
        {
          du = temp;
          p = temp.getProtocol();
          
          if ( p.getID( remote ).equals( id ) )
          {
            inDeviceUpgrade = true;
            break;
          }
        }
        if ( inDeviceUpgrade )
        {
          String title = "Manual Settings";
          boolean exit = false;
          String starredID = du.getStarredID();
          boolean usesProtocolUpgrade = ( starredID.endsWith( "*" ) );

          if ( usesProtocolUpgrade )
          {
            String message = "There is a Device Upgrade that is using a protocol upgrade with\n"
                           + "PID " + id + " Do you want to abort this PID choice and enter\n"
                           + "a different one?  If so, please press OK.\n\n"
                           + "If you want to edit that protocol code, also press OK, then exit\n"
                           + "this dialog, change to the Devices page and edit the protocol of\n"
                           + "the device upgrade from there.\n\n"
                           + "To continue, press CANCEL but you will be creating a Manual Protocol\n"
                           + "that cannot be accessed while that Device Upgrade is present.";
            exit = ( JOptionPane.showConfirmDialog( null, message, title, 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.OK_OPTION );
          }
          else
          {
            String message = "There is a Device Upgrade with protocol with PID " + id + " that\n"
                           + "is not yet using a protocol upgrade, so you cannot create a new\n"
                           + "manual protocol with that PID.  If you want to create a manual\n"
                           + "protocol then please choose a different PID.  If you want to\n"
                           + "provide code for that device upgrade, please change to the\n"
                           + "Devices page and edit the protocol from there.";
            JOptionPane.showMessageDialog( null, message, title, JOptionPane.WARNING_MESSAGE );
            exit = true;
          }
          
          if ( exit )
          {
            pid.setValue( null );
            enableButtons();
            return;
          }         
        }
      }
      protocol.setID( id );
    }
    enableButtons();
  }

  protected void enableButtons()
  {
    if ( deviceTable.isEnabled() )
    {
      // Normal Manual Settings usage
      Hex id = ( Hex )pid.getValue();
      boolean flag = ( id != null ) && ( id.length() != 0 ) && protocol.hasAnyCode();
      ok.setEnabled( flag );
      view.setEnabled( flag );
    }
    else
    {
      // Custom Code usage
      ok.setEnabled( true );
      view.setEnabled( false );
    }
  }

  // DocumentListener methods
  /**
   * Document changed.
   * 
   * @param e
   *          the e
   */
  public void documentChanged( DocumentEvent e )
  {
    Document doc = e.getDocument();

    if ( doc == name.getDocument() )
    {
      protocol.setName( name.getText() );
    }
    else if ( doc == rawHexData.getDocument() )
    {
      protocol.setRawHex( new Hex( rawHexData.getText() ) );
    }
    enableButtons();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    documentChanged( e );
  }

  /**
   * The Class CodeTableModel.
   */
  public class CodeTableModel extends AbstractTableModel
  {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount()
    {
      return procs.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
      return colNames.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName( int col )
    {
      return colNames[ col ];
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class< ? > getColumnClass( int col )
    {
      return classes[ col ];
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable( int row, int col )
    {
      if ( displayRemote != null && !procs[ row ].getEquivalentName().equals( displayRemote.getProcessor().getEquivalentName() ) )
      {
        return false;
      }
      return ( col == 1 );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt( int row, int col )
    {
      Hex hex = protocol.getCode( procs[ row ] );
      Hex dispHex = ( displayProtocol == null ) ? null : displayProtocol.getCode( procs[ row ] );
      if ( dispHex == null )
      {
        dispHex = new Hex();
      }
      switch ( col )
      {
        case 0:
          return procs[ row ];
        case 1:
          return ( hex == null || hex.length() == 0 ) ? dispHex : hex;
        default:
          // There are no other columns but this value is used by cell renderer
          return hex == null || hex.length() == 0 || isEmpty[ row ] && hex.equals( dispHex );
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt( Object value, int row, int col )
    {
      if ( col == 1 )
      {
        Hex newCode = ( Hex )value;
        if ( ( newCode != null ) && ( newCode.length() != 0 ) )
        {
          if ( !protocol.hasAnyCode() )
          {
            int fixedDataLength = Protocol.getFixedDataLengthFromCode( procs[ row ].getEquivalentName(), newCode );
            rawHexData.setText( Hex.toString( new short[ fixedDataLength ] ) );
            ArrayList< Value > devParms = new ArrayList< Value >();
            Value zero = new Value( 0 );
            for ( int i = 0; i < fixedDataLength; ++i )
              devParms.add( zero );
            int cmdLength = Protocol.getCmdLengthFromCode( procs[ row ].getEquivalentName(), newCode );
            DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
            for ( int i = 0; i < cmdLength; ++i )
            {
              comboModel.addElement( importButton );
            }
            SpinnerNumberModel spinnerModel = ( SpinnerNumberModel )cmdIndex.getModel();
            spinnerModel.setMaximum( cmdLength - 1 );
            protocol.createDefaultParmsAndTranslators( cmdLength << 4, false, false, 8, devParms, new short[ 0 ], 8 );
            deviceModel.fireTableDataChanged();
            commandModel.fireTableDataChanged();
          }
        }
        else if ( codeWhenNull != null )
        {
          String title = "Code deletion";
          String message = "This protocol is not built in to the remote.  Do you want to restore\n"
                         + "the code to the standard code for this protocol?\n\n"
                         + "If you select NO then the protocol upgrade for this device upgrade\n"
                         + "will be deleted.  The device upgrade will not function until you\n"
                         + "restore the protocol upgrade, which you may do by deleting this\n"
                         + "null entry and answering this question again.";
          boolean restore = ( JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION );
          if ( restore )
          {
            value = codeWhenNull;
          }
          else
          {
            value = new Hex();
          }
        }
        protocol.setCode( ( Hex )value, procs[ row ] );
        fireTableRowsUpdated( row, row );
        enableButtons();
        assemblerModel.disassemble( protocol, procs[ row ] );
      }
    }
  }

  /**
   * Import protocol code.
   * 
   * @param string
   *          the string
   */
  private void importProtocolCode( String string )
  {
    StringTokenizer st = new StringTokenizer( string, "\n" );
    String text = null;
    String processor = null;
    String pidStr = null;
    while ( st.hasMoreTokens() )
    {
      while ( st.hasMoreTokens() )
      {
        text = st.nextToken().toUpperCase();
        System.err.println( "got '" + text );
        if ( text.startsWith( "UPGRADE PROTOCOL 0 =" ) )
        {
          StringTokenizer st2 = new StringTokenizer( text, "()=" );
          st2.nextToken(); // discard everything before the =
          pidStr = st2.nextToken().trim();
          System.err.println( "Imported pid is " + pidStr );
          processor = st2.nextToken().trim();
          System.err.println( "processorName is " + processor );
          if ( processor.startsWith( "S3C8" ) )
            processor = "S3C80";
          else if ( processor.startsWith( "S3F8" ) )
            processor = "S3F80";
          if ( st2.hasMoreTokens() )
          {
            String importedName = st2.nextToken().trim();
            System.err.println( "importedName is " + importedName );
          }
          break;
        }
      }
      if ( st.hasMoreTokens() )
      {
        text = st.nextToken(); // 1st line of code
        while ( st.hasMoreTokens() )
        {
          String temp = st.nextToken();
          if ( temp.trim().equals( "End" ) )
            break;
          text = text + ' ' + temp;
        }
        System.err.println( "getting processor with name " + processor );
        Processor p = ProcessorManager.getProcessor( processor );
        if ( p != null )
        {
          // processor = p.getFullName();
          processor = p.getEquivalentName();
          p = ProcessorManager.getProcessor( processor );
          pid.setValue( new Hex( pidStr ) );
        }
        System.err.println( "Adding code for processor " + processor );
        System.err.println( "Code is " + text );
        for ( int i = 0; i < procs.length; i++ )
        {
          if ( procs[ i ] == p )
            codeModel.setValueAt( new Hex( text ), i, 1 );
        }
      }
    }
  }
  
  private PropertyFile properties = JP1Frame.getProperties();

  /** The protocol. */
  private ManualProtocol protocol = null;
  
  private Protocol displayProtocol = null;
  
  private Remote displayRemote = null;

  /** The code model. */
  private CodeTableModel codeModel = null;

  /** The code table. */
  private JTableX codeTable = null;

  /** The device model. */
  private ParameterTableModel deviceModel = null;

  /** The device table. */
  private JTableX deviceTable = null;

  /** The command model. */
  private ParameterTableModel commandModel = null;

  /** The command table. */
  private JTableX commandTable = null;

  /** The name. */
  private JTextField name = null;

  /** The pid. */
  public JFormattedTextField pid = null;

  /** The raw hex data. */
  private JTextField rawHexData = null;

  private JSpinner cmdIndex = null;

  /** The import button. */
  private JButton importButton = null;

  /** The view. */
  private JButton view = null;
  
  private JButton codeButton = null;

  /** The ok. */
  private JButton ok = null;

  /** The cancel. */
  private JButton cancel = null;
  
  private int dataStyle = 0;
  private Processor processor = null;
  private List< Integer > absUsed = null;
  private List< Integer > zeroUsed = null;
  
  public JCheckBox useRegisterConstants = new JCheckBox( "Registers" );
  public JCheckBox useFunctionConstants = new JCheckBox( "Functions" );
  public JRadioButton asCodeButton = new JRadioButton( "As code" );
  public JRadioButton rcButton = new JRadioButton( "Force RCn" );
  public JRadioButton wButton = new JRadioButton( "Force Wn" );
  private JToggleButton[] optionButtons = { useRegisterConstants, useFunctionConstants, asCodeButton, rcButton, wButton };
  private JRadioButton pfButtons[] = null;
  
  private JPanel upperPanel = null;
  private JTabbedPane tabbedPane = null;
  private JSplitPane outerPane = null;
  private JPanel pfMainPanel = null;
  private JPanel pfPanel = null;
  private JScrollPane pfScrollPane = null;
  private Integer[] pfValues = null;
  
  private JPanel pdMainPanel = null;
  private JPanel pdHeaderPanel = null;
  private JPanel pdPanel = null;
  private JScrollPane pdScrollPane = null;
  private Integer[] pdValues = null;
  
  private JPanel fnMainPanel = null;
  private JPanel fnHeaderPanel = null;
  private JPanel fnPanel = null;
  private JScrollPane fnScrollPane = null;
  
  public JTextField frequency = new JTextField();
  public JTextField dutyCycle = new JTextField();
  public JComboBox sigStruct = new JComboBox();
  
  public JComboBox devBytes = new JComboBox();
  public JComboBox devBits1 = new JComboBox();
  public JComboBox devBits2 = new JComboBox();
  public JComboBox devBitDbl = new JComboBox();
  
  public JComboBox cmdBytes = new JComboBox();
  public JComboBox cmdBits1 = new JComboBox();
  public JComboBox cmdBits2 = new JComboBox();
  public JComboBox cmdBitDbl = new JComboBox();
  
  public JTextField rptValue = new JTextField();
  public JComboBox rptType = new JComboBox();
  public JComboBox rptHold = new JComboBox();
  
//  public JComboBox chkByteStyle = new JComboBox();
//  public JTextField bitsHeld = new JTextField();
//  
//  public JComboBox miniCombiner = new JComboBox();
//  public JComboBox sigStyle = new JComboBox();
//  
//  public JTextField vecOffset = new JTextField();
//  public JTextField dataOffset = new JTextField();
  
  public JTextField burst1On = new JTextField();
  public JTextField burst1Off = new JTextField();
  
  public JTextField burst0On = new JTextField();
  public JTextField burst0Off = new JTextField();
  public JComboBox xmit0rev = new JComboBox();
  
  public JComboBox leadInStyle = new JComboBox();
  public JComboBox burstMidFrame = new JComboBox();
  public JTextField afterBits = new JTextField();
  public JTextField leadInOn = new JTextField();
  public JTextField leadInOff = new JTextField();
  
  public JComboBox leadOutStyle = new JComboBox();
  public JTextField leadOutOff = new JTextField();
  public JComboBox offAsTotal = new JComboBox();
  
  public JTextField altLeadOut = new JTextField();
  public JComboBox useAltLeadOut = new JComboBox();
  public JTextField altFreq = new JTextField();
  public JTextField altDuty = new JTextField();
  
//  public JComboBox toggleBit = new JComboBox();
  
  public JLabel devBits1lbl = new JLabel();
  public JLabel devBits2lbl = new JLabel();
  public JLabel cmdBits1lbl = new JLabel();
  public JLabel cmdBits2lbl = new JLabel();
  public JLabel burstMidFrameLbl = new JLabel();
  public JLabel afterBitsLbl = new JLabel();
  public JLabel altFreqLbl = new JLabel();
  public JLabel altDutyLbl = new JLabel();
  
  
  private Component[][] dataComponents = { 
      { frequency }, { dutyCycle }, { sigStruct }, null,
      { devBytes }, { devBits1, devBits1lbl }, { devBits2, devBits2lbl }, { devBitDbl }, null,
      { cmdBytes }, { cmdBits1, cmdBits1lbl }, { cmdBits2, cmdBits2lbl }, { cmdBitDbl }, null,
      { rptValue }, { rptType }, { rptHold }, null,
      { burst1On }, { burst1Off }, null,
      { burst0On }, { burst0Off }, { xmit0rev }, null,
      { leadInStyle }, { burstMidFrame, burstMidFrameLbl }, { afterBits, afterBitsLbl }, { leadInOn }, { leadInOff }, null,
      { leadOutStyle }, { leadOutOff }, { offAsTotal }, null,
      { altLeadOut }, { useAltLeadOut }, { altFreq, altFreqLbl }, { altDuty, altDutyLbl }//, null,
//      { chkByteStyle }, { bitsHeld }, null,
//      { miniCombiner }, { sigStyle }, null,
//      { vecOffset }, { dataOffset }, null,
//      { toggleBit }
  };
  
  private String[][] dataLabels = { 
      { "Frequency", "kHz" }, { "Duty Cycle", "%" }, { "Signal Structure" }, null,
      { "Device Bytes" }, { "Bits/Dev1" }, { "Bits/Dev2" }, { "Dev Bit Doubling" }, null,
      { "Command Bytes" }, { "Bits/Cmd1" }, { "Bits/Cmd2" }, { "Cmd Bit Doubling" }, null,
      { "Repeat Value" }, { "Type" }, { "Hold" }, null,
      { "1 Burst ON", "uSec" }, { "OFF", "uSec" }, null,
      { "0 Burst ON", "uSec" }, { "OFF", "uSec" }, { "Xmit 0 Reversed" }, null,
      { "Lead-In Style" }, { "Burst Mid-Frame" }, { "After # of bits" }, { "Lead-In ON", "uSec" }, { "OFF", "uSec" }, null,
      { "Lead-Out Style" }, { "Lead-Out OFF", "uSec" }, { "OFF as Total" }, null,
      { "Alt Lead-Out", "uSec" }, { "Use Alt Lead-Out" }, { "Alt Freq", "kHz" }, { "Alt Duty", "%" }//, null,
//      { "Check Byte Style" }, { "# Bytes Checked" }, null,
//      { "Mini-Combiner" }, { "Signal Style" }, null,
//      { "Vector Offset" }, { "Data Offset" }, null,
//      { "Toggle Bit }"
  };

  /** The user action. */
  private int userAction = JOptionPane.CANCEL_OPTION;
  // private final static Object[] typeChoices = { "Numeric entry", "Drop-down list", "Check-box" };
  /** The Constant colNames. */
  private final static String[] colNames =
  {
      "Processor", "Protocol Code"
  };

  /** The Constant classes. */
  private final static Class< ? >[] classes =
  {
      Processor.class, Hex.class
  };

  /** The procs. */
  private static Processor[] procs = new Processor[ 0 ];

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged( ChangeEvent event )
  {
    if ( event.getSource() == tabbedPane )
    {
      setPFPanel();
      setPDPanel();
      setFunctionPanel();
    }
    else if ( protocol.setCmdIndex( ( ( Integer )cmdIndex.getValue() ).intValue() ) )
    {
      commandModel.fireTableDataChanged();
    }
  }
  
  private void setTableLayout( double[][] size, String[][] data, boolean interleave )
  {
    double b = 5; // space between rows and around border
    double c = 10; // space between columns
    List< Double > rows = new ArrayList< Double >();
    rows.add(  b  );
    for ( int i = 0; i < data.length; i++ )
    {
      if ( data[ i ] != null && data[ i ][ 0 ].equals(  "0" ) ) continue;
      if ( interleave )
      {
        for ( int j = 2; j < Math.max( data[ i ].length, 3 ); j++ )
          rows.add( TableLayout.PREFERRED );
      }
      else
      {
        rows.add( data[ i ] == null ? c : TableLayout.PREFERRED );
      }
      if ( i == data.length - 1 && data[ i ] != null || interleave ) rows.add( b );
    }
    size[ 1 ] = new double[ rows.size() ];
    for ( int i = 0; i < rows.size(); i++ )
    {
      size[ 1 ][ i ] = rows.get( i );
    }
  }
  
  public void setPFPanel()
  {
    if ( tabbedPane.getSelectedComponent() != pfMainPanel )
    {
      return;
    }
    int n = -1;
    for (int i = 0 ; i < pfButtons.length; i++ )
    {
      pfButtons[ i ].setEnabled( pfValues[ i ] != null );
      if ( pfButtons[ i ].isSelected() ) n = i;
    }
    if ( n < 0 )
    {
      n = 0;
      pfButtons[ 0 ].setSelected( true );
    }
    List< JTextArea > areas = new ArrayList< JTextArea >();
    double size[][] = { { getWidth( "0-0__" ), TableLayout.FILL }, null };
    setTableLayout( size, CommonData.pfData[ n ], true );
    pfPanel.setLayout( new TableLayout( size ) );
    
    int bitPos = 0;
    int row = 1;
    for ( String[] data : CommonData.pfData[ n ] )
    {
      DisplayArea label = new DisplayArea( data[ 1 ], areas );
      pfPanel.add( label, "0, " + row + ", l, t" );
      
      DisplayArea area = new DisplayArea( data[ 2 ], areas );
      pfPanel.add( area, "1, " + row++ );

      JComboBox combo = new JComboBox();
      String text = data[ 3 ];
      while ( true )
      {
        int pos = text.indexOf( "\n" );
        if ( pos >= 0 )
        {
          combo.addItem( text.substring( 0, pos ) );
          text = text.substring( pos + 1 );
        }
        else
        {
          combo.addItem( text.substring( 0 ) );
          break;
        }
      };
      
      pfPanel.add( combo, "1, " + row++ );
      if ( data.length > 4 )
      {
        area = new DisplayArea( data[ 4 ], areas );
        pfPanel.add( area, "1, " + row++ );
      }
      
      if ( pfValues[ n ] != null )
      {
        int len = Integer.parseInt( data[ 0 ] );
        int val = ( pfValues[ n ] >> bitPos ) & ( ( 1 << len ) - 1 );
        bitPos += len;
        for ( int i = 0; ; i++ )
        {
          text = ( String )combo.getModel().getElementAt( i );
          if ( text.startsWith( "" + val + " =" ) || i == combo.getModel().getSize() - 1 /* other */ )
          {
            combo.setSelectedIndex( i );
            break;
          }
        }
      }
      row++;
    }
    pfPanel.validate();
    for ( JTextArea area : areas )
    {
      Dimension d = area.getPreferredSize();
      d.width = 100;
      area.setPreferredSize( d );
    }
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run() 
      {
        pfScrollPane.getVerticalScrollBar().setValue(0);
      }
    } );
  }
  
  public void setPDPanel()
  {
    if ( tabbedPane.getSelectedComponent() != pdMainPanel )
    {
      return;
    }
    List< JTextArea > areas = new ArrayList< JTextArea >();
    double size[][] = { { getWidth( "PD00/PD00__" ), TableLayout.FILL }, null };
    setTableLayout( size, CommonData.pdData, true );
    pdPanel.setLayout( new TableLayout( size ) );
    
    int pdNum = 0;
    int row = 1;
    for ( String[] data : CommonData.pdData )
    {
      int n = Integer.parseInt( data[ 0 ] );
      String text = "";
      for ( int i = 0; i < n; i++ )
      {
        if ( i > 0 )
        {
          text += "/";
        }
        text += String.format( "PD%02X", pdNum + i );
      }
      DisplayArea label = new DisplayArea( text, areas );
      
      text = data[ 1 ];
      DisplayArea area = new DisplayArea( "", areas );
      
      if ( n > 0 )
      {
        pdPanel.add( label, "0, " + row + ", l, t" );
        pdPanel.add( area, "1, " + row );
        int val = 0;
        int time = 0;
        if ( pdValues[ pdNum ] != null )
        {
          switch ( pdNum )
          {
            case 0x00:
            case 0x01:
            case 0x10:
            case 0x12:
              text += String.format( "\n$%02X -> %<d bits", pdValues[ pdNum ] );
              break;
            case 0x11:
              text += String.format( "\n$%02X -> %<d repeats", pdValues[ pdNum ] );
              break;
            case 0x04:
            case 0x08:
            case 0x0E:
              time += ( dataStyle == 0 ) ? 40 : 0;
              // run through  
            case 0x02:
            case 0x06:
            case 0x0A:
            case 0x0C:
              if ( pdValues[ pdNum + 1 ] != null )
              {
                val = pdValues[ pdNum ] * 0x100 + pdValues[ pdNum + 1 ];
                time += 2 * val;
                text += String.format( "\n$%04X -> %d uSec", val, time );
              }
              break;
            case 0x13:
              int pos1 = text.indexOf( "\n" );
              pos1 = text.indexOf( "\n", pos1 + 1 );
              int pos2 = text.indexOf( "\n", pos1 + 1 );
              text += String.format( "\n    $%02X -> %<d bits", pdValues[ pdNum ] );
              if ( pdValues[ pdNum + 1 ] != null )
              {
                double onTime = pdValues[ pdNum ];
                double offTime = pdValues[ pdNum + 1 ];
                if ( dataStyle == 0 )
                {
                  onTime = ( onTime + 2 )/ 8.0;
                  offTime = ( offTime + 2 )/ 8.0;
                }
                else
                {
                  onTime /= 4.0;
                  offTime /= 4.0;
                }
                val = pdValues[ pdNum ] * 0x100 + pdValues[ pdNum + 1 ];
                text = text.substring( 0, pos1 + 1 )
                + String.format( "    $%04X -> %d uSec\n", val, 2 * val )
                + text.substring( pos1 + 1, pos2 + 1 )
                + String.format( "    $%04X -> ON %.3f uSec, OFF %.3f uSec\n", val, onTime, offTime )
                + text.substring( pos2 + 1 );
              }
              break;
          }
        }
        row += 2;
        pdNum += n;
      }
      else
      {
        pdHeaderPanel.removeAll();
        pdHeaderPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        pdHeaderPanel.add( area, BorderLayout.CENTER );
      }
      area.setText( text );
    }
    pdMainPanel.validate();
    for ( JTextArea area : areas )
    {
      Dimension d = area.getPreferredSize();
      d.width = 100;
      area.setPreferredSize( d );
      area.setMinimumSize( new Dimension( 10, 10 ) );
    }
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run() 
      {
        pdScrollPane.getVerticalScrollBar().setValue(0);
      }
    } );
  }
  
  public void setFunctionPanel()
  {
    if ( tabbedPane.getSelectedComponent() != fnMainPanel )
    {
      return;
    }
    List< JTextArea > areas = new ArrayList< JTextArea >();
    int n = 0;
    for ( String[] data : CommonData.fnData )
    {
      if ( processor.getZeroAddresses().keySet().contains( data[ 0 ] )
          || processor.getAbsAddresses().keySet().contains( data[ 0 ] )
          || data[ 0 ].equals( "" ) )
      {
        n++;
      }
    }
    String functions[][] = new String[ n ][ 3 ];
    n = 0;
    for ( String[] data : CommonData.fnData )
    {
      Integer address = processor.getZeroAddresses().get( data[ 0 ] );
      if ( data[ 0 ].equals( "" ) )
      {
        functions[ n ][ 0 ] = "0";
      }
      else if ( address != null )
      {
        functions[ n ][ 0 ] = String.format( "%02X    ", address );
      }
      else
      {
        address = processor.getAbsAddresses().get( data[ 0 ] );
        if ( address != null )
        {
          functions[ n ][ 0 ] = String.format( "%04X", address );
        }
        else continue;
      }
      functions[ n ][ 1 ] = data[ 0 ];
      functions[ n++ ][ 2 ] = data[ 1 ];
    }
    
    Arrays.sort( functions, new Comparator< String[] >()
    {
      @Override
      public int compare( String[] o1, String[] o2 )
      {
        int n1 = Integer.parseInt( o1[ 0 ].trim(), 16 );
        int n2 = Integer.parseInt( o2[ 0 ].trim(), 16 );
        if ( ( zeroUsed.contains( n1 ) || absUsed.contains( n1 ) )
            && !( zeroUsed.contains( n2 ) || absUsed.contains( n2 ) ) )
        {
          return - 1;
        }
        else if ( !( zeroUsed.contains( n1 ) || absUsed.contains( n1 ) )
            && ( zeroUsed.contains( n2 ) || absUsed.contains( n2 ) ) )
        {    
          return 1;
        }
        else return n1 - n2;
      }
    } );

    double size[][] = { { getWidth( "$FFFF_*_" ), TableLayout.FILL }, null };
    setTableLayout( size, functions, true );
    fnPanel.setLayout( new TableLayout( size ) );
    
    int row = 1;
    for ( String[] fn : functions )
    {
      String text = fn[ 0 ];
      n = Integer.parseInt( fn[ 0 ].trim(), 16 );
      if ( dataStyle > 0 || n < 0x100 )
      {
        text = ( dataStyle == 0 ? "R" : "$" ) + text;
      }
      else
      {
        text += "H";
      }
      
      if ( zeroUsed.contains( n ) || absUsed.contains( n ) )
      {
        text += " * ";
      }

      DisplayArea label = new DisplayArea( text, areas );

      text = fn[ 1 ] + ( fn[ 1 ].equals( "" ) ? "" : "\n" ) + fn[ 2 ];
      DisplayArea area = new DisplayArea( text, areas );
      if ( fn[ 1 ].equals( "" ) )
      {
        fnHeaderPanel.removeAll();
        fnHeaderPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        fnHeaderPanel.add( area, BorderLayout.CENTER );
      }
      else
      {
        fnPanel.add( label, "0, " + row + ", l, t" );
        fnPanel.add( area, "1, " + row );
        row += 2;
      }
    }
    
    fnMainPanel.validate();
    for ( JTextArea area : areas )
    {
      Dimension d = area.getPreferredSize();
      d.width = 100;
      area.setPreferredSize( d );
      area.setMinimumSize( new Dimension( 10, 10 ) );
    }
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run() 
      {
        fnScrollPane.getVerticalScrollBar().setValue(0);
      }
    } );
  }
  
  public Hex codeWhenNull = null;
  
  public RemoteConfiguration remoteConfig = null;
  
  private AssemblerTableModel assemblerModel = new AssemblerTableModel();
  private JP1Table assemblerTable = null;
  private boolean isEmpty[] = null;
  private JLabel messageLabel = new JLabel();

  @Override
  public void valueChanged( ListSelectionEvent e )
  {
    if ( !e.getValueIsAdjusting() )
    {
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        int row = codeTable.getSelectedRow();
        Hex hex = protocol.getCode( procs[ row ] );
        Protocol prot = ( ( hex == null || hex.length() == 0 ) && displayProtocol != null ) ?
            displayProtocol : protocol;
        assemblerModel.disassemble( prot, procs[ codeTable.getSelectedRow() ] );
        setPFPanel();
        setPDPanel();
        setFunctionPanel();
        importButton.setEnabled( codeTable.isCellEditable( codeTable.getSelectedRow(), 1 ) );
        int tabCount = tabbedPane.getTabCount();
        if ( row != 0 && row != 4 && tabCount > 2 )
        {
          for ( int i = 3; i < tabCount; i++ )
          {
            tabbedPane.remove( 3 );
          }
        }
        else if ( ( row == 0 || row == 4 ) && tabCount == 3 )
        {
          tabbedPane.add( "PF Details", pfMainPanel );
          tabbedPane.add( "PD Details", pdMainPanel );
        }
      }
      else
      {
        importButton.setEnabled( false );
      }
    }
  }

  public void setDisplayProtocol( Protocol displayProtocol )
  {
    this.displayProtocol = displayProtocol;
  }

  public void setDisplayRemote( Remote displayRemote )
  {
    this.displayRemote = displayRemote;
  }
  
  public void setMessage( int n )
  {
    String text = "<HTML>";
    if ( n == 2 )
    {
      text += "This is a custom protocol, so only the code for the remote's <BR>processor is editable.<BR>";
    }
    text += "Code shown in gray is standard code for information only.</HTML>";
    messageLabel.setText( text );
  }
  
  public void populateComboBox( Component component, Object[] array )
  {
    if ( !( component instanceof JComboBox ) )
    {
      return;
    }
    JComboBox comboBox = ( JComboBox )component;
    ( (DefaultComboBoxModel )comboBox.getModel() ).removeAllElements();
    if ( array == null )
    {
      return;
    }
    for ( int i = 0; i < array.length; i++ )
    {
      comboBox.addItem( array[ i ] );
    }
  }

  @Override
  public void itemStateChanged( ItemEvent e )
  {
    for ( int i = 0; i < pfButtons.length; i++ )
    {
      if ( pfButtons[ i ] == e.getSource() )
      {
        if ( pfButtons[ i ].isSelected() )
        {
          setPFPanel();
        }
        return;
      }
    }
    saveOptionButtons();
    if ( codeTable.getSelectedRowCount() == 1 )
    {
      Hex hex = protocol.getCode( procs[ codeTable.getSelectedRow() ] );
      Protocol prot = ( ( hex == null || hex.length() == 0 ) && displayProtocol != null ) ?
          displayProtocol : protocol;
      assemblerModel.disassemble( prot, procs[ codeTable.getSelectedRow() ] );
    }
  }
  
  private void saveOptionButtons()
  {
    int opt = 0;
    for ( int i = 0; i < optionButtons.length; i++ )
    {
      opt |= optionButtons[ i ].isSelected() ? 1 << i : 0;
    }
    if ( opt == 7 )
    {
      properties.remove( "AssemblerOptions" );
    }
    else
    {
      properties.setProperty( "AssemblerOptions", "" + opt );
    }
  }
  
  private void setOptionButtons()
  {
    int opt = 7;
    try
    {
      opt = Integer.parseInt( properties.getProperty( "AssemblerOptions", "7" ) );
    }
    catch ( NumberFormatException e )
    {
      e.printStackTrace();
    }

    for ( int i = 0; i < optionButtons.length; i++ )
    {
      optionButtons[ i ].setSelected( ( ( opt >> i ) & 1 ) == 1 );
    }
  }
  
  public void setSelectedCode( Processor proc )
  {
    for ( int i = 0; i < procs.length; i++ )
    {
      if ( procs[ i ].getEquivalentName().equals( proc.getEquivalentName() ) )
      {
        codeTable.getSelectionModel().setSelectionInterval( i, i );
        break;
      }
    }
  }
  
  private int getWidth( String text )
  {
    return ( new JLabel( text ) ).getPreferredSize().width + 4;
  }

  public Integer[] getPdValues()
  {
    return pdValues;
  }

  public Integer[] getPfValues()
  {
    return pfValues;
  }

  public void setDataStyle( int dataStyle )
  {
    this.dataStyle = dataStyle;
  }

  public void setProcessor( Processor processor )
  {
    this.processor = processor;
  }

  public void setAbsUsed( List< Integer > absUsed )
  {
    this.absUsed = absUsed;
  }

  public void setZeroUsed( List< Integer > zeroUsed )
  {
    this.zeroUsed = zeroUsed;
  }
  
  
}


