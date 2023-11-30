# Audit results

## Binary File Verification

### Dr. Garbage Tools

_Primary Source: https://sourceforge.net/projects/drgarbagetools/files/eclipse/4.4/stable/plugins/_ -> com.drgarbage.asm_5.0.3.201408050542.jar contains both files

* `./org.sf.feeling.decompiler/lib/com.drgarbage.asm.util_5.0.3.jar`
  * SHA-256: 06096adf6144ee613a9cc6d55325f6b1f04b03c52ca1841e9b3d7ae9dcadd2f2
  * Equal to the file of the same name in https://downloads.sourceforge.net/project/drgarbagetools/eclipse/4.4/stable/plugins/com.drgarbage.asm_5.0.3.201408050542.jar
* `./org.sf.feeling.decompiler/lib/com.drgarbage.asm_5.0.3.jar`
  * SHA-256: d14546c965fea4a8fef3f7c267788d9748058957b84ae11a9fb22d5e386921d1
  * Equal to the file of the same name in https://downloads.sourceforge.net/project/drgarbagetools/eclipse/4.4/stable/plugins/com.drgarbage.asm_5.0.3.201408050542.jar

### ASM - org.ow2.asm

_Primary Source: https://mvnrepository.com/artifact/org.ow2.asm/asm_ 

* `./org.sf.feeling.decompiler/lib/asm-9.1.jar`
  * SHA-256: cda4de455fab48ff0bcb7c48b4639447d4de859a7afc30a094a986f0936beba2
  * https://repo1.maven.org/maven2/org/ow2/asm/asm/9.1/asm-9.1.jar

### Apache commons

_Primary Source: https://commons.apache.org/downloads/index.html_

* `./org.sf.feeling.decompiler.source.attach/lib/commons-compress.jar`
  * Not included in ECD repository, downloaded automatically via Maven
  * SHA-256: c267f17160e9ef662b4d78b7f29dca7c82b15c5cff2cb6a9865ef4ab3dd5b787
  * Equal to https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.23.0/commons-compress-1.23.0.jar

### Fernflower
* `./org.sf.feeling.decompiler/lib/fernflower.jar`
  * Not included in ECD repository, downloaded automatically via Maven
  * SHA-256: 97b3579a221a3edf9651fca278257307441cb6dfdcfe1a1c6579f11002ff5d27
  * Version from https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/java/java-decompiler-engine/232.10203.10/java-decompiler-engine-232.10203.10.jar

### Vineflower
_Primary Source: https://github.com/Vineflower/vineflower/releases_
_Build Source: https://github.com/Vineflower/vineflower_
* `./org.sf.feeling.decompiler.vineflower/lib/vineflower.jar`
  * Not included in ECD repository, downloaded automatically via Maven
  * SHA-256: 2e563300af223b04772195709539fba69237e61eba14090ee3a44e2992c41cdb
  * version from: https://repo1.maven.org/maven2/org/vineflower/vineflower/1.9.3/vineflower-1.9.3.jar

### Minimal JSON
_Primary Source: https://github.com/ralfstx/minimal-json_

* `./org.sf.feeling.decompiler/lib/json.jar`
  * SHA-256: e1d280900c78f18ae2e00c14e7410a77ba19cf084154b386532846aa6dc81721
  * Version from http://central.maven.org/maven2/com/eclipsesource/minimal-json/minimal-json/0.9.4/minimal-json-0.9.4.jar

### CFR
_Project homepage: https://www.benf.org/other/cfr/_
_Primary Source: https://repo1.maven.org/maven2/org/benf/cfr/_

* `./org.sf.feeling.decompiler.cfr/lib/cfr-0.152.jar`
  * Not included in ECD repository, downloaded automatically via Maven
  * SHA-256: f686e8f3ded377d7bc87d216a90e9e9512df4156e75b06c655a16648ae8765b2
  * Equal to https://repo1.maven.org/maven2/org/benf/cfr/0.152/cfr-0.152.jar
  * Signed with GPG key 0x01066A5BDD7A425BDE0BB3ED101B4F4D25952FC7

### Procyon
_Primary Source: https://repo1.maven.org/maven2/org/bitbucket/mstrobel/_

