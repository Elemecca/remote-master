package com.hifiremote.jp1;

import info.clearthought.layout.TableLayout;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import com.hifiremote.jp1.ButtonPanel.SelectionPanel;
import com.hifiremote.jp1.RemoteConfiguration.KeySpec;

/**
 * The Class LayoutPanel.
 */
public class LayoutPanel extends KMPanel implements ActionListener, Runnable
{

  /**
   * Instantiates a new layout panel.
   * 
   * @param devUpgrade
   *          the dev upgrade
   */
  public LayoutPanel( DeviceUpgrade devUpgrade )
  {
    super( "Layout", devUpgrade );
    setLayout( new BorderLayout() );
    imagePanel = new ImagePanel();
    // Don't know why, but tooltips don't work without this
    imagePanel.setToolTipText( "" );
    imagePanel.setFocusable( true );
    imagePanel.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0 ), "delete" );
    deleteAction = new AbstractAction( "Remove" )
    {
      public void actionPerformed( ActionEvent e )
      {
        setFunction( currentShape, null );
        doRepaint();
      }
    };

    imagePanel.getActionMap().put( "delete", deleteAction );
    remotePanel = new JPanel( new BorderLayout() );
    
    Remote remote = devUpgrade.getRemote();
    if ( remote.isSSD() )
    {
      pagePanel = new JPanel( new GridLayout( 2, 2 ) );
      pagePanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
      pageDown = new JRadioButton( "Page-" );
      pageUp = new JRadioButton( "Page+" );
      pageDown.addActionListener( this );
      pageUp.addActionListener( this );
      pageNumber = new JTextField();
      pageNumber.setText( Integer.toString( page ) );
      pageNumber.setEditable( false );
      pageNumber.setEnabled( false );
      pagePanel.add( pageDown );
      pagePanel.add( pageUp );
      pagePanel.add( new JLabel( "Page:" ) );
      pagePanel.add( pageNumber );
      remotePanel.add( pagePanel, BorderLayout.PAGE_START );
    }

    scrollPanel = Box.createHorizontalBox();
    remotePanel.add( scrollPanel, BorderLayout.SOUTH );
    scrollLeft = new JButton( "<" );
    scrollLeft.setEnabled( false );
    scrollLeft.addActionListener( this );
    scrollPanel.add( scrollLeft );

    scrollPanel.add( Box.createHorizontalGlue() );
    scrollPanel.add( new JLabel( "Scroll" ) );
    scrollPanel.add( Box.createHorizontalGlue() );

    scrollRight = new JButton( ">" );
    scrollRight.setEnabled( false );
    scrollRight.addActionListener( this );
    scrollPanel.add( scrollRight );

    scrollPane = new JScrollPane( imagePanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
    remotePanel.add( scrollPane, BorderLayout.CENTER );

    JPanel rightPanel = new JPanel( new BorderLayout() );

    ComponentAdapter componentListener = new ComponentAdapter()
    {
      public void componentResized( ComponentEvent event )
      {
        int width = scrollPane.getViewport().getExtentSize().width;
        Remote remote = deviceUpgrade.getRemote();
        int height = ( width * remote.getHeight() ) / remote.getWidth();
        Dimension d = new Dimension( width, height );
        imagePanel.setPreferredSize( d );
        if ( width != remote.getWidth() )
        {
          double scale = ( double )width / ( double )remote.getWidth();
          transform = AffineTransform.getScaleInstance( scale, scale );
        }
        else
        {
          transform = null;
        }

        scrollPane.revalidate();
      }
    };
    remotePanel.addComponentListener( componentListener );

    splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, remotePanel, rightPanel );
    add( splitPane, BorderLayout.CENTER );
    
    Box box = Box.createVerticalBox();
    normalMode = new JRadioButton( "Normal", true );
    shiftMode = new JRadioButton( "Shift" );
    xShiftMode = new JRadioButton( "XShift" );
    selector = new SelectionPanel( this, this );

    if ( devUpgrade.getRemote().usesEZRC() )
    {
      box.add( selector );
    }
    else
    {
      JPanel modePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      modePanel.setBorder( BorderFactory.createTitledBorder( "Mode" ) );

      ButtonGroup group = new ButtonGroup();

      normalMode.addActionListener( this );
      group.add( normalMode );
      modePanel.add( normalMode );

      shiftMode.addActionListener( this );
      group.add( shiftMode );
      modePanel.add( shiftMode );
      
      xShiftMode.addActionListener( this );
      group.add( xShiftMode );
      modePanel.add( xShiftMode );
      box.add( modePanel );
    }

    double b = 5; // space between rows and around border
    double c = 10; // space between columns
    double pr = TableLayout.PREFERRED;
    double pf = TableLayout.FILL;
    double size[][] =
    {
        {
            b, pr, c, pf, b
        },  // cols
        {}  // rows added later
    };
    double rowSize[] = remote.usesEZRC() ? new double[]{  b, pr, b, pr, b, pr, b, pr, b } :  new double[]{ b, pr, b, pr, b };
    size[ 1 ] = rowSize;
