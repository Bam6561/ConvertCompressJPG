import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JPGConverter converts image files from a source
 * directory and its subdirectories into a .jpg format.
 * <p>
 * Converted images are stored in the {@link #target target directory}
 * with the same file structure as the {@link #source source directory}.
 * <p>
 * Optionally, the images can also be compressed
 * by adjusting the {@link #quality} value.
 * <p>
 * By default, the number of {@link #threads} to use is 1.
 *
 * @author Danny Nguyen
 * @version 1.3.0
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
   * Image quality.
   * <p>
   * Must be within a range of 0.0 (lowest) to 1.0 (highest).
   */
  private static final float quality = 1.0f;

  /**
   * Number of threads to use.
   */
  private static final int threads = 1;

  /**
   * Number of images converted.
   */
  private static int conversions = 0;

  /**
   * Checks if the {@link #source}, {@link #target}, {@link #quality},
   * and {@link #threads} inputs are valid before parsing the file system.
   *
   * @param args user provided parameters
   */
  public static void main(String[] args) {
    if (!source.isDirectory()) {
      System.out.println("Source directory does not exist.");
      return;
    }
    File[] sourceFiles = source.listFiles();
    if (sourceFiles.length == 0) {
      System.out.println("Source directory is empty.");
      return;
    }
    if (!target.isDirectory()) {
      System.out.println("Target directory does not exist.");
      return;
    }
    if (!(quality >= 0 || quality <= 1)) {
      System.out.println("Image quality must be within a range of 0.0 - 1.0.");
      return;
    }
    if (threads <= 0) {
      System.out.println("Cannot use less than 1 thread.");
      return;
    }

    long start = System.currentTimeMillis();
    List<List<File>> fileGroups = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      fileGroups.add(new ArrayList<>());
    }

    divideTasks(sourceFiles, fileGroups);
    startTasks(fileGroups);

    long end = System.currentTimeMillis();
    System.out.println("Converted " + conversions + " images in " + millisecondsToMinutesSeconds(end - start).trim() + ".");
  }

  /**
   * Divides the {@link #source source directory}'s files
   * into groups by the number of {@link #threads}.
   *
   * @param sourceFiles {@link #source}
   * @param fileGroups  {@link #threads}
   */
  private static void divideTasks(File[] sourceFiles, List<List<File>> fileGroups) {
    int groupNumber = 0;
    for (File sourceFile : sourceFiles) {
      fileGroups.get(groupNumber).add(sourceFile);
      groupNumber++;
      if (groupNumber > threads - 1) {
        groupNumber = 0;
      }
    }
  }

  /**
   * Starts parallel work on each file group.
   *
   * @param fileGroups {@link #threads}
   */
  private static void startTasks(List<List<File>> fileGroups) {
    List<Thread> tasks = new ArrayList<>();
    for (int i = 0; i < threads; i++) {
      final int taskNumber = i;
      Thread task = new Thread(() -> parseDirectory(fileGroups.get(taskNumber)));
      tasks.add(task);
      task.start();
    }
    try {
      for (Thread task : tasks) {
        task.join();
      }
    } catch (InterruptedException ex) {
      System.out.println("Tasks interrupted.");
    }
  }

  /**
   * Recursively parses the directory to convert images.
   *
   * @param directory source directory
   */
  private static void parseDirectory(List<File> directory) {
    for (File file : directory) {
      if (file.isFile()) {
        createTargetDirectory(file);
        convertIntoJPG(file);
      } else {
        parseDirectory(Arrays.asList(file.listFiles()));
      }
    }
  }

  /**
   * Creates the target directory if it does not exist.
   *
   * @param file image file
   */
  private static void createTargetDirectory(File file) {
    File targetDirectory = new File(target.getPath() + file.getParentFile().getPath().substring(source.getPath().length()));
    if (!targetDirectory.exists()) {
      targetDirectory.mkdirs();
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
      if (image == null) {
        System.out.println("Not an image file: " + relativePath);
        return;
      }
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
      System.out.println("Converted file: " + relativePath);
      output.close();
      writer.dispose();
      conversions++;
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

  /**
   * Converts milliseconds to minutes and seconds.
   *
   * @param duration elapsed time in milliseconds
   * @return minutes and seconds
   */
  private static String millisecondsToMinutesSeconds(long duration) {
    long minutes = duration / 60000L % 60;
    long seconds = duration / 1000L % 60;
    return (minutes == 0 ? "" : minutes + "m ") + (seconds == 0 ? "" : seconds + "s ");
  }
}
