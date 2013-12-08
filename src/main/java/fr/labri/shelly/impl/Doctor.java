package fr.labri.shelly.impl;

import java.util.Set;

import javax.annotation.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value= {"fr.labri.shelly.annotations.*"})
public class Doctor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element elt : roundEnv.getRootElements()) {
			System.out.println(elt);
			// FIXME need to build a full model with this
		}
		System.out.println(roundEnv);
		return true;
	}
}
