# Communote Installation Packages
This module creates the Communote installation packages by bundling the extracted Communote WAR file with a platform specific distribution of Apache Tomcat and adding appropriate configurations. The following packages are created:
- a Windows 32-bit package which includes the Windows service wrapper and the compiled APR/native library for use with 32-bit JVMs
- a Windows 64-bit package which includes the Windows service wrapper and the compiled APR/native library for use with 64-bit JVMs
- a Linux package which actually is a generic package because it does not contain any native code

# Building
A prerequesite for building is the standalone WAR File of Communote which must exist in the local or a remote Maven repository. Therefore, you should typically build the `war-standalone` module (with `mvn install`) first.

## Instructions
To build use
```
mvn -DformatWindows=zip -DformatLinux=tar.gz
```
This will invoke the `clean` and `package` phases and create zip files for the Windows packages and a gzipped tar for the Linux package. When not specifying the format-properties the packages will be created in directories.

## Notes
If you want to use the Linux artifact you are advised to build on Linux and **not** on Windows for not messing the file permissions. If you don't want your user/group as owner in the tar file you'll have to re-package the tgz with tar command and `--owner=0 --group=0` options. The Maven assembly plugin doesn't seem to have an option for this.

The Linux Tomcat distribution is available in Maven Central. The Windows distributions are downloaded with the wget goal of the [maven-download-plugin](https://github.com/maven-download-plugin/maven-download-plugin). This plugin will cache the files in the `.cache/download-maven-plugin` directory of your local maven repository. If this is not desired you could change the cache directory for instance to a subdirectory of the `target` directory so that it will be removed with the next clean. This can be done with the property `download.cache.directory`.

## Troubleshooting
If the Windows Tomcat distributions cannot be downloaded from the configured mirror another server can be defined with the property `tomcatDownloadMirror`. Available mirrors for each version can be found on the corresponding download page at http://tomcat.apache.org.

# Update Tomcat Version
If the Tomcat version should be updated you have to
- modify the value of the `tomcatVersion` property (and the value of the `tomcatVersionMajor` property if the a new major version should be used)
- correct the sha512 hash-sums to the values of the new Windows packages
- test whether the download of the Tomcat distributions is possible

Additionally you should check whether the context.xml and server.xml files in src/main/resources/ subdirectories need to be modified because of changes in new Tomcat version.