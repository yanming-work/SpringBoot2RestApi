package com.test.core.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;  
import java.util.Enumeration;  
  
import org.apache.commons.compress.archivers.zip.Zip64Mode;  
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;  
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;  
import org.apache.commons.compress.utils.IOUtils; 
/**
 * 压缩文档相关的工具类
 */
public final class ZIPUtil {
    /**
     * 文档压缩
     *
     * @param file 需要压缩的文件或目录
     * @param dest 压缩后的文件名称
     * @throws Exception
     */
    public final static void deCompress(File file, String dest) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest))) {
            zipFile(file, zos, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final static void zipFile(File inFile, ZipOutputStream zos, String dir) throws IOException {
        if (inFile.isDirectory()) {
            File[] files = inFile.listFiles();
            if (CheckUtil.valid(files)) {
                for (File file : files) {
                    String name = inFile.getName();
                    if (!"".equals(dir)) {
                        name = dir + "/" + name;
                    }
                    zipFile(file, zos, name);
                }
            }
        } else {
            String entryName = null;
            if (!"".equals(dir)) {
                entryName = dir + "/" + inFile.getName();
            } else {
                entryName = inFile.getName();
            }
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            try (InputStream is = new FileInputStream(inFile)) {
                int len = 0;
                while ((len = is.read()) != -1) {
                    zos.write(len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文档解压
     *
     * @param source 需要解压缩的文档名称
     * @param path   需要解压缩的路径
     */
    public final static void unCompress(File source, String path) throws IOException {
        ZipEntry zipEntry = null;
        FileUtil.createPaths(path);
        //实例化ZipFile，每一个zip压缩文件都可以表示为一个ZipFile
        //实例化一个Zip压缩文件的ZipInputStream对象，可以利用该类的getNextEntry()方法依次拿到每一个ZipEntry对象
        try (
                ZipFile zipFile = new ZipFile(source);
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(source))
        ) {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                File temp = new File(path + "/" + fileName);
                if (!temp.getParentFile().exists()) {
                    temp.getParentFile().mkdirs();
                }
                try (OutputStream os = new FileOutputStream(temp);
                     //通过ZipFile的getInputStream方法拿到具体的ZipEntry的输入流
                     InputStream is = zipFile.getInputStream(zipEntry)) {
                    int len = 0;
                    while ((len = is.read()) != -1) {
                        os.write(len);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    
    
    
    /** 
     * 把N多文件或文件夹压缩成zip。 
     *  
     * @param files 
     *            需要压缩的文件或文件夹。 
     * @param zipFilePath 
     *            压缩后的zip文件 
     * @throws IOException 
     *             压缩时IO异常。 
     * 
     * @date 2014年06月25日 
     */  
    public static void compress(File[] files, File zipFile) throws IOException {  
        if (CollectionUtil.isEmpty(files)) {  
            return;  
        }  
        ZipArchiveOutputStream out = new ZipArchiveOutputStream(zipFile);  
        out.setUseZip64(Zip64Mode.AsNeeded);  
        // 将每个文件用ZipArchiveEntry封装  
        for (File file : files) {  
            if (file == null) {  
                continue;  
            }  
            compressOneFile(file, out, "");  
        }  
        if (out != null) {  
            out.close();  
        }  
    }  
  
    /** 
     * 功能：压缩文件或文件夹。 
     *  
     * 
     * @date 2014年06月25日 
     * @param srcFile 
     *            源文件。 
     * @param destFile 
     *            压缩后的文件 
     * @throws IOException 
     *             压缩时出现了异常。 
     */  
    public static void compress(File srcFile, File destFile) throws IOException {  
        ZipArchiveOutputStream out = null;  
        try {  
            out = new ZipArchiveOutputStream(new BufferedOutputStream(  
                    new FileOutputStream(destFile), 1024));  
            compressOneFile(srcFile, out, "");  
        } finally {  
            out.close();  
        }  
    }  
  
    /** 
     * 功能：压缩单个文件,非文件夹。私有，不对外开放。 
     *  
     * 
     * @date 2014年06月25日 
     * @param srcFile 
     *            源文件，不能是文件夹。 
     * @param out 
     *            压缩文件的输出流。 
     * @param destFile 
     *            压缩后的文件 
     * @param dir 
     *            在压缩包中的位置,根目录传入/。 
     * @throws IOException 
     *             压缩时出现了异常。 
     */  
    private static void compressOneFile(File srcFile,  
            ZipArchiveOutputStream out, String dir) throws IOException {  
        if (srcFile.isDirectory()) {// 对文件夹进行处理。  
            ZipArchiveEntry entry = new ZipArchiveEntry(dir + srcFile.getName()  
                    + "/");  
            out.putArchiveEntry(entry);  
            out.closeArchiveEntry();  
            // 循环文件夹中的所有文件进行压缩处理。  
            String[] subFiles = srcFile.list();  
            for (String subFile : subFiles) {  
                compressOneFile(new File(srcFile.getPath() + "/" + subFile),  
                        out, (dir + srcFile.getName() + "/"));  
            }  
        } else { // 普通文件。  
            InputStream is = null;  
            try {  
                is = new BufferedInputStream(new FileInputStream(srcFile));  
                // 创建一个压缩包。  
                ZipArchiveEntry entry = new ZipArchiveEntry(srcFile, dir  
                        + srcFile.getName());  
                out.putArchiveEntry(entry);  
                IOUtils.copy(is, out);  
                out.closeArchiveEntry();  
            } finally {  
                if (is != null)  
                    is.close();  
            }  
        }  
    }  
  
    /** 
     * 功能：解压缩zip压缩包下的所有文件。 
     *  
     * 
     * @date 2014年06月25日 
     * @param zipFile 
     *            zip压缩文件 
     * @param dir 
     *            解压缩到这个路径下 
     * @throws IOException 
     *             文件流异常 
     */  
    public void decompressZip(File zipFile, String dir) throws IOException {  
    	org.apache.commons.compress.archivers.zip.ZipFile zf = new org.apache.commons.compress.archivers.zip.ZipFile(zipFile);  
        try {  
            for (Enumeration<ZipArchiveEntry> entries = zf.getEntries(); entries  
                    .hasMoreElements();) {  
                ZipArchiveEntry ze = entries.nextElement();  
                // 不存在则创建目标文件夹。  
                File targetFile = new File(dir, ze.getName());  
                // 遇到根目录时跳过。  
                if (ze.getName().lastIndexOf("/") == (ze.getName().length() - 1)) {  
                    continue;  
                }  
                // 如果文件夹不存在，创建文件夹。  
                if (!targetFile.getParentFile().exists()) {  
                    targetFile.getParentFile().mkdirs();  
                }  
  
                InputStream i = zf.getInputStream(ze);  
                OutputStream o = null;  
                try {  
                    o = new FileOutputStream(targetFile);  
                    IOUtils.copy(i, o);  
                } finally {  
                    if (i != null) {  
                        i.close();  
                    }  
                    if (o != null) {  
                        o.close();  
                    }  
                }  
            }  
        } finally {  
            zf.close();  
        }  
    }  
  
    /** 
     * 功能：解压缩zip压缩包下的某个文件信息。 
     *  
     * 
     * @date 2014年06月25日 
     * @param zipFile 
     *            zip压缩文件 
     * @param fileName 
     *            某个文件名,例如abc.zip下面的a.jpg，需要传入/abc/a.jpg。 
     * @param dir 
     *            解压缩到这个路径下 
     * @throws IOException 
     *             文件流异常 
     */  
    public void decompressZip(File zipFile, String fileName, String dir)  
            throws IOException {  
        // 不存在则创建目标文件夹。  
        File targetFile = new File(dir, fileName);  
        if (!targetFile.getParentFile().exists()) {  
            targetFile.getParentFile().mkdirs();  
        }  
  
        org.apache.commons.compress.archivers.zip.ZipFile zf = new org.apache.commons.compress.archivers.zip.ZipFile(zipFile);  
        Enumeration<ZipArchiveEntry> zips = zf.getEntries();  
        ZipArchiveEntry zip = null;  
        while (zips.hasMoreElements()) {  
            zip = zips.nextElement();  
            if (fileName.equals(zip.getName())) {  
                OutputStream o = null;  
                InputStream i = zf.getInputStream(zip);  
                try {  
                    o = new FileOutputStream(targetFile);  
                    IOUtils.copy(i, o);  
                } finally {  
                    if (i != null) {  
                        i.close();  
                    }  
                    if (o != null) {  
                        o.close();  
                    }  
                }  
            }  
        }  
    }  
  
    /** 
     * 功能：得到zip压缩包下的某个文件信息,只能在根目录下查找。 
     *  
     * 
     * @date 2014年06月25日 
     * @param zipFile 
     *            zip压缩文件 
     * @param fileName 
     *            某个文件名,例如abc.zip下面的a.jpg，需要传入/abc/a.jpg。 
     * @return ZipArchiveEntry 压缩文件中的这个文件,没有找到返回null。 
     * @throws IOException 
     *             文件流异常 
     */  
    public ZipArchiveEntry readZip(File zipFile, String fileName)  
            throws IOException {  
    	org.apache.commons.compress.archivers.zip.ZipFile zf = new org.apache.commons.compress.archivers.zip.ZipFile(zipFile);  
        Enumeration<ZipArchiveEntry> zips = zf.getEntries();  
        ZipArchiveEntry zip = null;  
        while (zips.hasMoreElements()) {  
            zip = zips.nextElement();  
            if (fileName.equals(zip.getName())) {  
                return zip;  
            }  
        }  
        return null;  
    }  
  
    /** 
     * 功能：得到zip压缩包下的所有文件信息。 
     *  
     * 
     * @date 2014年06月25日 
     * @param zipFile 
     *            zip压缩文件 
     * @return Enumeration<ZipArchiveEntry> 压缩文件中的文件枚举。 
     * @throws IOException 
     *             文件流异常 
     */  
    public Enumeration<ZipArchiveEntry> readZip(File zipFile)  
            throws IOException {  
    	org.apache.commons.compress.archivers.zip.ZipFile zf = new org.apache.commons.compress.archivers.zip.ZipFile(zipFile);  
        Enumeration<ZipArchiveEntry> zips = zf.getEntries();  
        return zips;  
    }  
}
