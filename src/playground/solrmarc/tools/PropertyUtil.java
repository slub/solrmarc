package playground.solrmarc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertyUtil
{
    protected static Logger logger = Logger.getLogger(PropertyUtil.class.getName());

    /**
     * Check first for a particular property in the System Properties, so that
     * the -Dprop="value" command line arg mechanism can be used to override
     * values defined in the passed in property file. This is especially useful
     * for defining the marc.source property to define which file to operate on,
     * in a shell script loop.
     * 
     * @param props
     *            property set in which to look.
     * @param propname
     *            name of the property to lookup.
     * @return value stored for that property (or null if it doesn't exist)
     */
    public static String getProperty(Properties props, String propname)
    {
        return getProperty(props, propname, null);
    }

    /**
     * Check first for a particular property in the System Properties, so that
     * the -Dprop="value" command line arg mechanism can be used to override
     * values defined in the passed in property file. This is especially useful
     * for defining the marc.source property to define which file to operate on,
     * in a shell script loop.
     * 
     * @param props
     *            property set in which to look.
     * @param propname
     *            name of the property to lookup.
     * @param defVal
     *            the default value to use if property is not defined
     * @return value stored for that property (or the if it doesn't exist)
     */
    public static String getProperty(Properties props, String propname, String defVal)
    {
        String prop;
        if ((prop = System.getProperty(propname)) != null)
        {
            return (prop);
        }
        if (props != null && (prop = props.getProperty(propname)) != null)
        {
            return (prop);
        }
        return defVal;
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName)
    {
        return (loadProperties(propertyPaths, propertyFileName, false, null));
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, boolean showName)
    {
        return (loadProperties(propertyPaths, propertyFileName, showName, null));
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param fullFilenameURLStr
     *            String representation of url to properties file whether it is
     *            in a local file or a resource
     * @return Properties object
     */
    public static Properties loadProperties(String fullFilenameURLStr)
    {
        InputStream in = getPropertyFileInputStream(fullFilenameURLStr);
        String errmsg = "Fatal error: Unable to find specified properties file: " + fullFilenameURLStr;

        // load the properties
        Properties props = new Properties();
        try
        {
            if (fullFilenameURLStr.endsWith(".xml") || fullFilenameURLStr.endsWith(".XML"))
            {
                props.loadFromXML(in);
            }
            else
            {
                props.load(in);
            }
            in.close();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }
        return props;
    }

    /**
     * load a properties file into a Properties object
     * 
     * @param propertyPaths
     *            the directories to search for the properties file
     * @param propertyFileName
     *            name of the sought properties file
     * @param showName
     *            whether the name of the file/resource being read should be
     *            shown.
     * @return Properties object
     */
    public static Properties loadProperties(String propertyPaths[], String propertyFileName, boolean showName,
            String filenameProperty)
    {
        String inputStreamSource[] = new String[] { null };
        InputStream in = getPropertyFileInputStream(propertyPaths, propertyFileName, showName, inputStreamSource);
        String errmsg = "Fatal error: Unable to find specified properties file: " + propertyFileName;

        // load the properties
        Properties props = new Properties();
        try
        {
            if (propertyFileName.endsWith(".xml") || propertyFileName.endsWith(".XML"))
            {
                props.loadFromXML(in);
            }
            else
            {
                props.load(in);
            }
            in.close();
            if (filenameProperty != null && inputStreamSource[0] != null)
            {
                File tmpFile = new File(inputStreamSource[0]);

                props.setProperty(filenameProperty, tmpFile.getParent());
            }
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }
        return props;
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName)
    {
        return (getPropertyFileInputStream(propertyPaths, propertyFileName, false));
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName,
            boolean showName)
    {
        return (getPropertyFileInputStream(propertyPaths, propertyFileName, false, null));
    }

    public static InputStream getPropertyFileInputStream(String propertyFileURLStr)
    {
        InputStream in = null;
        String errmsg = "Fatal error: Unable to open specified properties file: " + propertyFileURLStr;
        try
        {
            URL url = new URL(propertyFileURLStr);
            in = url.openStream();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(errmsg);
        }

        return (in);
    }

    public static InputStream getPropertyFileInputStream(String[] propertyPaths, String propertyFileName,
            boolean showName, String inputSource[])
    {
        InputStream in = null;
        String fullPropertyFileURLStr = getPropertyFileAbsoluteURL(propertyPaths, propertyFileName, showName,
                inputSource);
        return (getPropertyFileInputStream(fullPropertyFileURLStr));
    }

    // String verboseStr = System.getProperty("marc.test.verbose");
    // boolean verbose = (verboseStr != null &&
    // verboseStr.equalsIgnoreCase("true"));
    // String lookedIn = "";
    // if (propertyPaths != null)
    // {
    // File propertyFile = new File(propertyFileName);
    // int pathCnt = 0;
    // do
    // {
    // if (propertyFile.exists() && propertyFile.isFile() &&
    // propertyFile.canRead())
    // {
    // try
    // {
    // in = new FileInputStream(propertyFile);
    // if (inputSource != null && inputSource.length >= 1)
    // {
    // inputSource[0] = propertyFile.getAbsolutePath();
    // }
    // if (showName)
    // logger.info("Opening file: "+ propertyFile.getAbsolutePath());
    // else
    // logger.debug("Opening file: "+ propertyFile.getAbsolutePath());
    // }
    // catch (FileNotFoundException e)
    // {
    // // simply eat this exception since we should only try to open the file if
    // we previously
    // // determined that the file exists and is readable.
    // }
    // break; // we found it!
    // }
    // if (verbose) lookedIn = lookedIn + propertyFile.getAbsolutePath() + "\n";
    // if (propertyPaths != null && pathCnt < propertyPaths.length)
    // {
    // propertyFile = new File(propertyPaths[pathCnt], propertyFileName);
    // }
    // pathCnt++;
    // } while (propertyPaths != null && pathCnt <= propertyPaths.length);
    // }
    // // if we didn't find it as a file, look for it as a URL
    // String errmsg = "Fatal error: Unable to find specified properties file: "
    // + propertyFileName;
    // if (verbose) errmsg = errmsg + "\n Looked in: "+ lookedIn;
    // if (in == null)
    // {
    // Utils utilObj = new Utils();
    // URL url =
    // utilObj.getClass().getClassLoader().getResource(propertyFileName);
    // if (url == null)
    // url = utilObj.getClass().getResource("/" + propertyFileName);
    // if (url == null)
    // {
    // logger.error(errmsg);
    // throw new IllegalArgumentException(errmsg);
    // }
    // if (showName)
    // logger.info("Opening resource via URL: "+ url.toString());
    // else
    // logger.debug("Opening resource via URL: "+ url.toString());
    //
    /// *
    // if (url == null)
    // url = utilObj.getClass().getClassLoader().getResource(propertyPath + "/"
    // + propertyFileName);
    // if (url == null)
    // url = utilObj.getClass().getResource("/" + propertyPath + "/" +
    // propertyFileName);
    // */
    // if (url != null)
    // {
    // try
    // {
    // in = url.openStream();
    // }
    // catch (IOException e)
    // {
    // throw new IllegalArgumentException(errmsg);
    // }
    // }
    // }
    // return(in);
    // }

    public static String getPropertyFileAbsoluteURL(String[] propertyPaths, String propertyFileName, boolean showName,
            String inputSource[])
    {
        InputStream in = null;
        // look for properties file in paths
        String verboseStr = System.getProperty("marc.test.verbose");
        boolean verbose = (verboseStr != null && verboseStr.equalsIgnoreCase("true"));
        String lookedIn = "";
        String fullPathName = null;
        if (propertyPaths != null)
        {
            File propertyFile = new File(propertyFileName);
            int pathCnt = 0;
            do
            {
                if (propertyFile.exists() && propertyFile.isFile() && propertyFile.canRead())
                {
                    try
                    {
                        fullPathName = propertyFile.toURI().toURL().toExternalForm();
                        if (inputSource != null && inputSource.length >= 1)
                        {
                            inputSource[0] = propertyFile.getAbsolutePath();
                        }
                    }
                    catch (MalformedURLException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (showName) logger.info("Opening file: " + propertyFile.getAbsolutePath());
                    else logger.debug("Opening file: " + propertyFile.getAbsolutePath());
                    break; // we found it!
                }
                if (verbose) lookedIn = lookedIn + propertyFile.getAbsolutePath() + "\n";
                if (propertyPaths != null && pathCnt < propertyPaths.length)
                {
                    propertyFile = new File(propertyPaths[pathCnt], propertyFileName);
                }
                pathCnt++;
            } while (propertyPaths != null && pathCnt <= propertyPaths.length);
        }
        // if we didn't find it as a file, look for it as a URL
        String errmsg = "Fatal error: Unable to find specified properties file: " + propertyFileName;
        if (verbose) errmsg = errmsg + "\n Looked in: " + lookedIn;
        if (fullPathName == null)
        {
            PropertyUtil utilObj = new PropertyUtil();
            URL url = utilObj.getClass().getClassLoader().getResource(propertyFileName);
            if (url == null) url = utilObj.getClass().getResource("/" + propertyFileName);
            if (url == null)
            {
                logger.error(errmsg);
                throw new IllegalArgumentException(errmsg);
            }
            if (showName) logger.info("Opening resource via URL: " + url.toString());
            else logger.debug("Opening resource via URL: " + url.toString());

            /*
             * if (url == null) url =
             * utilObj.getClass().getClassLoader().getResource(propertyPath +
             * "/" + propertyFileName); if (url == null) url =
             * utilObj.getClass().getResource("/" + propertyPath + "/" +
             * propertyFileName);
             */
            fullPathName = url.toExternalForm();
        }
        return (fullPathName);
    }

    /**
     * Takes an InputStream, reads the entire contents into a String
     * 
     * @param stream
     *            - the stream to read in.
     * @return String containing entire contents of stream.
     */
    public static String readStreamIntoString(InputStream stream) throws IOException
    {
        Reader in = new BufferedReader(new InputStreamReader(stream));

        StringBuilder sb = new StringBuilder();
        char[] chars = new char[4096];
        int length;

        while ((length = in.read(chars)) > 0)
        {
            sb.append(chars, 0, length);
        }

        return sb.toString();
    }
}
