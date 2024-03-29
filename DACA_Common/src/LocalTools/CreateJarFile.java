package LocalTools;

import java.io.*;
import java.util.ArrayList;
import java.util.jar.*;
import java.util.zip.ZipException;

public class CreateJarFile {

    public static int BUFFER_SIZE = 10240;

    public static void addtoJar(File TmpDir, String path, String jarName, String Rnd, String Filename, BusinessEntity_Context BE) {
        File finaljar = new File(path + jarName);
        File tempjar = new File(path + Rnd + ".jar");
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(tempjar);
            try (JarOutputStream outjar = new JarOutputStream(stream, manifest)) {
                if (finaljar.exists()) {
                    FileInputStream streamin2 = new FileInputStream(finaljar);
                    try (JarInputStream injar2 = new JarInputStream(streamin2)) {
                        JarEntry jaren = (JarEntry) injar2.getNextEntry();

                        while (jaren != null) {//add business services to new temporary file

                            outjar.putNextEntry(jaren);

                            while (true) {
                                int nRead = injar2.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                outjar.write(buffer, 0, nRead);

                            }

                            jaren = (JarEntry) injar2.getNextEntry();
                        }
                    }
                } else {
                    JarEntry newentry;
                    //add business entity main interface file
                    newentry = new JarEntry("BusinessInterfaces/");
                    outjar.putNextEntry(newentry);
                    FileInputStream rfile;
                    File[] mainfiles = new File(TmpDir + "/BusinessInterfaces/").listFiles();
                    for (File mainfile : mainfiles) {
                        if (mainfile.isFile() && mainfile.getName().contains(".class")) {
                            newentry = new JarEntry("BusinessInterfaces/" + mainfile.getName());
                            outjar.putNextEntry(newentry);
                            rfile = new FileInputStream(mainfile);
                            while (true) {
                                int nRead = rfile.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                outjar.write(buffer, 0, nRead);

                            }
                            rfile.close();
                        }
                    }
                }

                JarEntry newentry;
                File InterfacePackageteste = new File(TmpDir.getAbsolutePath());

                ArrayList<File> files = new ArrayList<>();
                getAllClassFilesInDirectory(files, InterfacePackageteste);

//                File InterfacePackage = new File(TmpDir.getAbsolutePath() + "/" + PackageNameUtils.getPackageNameDir(BE.getPackage()));

                if (!CreateInterfaceDirPackage(finaljar, outjar, BE.getPackage())) {
                    DirectoryOp.removeDirectory(new File(path + Rnd + "/"));
                    outjar.close();
                    tempjar.delete();
                    throw new BTC_Exception("This Interface Package Already Exists");
                }

//                String teststr = PackageNameUtils.getPackageNameDir(BE.getPackage());
                FileInputStream rfile;
                for (int i = 0; i < files.size(); i++) {
                    String file = getCorrectDirectory(files.get(i), TmpDir);
                    try {
                        newentry = new JarEntry(file);
                        outjar.putNextEntry(newentry);
                        rfile = new FileInputStream(files.get(i));
                        while (true) {
                            int nRead = rfile.read(buffer, 0, buffer.length);
                            if (nRead <= 0) {
                                break;
                            }
                            outjar.write(buffer, 0, nRead);

                        }
                        rfile.close();

                    } catch (ZipException ignored) {
                    }

                }
            }
            String pathnew = finaljar.getAbsolutePath();

            boolean deleted = false;
            while (!deleted && finaljar.exists()) {
                deleted = finaljar.delete();
            }

            tempjar.renameTo(new File(pathnew));

            //DirectoryOp.removeDirectory(new File(path + Rnd + "/"));

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }

    }

    public static void getAllClassFilesInDirectory(ArrayList<File> files, File Directory) {

        File[] tmp = Directory.listFiles();
        for (File tmp1 : tmp) {
            if (tmp1.isDirectory()) {
                getAllClassFilesInDirectory(files, tmp1);
            } else {
                if (tmp1.isFile() && tmp1.getName().contains(".class")) {
                    files.add(tmp1);
                }
            }
        }

    }

    public static String getCorrectDirectory(File file, File tmpdir) throws IOException {
        String tmpstr = file.getCanonicalPath().substring(file.getCanonicalPath().lastIndexOf(tmpdir.getName()) + tmpdir.getName().length() + 1);
        tmpstr = tmpstr.replace('\\', '/');
        return tmpstr;
    }

    /**
     * Method that creates a dir package in jar
     *
     * @param finaljar the jar file
     * @param outjar   the jar we want has output
     * @param Package  the name of the package
     * @return true if it has sucefully
     * @throws BTC_Exception
     * @throws java.io.IOException
     */
    public static boolean CreateInterfaceDirPackage(File finaljar, JarOutputStream outjar, String Package) throws BTC_Exception, IOException {
        if (finaljar.exists()) {
            if (PackageUtils.CheckBusinessExists(finaljar.getAbsolutePath(), PackageNameUtils.getPackageNameDir(Package))) {
                return false;
            }
            String dirtmp = "";
            String[] dirs = Package.split("\\.");
            for (String dir : dirs) {
                dirtmp += dir + "/";
                JarEntry newentry;
                newentry = new JarEntry(dirtmp);
                if (!PackageUtils.CheckBusinessExists(finaljar.getAbsolutePath(), dirtmp)) {
                    outjar.putNextEntry(newentry);
                }
            }
        } else {
            String dirtmp = "";
            String[] dirs = Package.split("\\.");
            for (String dir : dirs) {
                dirtmp += dir + "/";
                JarEntry newentry;
                newentry = new JarEntry(dirtmp);
                if (dirtmp.compareTo("BusinessInterfaces/") != 0) {
                    outjar.putNextEntry(newentry);
                }
            }
        }
        return true;
    }

    /**
     * Method that deletes a interface from the java file
     *
     * @param jarPath the path of the jarfile
     * @param jarFile the name of the jarfile
     * @param bs      the class of the interface to be deleted
     * @throws BTC_Exception
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void DeleteBusinessEntity(String jarPath, String jarFile, Class bs) throws BTC_Exception, IOException {
        File finaljar = new File(jarPath + jarFile);
        if (PackageUtils.CheckBusinessExists(finaljar.getAbsolutePath(), PackageNameUtils.getPackageNameDir(bs.getPackage().getName()))) {
            String Rnd = SessionIdentifierGenerator.nextSessionId();
            File tempjar = new File(jarPath + Rnd + ".jar");
            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(tempjar);
            JarOutputStream outjar = new JarOutputStream(stream, manifest);

            if (finaljar.exists()) {
                FileInputStream streamin2 = new FileInputStream(finaljar);
                try (JarInputStream injar2 = new JarInputStream(streamin2)) {
                    JarEntry jaren = (JarEntry) injar2.getNextEntry();

                    while (jaren != null) {//add business services to new temporary file
                        if (!jaren.getName().contains(PackageNameUtils.getPackageNameDir(bs.getPackage().getName()))) {
                            outjar.putNextEntry(jaren);

                            while (true) {
                                int nRead = injar2.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                outjar.write(buffer, 0, nRead);

                            }

                        }

                        jaren = (JarEntry) injar2.getNextEntry();

                    }
                }

                outjar.close();
                String pathnew = finaljar.getAbsolutePath();

                finaljar.delete();
                tempjar.renameTo(new File(pathnew));
            }

        } else {
            //TODO: THIS CAN BE THROWN! CHECK THIS.
            throw new BTC_Exception("This Interface Package Does Not Exist");
        }
    }

    /**
     * Method that adds cruds to a jar
     *
     * @param jarfile
     * @param tmpfile
     */
    public static void addCrudToJar(File jarfile, File tmpfile) {
        try {
            String temppath = tmpfile.getAbsolutePath().substring(0, tmpfile.getAbsolutePath().lastIndexOf("\\"));
            File tempjar = new File(temppath + "/" + "temp.jar");
            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(tempjar);
            try (JarOutputStream outjar = new JarOutputStream(stream, manifest)) {
                if (jarfile.exists()) {
                    FileInputStream streamin2 = new FileInputStream(jarfile);
                    try (JarInputStream injar2 = new JarInputStream(streamin2)) {
                        JarEntry jaren = (JarEntry) injar2.getNextEntry();

                        while (jaren != null && jaren.getName().compareTo("crud/") != 0 && jaren.getName().compareTo("crud/Cruds.class") != 0) {//add business services to new temporary file

                            outjar.putNextEntry(jaren);

                            while (true) {
                                int nRead = injar2.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                outjar.write(buffer, 0, nRead);

                            }
                            jaren = (JarEntry) injar2.getNextEntry();
                        }
                    }
                }
                outjar.putNextEntry(new JarEntry("crud/"));
                outjar.putNextEntry(new JarEntry("crud/" + tmpfile.getName()));
                try (FileInputStream rfile = new FileInputStream(tmpfile)) {
                    while (true) {
                        int nRead = rfile.read(buffer, 0, buffer.length);
                        if (nRead <= 0) {
                            break;
                        }
                        outjar.write(buffer, 0, nRead);

                    }
                }
            }
            String pathnew = jarfile.getAbsolutePath();

            boolean deleted = false;
            while (!deleted && jarfile.exists()) {
                deleted = jarfile.delete();
            }

            tempjar.renameTo(new File(pathnew));

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    public static void addClassesToJar(File finaljar, File itfsjar, File tmpdir) {
        try {

            if (finaljar.exists()) {
                boolean deleted = false;
                while (!deleted && finaljar.exists()) {
                    deleted = finaljar.delete();
                }
            }

            byte buffer[] = new byte[BUFFER_SIZE];
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
            FileOutputStream stream = new FileOutputStream(finaljar);
            try (JarOutputStream outjar = new JarOutputStream(stream, manifest)) {
                if (itfsjar.exists()) {
                    FileInputStream streamin2 = new FileInputStream(itfsjar);
                    try (JarInputStream injar2 = new JarInputStream(streamin2)) {
                        JarEntry jaren = (JarEntry) injar2.getNextEntry();

                        //while (jaren != null && jaren.getName().compareTo("crud/")!=0 && jaren.getName().compareTo("crud/Cruds.class")!=0 ) {//add business services to new temporary file
                        while (jaren != null) {
                            outjar.putNextEntry(jaren);

                            while (true) {
                                int nRead = injar2.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                outjar.write(buffer, 0, nRead);


                            }
                            jaren = (JarEntry) injar2.getNextEntry();
                        }
                    }
                }

                outjar.putNextEntry(new JarEntry("Classes/"));
                File[] Filelist = tmpdir.listFiles();
                for (File Filelist1 : Filelist) {
                    if (Filelist1.getName().endsWith(".class")) {
                        System.out.println("Adding .class : "+Filelist1.getName());
                        outjar.putNextEntry(new JarEntry("Classes/" + Filelist1.getName()));
                        try (final FileInputStream rfile = new FileInputStream(Filelist1)) {
                            while (true) {
                                int nRead = rfile.read(buffer, 0, buffer.length);
                                if (nRead <= 0) {
                                    break;
                                }
                                outjar.write(buffer, 0, nRead);

                            }
                        }
                    }
                }
            }

            LocalTools.DirectoryOp.removeDirectory(tmpdir);


        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error: " + ex.getMessage());
        }
    }
}
