package org.solrmarc.marc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

/**
 * Read a binary marc file
 * @author Robert Haschart
 * @version $Id$
 *
 */
public class RawRecordReader
{

	 // Initialize logging category
    static Logger logger = Logger.getLogger(MarcFilteredReader.class.getName());
	
    private static int parseRecordLength(byte[] leaderData) throws IOException {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(
                leaderData));
        int length = -1;
        char[] tmp = new char[5];
        isr.read(tmp);
        try {
            length = Integer.parseInt(new String(tmp));
        } catch (NumberFormatException e) {
            throw new IOException("unable to parse record length");
        }
        return(length);
    }
    
	/**
	 * 
	 * @param args
	 */
    public static void main(String[] args)
    {
    //    try {

            byte[] byteArray = new byte[24];
            try
            {
                DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(args[0]))));
                while (true)
                {
                    input.readFully(byteArray);
                    int recordLength = parseRecordLength(byteArray);
                    byte[] recordBuf = new byte[recordLength - 24];
                    input.readFully(recordBuf);
                    String recordStr = null;
                    try
                    {
                        recordStr = new String(recordBuf, "ISO-8859-1");
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        // e.printStackTrace();
                    	logger.error(e.getMessage());
                    }
                    if (recordStr.contains(args[1]))
                    { 
                        System.out.write(byteArray);
                        System.out.write(recordBuf);
                    }
                }
            }
            catch (EOFException e)
            {
            }
            catch (IOException e)
            {
                //  e.printStackTrace();
            	logger.error(e.getMessage());
            }

    //    }
    }

}
