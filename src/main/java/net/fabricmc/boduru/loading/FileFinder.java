package net.fabricmc.boduru.loading;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * The FileFinder class is responsible for providing
 * the custom shader files from the mod to minecraft.
 */

public class FileFinder {
    public final static String CORE_SHADERS_PATH = "../src/main/resources/assets/water_shader/shaders/core/";
    public final static String INCLUDE_SHADERS_PATH = "../src/main/resources/assets/water_shader/shaders/include/";
    public final static String POST_SHADERS_PATH = "../src/main/resources/assets/water_shader/shaders/post/";
    public final static String PROGRAM_SHADERS_PATH = "../src/main/resources/assets/water_shader/shaders/program/";

    public static boolean DoesFileExist(String path) {
        return new File(path).exists();
    }

    public static String WhereFileExists(String name) {
        if (DoesFileExist(CORE_SHADERS_PATH + name))
            return CORE_SHADERS_PATH + name;
        else if (DoesFileExist(INCLUDE_SHADERS_PATH + name))
            return INCLUDE_SHADERS_PATH + name;
        else if (DoesFileExist(POST_SHADERS_PATH + name))
            return POST_SHADERS_PATH + name;
        else if (DoesFileExist(PROGRAM_SHADERS_PATH + name))
            return PROGRAM_SHADERS_PATH + name;
        else
            return null;
    }

    public static String ReadFile(String path) {
        try {
            return IOUtils.toString(new FileInputStream(path), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
