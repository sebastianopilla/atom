package atom.examples;

import java.io.File;

/**
 * Example component for testing absolute files
 */
public class ExampleComponent3 {
  
  private File mWindowsFile;
  private File mLinuxFile;

  public File getWindowsFile() {
    return mWindowsFile;
  }

  public void setWindowsFile(File pWindowsFile) {
    mWindowsFile = pWindowsFile;
  }

  public File getLinuxFile() {
    return mLinuxFile;
  }

  public void setLinuxFile(File pLinuxFile) {
    mLinuxFile = pLinuxFile;
  }
  
} // end ExampleComponent3
