package tests;

import com.lre.actions.lre.testcontentvalidator.groups.LreRtsJavaVmValidator;
import com.lre.model.test.testcontent.groups.Group;
import com.lre.model.test.testcontent.groups.rts.javavm.JavaVM;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LreRtsJavaVmValidatorTest {

    private final LreRtsJavaVmValidator validator = new LreRtsJavaVmValidator();

    @Test
    void testEmptyInputReturnsDefaultJavaVM() {
        Group group = new Group();
        group.setYamlJavaVM("");
        validator.validateJavaVmForGroup(group);
        assertNotNull(group.getRts().getJavaVM());
    }

    @Test
    void testValidSingleKeyValue() {
        Group group = new Group();
        group.setYamlJavaVM("UserSpecifiedJdk=true,JdkHome=/usr/lib/jvm/java-17");
        validator.validateJavaVmForGroup(group);

        JavaVM javaVm = group.getRts().getJavaVM();
        assertTrue(javaVm.getUserSpecifiedJdk());
        assertEquals("/usr/lib/jvm/java-17", javaVm.getJdkHome());
    }

    @Test
    void testValidClasspathParsing() {
        Group group = new Group();
        group.setYamlJavaVM("JavaEnvClassPaths=/path1.jar;/path2.jar");
        validator.validateJavaVmForGroup(group);

        JavaVM javaVm = group.getRts().getJavaVM();
        List<String> paths = javaVm.getJavaEnvClassPaths();
        assertEquals(2, paths.size());
        assertEquals("/path1.jar", paths.get(0));
        assertEquals("/path2.jar", paths.get(1));
    }

    @Test
    void testInvalidBooleanThrowsException() {
        Group group = new Group();
        group.setYamlJavaVM("UseXboot=maybe");
        assertThrows(LreRtsJavaVmValidator.JavaVmException.class,
                () -> validator.validateJavaVmForGroup(group));
    }

    @Test
    void testMissingJdkHomeThrowsException() {
        Group group = new Group();
        group.setYamlJavaVM("UserSpecifiedJdk=true");
        assertThrows(LreRtsJavaVmValidator.JavaVmException.class,
                () -> validator.validateJavaVmForGroup(group));
    }

    @Test
    void testJavaVmParametersParsing() {
        Group group = new Group();
        group.setYamlJavaVM("JavaVmParameters=-Xmx512m -Ddebug=true");
        validator.validateJavaVmForGroup(group);

        JavaVM javaVm = group.getRts().getJavaVM();
        assertEquals("-Xmx512m -Ddebug=true", javaVm.getJavaVmParameters());
    }
}
