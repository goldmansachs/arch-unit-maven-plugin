package com.societegenerale.commons.plugin.rules;

import com.societegenerale.commons.plugin.utils.ArchUtils;
import com.tngtech.archunit.base.Optional;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@Slf4j
public class NoPowerMockRuleTest implements ArchRuleTest {

    private static final String POWER_MOCK_RUNNER_CLASS_NAME = "PowerMockRunner";

    @Override
    public void execute(String path) {
        classes().should(notUsePowerMock()).check(ArchUtils.importAllClassesInPackage(path, ArchUtils.TEST_CLASSES_FOLDER));
    }

    public static ArchCondition<JavaClass> notUsePowerMock() {

        return new ArchCondition<JavaClass>(" not use Powermock ") {

            @Override
            @SuppressWarnings("squid:S1166")
            public void check(JavaClass testClass, ConditionEvents events) {

                try {
                    Optional<RunWith> runWithAnnotation = testClass.tryGetAnnotationOfType(RunWith.class);

                    if (runWithAnnotation.isPresent() && runWithAnnotation.get().toString().contains(POWER_MOCK_RUNNER_CLASS_NAME)) {

                        events.add(SimpleConditionEvent.violated(testClass,
                                ArchUtils.POWER_MOCK_VIOLATION_MESSAGE + testClass.getName()));
                    }
                }
                catch(RuntimeException e){
                    // if the runner used is another than Powermock (SpringRunner for instance), a Runtime exception will be thrown
                    // see https://github.com/TNG/ArchUnit/issues/120

                    //therefore, just swallowing the exception, as we are interested only by PowerMockRunner here.
                }

            }

        };
    }
}
