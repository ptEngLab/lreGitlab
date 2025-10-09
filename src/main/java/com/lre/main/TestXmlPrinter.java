package com.lre.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lre.actions.utils.LogHelper;
import com.lre.actions.utils.XmlUtils;
import com.lre.model.test.testcontent.TestContent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class TestXmlPrinter {
    public static void main(String[] args) throws Exception {

        LogHelper.setup("INFO", true);
        TestContent lreTest = readYamlFile(new File("yamlTest/CreateTestFromYaml.yaml"));
        log.info(XmlUtils.toXml(lreTest));




//        log.info(XmlUtils.toXml(javaVm));

    }

    public static TestContent readYamlFile(File yamlFile) throws Exception {
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        return yamlMapper.readValue(yamlFile, TestContent.class);
    }
}
