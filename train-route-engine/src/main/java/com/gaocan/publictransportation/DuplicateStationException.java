package com.gaocan.publictransportation;

/**
 * Thrown in cases like adding a pre-existing station to a schedule again
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class DuplicateStationException extends RuntimeException {
   public DuplicateStationException(String name)   {
      super(name);
   }

}



