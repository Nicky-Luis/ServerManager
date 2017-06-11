/**
 * 文件名:ClassSearcher.java
 * 版本信息:1.0
 * 日期:2015-5-9
 * Copyright 广州点步信息科技
 * 版权所有
 */
package com.dbumama.market.web.core.annotation;

import com.dbumama.market.service.utils.FileUtils;
import com.dbumama.market.web.core.render.Reflect;
import com.google.common.collect.Lists;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author: wjun.java@gmail.com
 * @date:2015-5-9
 */
public final class ClassSearcher {
    protected static final Log LOG = Log.getLog(ClassSearcher.class);

    private String classpath = PathKit.getRootClassPath();

    private String libDir = PathKit.getWebRootPath() + File.separator + "WEB-INF" + File.separator + "lib";

    private List<String> scanPackages = Lists.newArrayList();

    private boolean includeAllJarsInLib = false;

    private List<String> includeJars = Lists.newArrayList();

    @SuppressWarnings("rawtypes")
    private Class target;

    @SuppressWarnings("unchecked")
    private static <T> List<Class<? extends T>> extraction(Class<T> clazz, List<String> classFileList) {
        List<Class<? extends T>> classList = Lists.newArrayList();
        for (String classFile : classFileList) {
            Class<?> classInFile = Reflect.on(classFile).get();
            if (clazz.isAssignableFrom(classInFile) && clazz != classInFile) {
                classList.add((Class<? extends T>) classInFile);
            }
        }

        return classList;
    }

    @SuppressWarnings("rawtypes")
    public static ClassSearcher of(Class target) {
        return new ClassSearcher(target);
    }

    @SuppressWarnings("unchecked")
    public <T> List<Class<? extends T>> search() {
        List<String> classFileList = Lists.newArrayList();
        if (scanPackages.isEmpty()) {
            classFileList = FileUtils.findFiles(classpath, "*.class");
        } else {
            for (String scanPackage : scanPackages) {
                classFileList = FileUtils.findFiles(classpath + File.separator + scanPackage.replaceAll("\\.", "\\" +
                        File.separator), "*.class");
            }
        }
        classFileList.addAll(findjarFiles(libDir));
        return extraction(target, classFileList);
    }

    /**
     * 查找jar包中的class
     */
    @SuppressWarnings("Duplicates")
    private List<String> findjarFiles(String baseDirName) {
        List<String> classFiles = Lists.newArrayList();
        File baseDir = new File(baseDirName);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            LOG.error("file search error:" + baseDirName + " is not a dir！");
        } else {
            File[] files = baseDir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    classFiles.addAll(findjarFiles(file.getAbsolutePath()));
                } else {
                    if (includeAllJarsInLib || includeJars.contains(file.getName())) {
                        JarFile localJarFile = null;
                        try {
                            localJarFile = new JarFile(new File(baseDirName + File.separator + file.getName()));
                            Enumeration<JarEntry> entries = localJarFile.entries();
                            while (entries.hasMoreElements()) {
                                JarEntry jarEntry = entries.nextElement();
                                String entryName = jarEntry.getName();
                                if (scanPackages.isEmpty()) {
                                    if (!jarEntry.isDirectory() && entryName.endsWith(".class")) {
                                        String className = entryName.replaceAll(File.separator, ".").substring(0,
                                                entryName.length() - 6);
                                        classFiles.add(className);
                                    }
                                } else {
                                    for (String scanPackage : scanPackages) {
                                        scanPackage = scanPackage.replaceAll("\\.", "\\" + File.separator);
                                        if (!jarEntry.isDirectory() && entryName.endsWith(".class") && entryName
                                                .startsWith(scanPackage)) {
                                            String className = entryName.replaceAll(File.separator, ".").substring(0,
                                                    entryName.length() - 6);
                                            classFiles.add(className);
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (localJarFile != null) {
                                    localJarFile.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }
        return classFiles;
    }

    @SuppressWarnings("rawtypes")
    public ClassSearcher(Class target) {
        this.target = target;
    }

    public ClassSearcher injars(List<String> jars) {
        if (jars != null) {
            includeJars.addAll(jars);
        }
        return this;
    }

    public ClassSearcher inJars(String... jars) {
        if (jars != null) {
            for (String jar : jars) {
                includeJars.add(jar);
            }
        }
        return this;
    }

    public ClassSearcher includeAllJarsInLib(boolean includeAllJarsInLib) {
        this.includeAllJarsInLib = includeAllJarsInLib;
        return this;
    }

    public ClassSearcher classpath(String classpath) {
        this.classpath = classpath;
        return this;
    }

    public ClassSearcher libDir(String libDir) {
        this.libDir = libDir;
        return this;
    }

    public ClassSearcher scanPackages(List<String> scanPaths) {
        if (scanPaths != null) {
            scanPackages.addAll(scanPaths);
        }
        return this;
    }
}
