package com.hifiremote.jp1;

public class DeviceType
{
  DeviceType( String aName, int aMap, int aType )
  {
    this.name = aName;
    this.map = aMap;
    this.type = aType;
  }

  public String toString(){ return name; }
  public String getName(){ return name; }
  public int getNumber(){ return type & 0x0F; }
  public int getMap(){ return map; }
  public int getType(){ return type; }
  public DeviceType setButtonMap( ButtonMap buttonMap )
  {
    this.buttonMap = buttonMap;
    return this;
  }
  public ButtonMap getButtonMap()
  {
    return buttonMap;
  }

  public boolean isMapped( Button b )
  {
    return buttonMap.isPresent( b );
  }

  public void setAbbreviation( String text )
  {
    abbreviation = text;
  }

  public String getAbbreviation()
  {
    if ( abbreviation != null )
      return abbreviation;
    return name;
  }

  public ImageMap[][] getImageMaps(){ return imageMaps; }

  public void setImageMaps( ImageMap[][] maps )
  {
    imageMaps = maps;
  }

  private String name;
  public String abbreviation;
  private int map;
  private int type;
  private ButtonMap buttonMap = null;
  private ImageMap[][] imageMaps = new ImageMap[ 0 ][];
}
