package com.gaocan.publictransportation;

import java.io.Serializable;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class InvalidHMTimeFormatException extends Exception implements Serializable
{
    private static final long serialVersionUID = -8528856886890767091L;

/**
    * Constructor declaration
    * 
    * 
    * @param s
    * 
    * @see
    */
   InvalidHMTimeFormatException(String s)
   {
      super(s);
   }

}



