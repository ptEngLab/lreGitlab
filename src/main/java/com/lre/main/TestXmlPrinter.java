package com.lre.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.lre.actions.helpers.LogHelper;
import com.lre.model.test.testcontent.TestContent;
import com.lre.actions.utils.XmlUtils;
import com.lre.model.test.testcontent.groups.rts.javavm.JavaVM;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;

@Slf4j
public class TestXmlPrinter {
    public static void main(String[] args) throws Exception {

        LogHelper.setup("INFO", true);
        TestContent lreTest = readYamlFile(new File("yamlTest/CreateTestFromYaml.yaml"));
        log.info(XmlUtils.toXml(lreTest));


        JavaVM javaVm = new JavaVM(
                Arrays.asList(
                        "/path/to/lib1",
                        "/path/to/lib2",
                        "/path/to/lib3"
                ),
                true, // userSpecifiedJdk
                "/usr/lib/jvm/java-11-openjdk",
                "-Xmx1024m -Dexample.property=value",
                false, // useXboot
                true   // enableClassLoaderPerVuser
        );

        log.info(XmlUtils.toXml(javaVm));

    }

    public static TestContent readYamlFile(File yamlFile) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(yamlFile, TestContent.class);
    }
}
