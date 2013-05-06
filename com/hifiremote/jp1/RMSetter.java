package com.hifiremote.jp1;

public interface RMSetter< T >
{
  public void setValue( T value );
  
  public T getValue();
  
  public void setRemoteConfiguration( RemoteConfiguration config );
}
