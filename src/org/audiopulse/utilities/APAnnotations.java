package org.audiopulse.utilities;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
public @interface APAnnotations {
	
	public enum Priority { LOW, MEDIUM, HIGH }
	
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD,
 ElementType.CONSTRUCTOR,ElementType.ANNOTATION_TYPE,
 ElementType.PACKAGE,ElementType.FIELD,ElementType.LOCAL_VARIABLE})
@Inherited
public @interface Unfinished {
        String value() default "This section is Under Construction.";
        String[] changedBy() default "";
        String[] lastChangedBy() default "";
        Priority priority() default Priority.MEDIUM;
        String createdBy() default "";
        String lastChanged() default "";
}


public @interface UnderConstruction {
    String owner() default "";
    String value() default "Class is Under Construction.";
    Priority priority() default Priority.LOW;
    String createdBy() default "";
    String lastChanged() default "";
}



}