//    JPanel infoPanel = new JPanel( new GridLayout( 2, 2 ) );
    JPanel infoPanel = new JPanel( new TableLayout( size ) );
    JLabel label = new JLabel( "Button:" );
    label.setToolTipText( "button label" );
    infoPanel.add( label, "1, 1" );
    buttonName = new JTextField();
    buttonName.setEditable( false );
    infoPanel.add( buttonName, "3, 1" );
    String sRow = "3";
    if ( remote.usesEZRC() )
    {
      infoPanel.add( new JLabel( "Device:" ), "1, " + sRow );
      device = new JTextField();
      device.setEditable( false );
      infoPanel.add( device, "3, " + sRow );
      if ( remote.isSSD() )
      {
        sRow = "7";
        infoPanel.add( new JLabel( "Alias:" ), "1, " + sRow );
        alias = new JTextField();
        alias.setEditable( true );
        infoPanel.add( alias, "3, " + sRow );
      }
      sRow = "5";
    }
    infoPanel.add( new JLabel( "Function:" ), "1, " + sRow );
    function = new JTextField();
    function.setEditable( false );
    infoPanel.add( function,"3, " + sRow );


    
    box.add( infoPanel );

    rightPanel.add( box, BorderLayout.NORTH );

    JPanel panel = new JPanel( new BorderLayout() );
    label = new JLabel( "Available Functions:" );
    label.setToolTipText( "available functions label" );
    label.setBorder( BorderFactory.createEmptyBorder( 2, 2, 3, 2 ) );
    panel.add( label, BorderLayout.NORTH );
    rightPanel.add( panel, BorderLayout.CENTER );

    JPanel outerPanel = new JPanel( new BorderLayout() );
    functionPanel = new GridFlowPanel();

    outerPanel.add( new JScrollPane( functionPanel ), BorderLayout.CENTER );
    panel.add( outerPanel, BorderLayout.CENTER );

    panel = new JPanel();
    autoAssign = new JButton( "Auto assign" );
    autoAssign.setToolTipText( "Automatically assign functions to buttons, by matching names." );
    autoAssign.addActionListener( this );
    panel.add( autoAssign );

    JButton button = new JButton( deleteAction );
    button.setToolTipText( "Remove the assigned function from the button." );
    panel.add( button );

    box = Box.createVerticalBox();
    box.add( new JSeparator() );
    box.add( panel );

    rightPanel.add( box, BorderLayout.SOUTH );

    DropTarget dropTarget = new LayoutDropTarget();
    dropTarget.setComponent( imagePanel );

    MouseListener ml = new MouseAdapter()
    {
      public void mousePressed( MouseEvent e )
      {
        imagePanel.requestFocusInWindow();
        Point p = e.getPoint();
        ButtonShape savedShape = currentShape;
        currentShape = getShapeAtPoint( p );
        Button b = getButtonForShape( currentShape );
        if ( b == null )
          currentShape = null;

        if ( currentShape == null )
          return;

        if ( currentShape != savedShape )
        {
          setButtonText( currentShape, getButtonForShape( currentShape ) );
          doRepaint();
        }
        showPopup( e );
      }

      public void mouseReleased( MouseEvent e )
      {
        showPopup( e );
      }

      private void showPopup( MouseEvent e )
      {
        if ( e.isPopupTrigger() )
        {
          ButtonShape buttonShape = getShapeAtPoint( e.getPoint() );
          if ( ( buttonShape != null ) && ( getButtonForShape( buttonShape ) != null ) )
          {
            currentShape = buttonShape;
            doRepaint();
            popup.show( imagePanel, e.getX(), e.getY() );
          }
        }
      }
    };
    imagePanel.addMouseListener( ml );
  }

  private GeneralFunction getFunction( Button b )
  {
    Remote remote = deviceUpgrade.getRemote();
    GeneralFunction f = null;
    if ( normalMode.isSelected() )
    {
      Macro macro = null;
      if ( remote.usesEZRC() )
      {
        LearnedSignal ls = deviceUpgrade.getLearnedMap().get( ( int )b.getKeyCode() );
        if ( ls != null )
        {
          f = ls;
        }
        macro = deviceUpgrade.getMacroMap().get( ( int )b.getKeyCode() );
        if ( f == null && macro != null )
        {
          if ( macro.isSystemMacro() )
          {
            KeySpec ks = macro.getItems().get( 0 );
            if ( remote.isSSD() )
            {
              f = ks.fn;
            }
            else
            {
              f = ks.db.getUpgrade().getFunction( ks.btn.getKeyCode() );
            }
          }
          else
          {
            f = macro;
          }
        }
      }
      if ( f == null )
      {
        f = deviceUpgrade.getFunction( b, Button.NORMAL_STATE );
      }
    }
    else if ( shiftMode.isSelected() )
    {
      f = deviceUpgrade.getFunction( b, Button.SHIFTED_STATE );
    }
    else if ( xShiftMode.isSelected() )
    {
      f = deviceUpgrade.getFunction( b, Button.XSHIFTED_STATE );
    }
    return f;
  }
  
  /**
   * Adds the function.
   * 
   * @param f
   *          the f
   */
  @Override
  public void addFunction( GeneralFunction f )
  {
    if ( ( f == null ) || ( f.hasData() && ( f.getName() != null ) && ( f.getName().length() > 0 ) ) )
    {
      FunctionLabel l;
      if ( f == null )
        l = new FunctionLabel( null );
      else
        l = f.getLabel();
      l.addMouseListener( doubleClickListener );
      l.showAssigned( deviceUpgrade.getButtonRestriction() );
      functionPanel.add( l );

      FunctionItem item;
      if ( f == null )
        item = new FunctionItem( null );
      else
        item = f.getFunctionItem();
      item.addActionListener( this );
      popup.add( item );
    }
  }
  
  @Override
  public void revalidateFunctions()
  {
    functionPanel.revalidate();
  }

  /**
   * Sets the functions.
   */
  private void setFunctions()
  {
    popup = new JPopupMenu();
    popup.setLayout( new GridLayout( 0, 3 ) );
    functionPanel.removeAll();
    selector.addFunctions();
  }

  /**
   * Enable scroll buttons.
   */
  private void enableScrollButtons()
  {
    if ( maps.length > 1 )
    {
      scrollPanel.setVisible( true );
      scrollLeft.setEnabled( screenIndex > 0 );
      scrollRight.setEnabled( screenIndex < maps.length - 1 );
    }
    else
    {
      scrollPanel.setVisible( false );
      scrollLeft.setEnabled( false );
      scrollRight.setEnabled( false );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.hifiremote.jp1.KMPanel#update()
   */
  public void update()
  {
    Remote r = deviceUpgrade.getRemote();
    maps = r.getImageMaps( deviceUpgrade.getDeviceType() );
    if ( screenIndex >= maps.length )
      screenIndex = maps.length - 1;
    enableScrollButtons();
    map = maps[ screenIndex ];
    image = new ImageIcon( map.getImageFile().getAbsolutePath() );

    splitPane.setDividerLocation( r.getWidth() + scrollPane.getVerticalScrollBar().getWidth() );

    boolean found = false;
    for ( ButtonShape shape : map.getShapes() )
    {
      if ( currentShape == shape )
      {
        found = true;
        break;
      }
    }
    if ( !found )
      currentShape = null;

    setButtonText( currentShape, getButtonForShape( currentShape ) );

    setFunctions();

    if ( !r.usesEZRC() )
    {
      shiftMode.setText( r.getShiftLabel() );
      if ( r.getShiftEnabled() )
        shiftMode.setEnabled( true );
      else
      {
        shiftMode.setEnabled( false );
        if ( shiftMode.isSelected() )
          normalMode.setSelected( true );
      }

      xShiftMode.setText( r.getXShiftLabel() );
      if ( r.getXShiftEnabled() )
        xShiftMode.setEnabled( true );
      else
      {
        xShiftMode.setEnabled( false );
        if ( xShiftMode.isSelected() )
          normalMode.setSelected( true );
      }
    }
    doRepaint();
  }

  /**
   * Do repaint.
   */
  private void doRepaint()
  {
    imagePanel.repaint( 0L, 0, 0, imagePanel.getWidth(), imagePanel.getHeight() );
  }

  /**
   * Sets the button text.
   * 
   * @param buttonShape
   *          the button shape
   * @param b
   *          the b
   */
  private void setButtonText( ButtonShape buttonShape, Button b )
  {
    Remote remote = deviceUpgrade.getRemote();
    if ( ( buttonShape != null ) && ( b != null ) )
    {
      String name = buttonShape.getName();
      if ( name == null )
      {
        if ( normalMode.isSelected() )
          name = b.getName();
        else if ( shiftMode.isSelected() )
          name = b.getShiftedName();
        else if ( xShiftMode.isSelected() )
          name = b.getXShiftedName();
      }
      buttonName.setText( name );
      GeneralFunction f = getFunction( b );
      if ( f != null )
        function.setText( f.getDisplayName() );
      else
        function.setText( "" );
      if ( remote.usesEZRC() )
      {
        Macro macro = null;
//        device.setText( "" );
//        alias.setText( "" );
        if ( f instanceof Function )
        {
          macro = deviceUpgrade.getMacroMap().get( ( int )b.getKeyCode() );
          if ( macro != null && !macro.isSystemMacro() )
          {
            macro = null;
          }
        }
        DeviceButton db = ( macro != null ) ? macro.getItems().get( 0 ).db
            : f != null ? f.getUpgrade( remote ).getButtonRestriction() : null;
        device.setText( db == null ? "" : db.getName() );
        if ( alias != null )
        {
          alias.setText( macro == null ? "" : macro.getName() );
          alias.setEnabled( macro != null );
        }
      }
      deleteAction.setEnabled( f != null );
    }
    else
    {
      buttonName.setText( "" );
      function.setText( "" );
      if ( remote.usesEZRC() )
      {
        device.setText( "" );
        if ( alias != null )
        {
          alias.setText( "" );
        }
      }
      deleteAction.setEnabled( false );
    }
  }

  /**
   * Gets the shape at point.
   * 
   * @param p
   *          the p
   * @return the shape at point
   */
  public ButtonShape getShapeAtPoint( Point p )
  {
    ButtonMap buttonMap = deviceUpgrade.getDeviceType().getButtonMap();
    ButtonShape closestMatch = null;
    for ( ButtonShape buttonShape : map.getShapes() )
    {
      Shape s = buttonShape.getShape();
      if ( s != null )
      {
        if ( transform != null )
        {
          s = transform.createTransformedShape( s );
        }
        if ( s.contains( p ) )
        {
          if ( closestMatch == null )
            closestMatch = buttonShape;
          Button b = getButtonForShape( buttonShape );
          if ( buttonMap.isPresent( b ) )
            return buttonShape;
        }
      }
    }
    return closestMatch;
  }

  /**
   * Gets the button for shape.
   * 
   * @param buttonShape
   *          the button shape
   * @return the button for shape
   */
  public Button getButtonForShape( ButtonShape buttonShape )
  {
    if ( buttonShape == null )
      return null;

    Remote remote = deviceUpgrade.getRemote();
    Button b = buttonShape.getButton();
    if ( remote.isSSD() )
    {
      if ( remote.isSoftButton( b ) )
      {
        int keyCode = b.getKeyCode();
        if ( keyCode < 0x37 )
        {
          keyCode += 6 * ( page - 1 );
          b = remote.getButton( keyCode );
        }
      }
    }
    ButtonMap buttonMap = deviceUpgrade.getDeviceType().getButtonMap();

    if ( !b.getIsNormal() && !normalMode.isSelected() )
      return null;

    if ( normalMode.isSelected() )
    {
      if ( b.allowsKeyMove() || buttonMap.isPresent( b ) )
        return b;
    }
    else if ( shiftMode.isSelected() )
    {
      if ( b.getIsNormal() )
      {
        if ( !b.allowsShiftedKeyMove() )
          return null;
        if ( ( b.getShiftedButton() != null ) )
          b = b.getShiftedButton();
        else
          return b;
      }
      if ( b.getIsShifted() )
      {
        if ( b.allowsKeyMove() || buttonMap.isPresent( b ) )
          return b.getBaseButton();
        else
          return null;
      }
      else
        return null;
    }
    else if ( xShiftMode.isSelected() )
    {
      if ( b.getIsNormal() )
      {
        if ( !b.allowsXShiftedKeyMove() )
          return null;
        if ( b.getXShiftedButton() != null )
          b = b.getXShiftedButton();
        else
          return b;
      }
      if ( b.getIsXShifted() )
      {
        if ( b.allowsKeyMove() || buttonMap.isPresent( b ) )
          // return b.getBaseButton();
          return b;
      }
      else if ( b.allowsXShiftedKeyMove() )
        return b;
    }
    return null;
  }

  /**
   * Sets the function.
   * 
   * @param shape
   *          the shape
   * @param f
   *          the f
   */
  private void setFunction( ButtonShape shape, GeneralFunction gf )
  {
    Button b = getButtonForShape( shape );
    if ( b != null )
    {
//      Macro macro = null;
//      GeneralFunction gf = null;
//      if ( remote.isSSD() )
//      {
//        LearnedSignal ls = deviceUpgrade.getLearnedMap().get( ( int )button.getKeyCode() );
//        if ( ls != null )
//        {
//          gf = ls;
//        }
//        macro = deviceUpgrade.getMacroMap().get( ( int )button.getKeyCode() );
//        if ( gf == null && macro != null )
//        {
//          gf = macro.isSystemMacro() ? macro.getItems().get( 0 ).fn : macro;
//          if ( !macro.isSystemMacro() )
//          {
//            macro = null;
//          }
//        }
//      }
//      if ( gf == null )
//      {
//        gf = deviceUpgrade.getFunction( button, Button.NORMAL_STATE );
//      }
      GeneralFunction current = getFunction( b );
      ButtonTableModel.setFunction( deviceUpgrade, b, current, gf, this );
      if ( current instanceof LearnedSignal && gf == null )
      {
        gf = getFunction( b );
        ButtonTableModel.setFunction( deviceUpgrade, b, null, gf, this );
      }
//      macro = deviceUpgrade.getMacroMap().get( ( int )button.getKeyCode() );
//      if ( gf == null && macro != null )
//      {
//        gf = macro.isSystemMacro() ? macro.getItems().get( 0 ).fn : macro;
//        if ( !macro.isSystemMacro() )
//        {
//          macro = null;
//        }
//      }
      setButtonText( shape, b );
      deviceUpgrade.checkSize();
    }
  }

  // From interface ActionListener
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed( ActionEvent e )
  {
    Object source = e.getSource();
    Remote remote = deviceUpgrade.getRemote();
    if ( source == scrollLeft )
    {
      map = maps[ --screenIndex ];
      image = new ImageIcon( map.getImageFile().getAbsolutePath() );

      enableScrollButtons();
      doRepaint();
    }
    else if ( source == scrollRight )
    {
      map = maps[ ++screenIndex ];
      image = new ImageIcon( map.getImageFile().getAbsolutePath() );
      enableScrollButtons();
      doRepaint();
    }
    else if ( source == autoAssign )
    {
      deviceUpgrade.autoAssignFunctions();
      doRepaint();
    }
    else if ( ( source == normalMode ) || ( source == shiftMode ) || ( source == xShiftMode ) )
    {
      Button b = getButtonForShape( currentShape );
      if ( b == null )
        currentShape = null;
      setButtonText( currentShape, b );
      doRepaint();
    }
    else if ( source == pageUp )
    {
      page++;
      pageUp.setSelected( false );
      pageUp.setEnabled( page < 5 );
      pageDown.setEnabled( true );
      pageNumber.setText( Integer.toString( page ) );
      Button b = getButtonForShape( currentShape );
      if ( remote.isSoftButton( b ) )
      {
        currentShape = null;
        setButtonText( null, b );
      }
      doRepaint();
    }
    else if ( source == pageDown )
    {
      page--;
      pageDown.setSelected( false );
      pageUp.setEnabled( true );
      pageDown.setEnabled( page > 1 );
      pageNumber.setText( Integer.toString( page ) );
      Button b = getButtonForShape( currentShape );
      if ( remote.isSoftButton( b ) )
      {
        currentShape = null;
        setButtonText( null, b );
      }
      doRepaint();
    }
    else if ( source == selector.deviceBox || source instanceof JRadioButton )
    {
      setFunctions();
      functionPanel.revalidate();
    }
    else
    {
      setFunction( currentShape, ( ( FunctionItem )source ).getFunction() );
      doRepaint();
    }
  }

  /**
   * The listener interface for receiving doubleClick events. The class that is interested in processing a doubleClick
   * event implements this interface, and the object created with that class is registered with a component using the
   * component's <code>addDoubleClickListener<code> method. When
   * the doubleClick event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see DoubleClickEvent
   */
  class DoubleClickListener extends MouseAdapter
  {

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked( MouseEvent e )
    {
      if ( ( currentShape != null ) && ( e.getClickCount() >= 2 ) )
      {
        Button button = getButtonForShape( currentShape );
        if ( button == null )
          return;

        Function f = ( Function )( ( FunctionLabel )e.getSource() ).getFunction();
        setFunction( currentShape, f );
      }
      doRepaint();
    }
  }

  /**
   * The Class LayoutDropTarget.
   */
  class LayoutDropTarget extends DropTarget
  {

    /**
     * Instantiates a new layout drop target.
     */
    public LayoutDropTarget()
    {
      setDefaultActions( DnDConstants.ACTION_COPY );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTarget#dragOver(java.awt.dnd.DropTargetDragEvent)
     */
    public void dragOver( DropTargetDragEvent dtde )
    {
      Point p = dtde.getLocation();
      currentShape = getShapeAtPoint( p );
      Button button = getButtonForShape( currentShape );
      if ( button != null )
        dtde.acceptDrag( dtde.getDropAction() );
      else
      {
        currentShape = null;
        dtde.rejectDrag();
      }
      doRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.dnd.DropTarget#drop(java.awt.dnd.DropTargetDropEvent)
     */
    public void drop( DropTargetDropEvent dtde )
    {
      Point p = dtde.getLocation();
      currentShape = getShapeAtPoint( p );
      Button button = getButtonForShape( currentShape );
      if ( button != null )
      {
        int action = dtde.getDropAction();
        dtde.acceptDrop( action );
        Transferable tf = dtde.getTransferable();
        try
        {
          GeneralFunction f = ( GeneralFunction )tf.getTransferData( LocalObjectTransferable.getFlavor() );
          setFunction( currentShape, f );
        }
        catch ( Exception e )
        {
          e.printStackTrace( System.err );
        }
      }
      else
        dtde.rejectDrop();
      doRepaint();
    }
  }

  /**
   * The Class ImagePanel.
   */
  private class ImagePanel extends JPanel implements Scrollable
  {

    /**
     * Instantiates a new image panel.
     */
    public ImagePanel()
    {
      super();
      // setHorizontalAlignment( SwingConstants.LEFT );
      // setVerticalAlignment( SwingConstants.TOP );
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paint( Graphics g )
    {
      super.paint( g );
      Graphics2D g2 = ( Graphics2D )g.create();
      g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
      Remote r = deviceUpgrade.getRemote();
      if ( transform != null )
      {
        g2.transform( transform );
      }
      if ( image != null )
        g2.drawImage( image.getImage(), null, null );

      g2.setPaint( Color.darkGray );
      for ( ButtonShape shape : r.getPhantomShapes() )
        g2.fill( shape.getShape() );

      if ( currentShape != null )
      {
        g2.setPaint( Color.white );
        g2.setStroke( new BasicStroke( 6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2.draw( currentShape.getShape() );
      }

      g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );

      DeviceType devType = deviceUpgrade.getDeviceType();
      ButtonMap buttonMap = devType.getButtonMap();

      for ( ButtonShape buttonShape : map.getShapes() )
      {
        Button b = getButtonForShape( buttonShape );
        if ( b == null )
          continue;

        Shape s = buttonShape.getShape();

        GeneralFunction f = getFunction( b );
        if ( f != null )
        {
          g2.setPaint( Color.yellow );
          g2.fill( s );
        }

        if ( shiftMode.isSelected() )
          b = b.getShiftedButton();
        else if ( xShiftMode.isSelected() )
          b = b.getXShiftedButton();

        if ( buttonMap.isPresent( b ) )
        {
          if ( ( currentShape != null ) && ( s == currentShape.getShape() ) )
          {
            g2.setPaint( Color.white );
            g2.setStroke( new BasicStroke( 6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
            g2.draw( currentShape.getShape() );
            g2.setStroke( new BasicStroke( 2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
          }
          g2.setPaint( Color.orange );
          g2.draw( s );
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
     */
    public String getToolTipText( MouseEvent e )
    {
      Remote remote = deviceUpgrade.getRemote();
      ButtonShape buttonShape = getShapeAtPoint( e.getPoint() );
      if ( buttonShape == null )
        return null;

      Button b = getButtonForShape( buttonShape );
      if ( b == null )
        return null;

      String name = buttonShape.getName();
      if ( name == null )
      {
        if ( normalMode.isSelected() )
          name = b.getName();
        else if ( shiftMode.isSelected() )
          name = b.getShiftedName();
        else if ( xShiftMode.isSelected() )
          name = b.getXShiftedName();
      }

      GeneralFunction f = getFunction( b );
      String text = "<html>" + name;
      if ( f != null )
      {
        if ( remote.usesEZRC() )
        {
          Macro macro = null;
          if ( f instanceof Function )
          {
            macro = deviceUpgrade.getMacroMap().get( ( int )b.getKeyCode() );
            if ( macro != null && !macro.isSystemMacro() )
            {
              macro = null;
            }
          }
          DeviceButton db = ( macro != null ) ? macro.getItems().get( 0 ).db
              : f.getUpgrade( remote ).getButtonRestriction();
          text = "<html>" + "Button: " + name;
          text += "<br>" + "Device: " + db.getName();
          text += "<br>" + "Function: " + f.getDisplayName();
          if ( macro != null )
          {
            text += "<br>" + "Alias: " + macro.getName();
          }
        }
        else
        {
          text = "<html>" + name + " = " + f.getDisplayName();
        }
      }
      return text + "</html>";
    }

    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
      return getPreferredSize();
    }

    @Override
    public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction )
    {
      if ( orientation == SwingConstants.VERTICAL )
      {
        return visibleRect.height;
      }

      if ( orientation == SwingConstants.HORIZONTAL )
      {
        return visibleRect.width;
      }
      return 0;
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
      return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
      return true;
    }

    @Override
    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction )
    {
      return 1;
    }
  }

  /** The maps. */
  private ImageMap[] maps = null;

  /** The map. */
  private ImageMap map = null;

  /** The screen index. */
  private int screenIndex = 0;

  /** The current shape. */
  private ButtonShape currentShape = null;

  /** The scroll panel. */
  private Box scrollPanel = null;

  /** The scroll left. */
  private JButton scrollLeft = null;

  /** The scroll right. */
  private JButton scrollRight = null;

  /** The image panel. */
  private ImagePanel imagePanel = null;
  
  private JPanel pagePanel = null;
  private JRadioButton pageUp = null;
  private JRadioButton pageDown = null;
  private JTextField pageNumber = null;
  private int page = 1;

  /** The normal mode. */
  private JRadioButton normalMode = null;

  /** The shift mode. */
  private JRadioButton shiftMode = null;

  /** The x shift mode. */
  private JRadioButton xShiftMode = null;

  /** The button name. */
  private JTextField buttonName = null;

  /** The function. */
  private JTextField function = null;
  
  private JTextField device = null;
  private JTextField alias = null;
  

  /** The auto assign. */
  private JButton autoAssign = null;

  /** The delete action. */
  private AbstractAction deleteAction = null;

  /** The popup. */
  private JPopupMenu popup = null;

  /** The function panel. */
  private JPanel functionPanel = null;

  /** The scroll pane. */
  private JScrollPane scrollPane = null;

  private JSplitPane splitPane = null;

  /** The double click listener. */
  private DoubleClickListener doubleClickListener = new DoubleClickListener();

  private ImageIcon image = null;

  private AffineTransform transform = null;

  private JPanel remotePanel = null;
  
  private SelectionPanel selector = null;

  @Override
  public void run()
  {
    doRepaint();
  }
}