* `./org.sf.feeling.decompiler.procyon/lib/procyon-core.jar`
  * Not included in ECD repository, downloaded automatically via Maven
  * SHA-256: e52096fde7ce4c84db7a943298ae6cad4ea9e33824fe6ccb99c308a7ad7e594c
  * Version from https://repo1.maven.org/maven2/org/bitbucket/mstrobel/procyon-core/0.6.0/procyon-core-0.6.0.jar

* `./org.sf.feeling.decompiler.procyon/lib/procyon-compilertools.jar`
  * Not included in ECD repository, downloaded automatically via Maven
  * SHA-256: 5b5b40d4bae758a823210c5c1513f2027bd7fe2e9421cd2b004c5d91b0676956
  * Version from https://repo1.maven.org/maven2/org/bitbucket/mstrobel/procyon-compilertools/0.6.0/procyon-compilertools-0.6.0.jar

### Netbeans CVSClient
_Primary Source: https://versioncontrol.netbeans.org/javacvs/library/_
* `./org.sf.feeling.decompiler.source.attach/lib/cvsclient-20060125.jar`

### Maven SCM
_Primary Source: https://maven.apache.org/scm/_
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-api-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-accurev-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-bazaar-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-clearcase-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-cvs-commons-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-cvsexe-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-cvsjava-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-git-commons-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-gitexe-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-hg-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-integrity-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-jazz-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-local-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-perforce-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-starteam-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-svn-commons-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-svnexe-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-synergy-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-tfs-1.9.4.jar`
* `./org.sf.feeling.decompiler.source.attach/lib/maven-scm-provider-vss-1.9.4.jar`

### Nexus

https://repo1.maven.org/maven2/org/sonatype/nexus/plugins/nexus-indexer-lucene-model/2.14.20-02/nexus-indexer-lucene-model-2.14.20-02.jar
* `./org.sf.feeling.decompiler.source.attach/lib/nexus-indexer-lucene-model-2.14.20-02.jar`
  * SHA-256: 74465BC0D1BBECEF4173522CFBAA3D07E4F5EEC6CF16E2E25268182D1350CB7A
  * https://repo1.maven.org/maven2/org/sonatype/nexus/plugins/nexus-indexer-lucene-model/2.14.20-02/nexus-indexer-lucene-model-2.14.20-02.jar
* `./org.sf.feeling.decompiler.source.attach/lib/nexus-restlet1x-model-2.9.2-01.jar`

### Plexus Utils
Primary Source: _https://github.com/codehaus-plexus/plexus-utils_
* `./org.sf.feeling.decompiler.source.attach/lib/plexus-utils-3.0.15.jar`
  * SHA-256: D0BE76C5CE910134A7E07FD2AA48D9B677800AB0E95873F314B445C6F177C973

## JD-Core
_Primary Source: https://github.com/java-decompiler/jd-core/releases_

* `./org.sf.feeling.decompiler.jd/lib/jd-core-1.1.3.jar`
  * SHA-256: 33F60FF75C77DC08905E767694A38F7B6AB792BD2E5831F41E2B56354BB806FB

## JAXB
_Primary Source: https://repo1.maven.org/maven2/_

* `org.sf.feeling.decompiler.source.attach/lib/activation-1.1.1.jar`
  * SHA-256: ae475120e9fcd99b4b00b38329bd61cdc5eb754eee03fe66c01f50e137724f99
  * https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar
* `org.sf.feeling.decompiler.source.attach/lib/jaxb-api-2.3.1.jar`
  * SHA-256: 88b955a0df57880a26a74708bc34f74dcaf8ebf4e78843a28b50eae945732b06
  * https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar
* `org.sf.feeling.decompiler.source.attach/lib/jaxb-impl-2.3.3.jar`
  * SHA-256: e5178d0c7948247f75a13c689bf36f4d5d4910a121f712aa3b20ae94377069d8
  * https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-impl/2.3.3/jaxb-impl-2.3.3.jar
* `org.sf.feeling.decompiler.source.attach/lib/jaxb-runtime-2.3.3.jar`
  * SHA-256: 3fcbf9247b08303ecaef2b8b91b47e220b6eced843e02837a5379d011c2c623d
  * https://repo1.maven.org/maven2/org/glassfish/jaxb/jaxb-runtime/2.3.3/jaxb-runtime-2.3.3.jar

