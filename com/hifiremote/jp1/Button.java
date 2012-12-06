package com.hifiremote.jp1;

import javax.swing.JCheckBox;

// TODO: Auto-generated Javadoc
/**
 * The Class Button.
 */
public class Button
{

  /**
   * Instantiates a new button.
   * 
   * @param standardName
   *          the standard name
   * @param name
   *          the name
   * @param code
   *          the code
   * @param r
   *          the r
   */
  public Button( String standardName, String name, short code, Remote r )
  {
    this.standardName = standardName.toLowerCase();
    this.name = name;
    keyCode = code;
    remote = r;
    int maskedCode = code & 0xC0;
    if ( maskedCode == r.getShiftMask() && r.getShiftMask() != 0 )
      setIsShifted( true );
    else if ( maskedCode == r.getXShiftMask() && r.getXShiftMask() != 0 )
      setIsXShifted( true );
    else if ( maskedCode != 0 )
      restrictions |= ( SHIFT | XSHIFT );
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return name;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name
   *          the new name
   */
  public void setName( String name )
  {
    this.name = name;
  }

  /**
   * Gets the shifted name.
   * 
   * @return the shifted name
   */
  public String getShiftedName()
  {
    if ( isShifted )
      return name;
    else if ( shiftedButton != null )
      return shiftedButton.getName();
    else
      return remote.getShiftLabel() + '-' + name;
  }

  /**
   * Gets the x shifted name.
   * 
   * @return the x shifted name
   */
  public String getXShiftedName()
  {
    if ( isXShifted )
      return name;
    else if ( xShiftedButton != null )
      return xShiftedButton.getName();
    else
      return remote.getXShiftLabel() + '-' + name;
  }

  /**
   * Gets the name.
   * 
   * @param state
   *          the state
   * @return the name
   */
  public String getName( int state )
  {
    if ( state == SHIFTED_STATE )
      return getShiftedName();
    else if ( state == XSHIFTED_STATE )
      return getXShiftedName();
    else
      return getName();
  }

  /**
   * Gets the standard name.
   * 
   * @return the standard name
   */
  public String getStandardName()
  {
    return standardName;
  }

  /**
   * Sets the standard name.
   * 
   * @param name
   *          the new standard name
   */
  public void setStandardName( String name )
  {
    standardName = name.toLowerCase();
  }

  /**
   * Gets the key code.
   * 
   * @return the key code
   */
  public short getKeyCode()
  {
    return keyCode;
  }

  /**
   * Gets the shifted key code.
   * 
   * @return the shifted key code
   */
  public short getShiftedKeyCode()
  {
    return ( short )( keyCode | remote.getShiftMask() );
  }

  /**
   * Gets the x shifted key code.
   * 
   * @return the x shifted key code
   */
  public short getXShiftedKeyCode()
  {
    return ( short )( keyCode | remote.getXShiftMask() );
  }

  /**
   * Gets the key code.
   * 
   * @param state
   *          the state
   * @return the key code
   */
  public short getKeyCode( int state )
  {
    if ( state == SHIFTED_STATE )
      return getShiftedKeyCode();
    else if ( state == XSHIFTED_STATE )
      return getXShiftedKeyCode();
    return getKeyCode();
  }

  /**
   * Gets the multi macro address.
   * 
   * @return the multi macro address
   */
  public MultiMacro getMultiMacro()
  {
    return multiMacro;
  }

  /**
   * Sets the multi macro address.
   * 
   * @param addr
   *          the new multi macro address
   */
  public void setMultiMacro( MultiMacro multiMacro )
  {
    this.multiMacro = multiMacro;
  }

  /**
   * Sets the base button.
   * 
   * @param button
   *          the new base button
   */
  public void setBaseButton( Button button )
  {
    baseButton = button;
    if ( isShifted && !allowsKeyMove() )
      baseButton.addRestrictions( SHIFT_MOVE_BIND );
    if ( isXShifted && !allowsKeyMove() )
      baseButton.addRestrictions( XSHIFT_MOVE_BIND );
  }

  /**
   * Gets the base button.
   * 
   * @return the base button
   */
  public Button getBaseButton()
  {
    return baseButton;
  }

  /**
   * Gets the shifted button.
   * 
   * @return the shifted button
   */
  public Button getShiftedButton()
  {
    return shiftedButton;
  }

  /**
   * Sets the shifted button.
   * 
   * @param button
   *          the new shifted button
   */
  public void setShiftedButton( Button button )
  {
    shiftedButton = button;
  }

  /**
   * Sets the x shifted button.
   * 
   * @param button
   *          the new x shifted button
   */
  public void setXShiftedButton( Button button )
  {
    xShiftedButton = button;
  }

  /**
   * Gets the x shifted button.
   * 
   * @return the x shifted button
   */
  public Button getXShiftedButton()
  {
    return xShiftedButton;
  }

  /**
   * Gets the checks if is normal.
   * 
   * @return the checks if is normal
   */
  public boolean getIsNormal()
  {
    return ( !isShifted && !isXShifted );
  }

  /**
   * Sets the checks if is shifted.
   * 
   * @param flag
   *          the new checks if is shifted
   */
  public void setIsShifted( boolean flag )
  {
    isShifted = flag;
    if ( isShifted )
      restrictions |= ( SHIFT | XSHIFT );
  }

  /**
   * Gets the checks if is shifted.
   * 
   * @return the checks if is shifted
   */
  public boolean getIsShifted()
  {
    return isShifted;
  }

  /**
   * Sets the checks if is x shifted.
   * 
   * @param flag
   *          the new checks if is x shifted
   */
  public void setIsXShifted( boolean flag )
  {
    isXShifted = flag;
    if ( isXShifted )
      restrictions |= ( SHIFT | XSHIFT );
  }

  /**
   * Gets the checks if is x shifted.
   * 
   * @return the checks if is x shifted
   */
  public boolean getIsXShifted()
  {
    return isXShifted;
  }

  /**
   * Gets the state.
   * 
   * @return the state
   */
  public int getState()
  {
    if ( getIsShifted() )
      return SHIFTED_STATE;
    else if ( getIsXShifted() )
      return XSHIFTED_STATE;
    else
      return NORMAL_STATE;
  }

  /**
   * Gets the restrictions.
   * 
   * @return the restrictions
   */
  public int getRestrictions()
  {
    return restrictions;
  }

  /**
   * Sets the restrictions.
   * 
   * @param restrictions
   *          the new restrictions
   */
  public void setRestrictions( int restrictions )
  {
    this.restrictions = restrictions;
  }

  /**
   * Adds the restrictions.
   * 
   * @param restrictions
   *          the restrictions
   */
  public void addRestrictions( int restrictions )
  {
    this.restrictions |= restrictions;
  }

  /**
   * Allows key move.
   * 
   * @return true, if successful
   */
  public boolean allowsKeyMove()
  {
    return ( ( restrictions & MOVE_BIND ) == 0 );
  }

  /**
   * Allows shifted key move.
   * 
   * @return true, if successful
   */
  public boolean allowsShiftedKeyMove()
  {
    if ( isShifted || isXShifted || !remote.getShiftEnabled() )
      return false;
    return ( ( restrictions & SHIFT_MOVE_BIND ) == 0 );
  }

  /**
   * Allows x shifted key move.
   * 
   * @return true, if successful
   */
  public boolean allowsXShiftedKeyMove()
  {
    if ( isShifted || isXShifted || !remote.getXShiftEnabled() )
      return false;
    return ( ( restrictions & XSHIFT_MOVE_BIND ) == 0 );
  }

  /**
   * Allows key move.
   * 
   * @param state
   *          the state
   * @return true, if successful
   */
  public boolean allowsKeyMove( int state )
  {
    if ( state == SHIFTED_STATE )
      return allowsShiftedKeyMove();
    else if ( state == XSHIFTED_STATE )
      return allowsXShiftedKeyMove();
    else
      return allowsKeyMove();
  }

  /**
   * Allows macro.
   * 
   * @return true, if successful
   */
  public boolean allowsMacro()
  {
    return ( ( restrictions & MACRO_BIND ) == 0 );
  }

  /**
   * Allows shifted macro.
   * 
   * @return true, if successful
   */
  public boolean allowsShiftedMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & SHIFT_MACRO_BIND ) == 0 );
  }

  /**
   * Allows x shifted macro.
   * 
   * @return true, if successful
   */
  public boolean allowsXShiftedMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & XSHIFT_MACRO_BIND ) == 0 );
  }
  
  public boolean allowsMacro( int state )
  {
    if ( state == SHIFTED_STATE )
      return allowsShiftedMacro();
    else if ( state == XSHIFTED_STATE )
      return allowsXShiftedMacro();
    else
      return allowsMacro();
  }

  /**
   * Can assign to macro.
   * 
   * @return true, if successful
   */
  public boolean canAssignToMacro()
  {
    return ( ( restrictions & MACRO_DATA ) == 0 );
  }

  /**
   * Can assign shifted to macro.
   * 
   * @return true, if successful
   */
  public boolean canAssignShiftedToMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & SHIFT_MACRO_DATA ) == 0 );
  }

  public boolean canAssignXShiftedToMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & XSHIFT_MACRO_DATA ) == 0 );
  }

  public boolean canAssignToFav()
  {
    return ( ( restrictions & FAV_DATA ) == 0 );
  }

  public boolean canAssignShiftedToFav()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & SHIFT_FAV_DATA ) == 0 );
  }

  public boolean canAssignXShiftedToFav()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & XSHIFT_FAV_DATA ) == 0 );
  }
  
  public boolean canAssignToTimedMacro()
  {
    return ( ( restrictions & TMACRO_DATA ) == 0 );
  }

  public boolean canAssignShiftedToTimedMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & SHIFT_TMACRO_DATA ) == 0 );
  }

  public boolean canAssignXShiftedToTimedMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & XSHIFT_TMACRO_DATA ) == 0 );
  }
  
  public boolean canAssignToPowerMacro()
  {
    return ( ( restrictions & PWRMACRO_DATA ) == 0 );
  }

  public boolean canAssignShiftedToPowerMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & SHIFT_PWRMACRO_DATA ) == 0 );
  }

  public boolean canAssignXShiftedToPowerMacro()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & XSHIFT_PWRMACRO_DATA ) == 0 );
  }
  
  /**
   * Allows learned signal.
   * 
   * @return true, if successful
   */
  public boolean allowsLearnedSignal()
  {
    return ( ( restrictions & LEARN_BIND ) == 0 );
  }

  /**
   * Allows shifted learned signal.
   * 
   * @return true, if successful
   */
  public boolean allowsShiftedLearnedSignal()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & SHIFT_LEARN_BIND ) == 0 );
  }

  /**
   * Allows x shifted learned signal.
   * 
   * @return true, if successful
   */
  public boolean allowsXShiftedLearnedSignal()
  {
    if ( isShifted || isXShifted )
      return false;
    return ( ( restrictions & XSHIFT_LEARN_BIND ) == 0 );
  }
  
  public boolean allowsLearnedSignal( int state )
  {
    if ( state == SHIFTED_STATE )
      return allowsShiftedLearnedSignal();
    else if ( state == XSHIFTED_STATE )
      return allowsXShiftedLearnedSignal();
    else
      return allowsLearnedSignal();
  }
  
  public boolean allowed( int type, int state )
  {
    if ( type == MOVE_BIND )
      return allowsKeyMove( state );
    else if ( type == MACRO_BIND )
      return allowsMacro( state );
    else if ( type == LEARN_BIND )
      return allowsLearnedSignal( state );
    else return false;
  }
  
  public void setShiftBoxes( int type, JCheckBox shiftBox, JCheckBox xShiftBox )
  {
    int stateCount = 0;
    if ( allowed( type, NORMAL_STATE ) ) stateCount++;
    if ( remote.getShiftEnabled() && allowed( type, SHIFTED_STATE ) ) stateCount++;
    if ( remote.getXShiftEnabled() && allowed( type, XSHIFTED_STATE ) ) stateCount++;
    if ( stateCount == 1 )
    {
      // If only one allowed state, don't allow shift states to be changed.
      if ( remote.getShiftEnabled() ) {
        shiftBox.setEnabled( false );
        shiftBox.setSelected( allowed( type, SHIFTED_STATE ) );
      }
      if ( remote.getXShiftEnabled() ) {
        xShiftBox.setEnabled( false );
        xShiftBox.setSelected( allowed( type, XSHIFTED_STATE ) );
      }
    }
    else if ( stateCount == 2 && ! allowed( type, NORMAL_STATE ) )
    {
      shiftBox.setEnabled( true );      
      xShiftBox.setEnabled( true );
      if ( ( !shiftBox.isSelected() ) && ( !xShiftBox.isSelected() ) )
      {
        shiftBox.setSelected( true );
      }
    }
    else
    {
      if ( remote.getShiftEnabled() ) {
        shiftBox.setEnabled( allowed( type, SHIFTED_STATE ) );
      }
      if ( remote.getXShiftEnabled() ) {
        xShiftBox.setEnabled( allowed( type, XSHIFTED_STATE ) );
      }
    }    
  }
  
  public boolean needsShift( int type )
  {
    // Returns true when either shifted or xshifted state is required for this type/
    return remote.getXShiftEnabled() && allowed( type, SHIFTED_STATE ) && 
      allowed( type, XSHIFTED_STATE ) && ! allowed( type, NORMAL_STATE );
  }

  /**
   * Adds the button map.
   * 
   * @param mapIndex
   *          the map index
   */
  public void addButtonMap( int mapIndex )
  {
    buttonMaps |= ( 1 << mapIndex );
  }

  /**
   * In button map.
   * 
   * @param mapIndex
   *          the map index
   * @return true, if successful
   */
  public boolean inButtonMap( int mapIndex )
  {
    int mask = ( 1 << mapIndex );
    return ( ( buttonMaps & mask ) != 0 );
  }

  /**
   * Gets the button maps.
   * 
   * @return the button maps
   */
  public int getButtonMaps()
  {
    return buttonMaps;
  }

  /**
   * Gets the key move.
   * 
   * @param f
   *          the f
   * @param mask
   *          the mask
   * @param setupCode
   *          the setup code
   * @param devType
   *          the dev type
   * @param remote
   *          the remote
   * @param keyMovesOnly
   *          the key moves only
   * @return the key move
   */
  public KeyMove getKeyMove( Function f, int mask, int setupCode, DeviceType devType, Remote remote,
      boolean keyMovesOnly )
  {
    KeyMove rc = null;
    if ( ( f != null ) && ( f.getHex() != null ) )
    {
      Hex hex = f.getHex();
      if ( f.isExternal() )
      {
        ExternalFunction ef = ( ExternalFunction )f;
        devType = remote.getDeviceTypeByAliasName( ef.getDeviceTypeAliasName() );
        setupCode = ef.getSetupCode();
      }

      if ( f.isExternal() || ( mask != 0 ) || !devType.isMapped( this ) || keyMovesOnly )
      {
        StringBuilder sb = new StringBuilder( f.getName() );
        String notes = f.getNotes();
        if ( ( notes != null ) && ( notes.length() != 0 ) )
        {
          sb.append( ": " );
          sb.append( notes );
        }
        rc = remote.createKeyMove( keyCode | mask, 0x0F, devType.getNumber(), setupCode, hex, sb.toString() );
      }
    }
    if ( rc != null && remote.getSegmentTypes() != null )
    {
      rc.setSegmentFlags( 0xFF );
    }
    return rc;
  }

  /**
   * Gets the key move.
   * 
   * @param f
   *          the f
   * @param mask
   *          the mask
   * @param deviceCode
   *          the device code
   * @param devType
   *          the dev type
   * @param remote
   *          the remote
   * @param keyMovesOnly
   *          the key moves only
   * @return the key move
   */
  public short[] getKeyMove( Function f, int mask, short[] deviceCode, DeviceType devType, Remote remote,
      boolean keyMovesOnly )
  {
    short[] rc = new short[ 0 ];
    if ( ( f != null ) && ( f.getHex() != null ) )
    {
      int len = 0;
      Hex hex = f.getHex();
      if ( f.isExternal() )
      {
        ExternalFunction ef = ( ExternalFunction )f;
        devType = remote.getDeviceTypeByAliasName( ef.getDeviceTypeAliasName() );
        short temp = ( short )( devType.getNumber() * 0x1000 );
        temp += ( short )ef.getSetupCode();
        temp -= ( short )remote.getDeviceCodeOffset();

        deviceCode = new short[ 2 ];
        deviceCode[ 0 ] = ( short )( temp >> 8 );
        deviceCode[ 1 ] = temp;
      }
      if ( remote.getAdvCodeFormat() == AdvancedCode.Format.EFC )
      {
        if ( ( hex.length() == 1 ) && ( remote.getEFCDigits() == 3 ) )
        {
          short[] data = new short[ 2 ];
          data[ 0 ] = 0;
          data[ 1 ] = ( short )( EFC.parseHex( hex, 0 ) & 0xFF ) ;
          hex = new Hex( data );
        }
        else
        {
          short[] data = new short[ 3 ];
          int value = EFC5.parseHex( hex );

          data[ 0 ] = ( short )( ( value >> 16 ) & 0xFF );
          data[ 1 ] = ( short )( (value >> 8 ) & 0xFF );
          data[ 2 ] = ( short )( value & 0xFF );

          hex = new Hex( data );
        }
      }
      else if ( ( hex.length() == 1 ) && ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG ) )
      {
        short[] newData = new short[ 2 ];
        short efc = hex.getData()[ 0 ];
        newData[ 0 ] = efc;
        hex = new Hex( newData );
        newData[ 1 ] = ( short )( EFC.parseHex( efc ) & 0xFF );
      }

      if ( f.isExternal() || ( mask != 0 ) || !devType.isMapped( this ) || keyMovesOnly )
        len = ( 4 + hex.length() );

      if ( len != 0 )
      {
        if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.LONG )
          ++len;

        rc = new short[ len ];

        int index = 0;
        rc[ index ] = ( short )( keyCode | mask );

        rc[ ++index ] = 0xF0;
        if ( remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL )
          rc[ index ] += ( short )( 2 + hex.length() );
        else
          rc[ ++index ] = ( short )( 2 + hex.length() );

        System.arraycopy( deviceCode, 0, rc, ++index, 2 );
        System.arraycopy( hex.getData(), 0, rc, index + 2, hex.length() );
      }
    }
    return rc;
  }

  /**
   * Sets the checks for shape.
   * 
   * @param flag
   *          the new checks for shape
   */
  public void setHasShape( boolean flag )
  {
    if ( shiftedButton != null )
      shiftedButton.hasShape = flag;
    if ( xShiftedButton != null )
      xShiftedButton.hasShape = flag;

    hasShape = flag;
  }

  /**
   * Gets the checks for shape.
   * 
   * @return the checks for shape
   */
  public boolean getHasShape()
  {
    return hasShape;
  }

  /** The name. */
  private String name;

  /** The standard name. */
  private String standardName;

  /** The key code. */
  private short keyCode;

  /** The remote. */
  private Remote remote;

  /** The multi macro address. */
  private MultiMacro multiMacro = null;
  /*
   * private Function function; private Function shiftedFunction; private Function xShiftedFunction;
   */
  /** The base button. */
  private Button baseButton = null;

  /** The shifted button. */
  private Button shiftedButton = null;

  /** The x shifted button. */
  private Button xShiftedButton = null;

  /** The is shifted. */
  private boolean isShifted = false;

  /** The is x shifted. */
  private boolean isXShifted = false;

  /** The restrictions. */
  private int restrictions = 0;

  /** The button maps. */
  private int buttonMaps = 0;

  /** The has shape. */
  private boolean hasShape = false;

  /** The NORMA l_ state. */
  public static int NORMAL_STATE = 0;

  /** The SHIFTE d_ state. */
  public static int SHIFTED_STATE = 1;

  /** The XSHIFTE d_ state. */
  public static int XSHIFTED_STATE = 2;

  /** The MOV e_ bind. */
  public static int MOVE_BIND = 0x01;

  /** The SHIF t_ mov e_ bind. */
  public static int SHIFT_MOVE_BIND = 0x02;

  /** The XSHIF t_ mov e_ bind. */
  public static int XSHIFT_MOVE_BIND = 0x04;

  /** The AL l_ mov e_ bind. */
  public static int ALL_MOVE_BIND = MOVE_BIND | SHIFT_MOVE_BIND | XSHIFT_MOVE_BIND;

  /** The MACR o_ bind. */
  public static int MACRO_BIND = 0x08;

  /** The SHIF t_ macr o_ bind. */
  public static int SHIFT_MACRO_BIND = 0x10;

  /** The XSHIF t_ macr o_ bind. */
  public static int XSHIFT_MACRO_BIND = 0x20;

  /** The AL l_ macr o_ bind. */
  public static int ALL_MACRO_BIND = MACRO_BIND | SHIFT_MACRO_BIND | XSHIFT_MACRO_BIND;

  /** The LEAR n_ bind. */
  public static int LEARN_BIND = 0x40;

  /** The SHIF t_ lear n_ bind. */
  public static int SHIFT_LEARN_BIND = 0x80;

  /** The XSHIF t_ lear n_ bind. */
  public static int XSHIFT_LEARN_BIND = 0x100;

  /** The AL l_ lear n_ bind. */
  public static int ALL_LEARN_BIND = LEARN_BIND | SHIFT_LEARN_BIND | XSHIFT_LEARN_BIND;

  /** The MACR o_ data. */
  public static int MACRO_DATA = 0x200;

  /** The SHIF t_ macr o_ data. */
  public static int SHIFT_MACRO_DATA = 0x400;

  /** The XSHIF t_ macr o_ data. */
  public static int XSHIFT_MACRO_DATA = 0x800;

  /** The AL l_ macr o_ data. */
  public static int ALL_MACRO_DATA = MACRO_DATA | SHIFT_MACRO_DATA | XSHIFT_MACRO_DATA;

  /** The TMACR o_ data. */
  public static int TMACRO_DATA = 0x1000;

  /** The SHIF t_ tmacr o_ data. */
  public static int SHIFT_TMACRO_DATA = 0x2000;

  /** The XSHIF t_ tmacr o_ data. */
  public static int XSHIFT_TMACRO_DATA = 0x4000;

  /** The AL l_ tmacr o_ data. */
  public static int ALL_TMACRO_DATA = TMACRO_DATA | SHIFT_TMACRO_DATA | XSHIFT_TMACRO_DATA;

  /** The FA v_ data. */
  public static int FAV_DATA = 0x8000;

  /** The SHIF t_ fa v_ data. */
  public static int SHIFT_FAV_DATA = 0x10000;

  /** The XSHIF t_ fa v_ data. */
  public static int XSHIFT_FAV_DATA = 0x20000;

  /** The AL l_ fa v_ data. */
  public static int ALL_FAV_DATA = FAV_DATA | SHIFT_FAV_DATA | XSHIFT_FAV_DATA;
  
  public static int PWRMACRO_DATA = 0x40000;

  public static int SHIFT_PWRMACRO_DATA = 0x80000;

  public static int XSHIFT_PWRMACRO_DATA = 0x100000;

  public static int ALL_PWRMACRO_DATA = PWRMACRO_DATA | SHIFT_PWRMACRO_DATA | XSHIFT_PWRMACRO_DATA;

  /** The BIND. */
  public static int BIND = MOVE_BIND | MACRO_BIND | LEARN_BIND;

  /** The SHIF t_ bind. */
  public static int SHIFT_BIND = SHIFT_MOVE_BIND | SHIFT_MACRO_BIND | SHIFT_LEARN_BIND;

  /** The XSHIF t_ bind. */
  public static int XSHIFT_BIND = XSHIFT_MOVE_BIND | XSHIFT_MACRO_BIND | XSHIFT_LEARN_BIND;

  /** The AL l_ bind. */
  public static int ALL_BIND = ALL_MOVE_BIND | ALL_MACRO_BIND | ALL_LEARN_BIND;

  /** The DATA. */
  public static int DATA = MACRO_DATA | TMACRO_DATA | FAV_DATA | PWRMACRO_DATA;

  /** The SHIF t_ data. */
  public static int SHIFT_DATA = SHIFT_MACRO_DATA | SHIFT_TMACRO_DATA | SHIFT_FAV_DATA | SHIFT_PWRMACRO_DATA;

  /** The XSHIF t_ data. */
  public static int XSHIFT_DATA = XSHIFT_MACRO_DATA | XSHIFT_TMACRO_DATA | XSHIFT_FAV_DATA | XSHIFT_PWRMACRO_DATA;

  /** The AL l_ data. */
  public static int ALL_DATA = ALL_MACRO_DATA | ALL_TMACRO_DATA | ALL_FAV_DATA | ALL_PWRMACRO_DATA;

  /** The SHIFT. */
  public static int SHIFT = SHIFT_BIND | SHIFT_DATA;

  /** The XSHIFT. */
  public static int XSHIFT = XSHIFT_BIND | XSHIFT_DATA;

  /** The ALL. */
  public static int ALL = ALL_DATA | ALL_BIND;
}
