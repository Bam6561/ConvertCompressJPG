import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * JPGConverter converts image files from a source
 * directory and its subdirectories into a .jpg format.
 * <p>
 * Converted images are stored in the {@link #target target directory}
 * with the same file structure as the {@link #source source directory}.
 * <p>
 * Optionally, the images can also be compressed
 * by adjusting the {@link #quality} value.
 *
 * @author Danny Nguyen
 * @version 1.2
 * @since 1.0
 */
public class JPGConverter {
  /**
   * Source directory.
   */
  private static final File source = new File("C:\\Users\\ndann\\Desktop\\old");

  /**
   * Target directory.
   */
  private static final File target = new File("C:\\Users\\ndann\\Desktop\\new");

  /**
   * Image quality.
   * <p>
   * Must be within a range of 0.0 (lowest) to 1.0 (highest).
   */
  private static final float quality = 1.0f;

  /**
   * Existing directory paths.
   * <p>
   * Used to signal when to create a new directory in the target directory.
   */
  private static final Set<String> targetDirectories = new HashSet<>();

  /**
   * Number of images converted.
   */
  private static int conversions = 0;

  /**
   * Checks if the {@link #source}, {@link #target}, and {@link #quality}
   * inputs are valid before parsing the file system.
   *
   * @param args user provided parameters
   */
  public static void main(String[] args) {
    if (source.isDirectory()) {
      if (target.isDirectory()) {
        if (quality >= 0 && quality <= 1) {
          parseDirectory(source);
          System.out.println("Number of images converted: " + conversions);
        } else {
          System.out.println("Image quality must be within a range of 0.0 - 1.0.");
        }
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
        createTargetDirectory(file);
        convertIntoJPG(file);
      } else {
        parseDirectory(file);
      }
    }
  }

  /**
   * Creates the target directory if it does not exist.
   *
   * @param file image file
   */
  private static void createTargetDirectory(File file) {
    String targetDirectory = target.getPath() + file.getParentFile().getPath().substring(source.getPath().length());
    if (!targetDirectories.contains(targetDirectory)) {
      targetDirectories.add(targetDirectory);
      new File(targetDirectory).mkdirs();
    }
  }

  /**
   * Converts an image file into .jpg format.
   * <p>
   * Compresses the image quality if the {@link #quality} value is set.
   *
   * @param file image file
   */
  private static void convertIntoJPG(File file) {
    String relativePath = file.getPath().substring(source.getPath().length());
    try {
      BufferedImage image = ImageIO.read(file);
      if (image != null) {
        if (image.getColorModel().hasAlpha()) {
          image = removeAlphaChannel(image);
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageOutputStream output = ImageIO.createImageOutputStream(new File(target.getPath() + relativePath.substring(0, relativePath.lastIndexOf(".")) + ".jpg"));
        writer.setOutput(output);

        ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(quality);

        writer.write(null, new IIOImage(image, null, null), params);
        output.close();
        writer.dispose();

        System.out.println("Converted file: " + relativePath);
        conversions++;
      } else {
        System.out.println("Not an image file: " + relativePath);
      }
    } catch (IOException e) {
      System.out.println("Failed to write file: " + relativePath);
    }
  }

  /**
   * Removes an image's alpha channel.
   *
   * @param image image with alpha channel
   * @return image without alpha channel
   */
  private static BufferedImage removeAlphaChannel(BufferedImage image) {
    BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
    return newImage;
  }
}
