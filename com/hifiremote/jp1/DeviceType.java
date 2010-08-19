package com.hifiremote.jp1;

// TODO: Auto-generated Javadoc
/**
 * The Class DeviceType.
 */
public class DeviceType
{

  /**
   * Instantiates a new device type.
   * 
   * @param aName
   *          the a name
   * @param aMap
   *          the a map
   * @param aType
   *          the a type
   */
  DeviceType( String aName, int aMap, int aType )
  {
    this.name = aName;
    this.map = aMap;
    this.type = aType;
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
   * Gets the number.
   * 
   * @return the number
   */
  public int getNumber()
  {
    // A device type from the RDF cannot have a value > 15
    // but a special one added as a signal may do so - see
    // DeviceButtonTableModel set( remoteConfiguration ).
    return type & 0xFF;
  }
  
  public int getGroup()
  {
    return type >> 8;
  }

  /**
   * Gets the map.
   * 
   * @return the map
   */
  public int getMap()
  {
    return map;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public int getType()
  {
    return type;
  }

  /**
   * Sets the button map.
   * 
   * @param buttonMap
   *          the button map
   * 
   * @return the device type
   */
  public DeviceType setButtonMap( ButtonMap buttonMap )
  {
    this.buttonMap = buttonMap;
    return this;
  }

  /**
   * Gets the button map.
   * 
   * @return the button map
   */
  public ButtonMap getButtonMap()
  {
    return buttonMap;
  }

  /**
   * Checks if is mapped.
   * 
   * @param b
   *          the b
   * 
   * @return true, if is mapped
   */
  public boolean isMapped( Button b )
  {
    return buttonMap.isPresent( b );
  }

  /**
   * Sets the abbreviation.
   * 
   * @param text
   *          the new abbreviation
   */
  public void setAbbreviation( String text )
  {
    abbreviation = text;
  }

  /**
   * Gets the abbreviation.
   * 
   * @return the abbreviation
   */
  public String getAbbreviation()
  {
    if ( abbreviation != null )
      return abbreviation;
    return name;
  }

  /**
   * Gets the image maps.
   * 
   * @return the image maps
   */
  public ImageMap[][] getImageMaps()
  {
    return imageMaps;
  }

  /**
   * Sets the image maps.
   * 
   * @param maps
   *          the new image maps
   */
  public void setImageMaps( ImageMap[][] maps )
  {
    imageMaps = maps;
  }

  public boolean equals( Object anObject )
  {
    if ( this == anObject )
      return true;

    if ( !( anObject instanceof DeviceType ) )
      return false;

    DeviceType aDeviceType = ( DeviceType ) anObject;

    return name.equals( aDeviceType.getName() );
  }

  /** The name. */
  private String name;

  /** The abbreviation. */
  public String abbreviation;

  /** The map. */
  private int map;

  /** The type. */
  private int type;

  /** The button map. */
  private ButtonMap buttonMap = null;

  /** The image maps. */
  private ImageMap[][] imageMaps = new ImageMap[ 0 ][];
}
