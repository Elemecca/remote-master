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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
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
import javax.swing.text.DefaultFormatter;
import javax.swing.text.Document;
import javax.swing.text.NumberFormatter;

import com.hifiremote.jp1.AssemblerOpCode.OpArg;
import com.hifiremote.jp1.AssemblerOpCode.Token;
import com.hifiremote.jp1.AssemblerTableModel.DisasmState;
import com.hifiremote.jp1.assembler.CommonData;

// TODO: Auto-generated Javadoc
/**
 * The Class ManualSettingsDialog.
 */
@SuppressWarnings( "unchecked" )
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
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int col )
    {
      Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, col );
      CodeTableModel model = ( CodeTableModel )table.getModel();
      if ( isSelected )
      {
        c.setForeground( ( Boolean )model.getValueAt( row, 4 ) ? Color.YELLOW : Color.WHITE );
      }
      else
      {
        c.setForeground( ( Boolean )model.getValueAt( row, 4 ) ? Color.GRAY : Color.BLACK );
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

    leftPanel.addComponentListener( new ComponentAdapter()
    {
      public void componentResized( ComponentEvent e )
      {
        interpretPFPD();
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
        }
    // rows
    };

    double size2[][] =
    {
        {
            b, pr, c, pf, b
        }, // cols
        {
            b, pr, b, pr, b, pr, b, pr, b
        }
    // rows
    };

    double size3[][] =
    {
        {
            b, pr, c, pf, b, pr, b
        }, // cols
        null
    // rows set later
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
    codeTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
    tablePanel.add( new JScrollPane( codeTable ), BorderLayout.CENTER );
    DefaultTableCellRenderer r = ( DefaultTableCellRenderer )codeTable.getDefaultRenderer( String.class );
    r.setHorizontalAlignment( SwingConstants.CENTER );
    codeTable.setDefaultEditor( Hex.class, new HexCodeEditor() );   
    codeTable.getSelectionModel().addListSelectionListener( this );
    JLabel l = ( JLabel )codeTable.getTableHeader().getDefaultRenderer()
        .getTableCellRendererComponent( codeTable, colNames[ 0 ], false, false, 0, 0 );

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
    itemLists = new List[ procs.length ];
    for ( int i = 0; i < procs.length; i++ )
    {
      l.setText( procs[ i ].getFullName() );
      width = Math.max( width, l.getPreferredSize().width );
      itemLists[ i ] = new ArrayList< AssemblerItem >();
      itemLists[ i ].add( new AssemblerItem() );
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
    d.width = ( int )( d.width * scale );
    codeTable.setPreferredScrollableViewportSize( d );

    upperPanel = mainPanel;

    JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
    importButton = new JButton( "Import Protocol Upgrade" );
    importButton.addActionListener( this );
    importButton.setToolTipText( "Import Protocol Upgrades(s) from the Clipboard" );
    // importButton.setEnabled( false );
    buttonPanel.add( importButton );

    JPanel messagePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    messagePanel.add( messageLabel );
    setMessage( 2 );

    JPanel midPanel = new JPanel( new BorderLayout() );
    midPanel.add( buttonPanel, BorderLayout.CENTER );
    midPanel.add( messagePanel, BorderLayout.PAGE_END );
    tablePanel.add( midPanel, BorderLayout.PAGE_END );

    // Create lower part of left panel as tabbed pane
    mainPanel = new JPanel( new TableLayout( size2 ) );
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab( "Device Data", mainPanel );
    tabbedPane.addChangeListener( this );
    leftPanel.add( tabbedPane, BorderLayout.CENTER );

    // Device Parameter Table on Device Data tab
    deviceModel = new ParameterTableModel( protocol, ParameterTableModel.Type.DEVICE );
    deviceTable = new JTableX( deviceModel );
    SpinnerCellEditor editor = new SpinnerCellEditor( 0, 8, 1 );
    new TextPopupMenu(
        ( JTextField )( ( DefaultCellEditor )deviceTable.getDefaultEditor( String.class ) ).getComponent() );
    deviceTable.setDefaultEditor( Integer.class, editor );
    JScrollPane scrollPane = new JScrollPane( deviceTable );
    tablePanel = new JPanel( new BorderLayout() );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Device Parameters" ) );
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 1, 3, 1" );
    d = deviceTable.getPreferredScrollableViewportSize();
    d.height = deviceTable.getRowHeight() * 4;
    d.width = ( int )( d.width * scale );
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
    new TextPopupMenu(
        ( JTextField )( ( DefaultCellEditor )commandTable.getDefaultEditor( String.class ) ).getComponent() );
    scrollPane = new JScrollPane( commandTable );
    tablePanel = new JPanel( new BorderLayout() );
    tablePanel.setBorder( BorderFactory.createTitledBorder( "Command Parameters" ) );
    tablePanel.add( scrollPane, BorderLayout.CENTER );
    mainPanel.add( tablePanel, "1, 5, 3, 5" );
    d = commandTable.getPreferredScrollableViewportSize();
    d.height = commandTable.getRowHeight() * 4;
    d.width = ( int )( d.width * scale );
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
    protDataScrollPane = new JScrollPane( mainPanel );
    protDataScrollPane.setPreferredSize( protDataScrollPane.getPreferredSize() ); // needed to limit height of pane
    tabbedPane.addTab( "Protocol Data", protDataScrollPane );
    basicValues = new Short[ 3 ];
    NumberFormat nf = NumberFormat.getInstance();
    nf.setGroupingUsed( false );
    frequency = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    dutyCycle = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    altFreq = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    altDuty = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );

    nf.setParseIntegerOnly( true );
    rptValue = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    burst1On = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    burst1Off = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    burst0On = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    burst0Off = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    afterBits = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    leadInOn = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    leadInOff = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    leadOutOff = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );
    altLeadOut = new RMFormattedTextField( new RMNumberFormatter( ( NumberFormat )nf.clone() ), pfpdListener );

    dataComponents = new Component[][]{ 
        { frequency }, { dutyCycle }, { sigStruct }, null,
        { devBytes }, { devBits1, devBits1lbl }, { devBits2, devBits2lbl }, { devBitDbl }, null,
        { cmdBytes }, { cmdBits1, cmdBits1lbl }, { cmdBits2, cmdBits2lbl }, { cmdBitDbl }, null,
        { rptType }, { rptHold }, { rptValue, rptValueLbl }, null,
        { burst1On }, { burst1Off }, null,
        { burst0On }, { burst0Off }, { xmit0rev }, null,
        { leadInStyle }, { leadInOn, leadInOnLbl }, { leadInOff, leadInOffLbl }, null,
        { leadOutStyle }, { leadOutOff }, { offAsTotal, offAsTotalLbl }, null,
        { useAltLeadOut }, { altLeadOut, altLeadOutLbl }, null,
        { useAltFreq }, { altFreq, altFreqLbl }, { altDuty, altDutyLbl }, null, null, null,
        { burstMidFrame, burstMidFrameLbl }, { afterBits, afterBitsLbl }
//        { chkByteStyle }, { bitsHeld }, null,
//        { miniCombiner }, { sigStyle }, null,
//        { vecOffset }, { dataOffset }, null,
//        { toggleBit }
    };

    for ( int i = 0; i < dataComponents.length; i++ )
    {
      if ( dataComponents[ i ] == null )
      {
        if ( dataLabels[ i ] == null )
          continue;
        label = new JLabel( dataLabels[ i ][ 0 ], SwingConstants.CENTER );
        label.setFocusable( false );
        mainPanel.add( label, "1, " + ( i + 1 ) + ", 5, " + ( i + 1 ) );
        continue;
      }
      if ( dataComponents[ i ].length > 1 )
      {
        label = ( JLabel )dataComponents[ i ][ 1 ];
      }
      else
      {
        label = new JLabel();
      }
      label.setFocusable( false );
      label.setText( dataLabels[ i ][ 0 ] );
      mainPanel.add( label, "1, " + ( i + 1 ) );
      mainPanel.add( dataComponents[ i ][ 0 ], "3, " + ( i + 1 ) );
      if ( dataLabels[ i ].length > 1 )
      {
        label = new JLabel( dataLabels[ i ][ 1 ] );
        label.setFocusable( false );
        mainPanel.add( label, "5, " + ( i + 1 ) );
      }
    }

    // PF Details tab of lower left panel (added to tabbed pane by valueChanged() when a protocol is selected)
    pfMainPanel = new JPanel( new BorderLayout() );
    pfPanel = new JPanel();
    pfScrollPane = new JScrollPane( pfPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
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
    pfBoxes = new JComboBox[ CommonData.pfData.length ][];
    pfValues = new Short[ CommonData.pfData.length ];
    for ( int i = 0; i < pfButtons.length; i++ )
    {
      pfButtons[ i ] = new JRadioButton( "PF" + i, false );
      pfButtons[ i ].addItemListener( this );
      pfChoice.add( pfButtons[ i ] );
      grp.add( pfButtons[ i ] );
      pfBoxes[ i ] = new JComboBox[ CommonData.pfData[ i ].length ];
    }

    // PD Details tab of lower left panel (added to tabbed pane by valueChanged() when a protocol is selected)
    pdMainPanel = new JPanel( new BorderLayout() );
    pdPanel = new JPanel();
    pdScrollPane = new JScrollPane( pdPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
    pdScrollPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    pdMainPanel.add( pdScrollPane, BorderLayout.CENTER );
    pdHeaderPanel = new JPanel( new BorderLayout() );
    pdMainPanel.add( pdHeaderPanel, BorderLayout.PAGE_START );
    int n = 0;
    for ( int i = 0; i < CommonData.pdData.length; i++ )
    {
      if ( CommonData.pdData[ i ][ 0 ] != null )
        n += Integer.parseInt( CommonData.pdData[ i ][ 0 ] );
    }
    pdValues = new Short[ n ];
    pdFields = new ArrayList< RMFormattedTextField >();
    pdSizes = new ArrayList< int[] >();

    // Function tab of lower left panel (added to tabbed pane by valueChanged() when a protocol is selected)
    fnMainPanel = new JPanel( new BorderLayout() );
    fnPanel = new JPanel();
    fnScrollPane = new JScrollPane( fnPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
    fnScrollPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    fnMainPanel.add( fnScrollPane, BorderLayout.CENTER );
    fnHeaderPanel = new JPanel( new BorderLayout() );
    fnMainPanel.add( fnHeaderPanel, BorderLayout.PAGE_START );
    tabbedPane.add( "Functions", fnMainPanel );

    // Disassembly on right pane
    assemblerTable = new JP1Table( assemblerModel );
    assemblerTable.initColumns( assemblerModel );
    assemblerTable.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
    assemblerTable.getSelectionModel().addListSelectionListener( this );
    assemblerModel.dialog = this;
    scrollPane = new JScrollPane( assemblerTable );
    asmBorder = BorderFactory.createTitledBorder( "" ); // Title added by setAssemblerButtons()
    scrollPane.setBorder( asmBorder );
    rightPanel.add( scrollPane, BorderLayout.CENTER );
    JPanel optionPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 5, 0 ) );
    optionPanel.add( new JLabel( "Show: " ) );
    optionPanel.add( disasmButton );
    optionPanel.add( asmButton );
    optionPanel.setMinimumSize( new Dimension( 10, 10 ) );
    disasmButton.addItemListener( this );
    asmButton.addItemListener( this );
    grp = new ButtonGroup();
    grp.add( disasmButton );
    grp.add( asmButton );
    disasmButton.setSelected( true );
    rightPanel.add( optionPanel, BorderLayout.PAGE_START );

    // Disassembly options
    JPanel lowerRightPanel = new JPanel();
    lowerRightPanel.setLayout( new BoxLayout( lowerRightPanel, BoxLayout.PAGE_AXIS ) );
    JPanel optionsPanel = new JPanel();
    optionsPanel.setLayout( new BoxLayout( optionsPanel, BoxLayout.PAGE_AXIS ) );
    optionsPanel.setBorder( BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder( "Disassembly options" ), BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) ) );
    lowerRightPanel.add( optionsPanel );
    rightPanel.add( lowerRightPanel, BorderLayout.PAGE_END );
    optionPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 5, 0 ) );
    useRegisterConstants.addItemListener( this );
    useFunctionConstants.addItemListener( this );
    optionPanel.add( new JLabel( "Use predefined constants for: " ) );
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
    optionPanel.add( new JLabel( "S3C80 only:" ) );
    optionPanel.add( asCodeButton );
    optionPanel.add( rcButton );
    optionPanel.add( wButton );
    optionsPanel.add( optionPanel );

    // Editing/Assembling buttons
    optionPanel = new JPanel( new GridLayout( 3, 5 ) );
    optionPanel.add( insert );
    optionPanel.add( copy );
    optionPanel.add( new JLabel() );
    optionPanel.add( load );
    optionPanel.add( assemble );
    optionPanel.add( delete );
    optionPanel.add( cut );
    optionPanel.add( new JLabel() );
    optionPanel.add( save );
    optionPanel.add( build );
    optionPanel.add( selectAll );
    optionPanel.add( paste );
    optionPanel.add( new JLabel() );
    optionPanel.add( disassemble );
    optionPanel.add( getData );
    moveUp.addActionListener( this );
    moveDown.addActionListener( this );
    insert.addActionListener( this );
    delete.addActionListener( this );
    cut.addActionListener( this );
    paste.addActionListener( this );
    paste.setEnabled( false );
    load.addActionListener( this );
    load.setEnabled( false );
    save.addActionListener( this );
    assemble.addActionListener( this );
    getData.addActionListener( this );
    selectAll.addActionListener( this );
    disassemble.addActionListener( this );
    build.addActionListener( this );
    copy.addActionListener( this );
    insert.setToolTipText( "Inserts above selection a number of rows equal to the number selected." );
    delete.setToolTipText( "Deletes the rows containing selected cells." );
    copy.setToolTipText( "Copies to clipboard the rows containing selected cells." );
    cut.setToolTipText( "Copies to clipboard the rows containing selected cells, then deletes these rows." );
    paste.setToolTipText( "Inserts rows from clipboard above current selection." );
    load.setToolTipText( "Opens dialog to select an assembler listing text file for loading." );
    save.setToolTipText( "Opens dialog to save assembler listing as text file." );
    build.setToolTipText( "Builds complete assembler listing for a protocol from data in Protocol Data tab." );
    getData.setToolTipText( "Updates the data section of an assembler listing from Protocol Data tab." );
    assemble.setToolTipText( "Assembles binary code from assembler listing and updates protocol code with result." );
    disassemble.setToolTipText( "Replaces assembler listing with a disassembly of current protocol code." );
    selectAll.setToolTipText( "Selects all the rows of the currently selected listing." );
    optionPanel.setMinimumSize( new Dimension( 10, 10 ) );
    lowerRightPanel.add( optionPanel );
    setAssemblerButtons( true );

    JP1Frame frame = RemoteMaster.getFrame();
    cutItems = ( frame instanceof RemoteMaster ) ? ( ( RemoteMaster )frame ).getClipBoardItems() : 
      ( frame instanceof KeyMapMaster ) ? ( ( KeyMapMaster )frame ).getClipBoardItems() : new ArrayList< AssemblerItem >();

    // Button Panel
    JPanel mainButtonPanel = new JPanel( new BorderLayout() );
    buttonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
    mainButtonPanel.add( buttonPanel, BorderLayout.LINE_START );

    codeButton = new JButton( "Expand" );
    codeButton
        .setToolTipText( "<HTML>Expand/Collapse the lower tabbed panel.  When expanded<BR>it hides the upper panel of protocol codes.</HTML>" );
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
    d.width = ( int )( leftPanel.getPreferredSize().width * 0.95 / scale );
    rightPanel.setPreferredSize( d );

    pack();
    Rectangle rect = getBounds();
    int x = rect.x - rect.width / 2;
    int y = rect.y - rect.height / 2;
    setLocation( x, y );

    tabbedPane.setSelectedIndex( 1 );
    
    isEmpty = new boolean[ procs.length ];
    for ( int i = 0; i < procs.length; i++ )
    {
      Hex hex = protocol.getCode( procs[ i ] );
      isEmpty[ i ] = ( hex == null || hex.length() == 0 );
    }
  }

  public void setForCustomCode()
  {
    // pid.setEditable( false );
    // pid.setEnabled( false );

    deviceTable.setEnabled( false );
    deviceTable.setForeground( Color.GRAY );

    commandTable.setEnabled( false );
    commandTable.setForeground( Color.GRAY );

    rawHexData.setEnabled( false );
    cmdIndex.setEnabled( false );

//    tabbedPane.setSelectedIndex( 1 );

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
      JPanel panel = new JPanel( new BorderLayout() );
      JLabel message = new JLabel( "Enter one or more PB-/KM-/IR-formatted protocol upgrades below." );
      message.setBorder( BorderFactory.createEmptyBorder( 5, 5, 10, 5 ) );
      panel.add( message, BorderLayout.NORTH );

      JTextArea textArea = new JTextArea( 10, 60 );
      new TextPopupMenu( textArea );

      JScrollPane scrollPane = new JScrollPane( textArea );
      scrollPane.setBorder( BorderFactory.createCompoundBorder(
          BorderFactory.createTitledBorder( "Protocol Upgrade Code" ), scrollPane.getBorder() ) );
      panel.add( scrollPane, BorderLayout.CENTER );
      int rc = JOptionPane.showConfirmDialog( this, panel, "Import Protocol Upgrade", JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.PLAIN_MESSAGE, null );

      if ( rc == JOptionPane.OK_OPTION )
      {
        importProtocolCode( textArea.getText() );
      }

      if ( codeTable.getSelectedRowCount() == 1 )
      {
        Processor proc = procs[ codeTable.getSelectedRow() ];
        assemblerModel.disassemble( protocol.getCode( proc ), proc );
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
    else if ( Arrays.asList( insert, delete, moveUp, moveDown, copy, cut, paste ).contains( source ) )
    {
      if ( assemblerTable.getCellEditor() != null )
      {
        assemblerTable.getCellEditor().stopCellEditing();
      }
      List< AssemblerItem > itemList = assemblerModel.getItemList();
      int row = assemblerTable.getSelectedRow();
      int col = assemblerTable.getSelectedColumn();
      int rowCount = assemblerTable.getSelectedRowCount();
      int colCount = assemblerTable.getSelectedColumnCount();
      if ( source == insert )
      {
        for ( int i = 0; i < rowCount; i++ )
          itemList.add( row, new AssemblerItem() );
        assemblerModel.fireTableDataChanged();
        assemblerTable.changeSelection( row + rowCount, col, false, false );
        assemblerTable.changeSelection( row + 2 * rowCount - 1, col + colCount - 1, false, true );
      }
      else if ( source == paste )
      {
        itemList.addAll( row, cutItems );
        assemblerModel.fireTableDataChanged();
        assemblerTable.changeSelection( row, col, false, false );
        assemblerTable.changeSelection( row + cutItems.size() - 1, col + colCount - 1, false, true );
      }
      else if ( source == delete || source == cut || source == copy )
      {
        if ( source == cut || source == copy )
        {
          cutItems.clear();
          cutItems.addAll( itemList.subList( row, row + rowCount ) );
          paste.setEnabled( true );
        }
        if ( source == delete || source == cut )
        {
          for ( int i = 0; i < rowCount; i++ )
            itemList.remove( row );
          if ( itemList.size() == 0 ) itemList.add( new AssemblerItem() );
          assemblerModel.fireTableDataChanged();
          assemblerTable.changeSelection( row, col, false, false );
          assemblerTable.changeSelection( row, col + colCount - 1, false, true );
        }
      }
//      else if ( source == moveUp && row > 0 )
//      {
//        AssemblerItem item = itemList.get( row - 1 );
//        itemList.remove( row - 1 );
//        itemList.add( row + rowCount - 1, item );
//        assemblerModel.fireTableDataChanged();
//        assemblerTable.changeSelection( row - 1, col, false, false );
//        assemblerTable.changeSelection( row + rowCount - 2, col + colCount - 1, false, true );
//      }
//      else if ( source == moveDown && row < itemList.size() - 1 )
//      {
//        AssemblerItem item = itemList.get( row + rowCount );
//        itemList.remove( row + rowCount );
//        itemList.add( row, item );
//        assemblerModel.fireTableDataChanged();
//        assemblerTable.changeSelection( row + 1, col, false, false );
//        assemblerTable.changeSelection( row + rowCount, col + colCount - 1, false, true );
//      }
      setAssemblerButtons( false );
    }
    else if ( source == selectAll )
    {
      assemblerTable.changeSelection( 0, 0, false, false );
      assemblerTable.changeSelection( assemblerModel.getItemList().size() - 2, assemblerTable.getColumnCount() - 1, false, true );
    }
    else if ( source == disassemble )
    {
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        int row = codeTable.getSelectedRow();
        Processor proc = procs[ row ];
        Hex hex = protocol.getCode( procs[ row ] );
        if ( ( hex == null || hex.length() == 0 ) && displayProtocol != null )
          hex = displayProtocol.getCode( proc );
        assemblerModel.disassemble( hex, proc );
        setAssemblerButtons( false );
      }
    }
    else if ( source == save )
    {
      PropertyFile properties = JP1Frame.getProperties();
      RMFileChooser chooser = new RMFileChooser( properties.getProperty( "IRPath" ) );
      EndingFileFilter txtFilter = new EndingFileFilter( "Text files (*.txt)", RemoteMaster.txtEndings );
      chooser.setFileFilter( txtFilter );

      if ( file != null )
      {
        chooser.setSelectedFile( file );
      }
      int returnVal = chooser.showSaveDialog( this );
      if ( returnVal == RMFileChooser.APPROVE_OPTION )
      {
        String ending = ( ( EndingFileFilter )chooser.getFileFilter() ).getEndings()[ 0 ];
        String name = chooser.getSelectedFile().getAbsolutePath();
        if ( !name.toLowerCase().endsWith( ending ) )
        {
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
        file = newFile;
        properties.setProperty( "IRPath", file.getParentFile() );
        if ( asmButton.isSelected() && codeTable.getSelectedRowCount() == 1 )
        {
          int row = codeTable.getSelectedRow();
          itemLists[ row ].clear();
          itemLists[ row ].addAll( assemblerModel.getItemList() );
        }
        try
        {
          PrintWriter pw = new PrintWriter( new FileWriter( file ) );
          boolean first = true;
          for ( int row = 0; row < itemLists.length; row++ )
          {
            List< AssemblerItem > list = itemLists[ row ];
            if ( list.size() <= 1 ) continue;
            if ( !first ) pw.print( "\n" );
            first = false;
            String line = "\tPROC\t" + procs[ row ].getEquivalentName() + "\n"; 
            pw.print( line );
            for ( int i = 0; i < itemLists[ row ].size(); i++ )
            {
              AssemblerItem item = itemLists[ row ].get( i );
              line = "";
              String str = item.getLabel().trim();
              line += str;
              if ( !str.isEmpty() && !str.equals( ";" ) && !str.endsWith( ":" ) )
                line += ":";
              line += "\t" + item.getOperation() + "\t" + item.getArgumentText();
              str = item.getComments();
              if ( !str.isEmpty() )
              {
                line += "\t";
                if ( !str.startsWith( ";" ) )
                  line += ";";
                line += str;
              }
              line += "\n";
              int j = 0;
              while ( j < line.length() && Character.isWhitespace( line.charAt( j ) ) )
                j++ ;
              if ( j < line.length() || i < assemblerModel.getItemList().size() - 1 )
              {
                pw.print( line );
              }
            }
          }
          pw.close();
        }
        catch ( IOException ex )
        {
          ex.printStackTrace( System.err );
        }
      }
    }
    else if ( source == load )
    {
      PropertyFile properties = JP1Frame.getProperties();
      File loadFile = null;
      while ( loadFile == null )
      {
        RMFileChooser chooser = new RMFileChooser( properties.getProperty( "IRPath" ) );
        EndingFileFilter txtFilter = new EndingFileFilter( "Text files (*.txt)", RemoteMaster.txtEndings );
        chooser.setFileFilter( txtFilter );
        int returnVal = chooser.showOpenDialog( this );
        if ( returnVal == RMFileChooser.APPROVE_OPTION )
        {
          loadFile = chooser.getSelectedFile();

          if ( !loadFile.exists() )
          {
            JOptionPane.showMessageDialog( this, loadFile.getName() + " doesn't exist.", "File doesn't exist.",
                JOptionPane.ERROR_MESSAGE );
          }
          else if ( loadFile.isDirectory() )
          {
            JOptionPane.showMessageDialog( this, loadFile.getName() + " is a directory.", "File doesn't exist.",
                JOptionPane.ERROR_MESSAGE );
          }
        }
        else
        {
          return;
        }
      }
      try
      {
        file = loadFile;
        properties.setProperty( "IRPath", file.getParentFile() );
        DataInputStream in = new DataInputStream( new FileInputStream( file ) );
        BufferedReader br = new BufferedReader( new InputStreamReader( in ) );
        String line = null;
        List< AssemblerItem > list = null;
        if ( codeTable.getSelectedRowCount() == 1 )
        {
          list = itemLists[ codeTable.getSelectedRow() ];
        }
        boolean hasItems = false;
        
//        List< AssemblerItem > itemList = assemblerModel.getItemList();
//        itemList.clear();
        while ( ( line = br.readLine() ) != null )
        {
          if ( line.isEmpty() ) continue;
          AssemblerItem item = new AssemblerItem();
          char firstChar = line.charAt( 0 );
          line = line.trim();
          int j = 0;
          while ( j < line.length() && !Character.isWhitespace( line.charAt( j ) ) )
            j++ ;
          if ( j == 0 ) continue;
          if ( line.charAt( j - 1 ) == ':' || j == 1 && firstChar == ';' )
          {
            item.setLabel( line.substring( 0, j ) );
            line = line.substring( j ).trim();
            j = 0;
            while ( j < line.length() && !Character.isWhitespace( line.charAt( j ) ) )
              j++ ;
          }
          if ( j == 0 ) continue;
          if ( line.charAt( 0 ) != ';' )
          {
            item.setOperation( line.substring( 0, j ) );
            line = line.substring( j ).trim();
          }
          int ndx = line.indexOf( ';' );
          if ( ndx > 0 )
          {
            item.setArgumentText( line.substring( 0, ndx ).trim() );
            item.setComments( line.substring( ndx + 1 ) );
          }
          else if ( ndx < 0 )
          {
            item.setArgumentText( line );
          }
          else
          {
            item.setComments( line.substring( 1 ) );
          }
          if ( item.getOperation().equals( "PROC" ) )
          {
            if ( hasItems ) list.add( new AssemblerItem() );
            for ( int k = 0; k < procs.length; k++ )
            {
              if ( procs[ k ].getEquivalentName().equals( item.getArgumentText() ) )
              {
                list = itemLists[ k ];
                hasItems = false;
                break;
              }
            }
            continue;
          }
          if ( !hasItems ) list.clear();
          list.add( item );
          hasItems = true;
        }
        in.close();
        list.add( new AssemblerItem() );
        asmButton.setSelected( true );
        if ( codeTable.getSelectedRowCount() == 1 )
        {
          assemblerModel.getData().clear();
          assemblerModel.getData().addAll( itemLists[ codeTable.getSelectedRow() ] );
          assemblerModel.setItemList( assemblerModel.getData() );
          assemblerModel.fireTableDataChanged();
        }
      }
      catch ( Exception ex )
      {
        ex.printStackTrace( System.err );
      }
    }

    else if ( source == assemble )
    {
      int row = codeTable.getSelectedRow();
      Processor proc = procs[ row ];
      if ( assemblerTable.getCellEditor() != null )
      {
        assemblerTable.getCellEditor().stopCellEditing();
      }
      Hex hex = assemblerModel.assemble( proc );
      assemblerModel.fireTableDataChanged();
      if ( hex != null )
      {
        if ( codeModel.isCellEditable( row, 1 ) )
        {
          assembled = true;
          codeModel.setValueAt( hex, row, 1 );
          assembled = false;
        }
        else
        {
          String title = "Assemble";
          String message = "The code for this processor is not editable, so the assembled\n" + 
                           "hex code will not be saved in the .rmdu/.rmir file.  You may,\n" +
                           "however, use the Save button to save the assembly source as a\n" +
                           "separate file.";
          JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
        }
      }
    }
    else if ( source == getData || source == build )
    {
      if ( assemblerTable.getCellEditor() != null )
      {
        assemblerTable.getCellEditor().stopCellEditing();
      }
      if ( source == build )
      {
        Iterator< AssemblerItem > it = assemblerModel.getItemList().iterator();
        while ( it.hasNext() )
        {
          if ( !it.next().getOperation().equals( "ORG" ) ) it.remove();
        }
      }
      if ( processor instanceof S3C80Processor && assemblerModel.testBuildMode( processor ) )
      {
        processor = ProcessorManager.getProcessor( "S3C80" );
        ramAddress = processor.getRAMAddress();
      }
      int i = 0;
      boolean hasOrg = false;
      AssemblerItem startItem = null;
      AssemblerItem endItem = null;
      List< AssemblerItem > newItemList = new ArrayList< AssemblerItem >();

      if ( pfValues[ 0 ] == null )
        pfValues[ 0 ] = 0;
      for ( i = 0; i < pfValues.length && pfValues[ i ] != null; i++ )
        ;
      assemblerModel.setPfCount( i );
      showMessages = false;
      if ( !Arrays.asList( pfMainPanel, pdMainPanel ).contains( tabbedPane.getSelectedComponent() ) )
      {
        // Refresh pd values that could be null
        ns = "0";
        interpretPFPD( true );
        ns = "";
        for ( i = 0; i < pfValues.length && pfValues[ i ] != null; i++ );
        assemblerModel.setPfCount( i );
        pfpdListener.actionPerformed( new ActionEvent( sigStruct, ActionEvent.ACTION_PERFORMED, "Internal" ) );
        pfpdListener.actionPerformed( new ActionEvent( leadInStyle, ActionEvent.ACTION_PERFORMED, "Internal" ) );
        pfpdListener.actionPerformed( new ActionEvent( rptType, ActionEvent.ACTION_PERFORMED, "Internal" ) );
        pfpdListener.actionPerformed( new ActionEvent( devBytes, ActionEvent.ACTION_PERFORMED, "Internal" ) );
        pfpdListener.actionPerformed( new ActionEvent( cmdBytes, ActionEvent.ACTION_PERFORMED, "Internal" ) );
        pfpdListener.actionPerformed( new ActionEvent( useAltLeadOut, ActionEvent.ACTION_PERFORMED, "Internal" ) );
        pfpdListener.actionPerformed( new ActionEvent( useAltFreq, ActionEvent.ACTION_PERFORMED, "Internal" ) );
      }

      boolean fill = false;
      short[] fillValues = CommonData.pdDefaults[ dataStyle ];
      for ( i = pdValues.length - 1; i >= 0; i-- )
      {
        if ( fill == false && pdValues[ i ] != null )
        {
          fill = true;
          assemblerModel.setPdCount( i + 1 );
        }
        if ( pdValues[ i ] == null && fill )
          pdValues[ i ] = i < fillValues.length ? fillValues[ i ] : 0;
      }

      Hex hex = new Hex( assemblerModel.getPdCount() + 10 );
      assemblerModel.setHex( hex );
      for ( i = 0; i < processor.getStartOffset(); i++ )
        hex.set( basicValues[ i ], i );
      for ( i = processor.getStartOffset(); i < 3; i++ )
        hex.set( basicValues[ i ], i + 2 );
      for ( i = 0; i < pfValues.length && pfValues[ i ] != null; i++ )
        hex.set( pfValues[ i ], i + 5 );
      for ( int j = 0; j < assemblerModel.getPdCount(); j++ )
        hex.set( pdValues[ j ], i + j + 5 );

      int length = 0;
      int start = 0;
      int endDirectives = 0;
      int end = 2;  // length of JR / BRA instruction
      i = 0;
      for ( i = 0; i < assemblerModel.getItemList().size(); i++ )
      {
        AssemblerItem item = assemblerModel.getItemList().get( i );
        if ( item.isCommentedOut() ) continue;
        if ( item.getOperation().equals( "ORG" ) )
        {
          hasOrg = true;
          for ( Token t : OpArg.getArgs( item.getArgumentText(), null, null ) ) ramAddress = t.value;
          if ( processor instanceof S3C80Processor )
          {
            processor = ProcessorManager.getProcessor( ( ramAddress & 0xC000 ) == 0xC000 ? "S3F80" : "S3C80" );
          }
        }
        if ( length == processor.getStartOffset() )
        {
          String op = item.getOperation();
          if ( op.equals( "JR" ) || op.equals( "BRA" ) )
          {
            start = 0;
            end += length;
            startItem = item;
            int j = i + 1;
            for ( ; j < assemblerModel.getItemList().size(); j++ )
            {
              // search for JR / BRA destination label
              AssemblerItem item2 = assemblerModel.getItemList().get( j );
              if ( item2.isCommentedOut() ) continue;
              if ( Arrays.asList( item.getArgumentText(), item.getArgumentText() + ":" ).contains( item2.getLabel() ) )
                break;
              end += item2.getLength();
            }
            if ( j == assemblerModel.getItemList().size() )
            {
              // label not found, so base end on hex data
              end += item.getHex().getData()[ 1 ];
            }
          }
        }
        length += item.getLength();
        start += item.getLength();
        if ( length == 0 && !( item.getOperation().trim().isEmpty() && item.getArgumentText().trim().isEmpty() && item.getComments().trim().isEmpty() ) )
        {
          newItemList.add( item );
          endDirectives = newItemList.size();
        }
        if ( length >= 5 ) break;
      }
      if ( startItem == null )
      {
        if ( length > processor.getStartOffset() )
        {
          String message = "Cannot get data as code is not a valid protocol format.\n\n"
            + "Get Data will only get data from the Protocol Data etc tabs into a protocol that is\n"
            + "already properly structured, so if you enter code into the listing grid, assemble it and\n"
            + "THEN try to get the data bytes into it, it won't work. Begin with a basic Build. You can\n"
            + "press Build right at the start. You don't need to put the proper, or even any, values\n"
            + "into the Protocol Data tab, it will work with just the defaults and you can change the\n"
            + "data later. The basic build will give you only one assembler instruction following the\n"
            + "data block, JP XmitIR or the equivalent for other processors. Replace this with whatever\n"
            + "code you want.\n\n"
            + "If you forget to do the initial build and have already entered assembler code, use Cut and\n"
            + "Paste. Select it all, Cut, then press Build with the empty assembler grid that results, and\n"
            + "finally Paste the cut data at the end of the build. After that, you can change the data and\n"
            + "use Get Data as required.";
          String title = "Update Data";
          JOptionPane.showMessageDialog( this, message, title, JOptionPane.INFORMATION_MESSAGE );
          return;
        }
        else
        {
          String op = ( processor instanceof S3C80Processor ) ? "JR" : "BRA";
          startItem = new AssemblerItem( ramAddress + processor.getStartOffset(), op, "L0" );
          op = ( processor instanceof S3C80Processor ) ? "JP" : "JMP";
          endItem = new AssemblerItem( 0, op, "XmitIR" );
        }
      }

      DisasmState state = new DisasmState();
      i = assemblerModel.getPfCount() + assemblerModel.getPdCount();
      List< AssemblerItem > oldItemList = assemblerModel.getData();
      assemblerModel.setItemList( newItemList );
      assemblerModel.dbOut( 0, processor.getStartOffset(), ramAddress, 0, processor );
      newItemList.add( startItem );
      assemblerModel.dbOut( processor.getStartOffset() + 2, i + 5, ramAddress, 0, processor );
      if ( endItem == null )
      {
        // Get Data mode
        startItem.getHex().set( ( short )( i + 3 - processor.getStartOffset() ), 1 );
        length = 0;
        i += ramAddress + 5;
        for ( AssemblerItem item : oldItemList )
        {
          if ( length >= end && item.getOpCode() != null )
          {
            item.setAddress( i );
            newItemList.add( item );
            i += item.getLength();
          }
          length += item.getLength();
        }
      }
      else
      {
        // Build mode
        LinkedHashMap< String, String > labels = processor.getAsmLabels();
        labels.put( "L0", String.format( "%04XH", ramAddress + i + 5 ) );
        LinkedHashMap< Integer, String > rptLabels = new LinkedHashMap< Integer, String >();
        rptLabels.put( ramAddress + i + 5, "L0" );
        int rptVal = assemblerModel.getForcedRptCount();
        Hex hx = new Hex( CommonData.forcedRptCode[ dataStyle ] );
        if ( rptType.getSelectedIndex() == 0 && rptVal > 0 )
        {
          short rpt = ( short )( long )rptVal;
          switch ( dataStyle )
          {
            case 0:
              hx.set( rpt, 2 );
              break;
            case 1:
            case 2:
              hx.set( rpt, 1 );
              break;
            case 3:
              int op = hx.getData()[ 0 ] * 0x100;
              hx = new Hex( 2 * ( rpt - 1 ) );
              for ( int j = 0; j < rpt - 1; j++ )
              {
                hx.put( op + +2 * ( rpt - j - 2 ), 2 * j );
              }
              labels.put( "L1", String.format( "%04XH", ramAddress + i + 5 + hx.length() ) );
              rptLabels.put( ramAddress + i + 5 + hx.length(), "L1" );
              break;
            case 4:
              hx.set( ( short )( rpt - 1 ), 1 );
              break;
          }
          while ( hx.length() > 0 )
          {
            AssemblerItem item = new AssemblerItem( ramAddress + i + 5, hx );
            int opLen = item.disassemble( processor, rptLabels, state );
            String lbl = rptLabels.get( item.getAddress() );
            if ( lbl != null )
              item.setLabel( lbl + ":" );
            newItemList.add( item );
            hx = hx.subHex( opLen );
            i += opLen;
          }
          if ( dataStyle != 3 ) state.zeroUsed.add( processor.getZeroAddresses().get( "RPT" ) );
        }
        endItem.setAddress( ramAddress + i + 5 );
        String lbl = rptLabels.get( endItem.getAddress() );
        if ( lbl != null )
          endItem.setLabel( lbl + ":" );
        if ( burstMidFrame.getSelectedIndex() == 1 )
          endItem.setArgumentText( "XmitSplitIR" );
        state.absUsed.add( processor.getAbsAddresses().get( endItem.getArgumentText() ) );
        newItemList.add( endItem );

        startItem.assemble( processor, labels, true );
        endItem.assemble( processor, labels, true );
        assemblerModel.insertEQU( endDirectives, processor, state );
        if ( !hasOrg ) assemblerModel.insertORG( endDirectives, ramAddress, processor );
      }
      newItemList.add( new AssemblerItem() );
      assemblerModel.getData().clear();
      assemblerModel.getData().addAll( newItemList );
      assemblerModel.setItemList( assemblerModel.getData() );
      setAssemblerButtons( false );
      
      interpretPFPD( false );
      setPFPanel();
      setPDPanel();
      showMessages = true;
      assemblerModel.fireTableDataChanged();
    }
    else if ( tabbedPane.getSelectedComponent() == pfMainPanel )
    {
      int m = -1;
      int n = 0;
      for ( ; n < pfButtons.length; n++ )
        if ( pfButtons[ n ].isSelected() )
          break;
      if ( n < pfButtons.length )
        m = Arrays.asList( pfBoxes[ n ] ).indexOf( e.getSource() );
      if ( m >= 0 && !isSettingPF )
      {
        int bitStart = Integer.parseInt( CommonData.pfData[ n ][ m ][ 1 ].substring( 0, 1 ) );
        int bitCount = Integer.parseInt( CommonData.pfData[ n ][ m ][ 0 ] );
        int mask = ~( ( ( 1 << bitCount ) - 1 ) << bitStart );
        int val = 0;
        JComboBox combo = pfBoxes[ n ][ m ];
        String text = ( String )combo.getSelectedItem();
        if ( Character.isDigit( text.charAt( 0 ) ) )
        {
          val = Integer.parseInt( text.substring( 0, 1 ) );
        }
        else
        // Handle "other"
        {
          for ( ; val < combo.getItemCount() - 1; val++ )
          {
            text = ( String )combo.getModel().getElementAt( val );
            if ( val != Integer.parseInt( text.substring( 0, 1 ) ) )
              break;
          }
        }
        pfValues[ n ] = ( short )( ( pfValues[ n ] & mask ) | ( val << bitStart ) );
        if ( bitStart == 7 )
        {
          isSettingPF = true;
          if ( val == 0 )
            for ( int i = n + 1; i < pfButtons.length; i++ )
            {
              pfButtons[ i ].setEnabled( false );
              pfValues[ i ] = null;
            }
          else
          {
            pfButtons[ n + 1 ].setEnabled( true );
            pfValues[ n + 1 ] = 0;
          }
          isSettingPF = false;
        }
      }
    }
    else if ( tabbedPane.getSelectedComponent() == pdMainPanel )
    {
      int n = pdFields.indexOf( source );
      if ( n >= 0 )
      {
        int index = pdSizes.get( n )[ 0 ];
        if ( pdSizes.get( n )[ 1 ] >> 4 == 0 )
        {
          Hex hex = ( Hex )pdFields.get( n ).getValue();
          Arrays.fill( pdValues, index, index + pdSizes.get( n )[ 1 ], null );
          for ( int i = 0; i < hex.length(); i++ )
            pdValues[ index + i ] = hex.getData()[ i ];

          int p = 0;
          for ( ; pdSizes.get( p )[ 0 ] != index; p++ )
            ;
          for ( ; p < pdSizes.size() && pdSizes.get( p )[ 0 ] == index; p++ )
            if ( pdSizes.get( p )[ 1 ] >> 4 == 0 )
            {
              Short val1 = pdValues[ index ];
              Short val2 = pdValues[ index + 1 ];
              int type = pdSizes.get( p )[ 1 ];
              if ( p != n )
                pdFields.get( p ).setValue(
                    ( type == 2 && val2 != null ) ? String.format( "%04X", val1 * 0x100 + val2 )
                        : ( val1 != null ) ? String.format( "%02X", val1 ) : "" );
              hex = ( Hex )pdFields.get( p ).getValue();
              type = pdSizes.get( p + 1 )[ 1 ] & 0xF;
              if ( type == 1 && hex.length() == 1 )
              {
                pdFields.get( p + 1 ).setValue( "" + hex.getData()[ 0 ] );
              }
              else if ( type == 4 && hex.length() >= 1 )
              {
                double m = ( dataStyle == 0 ) ? 2.0 : 0.0;
                double d = ( dataStyle == 0 ) ? 8.0 : 4.0;
                pdFields.get( p + 1 ).setValue( "" + ( hex.getData()[ 0 ] + m ) / d );
                if ( hex.length() > 1 )
                  pdFields.get( p + 2 ).setValue( "" + ( hex.getData()[ 1 ] + m ) / d );
              }
              else if ( hex.length() == 2 )
              {
                int time = 2 * hex.get( 0 ) + ( ( type == 3 && dataStyle == 0 ) ? 40 : 0 );
                pdFields.get( p + 1 ).setValue( "" + time );
              }
              else
                for ( int i = 1; p + i < pdSizes.size() && pdSizes.get( p + i )[ 1 ] >> 4 == i; i++ )
                {
                  pdFields.get( p + i ).setValue( "" );
                }
            }
        }
        else
        {
          int type = pdSizes.get( n )[ 1 ] & 0xF;
          int pos = pdSizes.get( n )[ 1 ] >> 4;
          Object obj = pdFields.get( n ).getValue();
          RMFormattedTextField pdField = pdFields.get( n - pos );
          if ( obj == null )
          {
            Hex hex = ( Hex )pdField.getValue();
            pdField.setValue( hex.subHex( 0, Math.min( hex.length(), pos - 1 ) ).toString() );
          }
          else if ( type == 1 )
          {
            pdField.setValue( String.format( "%02X", ( Long )obj & 0xFF ) );
          }
          else if ( type == 4 )
          {
            Hex hex = ( Hex )pdFields.get( n - pos ).getValue();
            double val = ( obj instanceof Long ) ? ( Long )obj : ( Double )obj;
            val *= ( dataStyle == 0 ) ? 8.0 : 4.0;
            val -= ( dataStyle == 0 ) ? 2.0 : 0.0;
            hex = new Hex( hex, 0, Math.max( hex.length(), pos ) );
            hex.set( ( short )( ( int )( val + 0.5 ) & 0xFF ), pos - 1 );
            pdField.setValue( hex.toString() );
          }
          else
          // types 2, 3
          {
            long val = ( Long )obj;
            val -= ( type == 3 && dataStyle == 0 ) ? 40 : 0;
            val /= 2;
            pdField.setValue( String.format( "%04X", val & 0xFFFF ) );
          }
          pdField.update();
        }
      }
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
            String message = "There is a Device Upgrade that is using a protocol upgrade with\n" + "PID " + id
                + " Do you want to abort this PID choice and enter\n" + "a different one?  If so, please press OK.\n\n"
                + "If you want to edit that protocol code, also press OK, then exit\n"
                + "this dialog, change to the Devices page and edit the protocol of\n"
                + "the device upgrade from there.\n\n"
                + "To continue, press CANCEL but you will be creating a Manual Protocol\n"
                + "that cannot be accessed while that Device Upgrade is present.";
            exit = ( JOptionPane.showConfirmDialog( null, message, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE ) == JOptionPane.OK_OPTION );
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
      enableButtons();
    }
    else if ( doc == rawHexData.getDocument() )
    {
      protocol.setRawHex( new Hex( rawHexData.getText() ) );
      enableButtons();
    }
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
      if ( displayRemote != null
          && !procs[ row ].getEquivalentName().equals( displayRemote.getProcessor().getEquivalentName() ) )
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
      // We want dispHex to be the official code, not custom code when that is present.
      Hex dispHex = ( displayProtocol == null ) ? null : displayProtocol.code.get( procs[ row ].getEquivalentName() );
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
        case 2:
          return itemLists[ row ];
        default:
          // There are no other columns but value 4 is used by cell renderer
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
      switch ( col )
      {
        case 1:
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
              newCode = codeWhenNull;
            }
            else
            {
              newCode = new Hex();
            }
          }

          Processor proc = procs[ row ];
          protocol.setCode( newCode, proc );
          fireTableRowsUpdated( row, row );
          enableButtons();
          int pfCount = assemblerModel.getPfCount();
          if ( assembled )
          {
            short[] data = newCode.getData();
            Arrays.fill( pfValues, null );
            Arrays.fill( pdValues, null );
            for ( int i = 0; i < pfCount; i++ )
            {
              pfValues[ i ] = data[ 5 + i ];
            }
            for ( int i = 0; i < assemblerModel.getPdCount(); i++ )
            {
              pdValues[ i ] = data[ 5 + pfCount + i ];
            }
          }
          else
          {
            assemblerModel.disassemble( newCode, proc );
          }

          if ( proc instanceof S3C80Processor
              && ( ( S3C80Processor )proc ).testCode( newCode ) == S3C80Processor.CodeType.NEW )
          {
            proc = ProcessorManager.getProcessor( "S3F80" ); // S3C8+ code
          }
          // int addr = proc.getRAMAddress();
          interpretPFPD();
          setPFPanel();
          setPDPanel();
          setFunctionPanel();
          break;
        case 2:
          itemLists[ row ] = ( List< AssemblerItem > )value;
          break;
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
  private int ramAddress = 0;
  private List< Integer > absUsed = null;
  private List< Integer > zeroUsed = null;

  public JCheckBox useRegisterConstants = new JCheckBox( "Registers" );
  public JCheckBox useFunctionConstants = new JCheckBox( "Functions" );
  public JRadioButton asCodeButton = new JRadioButton( "As code" );
  public JRadioButton rcButton = new JRadioButton( "Force RCn" );
  public JRadioButton wButton = new JRadioButton( "Force Wn" );
  public JRadioButton asmButton = new JRadioButton( "Assembly" );
  public JRadioButton disasmButton = new JRadioButton( "Disassembly" );
  private JToggleButton[] optionButtons =
  {
      useRegisterConstants, useFunctionConstants, asCodeButton, rcButton, wButton
  };

  public JButton moveUp = new JButton( "Up" );
  public JButton moveDown = new JButton( "Down" );
  public JButton insert = new JButton( "Insert" );
  public JButton delete = new JButton( "Delete" );
  public JButton selectAll = new JButton( "Select All" );
  public JButton copy = new JButton( "Copy" );
  public JButton cut = new JButton( "Cut" );
  public JButton paste = new JButton( "Paste" );
  public JButton load = new JButton( "Load" );
  public JButton save = new JButton( "Save" );
  public JButton assemble = new JButton( "Assemble" );
  public JButton disassemble = new JButton( "Disassemble" );
  public JButton getData = new JButton( "Update" );
  public JButton build = new JButton( "Build" );

  private File file = null;
  private List< AssemblerItem > cutItems = null;
  private boolean assembled = false;

  private JPanel upperPanel = null;
  private JTabbedPane tabbedPane = null;
  private JSplitPane outerPane = null;
  private JPanel pfMainPanel = null;
  private JPanel pfPanel = null;
  private JScrollPane pfScrollPane = null;
  private Short[] pfValues = null;
  private JRadioButton[] pfButtons = null;
  private JComboBox[][] pfBoxes = null;
  private boolean isSettingPF = false;
  private boolean showMessages = true;

  private JPanel pdMainPanel = null;
  private JPanel pdHeaderPanel = null;
  private JPanel pdPanel = null;
  private JScrollPane pdScrollPane = null;
  private Short[] pdValues = null;
  private List< RMFormattedTextField > pdFields = null;
  private List< int[] > pdSizes = null;
  
  private TitledBorder asmBorder = null;

  private JPanel fnMainPanel = null;
  private JPanel fnHeaderPanel = null;
  private JPanel fnPanel = null;
  private JScrollPane fnScrollPane = null;

  private JScrollPane protDataScrollPane = null;
  private Short[] basicValues = null;

  public RMFormattedTextField frequency = null;
  public RMFormattedTextField dutyCycle = null;
  public JComboBox sigStruct = new JComboBox();

  public JComboBox devBytes = new JComboBox();
  public JComboBox devBits1 = new JComboBox();
  public JComboBox devBits2 = new JComboBox();
  public JComboBox devBitDbl = new JComboBox();

  public JComboBox cmdBytes = new JComboBox();
  public JComboBox cmdBits1 = new JComboBox();
  public JComboBox cmdBits2 = new JComboBox();
  public JComboBox cmdBitDbl = new JComboBox();

  public RMFormattedTextField rptValue = null;
  public JComboBox rptType = new JComboBox();
  public JComboBox rptHold = new JComboBox();

  public RMFormattedTextField burst1On = null;
  public RMFormattedTextField burst1Off = null;

  public RMFormattedTextField burst0On = null;
  public RMFormattedTextField burst0Off = null;
  public JComboBox xmit0rev = new JComboBox();

  public JComboBox leadInStyle = new JComboBox();
  public JComboBox burstMidFrame = new JComboBox();
  public RMFormattedTextField afterBits = null;
  public RMFormattedTextField leadInOn = null;
  public RMFormattedTextField leadInOff = null;

  public JComboBox leadOutStyle = new JComboBox();
  public RMFormattedTextField leadOutOff = null;
  public JComboBox offAsTotal = new JComboBox();

  public JComboBox useAltLeadOut = new JComboBox();
  public RMFormattedTextField altLeadOut = null;

  public JComboBox useAltFreq = new JComboBox();
  public RMFormattedTextField altFreq = null;
  public RMFormattedTextField altDuty = null;

  // public JComboBox toggleBit = new JComboBox();
  // public JComboBox chkByteStyle = new JComboBox();
  // public JTextField bitsHeld = new JTextField();
  //
  // public JComboBox miniCombiner = new JComboBox();
  // public JComboBox sigStyle = new JComboBox();
  //
  // public JTextField vecOffset = new JTextField();
  // public JTextField dataOffset = new JTextField();

  public JLabel devBits1lbl = new JLabel();
  public JLabel devBits2lbl = new JLabel();
  public JLabel cmdBits1lbl = new JLabel();
  public JLabel cmdBits2lbl = new JLabel();
  public JLabel burstMidFrameLbl = new JLabel();
  public JLabel afterBitsLbl = new JLabel();
  public JLabel altFreqLbl = new JLabel();
  public JLabel altDutyLbl = new JLabel();
  public JLabel rptValueLbl = new JLabel();
  public JLabel leadInOnLbl = new JLabel();
  public JLabel leadInOffLbl = new JLabel();
  public JLabel offAsTotalLbl = new JLabel();
  public JLabel altLeadOutLbl = new JLabel();
  
  private int errorNumber = -1;

  private Object[] interpretations = null;
  
  private Component[][] dataComponents = null;

  private String[][] dataLabels = { 
      { "Frequency", "kHz" }, { "Duty Cycle", "%" }, { "Signal Structure" }, null,
      { "Device Bytes" }, { "Bits/Dev1" }, { "Bits/Dev2" }, { "Dev Bit Doubling" }, null,
      { "Command Bytes" }, { "Bits/Cmd1" }, { "Bits/Cmd2" }, { "Cmd Bit Doubling" }, null,
      { "Repeat Type" }, { "Hold" }, { "Count" }, null,
      { "1 Burst ON", "uSec" }, { "OFF", "uSec" }, null,
      { "0 Burst ON", "uSec" }, { "OFF", "uSec" }, { "Xmit 0 Reversed" }, null,
      { "Lead-In Style" }, { "Lead-In ON", "uSec" }, { "OFF", "uSec" }, null,
      { "Lead-Out Style" }, { "Lead-Out OFF", "uSec" }, { "OFF as Total" }, null,
      { "Use Alt Lead-Out" }, { "Alt Lead-Out", "uSec" }, null,
      { "Use Alt Freq" }, { "Alt Freq", "kHz" }, { "Alt Duty", "%" }, null,
      { "*****    Active in Build mode only    *****" }, null,
      { "Burst Mid-Frame" }, { "After # of bits" },
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
      Processor.class, Hex.class, List.class
  };

  /** The procs. */
  private static Processor[] procs = new Processor[ 0 ];
  
  private static List< AssemblerItem >[] itemLists = new List[ 0 ];

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
      interpretPFPD();
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
    rows.add( b );
    for ( int i = 0; i < data.length; i++ )
    {
      if ( data[ i ] != null && data[ i ][ 0 ] != null && data[ i ][ 0 ].equals( "0" ) )
        continue;
      if ( interleave )
      {
        for ( int j = 2; j < Math.max( data[ i ].length, 3 ); j++ )
          rows.add( TableLayout.PREFERRED );
      }
      else
      {
        rows.add( data[ i ] == null ? c : TableLayout.PREFERRED );
      }
      if ( i == data.length - 1 && data[ i ] != null || interleave )
        rows.add( b );
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
    for ( int i = 0; i < pfButtons.length; i++ )
    {
      pfButtons[ i ].setEnabled( pfValues[ i ] != null );
      if ( pfButtons[ i ].isSelected() )
        n = i;
    }
    if ( n < 0 )
    {
      n = 0;
      pfButtons[ 0 ].setSelected( true );
    }
    List< JTextArea > areas = new ArrayList< JTextArea >();
    double size[][] =
    {
        {
            getWidth( "0-0__" ), TableLayout.FILL
        }, null
    };
    setTableLayout( size, CommonData.pfData[ n ], true );
    pfPanel.setLayout( new TableLayout( size ) );

    int bitPos = 0;
    int row = 1;
    int m = 0;
    isSettingPF = true;
    for ( String[] data : CommonData.pfData[ n ] )
    {
      DisplayArea label = new DisplayArea( data[ 1 ], areas );
      label.setFocusable( false );
      pfPanel.add( label, "0, " + row + ", l, t" );

      DisplayArea area = new DisplayArea( data[ 2 ], areas );
      area.setFocusable( false );
      pfPanel.add( area, "1, " + row++ );

      JComboBox combo = new JComboBox();
      pfBoxes[ n ][ m++ ] = combo;
      combo.addActionListener( this );
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
      }
      ;

      pfPanel.add( combo, "1, " + row++ );
      if ( data.length > 4 )
      {
        area = new DisplayArea( data[ 4 ], areas );
        area.setFocusable( false );
        pfPanel.add( area, "1, " + row++ );
      }

      if ( pfValues[ n ] != null )
      {
        int len = Integer.parseInt( data[ 0 ] );
        int val = ( pfValues[ n ] >> bitPos ) & ( ( 1 << len ) - 1 );
        bitPos += len;
        for ( int i = 0;; i++ )
        {
          text = ( String )combo.getModel().getElementAt( i );
          if ( text.startsWith( "" + val + " =" ) || i == combo.getModel().getSize() - 1 /* other */)
          {
            combo.setSelectedIndex( i );
            break;
          }
        }
      }
      row++ ;
    }
    isSettingPF = false;
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
        pfScrollPane.getVerticalScrollBar().setValue( 0 );
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
    double size[][] =
    {
        {
            getWidth( "PD00/PD00__" ), getWidth( "$" ), getWidth( "FF_FF_" ), getWidth( " -> " ),
            getWidth( "999999_" ), TableLayout.FILL
        }, null
    };
    setTableLayout( size, CommonData.pdData, true );
    pdPanel.setLayout( new TableLayout( size ) );
    RMFormattedTextField tf = null;
    pdFields.clear();
    pdSizes.clear();

    int pdNum = 0;
    int pdLastNum = 0;
    int row = 1;
    for ( String[] data : CommonData.pdData )
    {
      int n = data[ 0 ] == null ? -1 : Integer.parseInt( data[ 0 ] );
      n = ( n == -1 ) ? 0 : ( n == 0 ) ? -1 : n;
      int type = Integer.parseInt( data[ 1 ] );
      int pdNdx = ( n > 0 ) ? pdNum : pdLastNum;
      String text = "";
      for ( int i = 0; i < n; i++ )
      {
        if ( i > 0 )
          text += "/";
        text += String.format( "PD%02X", pdNum + i );
      }

      if ( n > 0 )
      {
        DisplayArea label = new DisplayArea( text, areas );
        label.setFocusable( false );
        pdPanel.add( label, "0, " + row + ", l, t" );
      }

      DisplayArea area = new DisplayArea( data[ 2 ], areas );
      area.setFocusable( false );
      if ( n >= 0 )
      {
        pdPanel.add( area, "1, " + row + ", 5, " + row );
        JLabel label = new JLabel( "$" );
        label.setFocusable( false );
        pdPanel.add( new JLabel( "$" ), "1, " + ++row );

        tf = new RMFormattedTextField( new HexFormat( -1, type == 1 ? 1 : 2 ), this );
        pdFields.add( tf );
        pdPanel.add( tf, "2, " + row );
        for ( int i = 3; i < data.length; i++ )
        {
          label = new JLabel( " -> ", SwingConstants.CENTER );
          label.setFocusable( false );
          pdPanel.add( label, "3, " + row );
          NumberFormat nf = NumberFormat.getInstance();
          nf.setGroupingUsed( false );
          nf.setParseIntegerOnly( type != 4 );
          tf = new RMFormattedTextField( new RMNumberFormatter( nf ), this );
          pdFields.add( tf );
          pdPanel.add( tf, "4, " + row );
          label = new JLabel( "  " + data[ i ] );
          label.setFocusable( false );
          pdPanel.add( label, "5, " + row++ );
        }

        int val = 0;
        int index = pdSizes.size();
        pdSizes.add( new int[]
        {
            pdNdx, type == 1 ? 1 : 2
        } );
        pdSizes.add( new int[]
        {
            pdNdx, 0x10 + type
        } );
        if ( type == 4 )
          pdSizes.add( new int[]
          {
              pdNdx, 0x24
          } );
        if ( pdValues[ pdNdx ] != null )
        {
          if ( type > 1 && pdValues[ pdNdx + 1 ] != null )
          {
            val = pdValues[ pdNdx ] * 0x100 + pdValues[ pdNdx + 1 ];
            pdFields.get( index ).setValue( String.format( "%04X", val ) );
          }
          else
          {
            pdFields.get( index ).setValue( String.format( "%02X", pdValues[ pdNdx ] ) );
          }
          pdFields.get( index ).update();
        }
        else
        {
          pdFields.get( index ).setValue( "" );
        }
        row++ ;
        if ( n > 0 )
          pdLastNum = pdNum;
        pdNum += n;
      }
      else
      {
        pdHeaderPanel.removeAll();
        pdHeaderPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        pdHeaderPanel.add( area, BorderLayout.CENTER );
      }
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
        pdScrollPane.getVerticalScrollBar().setValue( 0 );
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
          || processor.getAbsAddresses().keySet().contains( data[ 0 ] ) || data[ 0 ].equals( "" ) )
      {
        n++ ;
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
        else
          continue;
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
          return -1;
        }
        else if ( !( zeroUsed.contains( n1 ) || absUsed.contains( n1 ) )
            && ( zeroUsed.contains( n2 ) || absUsed.contains( n2 ) ) )
        {
          return 1;
        }
        else
          return n1 - n2;
      }
    } );

    double size[][] =
    {
        {
            getWidth( "$FFFF_*_" ), TableLayout.FILL
        }, null
    };
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
        fnScrollPane.getVerticalScrollBar().setValue( 0 );
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
    if ( e.getSource() == assemblerTable.getSelectionModel() )
    {
      setAssemblerButtons( false );
      return;
    }
    // else ( e.getSource() == codeTable.getSelectionModel() ) 
    if ( !e.getValueIsAdjusting() )
    {
      assemble.setEnabled( codeTable.getSelectedRowCount() == 1 );
      getData.setEnabled( codeTable.getSelectedRowCount() == 1 );
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        Processor proc = procs[ codeTable.getSelectedRow() ];
        int row = codeTable.getSelectedRow();
        Hex hex = protocol.getCode( procs[ row ] );
        if ( ( hex == null || hex.length() == 0 ) && displayProtocol != null )
          hex = displayProtocol.getCode( proc );
        
        Hex oldHex = null;
        if ( processor != null )
        {
          if ( asmButton.isSelected() ) for ( int i = 0; i < procs.length; i++ )
          {
            if ( procs[ i ].getEquivalentName().equals( processor.getEquivalentName() ) )
            {
              if ( assemblerTable.getCellEditor() != null )
              {
                assemblerTable.getCellEditor().stopCellEditing();
              }
              itemLists[ i ].clear();
              itemLists[ i ].addAll( assemblerModel.getItemList() );
              break;
            }
          }
          oldHex = protocol.getCode( processor );
          if ( oldHex == null )  oldHex = ( displayProtocol == null ) ? null : displayProtocol.getCode( processor );
        }
        // If new processor has no code but old one does, then preserve the Protocol Data fields.
        if ( oldHex != null && ( hex == null || hex.length() == 0 ) )
        {
          // Ensure values all in sync before saving the Protocol Data fields
//          actionPerformed( new ActionEvent( getData, ActionEvent.ACTION_PERFORMED, "Internal" ) );
          interpretations = new Object[ dataComponents.length ];
          for ( int i = 0; i < dataComponents.length; i++ )
          {
            if ( dataComponents[ i ] == null ) continue;
            Component cpt = dataComponents[ i ][ 0 ];
            if ( cpt instanceof JComboBox )
            {
              interpretations[ i ] = ((JComboBox)cpt).getSelectedItem();
            }
            else if ( cpt instanceof RMFormattedTextField )
            {
              interpretations[ i ] = ( ( RMFormattedTextField )cpt ).getText();
            }
          }
        }
        assemblerModel.disassemble( hex, proc );
        if ( interpretations != null && ( hex == null || hex.length() == 0 ) )
        {
          interpretPFPD( true );
          for ( int i = 0; i < dataComponents.length; i++ )
          {
            if ( dataComponents[ i ] == null ) continue;
            Component cpt = dataComponents[ i ][ 0 ];
            if ( !cpt.isEnabled() ) continue;
            if ( cpt instanceof JComboBox )
            {
              ((JComboBox)cpt).setSelectedItem( interpretations[ i ] );
            }
            else if ( cpt instanceof RMFormattedTextField )
            {
              ( ( RMFormattedTextField )cpt ).setValue ( ( String )interpretations[ i ] );
            }
          }
        }
        interpretPFPD();
        setPFPanel();
        setPDPanel();
        setFunctionPanel();
        
        if ( asmButton.isSelected() )
        {
            assemblerModel.getData().clear();
            assemblerModel.getData().addAll( itemLists[ codeTable.getSelectedRow() ] );
            assemblerModel.setItemList( assemblerModel.getData() );
            assemblerModel.fireTableDataChanged();
        }
        
        load.setEnabled( true );
        setAssemblerButtons( false );
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
        load.setEnabled( false );
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
    if ( n == 0 ) text += "Note: The processor of the selected remote is ";
    else if ( n == 2 ) text += "This is a custom protocol, so only the code for the remote's <BR>processor (";
    if ( n != 1 )
    {
      if ( codeTable.getSelectedRow() >= 0 )
      {
        text += ( ( Processor )codeTable.getValueAt( codeTable.getSelectedRow(), 0 ) ).getName();
      }
      else
      {
        text += "??????";
      }
    }
    if ( n == 0 ) text += ".";
    else if ( n == 2 ) text += ") is editable.<BR>";
    if ( n >= 1 ) text += "Code shown in gray is standard code for information only.";
    text += "</HTML>";
    messageLabel.setText( text );
  }

  public void populateComboBox( Component component, Object[] array )
  {
    if ( !( component instanceof JComboBox ) )
    {
      return;
    }
    isSettingPF = true;
    JComboBox comboBox = ( JComboBox )component;
    ( ( DefaultComboBoxModel )comboBox.getModel() ).removeAllElements();
    if ( array == null )
    {
      isSettingPF = false;
      return;
    }
    for ( int i = 0; i < array.length; i++ )
    {
      comboBox.addItem( array[ i ] );
    }
    if ( comboBox.getActionListeners().length == 0 ) comboBox.addActionListener( pfpdListener );
    isSettingPF = false;
  }

  @Override
  public void itemStateChanged( ItemEvent e )
  {
    Object source = e.getSource();
    int n = 0;
    if ( ( n = Arrays.asList( pfButtons ).indexOf( source ) ) >= 0 )
    {
      if ( pfButtons[ n ].isSelected() )
        setPFPanel();
      return;
    }
    else if ( source == disasmButton )
    {
      if (  disasmButton.isSelected() && codeTable.getSelectedRowCount() == 1 )
      {
        if ( assemblerTable.getCellEditor() != null )
        {
          assemblerTable.getCellEditor().stopCellEditing();
        }
        int row = codeTable.getSelectedRow();
        itemLists[ row ].clear();
        itemLists[ row ].addAll( assemblerModel.getItemList() );
        
        Processor proc = procs[ row ];
        Hex hex = protocol.getCode( proc );
        if ( ( hex == null || hex.length() == 0 ) && displayProtocol != null )
          hex = displayProtocol.getCode( proc );
        assemblerModel.disassemble( hex, proc );
        setAssemblerButtons( true );
      }
    }
    else if ( source == asmButton )
    {
      if ( asmButton.isSelected() && codeTable.getSelectedRowCount() == 1 )
      {
        assemblerModel.getData().clear();
        assemblerModel.getData().addAll( itemLists[ codeTable.getSelectedRow() ] );
        assemblerModel.setItemList( assemblerModel.getData() );
        assemblerModel.fireTableDataChanged();
        setAssemblerButtons( true );
      }
    }
    else
    // Disassembler options changed
    {
      saveOptionButtons();
      if ( codeTable.getSelectedRowCount() == 1 )
      {
        Processor proc = procs[ codeTable.getSelectedRow() ];
        Hex hex = protocol.getCode( proc );
        if ( ( hex == null || hex.length() == 0 ) && displayProtocol != null )
          hex = displayProtocol.getCode( proc );
        assemblerModel.disassemble( hex, proc );
      }
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

  private class RMFormattedTextField extends JFormattedTextField implements KeyListener
  {
    private Format fmt;
    private DefaultFormatter ff;
    private String lastText = "";
    private ActionListener al = null;

    public RMFormattedTextField( Format fmt, ActionListener al )
    {
      super( fmt );
      this.fmt = fmt;
      this.al = al;
      addKeyListener( this );
      setFocusLostBehavior( JFormattedTextField.COMMIT );
    }

    public RMFormattedTextField( DefaultFormatter ff, ActionListener al )
    {
      super( ff );
      this.ff = ff;
      this.al = al;
      addKeyListener( this );
      setFocusLostBehavior( JFormattedTextField.COMMIT );
    }

    @Override
    protected void processFocusEvent( FocusEvent e )
    {
      super.processFocusEvent( e );
      if ( e.getID() == FocusEvent.FOCUS_GAINED )
      {
        selectAll();
      }
      else if ( e.getID() == FocusEvent.FOCUS_LOST )
      {
        endEdit();
      }
    }

    protected void setValue( String text )
    {
      try
      {
        Object obj = ( fmt == null ) ? ff.stringToValue( text ) : fmt.parseObject( text );
        lastText = obj == null ? "" : obj.toString();
        setText( lastText );
        commitEdit();
        if ( al != null && al == pfpdListener )
          update();
      }
      catch ( ParseException e1 )
      {
        e1.printStackTrace();
      }
    }

    // public String getLastText()
    // {
    // return lastText;
    // }

    protected void update()
    {
      al.actionPerformed( new ActionEvent( this, ActionEvent.ACTION_PERFORMED, "" ) );
    }
    
    private void endEdit()
    {
      if ( !isEditValid() )
      {
        showWarning( getText() + " : Invalid value" );
        setText( getValue() == null ? "" : getValue().toString() );
      }
      else
        try
        {
          commitEdit();
        }
        catch ( ParseException ex )
        {
          ex.printStackTrace();
        }
      if ( !getText().equals( lastText ) )
      {
        lastText = getText();
        update();
      }
    }

    private void showWarning( String message )
    {
      JOptionPane.showMessageDialog( this, message, "Invalid Value", JOptionPane.ERROR_MESSAGE );
    }

    @Override
    public void keyPressed( KeyEvent e )
    {
      int key = e.getKeyCode();
      if ( key == KeyEvent.VK_ENTER ) 
      {
        endEdit();
      }
    }

    @Override
    public void keyReleased( KeyEvent e ){}

    @Override
    public void keyTyped( KeyEvent e ){}
    
  }

  /**
   * A subclass of NumberFormatter that allows values to be null.
   */
  private static class RMNumberFormatter extends NumberFormatter
  {
    RMNumberFormatter( NumberFormat nf )
    {
      super( nf );
    }

    @Override
    public Object stringToValue( String string ) throws ParseException
    {
      if ( string == null || string.isEmpty() )
      {
        setEditValid( true );
        return null;
      }
      else
        return super.stringToValue( string );
    }

    @Override
    public String valueToString( Object value ) throws ParseException
    {
      if ( value == null )
        return "";
      else
        return super.valueToString( value );
    }
  }

  private int getWidth( String text )
  {
    return ( new JLabel( text ) ).getPreferredSize().width + 4;
  }

  public Short[] getPdValues()
  {
    return pdValues;
  }

  public Short[] getPfValues()
  {
    return pfValues;
  }

  public Short[] getBasicValues()
  {
    return basicValues;
  }

  public void setDataStyle( int dataStyle )
  {
    this.dataStyle = dataStyle;
  }

  public void setProcessor( Processor processor, int ramAddress )
  {
    this.processor = processor;
    this.ramAddress = ramAddress;
  }

  public void setAbsUsed( List< Integer > absUsed )
  {
    this.absUsed = absUsed;
  }

  public void setZeroUsed( List< Integer > zeroUsed )
  {
    this.zeroUsed = zeroUsed;
  }

  public void interpretPFPD()
  {
    interpretPFPD( false );
  }

  public void interpretPFPD( boolean force )
  {
    // DataStyle values:
    // 0 = S3C80
    // 1 = HCS08
    // 2 = 6805-RC16/18
    // 3 = 6805-C9
    // 4 = P8/740

    if ( processor == null || !force && tabbedPane.getSelectedComponent() != protDataScrollPane )
    {
      return;
    }

    int ni = ns.isEmpty() ? -1 : 0;

    int dataStyle = processor.getDataStyle();
    if ( ( (DefaultComboBoxModel )devBits1.getModel() ).getSize() == 0 )
    {
      // Populate those combo boxes whose content is fixed
      populateComboBox( devBytes, CommonData.to15 );
      populateComboBox( cmdBytes, CommonData.to15 );
      populateComboBox( devBits1, CommonData.to8 );
      populateComboBox( cmdBits1, CommonData.to8 );
      populateComboBox( devBits2, CommonData.to8 );
      populateComboBox( cmdBits2, CommonData.to8 );
      populateComboBox( xmit0rev, CommonData.noYes );
      populateComboBox( leadInStyle, CommonData.leadInStyle );
      populateComboBox( offAsTotal, CommonData.noYes );
      populateComboBox( useAltLeadOut, CommonData.noYes );
      populateComboBox( useAltFreq, CommonData.noYes );
      populateComboBox( leadOutStyle, CommonData.leadOutStyle );
      populateComboBox( rptType, CommonData.repeatType );
      populateComboBox( burstMidFrame, CommonData.noYes );
    }

    populateComboBox( sigStruct, dataStyle < 3 ? CommonData.sigStructs012 : CommonData.sigStructs34 );
    populateComboBox( devBitDbl, dataStyle < 3 ? CommonData.bitDouble012 : CommonData.bitDouble34 );
    populateComboBox( cmdBitDbl, dataStyle < 3 ? CommonData.bitDouble012 : CommonData.bitDouble34 );
    populateComboBox( rptHold, dataStyle < 3 ? CommonData.repeatHeld012 : CommonData.noYes );

    isSettingPF = !force;

    Integer valI = Hex.get( basicValues, 0 );
    frequency.setValue( valI == null ? "35" : getFrequency( valI ) );
    dutyCycle.setValue( valI == null ? "30" : getDutyCycle( valI ) );
    if ( valI == null )
    {
      valI = Hex.get( basicValues, 0 );
      frequency.setValue( getFrequency( valI ) );
      dutyCycle.setValue( getDutyCycle( valI ) );
    }
    Short valS = basicValues[ 2 ];

    devBytes.setSelectedIndex( valS == null ? 0 : valS >> 4 );
    cmdBytes.setSelectedIndex( valS == null ? 0 : valS & 0x0F );
    burstMidFrame.setEnabled( dataStyle < 3 );
    if ( dataStyle >= 3 ) burstMidFrame.setSelectedIndex( 0 );
    afterBitsLbl.setEnabled( dataStyle < 3 );
    afterBits.setEnabled( dataStyle < 3 );
    useAltFreq.setEnabled( dataStyle < 3 );
    altFreqLbl.setEnabled( dataStyle < 3 );
    altFreq.setEnabled( dataStyle < 3 );
    altDutyLbl.setEnabled( dataStyle < 3 );
    altDuty.setEnabled( dataStyle < 3 );
    offAsTotal.setEnabled( dataStyle != 3 );
    
    if ( dataStyle < 3 )
    {
      devBits1.setSelectedIndex( ( pdValues[ 0 ] != null && pdValues[ 0 ] <= 8 ) ? pdValues[ 0 ] : ni );
      cmdBits1.setSelectedIndex( ( pdValues[ 1 ] != null && pdValues[ 1 ] <= 8 ) ? pdValues[ 1 ] : ni );
      int n = ( dataStyle < 2 ) ? 0x10 : 0x0E;
      devBits2.setSelectedIndex( ( pdValues[ n ] != null && pdValues[ n ] <= 8 ) ? pdValues[ n ] : -1 );
      n = ( dataStyle < 2 ) ? 0x12 : 0x10;
      cmdBits2.setSelectedIndex( ( pdValues[ n ] != null && pdValues[ n ] <= 8 ) ? pdValues[ n ] : -1 );
      sigStruct.setSelectedIndex( ( pfValues[ 0 ] != null ) ? ( pfValues[ 0 ] >> 4 ) & 0x03 : -1 );
      devBitDbl.setSelectedIndex( ( pfValues[ 2 ] != null ) ? pfValues[ 2 ] & 3 : 0 );
      cmdBitDbl.setSelectedIndex( ( pfValues[ 2 ] != null ) ? ( pfValues[ 2 ] >> 2 ) & 3 : 0 );
      n = ( dataStyle < 2 ) ? 0x11 : 0x0F;
      rptType.setSelectedIndex( ( pfValues[ 1 ] != null && ( ( pfValues[ 1 ] & 0x10 ) != 0 ) && pdValues[ n ] != null && pdValues[ n ] != 0xFF  ) ? 1 : 0 );
      rptValue.setValue( ( rptType.getSelectedIndex() == 1 ) ? ( pdValues[ n ] != null ) ? "" + pdValues[ n ] : "" : "" + assemblerModel.getForcedRptCount() );
      rptHold.setSelectedIndex( ( pfValues[ 1 ] != null ) ? pfValues[ 1 ] & 0x03 : 0 );
      xmit0rev.setSelectedIndex( ( pfValues[ 2 ] != null ) ? ( pfValues[ 2 ] >> 4 ) & 1 : 0 );
      leadInStyle.setSelectedIndex( ( pfValues[ 1 ] != null ) ? ( pfValues[ 1 ] >> 2 ) & 3 : 0 );
      boolean b;
      if ( burstMidFrame.isEnabled() )
      {
        b = assemblerModel.getMidFrameIndex() > 0;
        burstMidFrame.setSelectedIndex( b ? 1 : 0 );
        afterBits.setValue( ( b && pdValues[ 0x13 ] != null ) ? "" + ( pdValues[ 0x13 ] - 1 ) : "" );
        afterBits.setEnabled( b );
        afterBitsLbl.setEnabled( b );
      }
      else
      {
        burstMidFrame.setSelectedIndex( 0 );
        afterBits.setEnabled( false );
        afterBitsLbl.setEnabled( false );
      }
      leadOutStyle.setSelectedIndex( ( pfValues[ 1 ] != null ) ? ( pfValues[ 1 ] >> 5 ) & 3 : 0 );
      offAsTotal.setSelectedIndex( pfValues[ 0 ] != null ? ( pfValues[ 0 ] >> 6 ) & 1 : 0 );
      useAltLeadOut.setSelectedIndex( ( pfValues[ 3 ] != null ) ? ( pfValues[ 3 ] >> 5 ) & 1 : 0 );
      useAltFreq.setSelectedIndex( ( pfValues[ 3 ] != null ) ? ( pfValues[ 3 ] >> 6 ) & 1 : 0 );
      int ndx = dataStyle < 2 ? 0x13 : 0x11;
      b = useAltFreq.getSelectedIndex() > 0 && Hex.get( pdValues, ndx ) != null && Hex.get( pdValues, ndx ) != 0xFFFF;
      altFreq.setValue( b ? getFrequency( Hex.get( pdValues, ndx ) ) : "" );
      altDuty.setValue( b ? getDutyCycle( Hex.get( pdValues, ndx ) ) : "" );

      if ( dataStyle < 2 )
      {
        burst1On.setValue( ( Hex.get( pdValues, 2 ) != null /* && Hex.get( pdValues, 2 ) > 0 */) ? ""
            + Hex.get( pdValues, 2 ) * 2 : ns );
        burst1Off.setValue( ( Hex.get( pdValues, 4 ) != null /* && Hex.get( pdValues, 4 ) > 0 */) ? ""
            + ( Hex.get( pdValues, 4 ) * 2 + ( ( dataStyle == 0 ) ? 40 : 0 ) ) : ns );
        burst0On.setValue( ( Hex.get( pdValues, 6 ) != null /* && Hex.get( pdValues, 6 ) > 0 */) ? ""
            + Hex.get( pdValues, 6 ) * 2 : ns );
        burst0Off.setValue( ( Hex.get( pdValues, 8 ) != null /* && Hex.get( pdValues, 8 ) > 0 */) ? ""
            + ( Hex.get( pdValues, 8 ) * 2 + ( ( dataStyle == 0 ) ? 40 : 0 ) ) : ns );
        leadInOn.setValue( ( leadInStyle.getSelectedIndex() > 0 && Hex.get( pdValues, 0x0C ) != null && Hex.get(
            pdValues, 0x0C ) != 0xFFFF ) ? "" + Hex.get( pdValues, 0x0C ) * 2 : "" );
        leadInOff.setValue( ( leadInStyle.getSelectedIndex() > 0 && Hex.get( pdValues, 0x0E ) != null && Hex.get(
            pdValues, 0x0E ) != 0xFFFF ) ? "" + ( Hex.get( pdValues, 0x0E ) * 2 + ( ( dataStyle == 0 ) ? 40 : 0 ) )
            : "" );
        leadOutOff.setValue( ( Hex.get( pdValues, 0x0A ) != null /* && Hex.get( pdValues, 0x0A ) > 0 */) ? ""
            + Hex.get( pdValues, 0x0A ) * 2 : ns );
        altLeadOut.setValue( ( useAltLeadOut.getSelectedIndex() == 1 && Hex.get( pdValues, 0x13 ) != null 
            /* && Hex.get( pdValues, 0x13 ) > 0*/ ) ? "" + Hex.get( pdValues, 0x13 ) * 2 : "" );
      }
      else
      {
        int t = ( Hex.semiGet( pdValues, 2, 0 ) != null ) ? Hex.semiGet( pdValues, 2, 0 ) : -1;
        burst1On.setValue( t >= 0 ? "" + 4 * ( t + 1 ) : ns );
        t = ( Hex.semiGet( pdValues, 2, 1 ) != null ) ? Hex.semiGet( pdValues, 2, 1 ) : -1;
        burst1Off.setValue( t >= 0 ? "" + 4 * t : ns );
        t = ( Hex.semiGet( pdValues, 5, 0 ) != null ) ? Hex.semiGet( pdValues, 5, 0 ) : -1;
        burst0On.setValue( t >= 0 ? "" + 4 * ( t + 1 ) : ns );
        t = ( Hex.semiGet( pdValues, 5, 1 ) != null ) ? Hex.semiGet( pdValues, 5, 1 ) : -1;
        burst0Off.setValue( t >= 0 ? "" + 4 * t : ns );
        t = ( Hex.semiGet( pdValues, 0x0B, 0 ) != null ) ? Hex.semiGet( pdValues, 0x0B, 0 ) : -1;
        leadInOn.setValue( leadInStyle.getSelectedIndex() > 0 && t >= 0 ? "" + 4 * ( t + 1 ) : "" );
        t = ( Hex.semiGet( pdValues, 0x0B, 1 ) != null ) ? Hex.semiGet( pdValues, 0x0B, 1 ) : -1;
        leadInOff.setValue( leadInStyle.getSelectedIndex() > 0 && t >= 0 ? "" + 4 * t : "" );
        t = ( Hex.get( pdValues, 8 ) != null ) ? Hex.get( pdValues, 8 ) - 10 : -1;
        leadOutOff.setValue( t >= 0 ? "" + 4 * t : ns );
        t = ( Hex.get( pdValues, 0x11 ) != null ) ? Hex.get( pdValues, 0x11 ) - 10 : -1;
        altLeadOut.setValue( useAltLeadOut.getSelectedIndex() == 1 && t >= 0 ? "" + 4 * t : "" );
      }
    }
    else
    {
      if ( dataStyle == 3 )
      {
        offAsTotal.setSelectedIndex( -1 );
        offAsTotalLbl.setEnabled( false );
      }
      else
      {
        offAsTotalLbl.setEnabled( true );
      }
      if ( pfValues[ 0 ] != null && ( ( pfValues[ 0 ] & 0x58 ) == 0x08 ) )
      {
        devBits1.setSelectedIndex( ( pdValues[ 0x0D ] != null ) ? pdValues[ 0x0D ] : ni );
      }
      else
      {
        devBits1.setSelectedIndex( ( pdValues[ 1 ] != null ) ? pdValues[ 1 ] : ni );
        if ( devBits2.isEnabled() )
        {
          devBits2.setSelectedIndex( ( pdValues[ 0x0D ] != null ) ? pdValues[ 0x0D ] : ni );
        }
      }
      cmdBits1.setSelectedIndex( ( pdValues[ 2 ] != null ) ? pdValues[ 2 ] : ni );
      String sig = "";
      String items[] =
      {
          "devs", "dev", "cmd", "!dev", "dev2", "cmd", "!cmd"
      };
      if ( pfValues[ 0 ] != null )
      {
        int key = ( ( pfValues[ 0 ] >> 1 ) & 0x3C ) | ( ( pfValues[ 0 ] >> 2 ) & 1 );
        if ( ( pfValues[ 0 ] & 0x41 ) == 0x41 )
        {
          key ^= 0x60; // replace bit for "dev" by that for "devs"
        }
        if ( ( pfValues[ 0 ] & 0x22 ) == 0x22 )
        {
          key ^= 0x12; // replace bit for first "cmd" by that for second one
        }
        for ( int i = 0; i < 7; i++ )
        {
          if ( ( ( key << i ) & 0x40 ) == 0x40 )
          {
            sig += items[ i ] + "-";
          }
        }
        sig = sig.substring( 0, Math.max( sig.length() - 1, 0 ) );
        sigStruct.setSelectedItem( sig );
      }
      else
      {
        sigStruct.setSelectedIndex( -1 );
      }
      sigStruct.setSelectedItem( pfValues[ 0 ]  == null ? -1 : sig );
      devBitDbl.setSelectedIndex( ( pfValues[ 2 ] != null ) ? ( pfValues[ 2 ] >> 1 ) & 1 : 0 );
      cmdBitDbl.setSelectedIndex( ( pfValues[ 2 ] != null ) ? ( pfValues[ 2 ] >> 1 ) & 1 : 0 );
      int count = assemblerModel.getForcedRptCount();
      rptType.setSelectedIndex( count > 0 ? 0 : ( pfValues[ 1 ] != null && ( ( pfValues[ 1 ] & 0x02 ) != 0 ) ) ? 1 : -1 );
      if ( rptType.getSelectedIndex() == 0 ) rptValue.setValue( "" + count );
      rptHold.setSelectedIndex( ( pfValues[ 1 ] != null && ( ( pfValues[ 1 ] & 0x02 ) != 0 ) ) ? 1 : 0 );

      burst1On.setValue( getONtime34( 0, null ) );

      burst0On.setValue( ( pfValues[ 2 ] != null  && ( pfValues[ 2 ] & 0x08 ) == 0x08 ) ? getONtime34( 0x0E, null ) : getONtime34( 0, null ) );

      burst1Off.setValue( getOFFtime34( 3, CommonData.burstOFFoffsets34, dataStyle ) );
      burst0Off.setValue( getOFFtime34( 5, CommonData.burstOFFoffsets34, dataStyle ) );
      xmit0rev.setSelectedIndex( ( pfValues[ 2 ] != null && ( pfValues[ 2 ] & 0x1C ) == 0x04 ) ? 1 : 0 );
      leadInStyle.setSelectedIndex( ( pfValues[ 1 ] != null && (( pfValues[ 1 ] & 0x10 ) == 0x10 ) ) ? 
         (  ( pfValues[ 1 ] & 0x04 ) == 0x04 && ( /*force ||*/ Hex.get( pdValues, 0x10 ) != null && Hex.get( pdValues, 0x10 ) != Hex.get( pdValues, 0x0A ) ) ) ? 3 : 1 : 0 );

      leadInOn.setValue( leadInStyle.getSelectedIndex() > 0 ? getONtime34( 9, 0x0C ) : "" );
      leadInOff.setValue( leadInStyle.getSelectedIndex() > 0 ? getOFFtime34( 0x0A, CommonData.leadinOFFoffsets34,
          dataStyle ) : "" );
      offAsTotal.setSelectedIndex( ( dataStyle == 4 && pfValues[ 2 ] != null ) ? pfValues[ 2 ] & 1 : 0 );
      leadOutStyle.setSelectedIndex( pfValues[ 1 ] != null ? offAsTotal.getSelectedIndex() == ( pfValues[ 1 ] >> 6 & 1 ) ? -1 : ( pfValues[ 1 ] >> 4 & 2 ) : 0 );
      leadOutOff.setValue( ( dataStyle == 3 ) ? getOFFtime34( 7, CommonData.leadinOFFoffsets34, dataStyle ) : ( Hex.get( pdValues, 7 ) != null && Hex.get( pdValues, 7 ) > 0 ) ? "" + ( Math.max( Hex.get( pdValues, 7 ) * 4 - 40, 0 ) ) : "0" );

      boolean b = pfValues[ 1 ] != null && ( pfValues[ 1 ] & 4 ) == 4 && ( pfValues[ 2 ] == null || ( pfValues[ 2 ] & 8 ) == 0 );
      int i1 = Hex.get( pdValues, 0x0E ) == null ? -1 : Hex.get( pdValues, 0x0E );
      int i2 = Hex.get( pdValues, 0x07 ) == null ? -1 : Hex.get( pdValues, 0x07 );
      b = b && i1 > 0 && i2 > 0 && i1 != i2;
      int ndx = b ? 1 : 0;
      
      // For some peculiar reason the following line that combines the above does not work, giving value 1 at times when it should be 0.
      // int ndx = ( pfValues[ 1 ] != null && ( pfValues[ 1 ] & 4 ) == 4 && ( pfValues[ 2 ] == null || ( pfValues[ 2 ] & 8 ) == 0 ) && Hex.get( pdValues, 0x0E ) != null && Hex.get( pdValues, 0x0E ) != Hex.get( pdValues, 0x07 ) ) ? 1 : 0;
      
      useAltLeadOut.setSelectedIndex( ndx );
      altLeadOut.setValue( ( useAltLeadOut.getSelectedIndex() == 1  ) ? ( dataStyle == 3 ) ? getOFFtime34( 0x0E, CommonData.leadinOFFoffsets34, dataStyle ) : ( Hex.get( pdValues, 0x0E ) != null && Hex.get( pdValues, 0x0E ) > 0 ) ? "" + ( Hex.get( pdValues, 0x0E ) * 4 - 40 ) : "" : "" );
    }
    isSettingPF = false;
  }

  private String getONtime34( int pdIndex1, Integer pdIndex2 )
  {
    if ( pdValues[ pdIndex1 ] == null )
    {
      return ns;
    }
    else if ( pfValues[ 2 ] != null && ( pfValues[ 2 ] & 0x7C ) == 0x40 )
    {
      int t = ( pdValues[ pdIndex1 ] + 255 ) % 256 + 1;
      return "" + ( 3 * t + 2 );
    }
    else
    {
      int t = ( pdValues[ pdIndex1 ] + 255 ) % 256 + 1;
      if ( pdIndex2 != null && pfValues[ 1 ] != null && ( pfValues[ 1 ] & 0x08 ) == 0x08 && pdValues[ pdIndex2 ] != null )
      {
        t += ( ( pdValues[ pdIndex2 ] + 255 ) % 256 ) * 256;
      }
      return "" + burstUnit * t / 1000;
    }
  }

  private void setONtime34( Long time, int pdIndex1, Integer pdIndex2 )
  {
    if ( time == null )
    {
      pdValues[ pdIndex1 ] = ns.isEmpty() ? null : ( short )1;
      if ( pdIndex2 != null )
        pdValues[ pdIndex2 ] = ns.isEmpty() ? null : ( short )1;
    }
    else if ( pfValues[ 2 ] != null && ( pfValues[ 2 ] & 0x7C ) == 0x40 )
    {
      time = Math.max( ( time - 2 ) / 3, 0 );
      pdValues[ pdIndex1 ] = ( short )( time % 256 );
    }
    else if ( burstUnit > 0 )
    {
      time = ( time * 1000 + burstUnit / 2 ) / burstUnit;
      if ( time == 0 ) time = 1L;
      time = ( time + 0xFFFF ) % 0x10000;
      int tHigh = ( ( int )( long )time / 256 + 1 ) % 256;
      int tLow = ( ( int )( long )time + 1 ) % 256;
      pdValues[ pdIndex1 ] = ( short )tLow;
      if ( pdIndex2 != null )
      {
        if ( tHigh == 1 )
        {
          pdValues[ pdIndex2 ] = null;
          setPFbits( 1, 0, 3, 1 );
        }
        else
        {
          pdValues[ pdIndex2 ] = ( short )tHigh;
          setPFbits( 1, 1, 3, 1 );
        }
      }
    }
    else
    {
      pdValues[ pdIndex1 ] = null;
      if ( pdIndex2 != null )
        pdValues[ pdIndex2 ] = null;
    }
  }
  
  private int testONtime34( Long time )
  {
    if ( time == null )
    {
      return -1;
    }
    else if ( pfValues[ 2 ] != null && ( pfValues[ 2 ] & 0x7C ) == 0x40 )
    {
      return ( int )Math.max( ( time - 2 ) / 3, 0 );

    }
    else if ( burstUnit > 0 )
    {
      return ( int )( ( time * 1000 + burstUnit/2 ) / burstUnit );
      
    }
    else
    {
      return -2;
    }
  }

  private String getOFFtime34( int pdIndex, int[] offsets, int dataStyle )
  {
    if ( Hex.get( pdValues, pdIndex ) == null )
    {
      return ns;
    }
    else
    {
      int t = ( pdValues[ pdIndex + 1 ] + 255 ) % 256;
      t += ( ( pdValues[ pdIndex ] + 255 ) % 256 ) * ( ( dataStyle == 3 ) ? 257 : 257.5 );
      t = ( dataStyle == 3 ) ? 3 * t + offsets[ 0 ] : 2 * t + offsets[ 1 ];
      return "" + t;
    }
  }

  private void setOFFtime34( Long time, int pdIndex, int[] offsets, int dataStyle )
  {
    if ( time == null )
    {
      pdValues[ pdIndex ] = ns.isEmpty() ? null : ( short )1;
      ;
      pdValues[ pdIndex + 1 ] = ns.isEmpty() ? null : ( short )1;
      ;
    }
    else
    {
      double d = ( dataStyle == 3 ) ? 257 : 257.5;
      time = ( dataStyle == 3 ) ? ( time - offsets[ 0 ] ) / 3 : ( time - offsets[ 1 ] ) / 2;
      if ( time < 0 )
        time = ( long )0;
      int tHigh = ( int )( time / d );
      int tLow = ( int )( time - ( tHigh * d ) );
      tHigh = ( tHigh + 1 ) % 256;
      tLow = ( tLow + 1 ) % 256;
      pdValues[ pdIndex ] = ( short )tHigh;
      pdValues[ pdIndex + 1 ] = ( short )tLow;
    }
  }

  public String getFrequency( int times )
  {
    burstUnit = 0;
    int on = times >> 8;
    int off = times & 0xFF;
    if ( on > 0 && off > 0 )
    {
      double f = processor.getOscillatorFreq() / ( on + off + processor.getCarrierTotalOffset() );
      burstUnit = ( int )( Math.round( 1000000000 / f ) );
      return String.format( "%.3f", f / 1000 );
    }
    else if ( on == 0 && off == 0 )
    {
      return "0";
    }
    else
    {
      return "** Error **";
    }
  }

  public String getDutyCycle( int times )
  {
    int on = times >> 8;
    int off = times & 0xFF;
    int totOffset = processor.getCarrierTotalOffset();
    int onOffset = processor.getCarrierOnOffset();
    if ( on > 0 && off > 0 )
    {
      double dc = 100.0 * ( on + onOffset ) / ( on + off + totOffset );
      return String.format( "%.2f", dc );
    }
    else if ( on == 0 && off == 0 )
    {
      return "";
    }
    else
    {
      return "** Error **";
    }
  }

  private Integer getCarrierData( Object freq, Object duty )
  {
    if ( freq == null )
      return null;
    double f = ( freq instanceof Double ) ? ( Double )freq : ( Long )freq;
    if ( f == 0 || duty == null )
      return 0;
    double dc = ( duty instanceof Double ) ? ( Double )duty : ( Long )duty;
    burstUnit = ( int )( Math.round( 1000000 / f ) );
    int tot = ( int )( processor.getOscillatorFreq() / ( f * 1000 ) + 0.5 );
    int on = ( int )( dc * tot / 100 + 0.5 ) - processor.getCarrierOnOffset();
    return on * 0xFF + tot - processor.getCarrierTotalOffset();
  }

  private ActionListener pfpdListener = new ActionListener()
  {
    @Override
    public void actionPerformed( ActionEvent e )
    {
      Object source = e.getSource();
      if ( source == frequency || source == dutyCycle )
      {
        Hex.put( getCarrierData( frequency.getValue(), dutyCycle.getValue() ), basicValues, 0 );
        if ( dataStyle > 2 && source == frequency )
        {
          if ( basicValues[ 0 ] == 0 ) setPFbits( 2, 16, 2, 5 );
        }
      }
      else if ( source == devBytes )
      {
        int val = devBytes.getSelectedIndex();
        boolean is2 = val == 2;
        if ( dataStyle > 2 )
          is2 = is2 && ( ( pfValues[ 0 ] & 0x58 ) != 0x08 );
        devBits1lbl.setText( is2 ? "Bits/Dev1" : "Bits/Dev" );
        devBits2lbl.setVisible( is2 );
        if ( !is2 )
          devBits2.setSelectedIndex( -1 );
        devBits2.setEnabled( is2 );
        if ( !isSettingPF )
        {
          if ( dataStyle < 3 )
            setPFbits( 0, Math.min( val, 3 ), 0, 2 );
          if ( basicValues[ 2 ] == null ) basicValues[ 2 ] = 0;
          basicValues[ 2 ] = ( short )( ( basicValues[ 2 ] & 0x0F ) | ( devBytes.getSelectedIndex() << 4 ) );
          if ( !is2 )
            pdValues[ dataStyle < 2 ? 0x10 : dataStyle < 3 ? 0x0E : 0x0D ] = null;
        }
      }
      else if ( source == cmdBytes )
      {
        int val = cmdBytes.getSelectedIndex();
        boolean is2 = val == 2 && ( dataStyle < 3 );
        cmdBits1lbl.setText( is2 ? "Bits/Cmd1" : "Bits/Cmd" );
        cmdBits2lbl.setVisible( is2 );
        if ( !is2 )
          cmdBits2.setSelectedIndex( -1 );
        cmdBits2.setEnabled( is2 );
        if ( !isSettingPF )
        {
          if ( dataStyle < 3 )
            setPFbits( 0, Math.min( val, 3 ), 2, 2 );
          if ( basicValues[ 2 ] == null )
            basicValues[ 2 ] = 0;
          basicValues[ 2 ] = ( short )( ( basicValues[ 2 ] & 0xF0 ) | cmdBytes.getSelectedIndex() );
          if ( !is2 && dataStyle < 3 ) pdValues[ dataStyle < 2 ? 0x12 : 0x10 ] = null;
        }
      }
      else if ( source == leadInStyle )
      {
        int index = leadInStyle.getSelectedIndex();
        leadInOn.setEnabled( index > 0 );
        leadInOff.setEnabled( index > 0 );
        leadInOnLbl.setEnabled( index > 0 );
        leadInOffLbl.setEnabled( index > 0 );

        if ( isSettingPF )
          return;

        if ( index == 0 )
        {
          burstMidFrame.setSelectedIndex( 0 );
          leadInOn.setValue( "" );
          leadInOff.setValue( "" );
        }
        else
        {

          if ( leadInOn.getValue() == null ) leadInOn.setValue( dataStyle == 2 ? "4" : dataStyle > 2 ? "" + Math.max( burstUnit/1000, 5 ) : "0" );
          if ( leadInOff.getValue() == null ) leadInOff.setValue( "0" );
//          if ( burstMidFrame.getSelectedIndex() == -1 ) burstMidFrame.setSelectedIndex( 0 );

        }
       
        if ( dataStyle < 3 ) 
        {
          setPFbits( 1, index, 2, 2 );
          if ( index == 0 )
          {
            burstMidFrame.setSelectedIndex( 0 );
          }
//          else if ( burstMidFrame.getSelectedIndex() == -1 )
//          {
//            burstMidFrame.setSelectedIndex( 0 );
//          }
        }
        
      }

      if ( dataStyle > 2 )
      {
        boolean rpt = rptType.getSelectedIndex() >= 0 && rptValue.getValue() != null && ( Long) rptValue.getValue() > 0;
        rptHold.setEnabled( !rpt );
        if ( rpt && rptType.getSelectedIndex() < rptHold.getModel().getSize() ) rptHold.setSelectedIndex( rptType.getSelectedIndex() );
        useAltLeadOut.setEnabled( true );
      }
      else
      {
        rptHold.setEnabled( true );
        boolean b = useAltFreq.getSelectedIndex() == 0 && burstMidFrame.getSelectedIndex() == 0;
        useAltLeadOut.setEnabled( b );
        altLeadOut.setEnabled( b );
        b = useAltLeadOut.getSelectedIndex() == 0 && burstMidFrame.getSelectedIndex() == 0;
        useAltFreq.setEnabled( b );
        altFreq.setEnabled( b );
        altDuty.setEnabled( b );
        b = useAltLeadOut.getSelectedIndex() == 0 && useAltFreq.getSelectedIndex() == 0 && leadInStyle.getSelectedIndex() > 0;
        burstMidFrame.setEnabled( b );
        afterBits.setEnabled( b );
      }
 
      if ( !isSettingPF )
      {
        if ( dataStyle > 2 )
        {
          boolean altLO = useAltLeadOut.getSelectedIndex() == 1 && altLeadOut.getValue() != null && ( Long )altLeadOut.getValue() > 0;
          setPFbits( 1, ( altLO || rptHold.getSelectedIndex() == 1 ) ? 1 : 0, 1, 1 );
          setPFbits( 1, ( altLO || leadInStyle.getSelectedIndex() == 3 ) ? 1 : 0, 2, 1 );
          setPFbits( 1, leadInStyle.getSelectedIndex() > 0 ? 1 : 0, 4, 1 );
          
          Long val = ( Long )rptValue.getValue();
          if ( rptType.getSelectedIndex() == 1 ) assemblerModel.setForcedRptCount( 0 );
          else assemblerModel.setForcedRptCount( val == null ? 0 : ( short )( long )val );

          Long t = ( Long )burst1On.getValue();
          if ( t == null ) t = ( Long )burst0On.getValue();
          setONtime34( t, 0, null );
          if ( burst0On.getValue() == null || burst1On.getValue() == null || testONtime34( ( Long )burst0On.getValue() ) == testONtime34( ( Long )burst1On.getValue() ) )
          {
            setPFbits( 2, 0, 3, 1 );  // signifies burst 0 and 1 on-times equal
            if ( leadInStyle.getSelectedIndex() == 3 )
            {
              if ( useAltLeadOut.getSelectedIndex() == 0 )
              {
                // set pd0E to lead-out off-time to indicate not using alternate lead-out
                t = ( Long )leadOutOff.getValue();
                if ( dataStyle == 3 ) setOFFtime34( t, 0x0E, CommonData.leadinOFFoffsets34, dataStyle );
                if ( dataStyle == 4 ) Hex.put( t == null ? null : ( int )( t / 4 + 10 ), pdValues, 0x0E );
                // set pd10 to half lead-in off-time
                t = ( Long )leadInOff.getValue();
                setOFFtime34( t == null ? null : ( long )( t / 2 ), 0x10, CommonData.burstOFFoffsets34, dataStyle );
              }
              else
              {
                errorMessage( 2 );
              }
            }
            else // leadInStyle < 3
            {
              Hex.put(  null, pdValues, 0x10 );
              if ( useAltLeadOut.getSelectedIndex() == 1 )
              {
                t = ( Long )altLeadOut.getValue();
                if ( dataStyle == 3 ) setOFFtime34( t, 0x0E, CommonData.leadinOFFoffsets34, dataStyle );
                if ( dataStyle == 4 ) Hex.put( t == null ? null : ( int )( t / 4 + 10 ), pdValues, 0x0E );
              }
              else
              {
                Hex.put(  null, pdValues, 0x0E );
              }
            }
          }
          else
          {
            if ( leadInStyle.getSelectedIndex() == 3 )
            {
              errorMessage( 0 );
            }
            else if ( useAltLeadOut.getSelectedIndex() == 1 )
            {
              errorMessage( 1 );
            }
            else
            {
              setPFbits( 2, 1, 3, 1 );
              t = ( Long )burst0On.getValue();
              setONtime34( t, 0x0E, null );
              pdValues[ 0x0F ] = null;
              Hex.put(  null, pdValues, 0x10 );
            }
          }
        }
        else  // dataStyle < 3
        {
          Long val = ( Long )rptValue.getValue();
          int n = ( dataStyle < 2 ) ? 0x11 : 0x0F;
          if ( rptType.getSelectedIndex() == 1 ) 
          {
            pdValues[ n ] = val == null ? 0 : ( short )( long )val;
            assemblerModel.setForcedRptCount( 0 );
          }
          else
          {
            pdValues[ n ] = null;
            assemblerModel.setForcedRptCount( val == null ? 0 : ( short )( long )val );
          }
        }
        if ( source == devBits1 )
        {
          short val = ( short )devBits1.getSelectedIndex();
          Short pdval = val == -1 ? null : val;
          if ( dataStyle < 3 )
            pdValues[ 0 ] = pdval;
          else if ( ( pfValues[ 0 ] & 0x58 ) != 0x08 )
            pdValues[ 1 ] = pdval;
          else
          {
            pdValues[ 0x0D ] = pdval;
            pdValues[ 1 ] = 0;
          }
        }
        else if ( source == cmdBits1 )
        {
          short val = ( short )cmdBits1.getSelectedIndex();
          Short pdval = val == -1 ? null : val;
          if ( dataStyle < 3 )
            pdValues[ 1 ] = pdval;
          else
            pdValues[ 2 ] = pdval;
        }
        else if ( source == devBits2 )
        {
          // code can generate this action, see source = sigStruct, hence need for test of isEnabled
          short val = ( short )devBits2.getSelectedIndex();
          if ( dataStyle < 3 )
            pdValues[ dataStyle < 2 ? 0x10 : 0x0E ] = val == -1 ? null : val;
          else if ( devBits2.isEnabled() )
            pdValues[ 0x0D ] = val == -1 ? null : val;
          else if ( ( pfValues[ 0 ] & 0x58 ) != 0x08 )
            pdValues[ 0x0D ] = null;
          // else pdValues[ 0x0D ] = ( ( pfValues[ 0 ] & 0x58 ) == 0x08 ) ? ( short )devBits1.getSelectedIndex() : null;
        }
        else if ( source == cmdBits2 )
        {
          if ( dataStyle < 3 )
          {
            short val = ( short )cmdBits2.getSelectedIndex();
            pdValues[ dataStyle < 2 ? 0x12 : 0x10 ] = val == -1 ? null : val;
          }
          // Disabled when dataStyle >= 3
        }
        else if ( source == sigStruct )
        {
          if ( dataStyle < 3 )
            setPFbits( 0, sigStruct.getSelectedIndex(), 4, 2 );
          else
          {
            String sig = ( String )sigStruct.getSelectedItem() + "-";
            String items[] =
            {
                "devs", "dev", "cmd", "!dev", "dev2", "cmd", "!cmd"
            };
            int key = 0;
            int p = 0;
            while ( true )
            {
              int n = sig.indexOf( '-' );
              if ( n < 0 )
                break;
              String item = sig.substring( 0, n );
              for ( ; p < items.length && !items[ p ].equals( item ); p++ )
                ;
              if ( p == items.length )
                break; // Should not occur
              key |= 1 << ( 6 - p );
              sig = sig.substring( n + 1 );
            }
            int val = 0;
            if ( ( key & 2 ) == 2 )
            {
              val |= 0x02;
              key ^= 0x12;
            }
            if ( ( key & 0x40 ) == 0x40 )
            {
              val |= 0x01;
              key ^= 0x60;
            }
            val |= ( ( key & 0x3C ) << 1 ) | ( key & 1 ) << 2;
            setPFbits( 0, val, 0, 7 );
            actionPerformed( new ActionEvent( devBytes, ActionEvent.ACTION_PERFORMED, "Internal" ) );
            actionPerformed( new ActionEvent( devBits1, ActionEvent.ACTION_PERFORMED, "Internal" ) );
            actionPerformed( new ActionEvent( devBits2, ActionEvent.ACTION_PERFORMED, "Internal" ) );
          }
        }
        else if ( source == devBitDbl )
        {
          if ( dataStyle < 3 )
            setPFbits( 2, devBitDbl.getSelectedIndex(), 0, 2 );
          else
          {
            setPFbits( 2, devBitDbl.getSelectedIndex(), 1, 1 );
            isSettingPF = true;
            cmdBitDbl.setSelectedIndex( devBitDbl.getSelectedIndex() );
            isSettingPF = false;
          }
        }
        else if ( source == cmdBitDbl )
        {
          if ( dataStyle < 3 )
            setPFbits( 2, cmdBitDbl.getSelectedIndex(), 2, 2 );
          else
          {
            setPFbits( 2, devBitDbl.getSelectedIndex(), 1, 1 ); // same as devBitDbl
            isSettingPF = true;
            devBitDbl.setSelectedIndex( cmdBitDbl.getSelectedIndex() );
            isSettingPF = false;
          }
        }
        else if ( source == rptType )
        {
          int index = rptType.getSelectedIndex();
          Long val = ( Long )rptValue.getValue();
          if ( !assemblerModel.testBuildMode( processor ) && val != null && val != 0 )
          {
            errorMessage( 3 );
          }
          if ( dataStyle > 2 ) return;
          
          setPFbits( 1, index, 4, 1 );
          int n = ( dataStyle < 2 ) ? 0x11 : 0x0F;
          pdValues[ n ] = ( index == 0 ) ? null : ( pdValues[ n ] == null || pdValues[ n ] == 0xFF ) ? 0 : pdValues[ n ];

        }
        else if ( source == rptValue )
        {
          int index = rptType.getSelectedIndex();
          if ( !assemblerModel.testBuildMode( processor ) && index != 1 )
          {
            errorMessage( 4 );
          }
        }
        else if ( source == rptHold )
        {
          if ( dataStyle < 3 ) setPFbits( 1, rptHold.getSelectedIndex(), 0, 2 );
        }
        else if ( source == xmit0rev )
        {
          if ( dataStyle < 3 )
            setPFbits( 2, xmit0rev.getSelectedIndex(), 4, 1 );
          else
            setPFbits( 2, xmit0rev.getSelectedIndex(), 2, 3 );
        }

        else if ( source == leadOutStyle )
        {
          if ( dataStyle < 3 )
            setPFbits( 1, leadOutStyle.getSelectedIndex(), 5, 2 );
          else
          {
            setPFbits( 1, leadOutStyle.getSelectedIndex() >> 1, 5, 1 );
            setPFbits( 1, 1 - offAsTotal.getSelectedIndex(), 6, 1 );
          }
        }
        else if ( source == offAsTotal )
        {
          if ( dataStyle < 3 )
            setPFbits( 0, offAsTotal.getSelectedIndex(), 6, 1 );
          if ( dataStyle == 4 )
          {
            setPFbits( 1, offAsTotal.getSelectedIndex(), 6, 1 );
            setPFbits( 2, offAsTotal.getSelectedIndex(), 0, 1 );
            actionPerformed( new ActionEvent( leadOutStyle, ActionEvent.ACTION_PERFORMED, "Internal" ) );
          }
        }
        else if ( source == burstMidFrame )
        {
          if ( dataStyle < 3 )
          {
            if ( burstMidFrame.getSelectedIndex() <= 0 )
            {
              afterBits.setValue( "" );
            }
            else if ( burstMidFrame.getSelectedIndex() == 1 && afterBits.getValue() == null )
            {
              afterBits.setValue( "0" );
            }
            assemblerModel.setMidFrameIndex( burstMidFrame.getSelectedIndex() );
          }
        }
        else if ( source == burst1On )
        {
          Long t = ( Long )burst1On.getValue();

          if ( dataStyle < 2 ) Hex.put( t == null ? null : ( int )( t / 2 ), pdValues, 2 );
          if ( dataStyle == 2 ) Hex.semiPut( t == null ? null : ( int )(( t / 4 + 255 ) % 256 ), pdValues, 2, 0 );
        }
        else if ( source == burst1Off )
        {
          Long t = ( Long )burst1Off.getValue();
          if ( dataStyle < 2 )
          {
            if ( t != null )
              t = Math.max( t - ( ( dataStyle == 0 ) ? 40 : 0 ), 0 );
            Hex.put( t == null ? null : ( int )( t / 2 ), pdValues, 4 );
          }
          if ( dataStyle == 2 )
            Hex.semiPut( t == null ? null : ( int )( t / 4 ), pdValues, 2, 1 );
          if ( dataStyle > 2 )
            setOFFtime34( t, 3, CommonData.burstOFFoffsets34, dataStyle );
        }
        else if ( source == burst0On )
        {
          Long t = ( Long )burst0On.getValue();
          if ( dataStyle < 2 ) Hex.put( t == null ? null : ( int )( t / 2 ), pdValues, 6 );
          if ( dataStyle == 2 ) Hex.semiPut( t == null ? null : ( int )(( t / 4 + 255 ) % 256 ), pdValues, 5, 0 );
        }
        else if ( source == burst0Off )
        {
          Long t = ( Long )burst0Off.getValue();
          if ( dataStyle < 2 )
          {
            if ( t != null )
              t = Math.max( t - ( ( dataStyle == 0 ) ? 40 : 0 ), 0 );
            Hex.put( t == null ? null : ( int )( t / 2 ), pdValues, 8 );
          }
          if ( dataStyle == 2 )
            Hex.semiPut( t == null ? null : ( int )( t / 4 ), pdValues, 5, 1 );
          if ( dataStyle > 2 )
            setOFFtime34( t, 5, CommonData.burstOFFoffsets34, dataStyle );
        }
        else if ( source == leadInOn )
        {
          Long t = ( Long )leadInOn.getValue();
          if ( dataStyle < 2 )
            Hex.put( t == null || leadInStyle.getSelectedIndex() == 0 ? null : ( int )( t / 2 ), pdValues, 0x0C );
          if ( dataStyle == 2 )
            Hex.semiPut( t == null || leadInStyle.getSelectedIndex() == 0 ? null : ( int )( ( t / 4 + 255 ) % 256 ),
                pdValues, 0x0B, 0 );
          if ( dataStyle > 2 )
            setONtime34( leadInStyle.getSelectedIndex() == 0 ? null : t, 9, 0x0C );

        }
        else if ( source == leadInOff )
        {
          Long t = ( Long )leadInOff.getValue();
          if ( dataStyle < 2 )
          {
            if ( t != null )
              t = Math.max( t - ( ( dataStyle == 0 ) ? 40 : 0 ), 0 );
            Hex.put( t == null || leadInStyle.getSelectedIndex() == 0 ? null : ( int )( t / 2 ), pdValues, 0x0E );
          }
          if ( dataStyle == 2 )
            Hex.semiPut( t == null || leadInStyle.getSelectedIndex() == 0 ? null : ( int )( t / 4 ), pdValues, 0x0B, 1 );
          if ( dataStyle > 2 )
          {
            isSettingPF = true;
            setOFFtime34( leadInStyle.getSelectedIndex() == 0 ? null : t, 0x0A, CommonData.leadinOFFoffsets34,
                dataStyle );
            actionPerformed( new ActionEvent( leadInStyle, ActionEvent.ACTION_PERFORMED, "Internal" ) );
            isSettingPF = false;
          }
        }
        else if ( source == leadOutOff )
        {
          Long t = ( Long )leadOutOff.getValue();
          if ( dataStyle < 2 )
            Hex.put( t == null ? null : ( int )( t / 2 ), pdValues, 0x0A );
          if ( dataStyle == 2 )
            Hex.put( t == null ? null : ( int )( t / 4 + 10 ), pdValues, 8 );
          if ( dataStyle == 3 )
            setOFFtime34( t, 7, CommonData.leadinOFFoffsets34, dataStyle );
          if ( dataStyle == 4 )
            Hex.put( t == null ? null : ( int )( t / 4 + 10 ), pdValues, 7 );
        }
        else if ( source == useAltLeadOut )
        {
          if ( dataStyle < 3 ) setPFbits( 3, useAltLeadOut.getSelectedIndex(), 5, 1 );
          if ( useAltLeadOut.getSelectedIndex() <= 0 ) altLeadOut.setValue( "" );
          if ( useAltLeadOut.getSelectedIndex() == 1 && altLeadOut.getValue() == null ) altLeadOut.setValue( "0" );
        }
        else if ( source == altLeadOut )
        {
          if ( useAltLeadOut.getSelectedIndex() == 1 || useAltLeadOut.getSelectedIndex() < 1
              && useAltFreq.getSelectedIndex() < 1 && burstMidFrame.getSelectedIndex() < 1 )
          {
            Long t = useAltLeadOut.getSelectedIndex() < 1 ? null : ( Long )altLeadOut.getValue();     
            if ( dataStyle < 2 ) Hex.put( t == null ? null : ( int )( t / 2 ), pdValues, 0x13 );
            if ( dataStyle == 2 ) Hex.put( t == null ? null : ( int )( t / 4 + 10 ), pdValues, 0x11 );
          }
        }
        else if ( source == useAltFreq )
        {
          if ( dataStyle > 2 ) return;
          setPFbits( 3, useAltFreq.getSelectedIndex(), 6, 1 );
          if ( useAltFreq.getSelectedIndex() <= 0 )
          {
            altFreq.setValue( "" );
            altDuty.setValue( "" );
          }
          else if ( altFreq.getValue() == null ) altFreq.setValue( "0" );
        }
        else if ( source == altFreq || source == altDuty )
        {
          if ( dataStyle > 2 ) return;
          if ( useAltFreq.getSelectedIndex() == 1 || useAltLeadOut.getSelectedIndex() < 1 && useAltFreq.getSelectedIndex() < 1 && burstMidFrame.getSelectedIndex() < 1 )
          {
            int ndx = dataStyle < 2 ? 0x13 : 0x11;
            Integer cd = useAltFreq.getSelectedIndex() < 1 ? null : useAltFreq.getSelectedIndex() <= 0 ? null
                : getCarrierData( altFreq.getValue(), altDuty.getValue() );
            if ( cd == null || cd == 0xFFFF )
            {
              pdValues[ ndx ] = null;
              pdValues[ ndx + 1 ] = null;
            }
            else
            {
              Hex.put( getCarrierData( altFreq.getValue(), altDuty.getValue() ), pdValues, dataStyle < 2 ? 0x13 : 0x11 );
            }
          }
        }
        else if ( source == afterBits )
        {
          Long val = ( Long )afterBits.getValue();
          if ( dataStyle < 3 && burstMidFrame.getSelectedIndex() == 1 )
          {
            pdValues[ 0x13 ] = ( val == null ) ? null : ( short )( long )( val + 1 );
          }
        }
      }
    }
  };

  private void putPFbits( int index, int value, int bitStart, int bitCount )
  {
    int mask = ( ( 1 << bitCount ) - 1 ) << bitStart;
    pfValues[ index ] = ( short )( ( pfValues[ index ] & ~mask ) | ( ( value << bitStart ) & mask ) );
  }

  private void setPFbits( int index, int value, int bitStart, int bitCount )
  {
    for ( int i = -1; i < index; i++ )
      if ( pfValues[ i + 1 ] == null )
      {
        if ( i >= 0 )
          pfValues[ i ] = ( short )( pfValues[ i ] | 0x80 );
        pfValues[ i + 1 ] = 0;
      }
    putPFbits( index, value, bitStart, bitCount );
    for ( int i = index; i > 0; i-- )
      if ( pfValues[ i ] == 0 )
      {
        pfValues[ i ] = null;
        pfValues[ i - 1 ] = ( short )( pfValues[ i - 1 ] & 0x7F );
      }
  }

  private void errorMessage( int n )
  {
    if ( !showMessages || errorNumber >= 0 ) return;
    errorNumber = n;
    javax.swing.SwingUtilities.invokeLater( new Runnable()
    {
      public void run() 
      {
        if ( errorNumber < 0 ) return;
        String title = "Data Error";
        String message = null;
        switch ( errorNumber )
        {
          case 0:
            message = "Half-size leadout after first frame is not allowed when\n0-Burst and 1-Burst have different ON times";
            break;
          case 1:
            message = "Alternate lead-out is not allowed when 0-Burst and 1-Burst\nhave different ON times";
            break;
          case 2:
            message = "Half-size leadout after first frame and Alternate lead-out cannot both be selected.";
            break;
          case 3:
            message = "A change of Repeat Type between Minimum and Forced when the Repeat Count" +
            		      "\nis nonzero will only be effective in Build mode, i.e. when the assembler" +
            		      "\nlisting is empty or contains only directives.";
            break;
          case 4:
            message = "A change of Repeat Count when the Repeat Type is Forced will only be" + 
                      "\neffective in Build mode, i.e. when the assembler listing is empty or" +
                      "\ncontains only directives.";
            break;
          default:
            message = "Unknown error";
        }
        JOptionPane.showMessageDialog( RemoteMaster.getFrame(), message, title, JOptionPane.ERROR_MESSAGE );
        errorNumber = -1;
      }
    } );
  }
  
  private void setAssemblerButtons( boolean retitle )
  {
    boolean valid = ( codeTable.getSelectedRowCount() == 1 ) 
        && ! ( procs[ codeTable.getSelectedRow() ] instanceof MAXQProcessor );
    boolean asm = valid && asmButton.isSelected();
    boolean sel = valid && assemblerTable.getSelectedRowCount() > 0;
    assemble.setEnabled( asm );
    disassemble.setEnabled( asm );
    insert.setEnabled( asm && sel );
    delete.setEnabled( asm && sel );
    build.setEnabled( asm );
    getData.setEnabled( asm && !assemblerModel.testBuildMode( processor ) );
    cut.setEnabled( asm && sel );
    paste.setEnabled( asm && sel && cutItems.size() > 0 );
    copy.setEnabled( sel );
    load.setEnabled( valid );
    save.setEnabled( valid );
    selectAll.setEnabled( valid );
    if ( retitle )
    {
      String title = asm ? "Assembler listing (editable)" : "Disassembler listing (not editable)";
      asmBorder.setTitle( title );
      repaint();
    }
  }
  
//  private void setGetData()
//  {
//    getData.setText( assemblerModel.testBuildMode( processor ) ? "Build" : "Get Data" );
//  }

  private int burstUnit = 0;

  private String ns = "";
}
