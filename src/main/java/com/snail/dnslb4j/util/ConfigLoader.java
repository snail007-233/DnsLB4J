
package com.snail.dnslb4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pengmeng
 */
public class ConfigLoader {

    private Properties props = new Properties();
    private String cfgPath = "";
    private String cfgFilename;

    public ConfigLoader(String ConfigFolderName) {
        this(ConfigFolderName, true, false);
    }

    public ConfigLoader(Class clazz, String path) {
        try {
            props.load(clazz.getResourceAsStream(path));
        } catch (IOException ex) {
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ConfigLoader(String ConfigFolderName, boolean useUserDir, boolean isAbs) {
        String pathString = ConfigFolderName + java.io.File.separator;
        cfgPath = isAbs ? pathString : (useUserDir ? System.getProperty("user.dir") : getCurrentDir()) + java.io.File.separator + pathString;
        File folder = new File(cfgPath);
        if (!folder.isDirectory()) {
            folder.mkdir();
        }
    }

    public String getCurrentDir() {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            //System.out.println(path);
            return java.net.URLDecoder.decode(new File(path).getParent(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
            return path;
        }
    }

    public String getValue(String key) {
        return props.getProperty(key) == null ? "" : props.getProperty(key);
    }

    public void updateProperties(String key, String value) {
        props.setProperty(key, value);
    }

    public void save() {
        try {
            props.store(new FileOutputStream(cfgPath + cfgFilename, false), "");
        } catch (IOException ex) {
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ConfigLoader setCfgFilename(String cfgFilename, boolean foreCreate) {
        this.cfgFilename = cfgFilename;
        if (foreCreate) {
            File cfgfile = new File(cfgPath + cfgFilename);
            if (!cfgfile.exists()) {
                try {
                    cfgfile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        try {
            props.load(new FileInputStream(cfgPath + cfgFilename));
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return this;
    }
}
