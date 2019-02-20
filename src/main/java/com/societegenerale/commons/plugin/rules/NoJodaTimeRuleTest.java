package com.societegenerale.commons.plugin.rules;

import com.societegenerale.commons.plugin.utils.ArchUtils;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static java.util.stream.Collectors.toList;

public class NoJodaTimeRuleTest implements ArchRuleTest {

  private static final String JODATIME_PACKAGE_PREFIX = "org.joda";

  @Override
  public void execute(String path) {
    classes().should(notUseJodaTime()).check(ArchUtils.importAllClassesInPackage(path, ArchUtils.SRC_CLASSES_FOLDER));
  }

  protected static ArchCondition<JavaClass> notUseJodaTime() {

    return new ArchCondition<JavaClass>("not use Joda time ") {
      @Override
      public void check(JavaClass item, ConditionEvents events) {

        List<JavaField> classesWithJodaTimeFields = item.getAllFields().stream()
                .filter( field -> isJodaTimeField(field))
                .collect(toList());

        for(JavaField field : classesWithJodaTimeFields){
          events.add(SimpleConditionEvent.violated(field, ArchUtils.NO_JODA_VIOLATION_MESSAGE
                  +" - class: "+field.getOwner().getName()
                  +" - field name: "+field.getName()));
        }

        List<JavaMethodCall> methodsUsingJodaTimeInternally = item.getCodeUnits().stream()
                .filter(codeUnit -> codeUnit instanceof JavaMethod)
                .flatMap(method -> method.getMethodCallsFromSelf().stream())
                .filter(method -> method instanceof JavaMethodCall)
                .filter(method -> isMethodUsingJodaTimeInternally(method))
                .collect(toList());

        for(JavaMethodCall methodCall : methodsUsingJodaTimeInternally){
          events.add(SimpleConditionEvent.violated(methodCall.getOriginOwner(), ArchUtils.NO_JODA_VIOLATION_MESSAGE
                  +" - class: "+methodCall.getOriginOwner().getName()+ " - line: "+methodCall.getLineNumber()));
        }
      }

      private boolean isJodaTimeField(JavaField field) {
        return field.getType().getName().startsWith(JODATIME_PACKAGE_PREFIX);
      }

      private boolean isMethodUsingJodaTimeInternally(JavaMethodCall javaMethodCall) {
        return javaMethodCall.getTarget().getFullName().startsWith(JODATIME_PACKAGE_PREFIX);
      }

    };
  }

}
