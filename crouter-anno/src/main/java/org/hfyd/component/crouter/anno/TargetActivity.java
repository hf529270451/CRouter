package org.hfyd.component.crouter.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.METHOD})
public @interface TargetActivity {

    String value();
}
