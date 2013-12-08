package fr.labri.shelly.impl;

import java.util.Set;

import javax.annotation.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementKindVisitor7;


@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value= {"fr.labri.shelly.annotations.*"})
public class Doctor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element elt : roundEnv.getRootElements()) {
			elt.accept(new NameVisitor(), null);
		}
		return true;
	}
	class NameVisitor  extends ElementKindVisitor7<Void, Void> {
		
		@Override
		protected Void defaultAction(Element e, Void p) {
			return super.defaultAction(e, p);
		}

		@Override
		public Void visitTypeAsClass(TypeElement e, Void p) {
			System.out.println(e);
			return super.visitTypeAsClass(e, p);
		}

		@Override
		public Void visitVariableAsField(VariableElement e, Void p) {
			System.out.println(e);
			return super.visitVariableAsField(e, p);
		}

		@Override
		public Void visitExecutableAsMethod(ExecutableElement e, Void p) {
			System.out.println(e);
			return super.visitExecutableAsMethod(e, p);
		}
		
	}
}
