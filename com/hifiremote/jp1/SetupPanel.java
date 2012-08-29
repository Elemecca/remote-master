package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;

/**
 * The Class SetupPanel.
 */
public class SetupPanel extends KMPanel implements ActionListener, ItemListener, PropertyChangeListener,
    DocumentListener, FocusListener
{
  public static class AltPIDStatus
  {
    public boolean visible = true;
    public boolean required = false;
    public boolean editable = true;
    public boolean hasValue = false;
    public int value = 0;
    public int msgIndex = 0;
  }
  
  public static String getAltPIDReason( int index )
  {
    String reason = "";
    if ( index == 0 )
    {
      return reason;
    }
    switch ( index & 0xFF )
    {
      case 1:
        reason = "Protocol ID exceeds 01FF, not valid for this remote.  An Alternate PID "
          + "is required.";
        break;
      case 2:
        reason = "Protocol is a manual protocol.  Its PID may be changed by setting an Alternate PID.";
        break;
      case 3:
        reason = "Protocol ID conflicts with a built-in protocol.  To use both this and the built-in "
          + "protocol in device upgrades, this one needs to be given an Alternate PID.";
        break;
      case 4:
        reason = "Protocol ID conflicts with existing upgrade.  To use this built-in protocol, edit "
          + "the conflicting upgrade to give it an Alternate PID.";
        break;
      case 5:
        reason = "Protocol has already been given an Alternate PID but it is not, or no longer, "
          + "needed.";
        break;
    }
    if ( ( index & 0x1000 ) == 0x1000 )
    {
      if ( !reason.isEmpty() ) reason += "\n";
      reason += "Protocol has custom code.  To change its PID, first convert it to Manual Settings.";
    }
    String addendum = "";
    switch ( index & 0xF00 )
    {
      case 0x100:
        addendum = "Protocol already used in another device upgrade, so the Alternate PID "
          + "has been taken from that upgrade and cannot be changed.";
        break;
      case 0x200:
        addendum = "Protocol ID already used in another device upgrade by a different protocol. "
          + " An Alternate PID is required.";
        break;
      case 0x300:
        addendum = "Protocol ID clashes with that of an unused protocol upgrade.  To keep that upgrade "
          + "accessible, this protocol needs an Alternate PID.";
        break;
      case 0x400:
        addendum = "Another protocol with same PID and code is already used in another device upgrade, "
          + "so the Alternate PID has been taken from that upgrade and cannot be changed.";
        break;
      case 0x500:
        addendum = "At least one other upgrade uses this manual protocol, so any alternate PID will "
          + "also affect those upgrades.";
        break;
      case 0x800:
        // No message in this case.  Probably should not occur.
        break;
      case 0x900:
        addendum = "Protocol already used by another device upgrade without an Alternate PID, "
          + "so an alternate cannot be given for this upgrade.";
        break;
      case 0xA00:
        addendum = "This case should not occur!";
        break;
      case 0xB00:
        // No message in this case.  Protocol ID clashes with that of an unused protocol upgrade
        // but an existing upgrade prevents an alternate being given.
        break;
      case 0xC00:
        addendum = "Another protocol with same PID and code is already used in another device upgrade "
          + "without an Alternate PID, so an alternate cannot be given for this upgrade.";
        break;
    }
    if ( !addendum.isEmpty() )
    {
      reason += ( reason.isEmpty() ) ? addendum : "\n" + addendum;
    }
    return reason;
  }
  
  /**
   * Instantiates a new setup panel.
   * 
   * @param deviceUpgrade
   *          the device upgrade
   */
  public SetupPanel( DeviceEditorPanel editor, DeviceUpgrade deviceUpgrade )
  {
    super( "Setup", deviceUpgrade );

    this.editor = editor;
    Border border = BorderFactory.createTitledBorder( "Protocol Parameters" );
    pScrollPane = new JScrollPane();
    pScrollPane.setBorder( border );
    pScrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );

    Insets insets = border.getBorderInsets( pScrollPane );
    double bl = insets.left + 10;
    double br = insets.right;
    double b = 10; // space around border
    double i = 5; // space between rows
    double v = 20; // space between groupings
    double c = 10; // space between columns
    double f = TableLayout.FILL;
    double p = TableLayout.PREFERRED;
    double size[][] =
    {
        {
            b, bl, p, b, p, br, c, f, b
        }, // cols
        {
            b, p, v, 0, 0, p, i, p, 0, 0, 0, 0, 0, v, p, b, f, 0, b
        }
    // rows
    };
    tl = new TableLayout( size );
    setLayout( tl );
    
    double sizePH[][] =
    {
        {
          b, p, b, f
        },
        {
          p
        }
    };
    tlPH = new TableLayout( sizePH );

    int row = 1;

    JLabel label = new JLabel( "Setup Code:", SwingConstants.RIGHT );
    add( label, "2, " + row );
    setupCode = new JTextField();
    SetupCodeFilter filter = new SetupCodeFilter( setupCode );
    ( ( AbstractDocument )setupCode.getDocument() ).setDocumentFilter( filter );
    // setupCode.addPropertyChangeListener( "value", this );
    setupCode.getDocument().addDocumentListener( this );
    FocusSelector.selectOnFocus( setupCode );
    label.setLabelFor( setupCode );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and " + SetupCode.getMax()
        + ") for the device upgrade." );

    add( setupCode, "4, " + row );

    row += 2;

    preserveLabel = new JLabel( "Preserve:", SwingConstants.RIGHT );
    String[] choices =
    {
        "OBC and function parameters", "EFC and function hex"
    };

    preserveBox = new JComboBox( choices );
    preserveLabel.setLabelFor( preserveBox );
    preserveRow = row;
    preserveBox.setToolTipText( "Select what to preserve when changing protocols" );
    preserveBox.setSelectedIndex( 0 );

    preserveBox.addActionListener( this );
    if ( Boolean.parseBoolean( JP1Frame.getProperties().getProperty( "enablePreserveSelection", "false" ) ) )
    {
      add( preserveLabel, "2, " + preserveRow );
      add( preserveBox, "4, " + preserveRow );
      tl.setRow( preserveRow, p );
      tl.setRow( preserveRow + 1, i );
    }

    row += 2;

    label = new JLabel( "Protocol:", SwingConstants.RIGHT );
    add( label, "2, " + row );

    protocolList = new JComboBox();
    protocolList.addActionListener( this );
    label.setLabelFor( protocolList );
    protocolList.setMaximumRowCount( 25 );
    protocolList.setToolTipText( "Select the protocol to be used for this device upgrade from the drop-down list." );
    add( protocolList, "4, " + row );

    row += 2;

    label = new JLabel( "Protocol ID:", SwingConstants.RIGHT );
    add( label, "2, " + row );

    protocolID = new JTextField();
    label.setLabelFor( protocolID );
    protocolID.setEditable( false );
    protocolID.setToolTipText( "This is the protocol ID (PID) that corresponds to the selected protocol." );
    add( protocolID, "4, " + row );
    
    row += 2;

    toManualRow = row;
    toManual = new JButton( "Convert to Manual" );
    toManual.setToolTipText( "Convert custom code to Manual Settings protocol" );
    toManual.addActionListener( this );
    toManual.setVisible( false );
    add( toManual, "4, " + row );
    
    row += 2;
    
    altPIDRow = row;
    altPIDLabel = new JLabel( "Alternate PID:", SwingConstants.RIGHT );
    add( altPIDLabel, "2, " + row );
    altPID = new JFormattedTextField( new HexFormat( 0, 2 ) );
    altPID.addPropertyChangeListener( "value", this );
    altPIDLabel.setLabelFor( altPID );
    altPID.setVisible( false );
    altPIDLabel.setVisible( false );
    altPID.setToolTipText( "An alternate PID to be used instead of the main PID." );
    add( altPID, "4, " + row );
    
    row++;
    
    altPIDMessage = new JLabel( "Prototype message" );
    altPIDMessage.setForeground( Color.RED );
    tl.setRow( row, altPIDMessage.getPreferredSize().height );
    add( altPIDMessage, "4, " + row );
    
    row++;
    tl.setRow( row, v - altPIDMessage.getPreferredSize().height );
    altPIDMessage.setText( "" );

    row++;
    
    protocolHolder = new JPanel( tlPH );
    pScrollPane.setViewportView( protocolHolder );
    add( pScrollPane, "1, " + row + ", 5, " + row );
    
    label = new JLabel( "Fixed Data:", SwingConstants.RIGHT );
    fixedData = new JTextField();
    fixedData.setEditable( false );
    protocolHolder.add( label, "1, 0" );
    protocolHolder.add( fixedData, "3, 0" );

    notes = new JTextArea( 5, 50 );
    notes.setToolTipText( "Enter any notes about this device upgrade." );
    notes.setLineWrap( true );
    notes.setWrapStyleWord( true );
    JScrollPane scrollPane = new JScrollPane( notes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Upgrade Notes" ),
        scrollPane.getBorder() ) );
    notes.getDocument().addDocumentListener( this );
    new TextPopupMenu( notes );
    add( scrollPane, "7, 1, 7, " + row );

    row += 2;

    protocolNotes = new JEditorPane();
    protocolNotes.setBackground( label.getBackground() );
    protocolNotes.setToolTipText( "Notes about the selected protocol." );
    protocolNotes.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    protocolNotes.setEditable( false );
    // protocolNotes.setLineWrap( true );
    // protocolNotes.setWrapStyleWord( true );
    scrollPane = new JScrollPane( protocolNotes );
    scrollPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createTitledBorder( "Protocol Notes" ),
        scrollPane.getBorder() ) );
    add( scrollPane, "1, " + row + ", 7, " + row );
    
    row++;
    
    altPIDReason = new JTextPane();
    altPIDReason.setEditable( false );
    altPIDReason.setBackground( label.getBackground() );
    Font font = altPIDReason.getFont();
    Font font2 = font.deriveFont( Font.BOLD, 12 );
    altPIDReason.setFont( font2 );
    altPIDReason.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ) );
    altPIDReasonRow = row;
    JP1Frame.getProperties().addPropertyChangeListener( "enablePreserveSelection", this );
  } // SetupPanel
  
  private void showToManual()
  {
    boolean isCustom = deviceUpgrade.isCustom();
    toManual.setVisible( isCustom );
    tl.setRow( toManualRow - 1, isCustom ? 5 : 0 );
    tl.setRow( toManualRow, isCustom ? TableLayout.PREFERRED : 0 );
  }
  
  private void showAltPID()
  {
    status = deviceUpgrade.testAltPID();
    altPIDLabel.setVisible( status.visible );
    altPID.setVisible( status.visible );
    tl.setRow( altPIDRow - 1, status.visible ? 5 : 0 );
    tl.setRow( altPIDRow, status.visible ? TableLayout.PREFERRED : 0 );
    altPID.setEditable( status.editable );
    if ( status.hasValue )
    {
      Hex hx = ( new Hex( 2 ) );
      hx.put(  status.value, 0 );
      altPID.setValue( hx );
    }
    if ( status.visible && status.required )
    {
      protocolID.setText( protocolID.getText() + " : ALT PID REQUIRED" );
    }
    if ( editor.getOwner().isVisible() )
    {
      // In the constructors that use the editor, setAltPIDReason() is called after pack()
      // and is excluded here, as presence of altPIDReason causes the windows to have
      // excessive size.
      setAltPIDReason();
    }
  }
  
  public void setAltPIDReason()
  {
    if ( status.msgIndex == 0 && tl.getRow( altPIDReasonRow ) != 0 )
    {
      remove( altPIDReason );
      tl.setRow( altPIDReasonRow, 0 );
    }
    else if ( status.msgIndex > 0 && tl.getRow( altPIDReasonRow ) == 0 )
    {
      add( altPIDReason, "1, " + altPIDReasonRow + ", 7, " + altPIDReasonRow );
      tl.setRow( altPIDReasonRow, TableLayout.PREFERRED );
    }
    altPIDReason.setText( getAltPIDReason( status.msgIndex ) );
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    if ( deviceUpgrade == null )
      return;
    updateInProgress = true;
    Protocol p = deviceUpgrade.getProtocol();
    Remote remote = deviceUpgrade.getRemote();
    setupCode.setText( SetupCode.toString( deviceUpgrade.getSetupCode() ) );
    setupCode.setToolTipText( "Enter the desired setup code (between 0 and " + SetupCode.getMax()
        + ") for the device upgrade." );
    java.util.List< Protocol > protocols = ProtocolManager.getProtocolManager().getProtocolsForRemote( remote );
    if ( !protocols.contains( p ) )
    {
      // ??? There should be a better way to handle this (the current protocol is
      // incompatible with the current remote), but this way is at least better than
      // the old way of displaying the first compatible protocol.
      protocols = new ArrayList< Protocol >( protocols );
      protocols.add( p );
    }

    Value[] vals = deviceUpgrade.getParmValues();
    p.setDeviceParms( vals );
    updateParameters();
    protocolList.setModel( new DefaultComboBoxModel( protocols.toArray() ) );
    protocolList.setSelectedItem( p );
    protocolID.setText( p.getID( remote, false ).toString() );
    notes.setText( deviceUpgrade.getNotes() );
    fixedData.setText( p.getFixedData( vals ).toString() );

    updateProtocolNotes( p.getNotes() );
    
    showAltPID();
    showToManual();
    altPID.setValue( p.getRemoteAltPID().get( remote.getSignature() ) );
    setAltPIDMessage();

    updateInProgress = false;
  }

  /**
   * Update parameters.
   */
  public void updateParameters()
  {
    DeviceParameter[] newParameters = deviceUpgrade.getProtocol().getDeviceParameters();
    int maxWidth = altPIDLabel.getPreferredSize().width;  // Widest fixed label
    int offset = pScrollPane.getVerticalScrollBar().getPreferredSize().width + 5;
    if ( parameters != newParameters )
    {
      if ( parameters != null )
      {
        for ( int i = 0; i < parameters.length; i++ )
        {
          parameters[ i ].removeListener( this );
          protocolHolder.remove( parameters[ i ].getLabel() );
          protocolHolder.remove( parameters[ i ].getComponent() );
          tlPH.deleteRow( 0 );
          tlPH.deleteRow( 0 );
        }
      }
      parameters = newParameters;
      if ( parameters != null )
      {
        int row = 0;
        for ( int i = 0; i < parameters.length; i++ )
        {
          parameters[ i ].addListener( this );
          tlPH.insertRow( row, TableLayout.PREFERRED );
          JLabel label = parameters[ i ].getLabel();
          maxWidth = Math.max( maxWidth, label.getPreferredSize().width );
          protocolHolder.add( label, "1, " + row );
          protocolHolder.add( parameters[ i ].getComponent(), "3, " + row );
          row++ ;
          tlPH.insertRow( row++ , 5 );
        }
        
        int maxRows = 7;
        if ( altPID.isVisible() )
        {
          maxRows--;
        }
        if ( Boolean.parseBoolean( JP1Frame.getProperties().getProperty( "enablePreserveSelection", "false" ) ) )
        {
          // Preserve Selection box is added/removed rather than made visible or invisible.
          maxRows--;
        }
        int maxHeight = maxRows * ( fixedData.getPreferredSize().height + 5 );
        Insets insets = pScrollPane.getBorder().getBorderInsets( pScrollPane );
        maxHeight += insets.top + insets.bottom;
        
        // Reset the scrollpane preferred size to null, so that getting it calls the layout manager
        // rather than returning the size set on the previous call to this routine.
        pScrollPane.setPreferredSize( null );
        Dimension d = pScrollPane.getPreferredSize();
        if ( d.height > maxHeight )
        {
          // This allows space for scrollbar without truncating the text fields.
          offset = 0;
        }
        d.height = maxHeight;
        pScrollPane.setPreferredSize( d );
        tl.setColumn( 2, maxWidth + offset ) ;
        tlPH.setColumn( 1, maxWidth + offset ) ;
      }
    }
  }

  /**
   * Update fixed data.
   */
  public void updateFixedData()
  {
    Protocol p = deviceUpgrade.getProtocol();
    p.initializeParms();
    deviceUpgrade.setParmValues( p.getDeviceParmValues() );
    fixedData.setText( p.getFixedData( deviceUpgrade.getParmValues() ).toString() );
  }

  // ActionListener Methods
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();

    if ( source == protocolList )
    {
      protocolList.hidePopup();
      Protocol newProtocol = getSelectedProtocol();
      Protocol oldProtocol = deviceUpgrade.getProtocol();
      RemoteConfiguration remoteConfig = deviceUpgrade.getRemoteConfig();
      if ( newProtocol != oldProtocol )
      {
        if ( deviceUpgrade.setProtocol( newProtocol ) )
        {
          Remote remote = deviceUpgrade.getRemote();
          protocolID.setText( newProtocol.getID( remote, false ).toString() );
          altPID.setValue( newProtocol.getRemoteAltPID().get( remote.getSignature() ) );
          showAltPID();
          showToManual();
          setAltPIDMessage();
          updateParameters();
          fixedData.setText( newProtocol.getFixedData( newProtocol.getDeviceParmValues() ).toString() );
          updateProtocolNotes( newProtocol.getNotes() );
          revalidate();

          deviceUpgrade.checkSize();
          propertyChangeSupport.firePropertyChange( "protocol", oldProtocol, newProtocol );
        }
        else
        {
          protocolList.removeActionListener( this );
          protocolList.setSelectedItem( oldProtocol );
          protocolList.addActionListener( this );
        }
      }
      else if ( !updateInProgress && remoteConfig != null )
      {
        // Protocol is unchanged, but if there is more than one custom protocol then cycle
        // through them
        String title = "Multiple custom protocols";
        String message = "You have reselected the existing protocol for this device upgrade\n"
            + "and there is more than one protocol upgrade in this remote that\n"
            + "can act as custom code for it.  Do you want to change to a different\n" + "custom code?\n\n"
            + "Repeating this and selecting YES each time will cycle through all\n"
            + "available compatible custom codes.";
        ProtocolUpgrade pu = oldProtocol.getCustomUpgrade( remoteConfig, false );
        if ( pu != null
            && oldProtocol.matched()
            && !pu.getCode().equals( deviceUpgrade.getCode() )
            && JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE ) == JOptionPane.YES_OPTION )
        {
          // Save old custom code and install new code
          String proc = remoteConfig.getRemote().getProcessor().getEquivalentName();
          oldProtocol.newCustomCode = pu;
          oldProtocol.customCode.put( proc, pu.getCode() );
        }
      }
    }
    else if ( source == preserveBox )
    {
      deviceUpgrade.setPreserveOBC( preserveBox.getSelectedIndex() == 0 );
    }
    else if ( source == toManual )
    {
      JFrame frame = editor.getOwner();
      if ( frame instanceof DeviceUpgradeEditor )
      {
        String title = "Convert Custom Code to Manual Settings";
        String message = 
          "Do you want to save this device upgrade as a separate .rmdu file before the\n" +
          "protocol is converted to Manual Settings?\n\n" +
          "In the conversion, you lose the device parameters of the original protocol, which\n" +
          "makes the device upgrade more difficult to edit.  By saving it, you preserve these\n" +
          "parameters in a file that can be edited with RM and then loaded into RMIR.";
        int ans = JOptionPane.showConfirmDialog( null, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE );
        if ( ans == JOptionPane.CANCEL_OPTION )
        {
          return;
        }
        else if ( ans == JOptionPane.YES_OPTION )
        {
          try
          {
            ( ( DeviceUpgradeEditor )frame ).save();
          }
          catch ( IOException e1 )
          {
            e1.printStackTrace();
          }
        }
      }

      Remote remote = deviceUpgrade.getRemote();
      deviceUpgrade.originalProtocol = deviceUpgrade.protocol;
      Hex code = null;
      try
      {
        code = ( Hex )deviceUpgrade.originalProtocol.getCustomCode( remote.getProcessor() ).clone();
      }
      catch ( CloneNotSupportedException e1 )
      {
        e1.printStackTrace();
      }
      deviceUpgrade.convertedProtocol = deviceUpgrade.originalProtocol.convertToManual( remote, deviceUpgrade.getParmValues(), code );
      ProtocolManager.getProtocolManager().add( deviceUpgrade.convertedProtocol );
      deviceUpgrade.changeProtocol( deviceUpgrade.originalProtocol, deviceUpgrade.convertedProtocol );    
      update();
    }
    else
    {
      // must be a protocol parameter
      updateFixedData();
    }
  } // actionPerformed

  protected void updateProtocolNotes( String text )
  {
    String contentType = "text/plain";
    if ( text != null && text.startsWith( "<" ) )
    {
      contentType = "text/html";
    }
    EditorKit kit = protocolNotes.getEditorKitForContentType( contentType );
    protocolNotes.setEditorKit( kit );
    protocolNotes.setText( text );
    protocolNotes.setCaretPosition( 0 );
    protocolNotes.revalidate();
  }

  /**
   * Gets the selected protocol.
   * 
   * @return the selected protocol
   */
  public Protocol getSelectedProtocol()
  {
    Protocol protocol = ( Protocol )protocolList.getSelectedItem();
    return protocol;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#commit()
   */
  public void commit()
  {
    deviceUpgrade.getProtocol().updateFunctions( deviceUpgrade.getFunctions() );
  }

  /**
   * Update notes.
   */
  private void updateNotes()
  {
    deviceUpgrade.setNotes( notes.getText() );
  }

  /**
   * Update setup code.
   */
  private void updateSetupCode()
  {
    String text = setupCode.getText();
    if ( text.equals( "" ) )
    {
      return;
    }
    int val = Integer.parseInt( setupCode.getText() );
    int oldSetupCode = deviceUpgrade.getSetupCode();
    deviceUpgrade.setSetupCode( val );
    propertyChangeSupport.firePropertyChange( "setupCode", oldSetupCode, val );
  }

  public void release()
  {
    for ( int i = 0; i < parameters.length; i++ )
    {
      parameters[ i ].removeListener( this );
    }
  }

  /**
   * Doc changed.
   * 
   * @param e
   *          the e
   */
  private void docChanged( DocumentEvent e )
  {
    if ( !updateInProgress )
    {
      Document doc = e.getDocument();
      if ( doc == notes.getDocument() )
        updateNotes();
      else if ( doc == setupCode.getDocument() )
        updateSetupCode();
      else
        updateFixedData();
    }
  }

  // DocumentListener
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate( DocumentEvent e )
  {
    docChanged( e );
  }

  // FocusListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
   */
  public void focusGained( FocusEvent e )
  {
    JP1Frame.clearMessage( controlWithFocus );
    controlWithFocus = ( JTextComponent )e.getSource();
    JP1Frame.clearMessage( controlWithFocus );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
   */
  public void focusLost( FocusEvent e )
  {}

  // ItemListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged( ItemEvent e )
  {
    if ( !updateInProgress )
      updateFixedData();
  }

  // PropertyChangeListener methods
  /*
   * (non-Javadoc)
   * 
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange( PropertyChangeEvent event )
  {
    if ( event.getPropertyName().equals( "enablePreserveSelection" ) )
    {
      boolean oldValue = Boolean.parseBoolean( ( String )event.getOldValue() );
      boolean newValue = Boolean.parseBoolean( ( String )event.getNewValue() );
      if ( oldValue && !newValue )
      {
        remove( preserveLabel );
        remove( preserveBox );
        tl.setRow( preserveRow, 0 );
        tl.setRow( preserveRow + 1, 0 );
      }
      else
      {
        add( preserveLabel, "2, " + preserveRow );
        add( preserveBox, "4, " + preserveRow );
        tl.setRow( preserveRow, TableLayout.PREFERRED );
        tl.setRow( preserveRow + 1, 5 );
      }
    }
    else if ( event.getSource() == altPID )
    {
      Hex pid = ( Hex )altPID.getValue();
      if ( pid == null ) return;
      if ( pid.length() == 1 )
      {
        short val = pid.getData()[ 0 ];
        pid = new Hex( 2 );
        pid.getData()[ 1 ] = val;
        altPID.setValue( pid );
        return;
      }
      
      if ( deviceUpgrade.originalProtocol == null && deviceUpgrade.protocol instanceof ManualProtocol )
      {
        deviceUpgrade.originalProtocol = deviceUpgrade.protocol;
      }
      if ( deviceUpgrade.originalProtocol != null && deviceUpgrade.convertedProtocol == null )
      {
        // Clone the protocol
        deviceUpgrade.convertedProtocol = new ManualProtocol( ( ( ManualProtocol )deviceUpgrade.originalProtocol ).getIniSection() );
        deviceUpgrade.setProtocol( deviceUpgrade.convertedProtocol );
      }
      
      deviceUpgrade.getProtocol().setAltPID( deviceUpgrade.getRemote(), pid );
      setAltPIDMessage();
      update();
    }
    else if ( !updateInProgress )
    {
      updateSetupCode();
    }
  }
  
  private boolean setAltPIDMessage()
  {
    boolean valid = true;
    if ( status.visible )
    {
      Hex pid = ( Hex )altPID.getValue();
      if ( pid == null ) pid = new Hex( 0 );
      Remote remote = deviceUpgrade.getRemote();
      RemoteConfiguration remoteConfig = deviceUpgrade.getRemoteConfig();
      List< Protocol > builtIn = ProtocolManager.getProtocolManager().getBuiltinProtocolsForRemote( remote, pid );
      if ( !builtIn.isEmpty() )
      {
        altPIDMessage.setText( "Conflicts with built-in protocol" );
        altPID.setForeground( Color.RED );
        valid = false;
      }
      else if ( status != null && status.required && pid.length() == 0 )
      {
        altPIDMessage.setText( "Alternate PID cannot be null" );
        altPID.setForeground( Color.RED );
        valid = false;
      }
      else
      {
        if ( remoteConfig != null )
        {
          for ( DeviceUpgrade du : remoteConfig.getDeviceUpgrades() )
          {
            if ( du == deviceUpgrade.getBaseUpgrade() 
                || du.getProtocol() == deviceUpgrade.protocol
                || du.getProtocol() == deviceUpgrade.originalProtocol )
            {
              continue;
            }
            if ( du.getProtocol().getID( remote ).equals( deviceUpgrade.getProtocol().getID( remote ) ) )
            {
              // A different protocol with same PID is already used by a device upgrade.  Alternate
              // required.
              altPIDMessage.setText( "Conflicts with existing upgrade" );
              altPID.setForeground( Color.RED );
              valid = false;
              break;
            }
          }
        }
      }
      if ( valid == true )
      {
        altPIDMessage.setText( "" );
        altPID.setForeground( Color.BLACK );
        if ( deviceUpgrade.getProtocol().getCustomUpgrade( remoteConfig, false ) != null )
        {
          altPIDMessage.setText( "Conflicts with protocol upgrade" );
        }
      }
    }
    else if ( status.required )
    {
      valid = false;
      altPIDMessage.setText( "PID in use, Alt PID not available" );
    }
    else
    {
      altPIDMessage.setText( "" );
    }
    editor.tabbedPane.setEnabled( valid );
    JFrame frame = editor.getOwner();
    if ( frame instanceof DeviceUpgradeEditor )
    {
      DeviceUpgradeEditor editor = ( DeviceUpgradeEditor )frame;
      editor.okButton.setEnabled( valid );
      editor.saveAsButton.setEnabled( valid );
    }
    else if ( frame instanceof KeyMapMaster )
    {
      KeyMapMaster km = ( KeyMapMaster )frame;
      km.saveItem.setEnabled( valid && deviceUpgrade.getFile() != null );
      km.saveAsItem.setEnabled( valid );
    }
    return valid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Container#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener( PropertyChangeListener listener )
  {
    if ( ( propertyChangeSupport != null ) && ( listener != null ) )
      propertyChangeSupport.addPropertyChangeListener( listener );
  }

  /** The setup code. */
  private JTextField setupCode = null;

  /** The protocol list. */
  private JComboBox protocolList = null;

  /** The protocol id. */
  private JTextField protocolID = null;
  private JButton toManual = null;
  private int toManualRow = 0;
  private JFormattedTextField altPID = null;
  private JLabel altPIDLabel = null;
  private JLabel altPIDMessage = null;
  private JTextPane altPIDReason = null;
  private int altPIDRow = 0;
  private int altPIDReasonRow = 0;
  private AltPIDStatus status = null;

  private JLabel preserveLabel = null;
  private JComboBox preserveBox = null;
  private int preserveRow = 0;

  /** The notes. */
  private JTextArea notes = null;

  /** The protocol holder. */
  private JPanel protocolHolder = null;
  private JScrollPane pScrollPane = null;

  /** The fixed data. */
  private JTextField fixedData = null;

  /** The protocol notes. */
  private JEditorPane protocolNotes = null;

  /** The parameters. */
  private DeviceParameter[] parameters = null;

  /** Layout for main panel. */
  private TableLayout tl;
  
  /** Layout for Protocol Holder. */
  private TableLayout tlPH;

  /** The update in progress. */
  private boolean updateInProgress = false;

  /** The control to select all. */
  private JTextComponent controlWithFocus = null;

  /** The property change support. */
  private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport( this );
  
  private DeviceEditorPanel editor = null;
}
