import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * JPGConverter converts image files from a source
 * directory and its subdirectories into .jpg format.
 * <p>
 * Converted images are stored in the target directory
 * with the same file structure as the source directory.
 *
 * @author Danny Nguyen
 * @version 1.1
 * @since 1.0
 */
public class JPGConverter {
  /**
   * Source directory.
   */
  private static final File source = new File("SOURCE DIRECTORY");

  /**
   * Target directory.
   */
  private static final File target = new File("TARGET DIRECTORY");

  /**
   * Number of images converted.
   */
  private static int conversions = 0;

  /**
   * Checks if the source and target directory are valid before parsing the file system.
   *
   * @param args user provided parameters
   */
  public static void main(String[] args) {
    if (source.isDirectory()) {
      if (target.isDirectory()) {
        parseDirectory(source);
        System.out.println("Number of images converted: " + conversions);
      } else {
        System.out.println("Target directory does not exist.");
      }
    } else {
      System.out.println("Source directory does not exist.");
    }
  }

  /**
   * Recursively parses the directory to convert images.
   *
   * @param directory source directory
   */
  private static void parseDirectory(File directory) {
    for (File file : directory.listFiles()) {
      if (file.isFile()) {
        convertIntoJPG(file);
      } else {
        parseDirectory(file);
      }
    }
  }

  /**
   * Converts an image file (typically a .png) into .jpg format.
   *
   * @param file file being converted
   */
  private static void convertIntoJPG(File file) {
    String relativePath = file.getPath().substring(source.getPath().length());
    try {
      BufferedImage image = ImageIO.read(file);
      BufferedImage imageData = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
      imageData.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
      
      new File(target.getPath() + file.getParentFile().getPath().substring(source.getPath().length())).mkdirs();
      ImageIO.write(imageData, "jpg", new File(target.getPath() + relativePath.substring(0, relativePath.length() - 4) + ".jpg"));

      System.out.println("Converted file: " + relativePath);
      conversions++;
    } catch (IOException e) {
      System.out.println("Failed to write file: " + relativePath);
    } catch (NullPointerException e) {
      System.out.println("Not an image file: " + relativePath);
    }
  }
}
