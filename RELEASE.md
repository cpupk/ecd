# Update versio in pom.xml files

    update-version.sh <new version number>

# Creating an unsigned release

    mvn clean package 

This command will create the release repository ZIP file in `update_site/target/com.github.ecd-plugin.update-<version>.zip`

# Creating a release with signed JAR files

(For ECD maintainers):

    mvn clean package -Dsigning.disabled=false -Dsigning.password=<password> -Dsigning.keystore=<absolute path-to-keystore>

This command will create the release repository ZIP file in `update_site/target/com.github.ecd-plugin.update-<version>.zip`

# Update site

1. Delete existing plugins and features directory in update site project. 
2. Extract the content of `com.github.ecd-plugin.update-<version>.zip` to the update site project directory.
3. Update `site.xml` file