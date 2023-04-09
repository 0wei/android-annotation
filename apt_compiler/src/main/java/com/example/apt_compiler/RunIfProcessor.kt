package com.example.apt_compiler

import com.example.apt_annotation.RunIf
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.sun.source.tree.MethodTree
import com.sun.source.tree.TreeVisitor
import com.sun.source.util.Trees
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedAnnotationTypes("com.example.apt_annotation.RunIf")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RunIfProcessor : AbstractProcessor() {

    private lateinit var treeMaker: TreeMaker
    private lateinit var trees: Trees
    private lateinit var filer: Filer
    private lateinit var messager: Messager
    fun note(msg: String) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg)
    }

    private val visitor = object : TreeTranslator() {
        override fun visitMethodDef(jcMethodDecl: JCTree.JCMethodDecl) {
            super.visitMethodDef(jcMethodDecl)
            note("visit: ${jcMethodDecl.name}")

        }
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        filer = processingEnv.filer
        messager = processingEnv.messager
        trees = Trees.instance(processingEnv)
        treeMaker = TreeMaker.instance((processingEnv as JavacProcessingEnvironment).context)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(RunIf::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment
    ): Boolean {
        for (element in roundEnv.getElementsAnnotatedWith(RunIf::class.java)) {
            if (element.kind != ElementKind.METHOD) {
                continue
            }
            val methodElement = element as ExecutableElement
            val methodName = methodElement.simpleName.toString()
            val className = methodElement.enclosingElement.simpleName.toString()
            val packageName =
                processingEnv.elementUtils.getPackageOf(methodElement).qualifiedName.toString()
            val shouldRun = methodElement.getAnnotation(RunIf::class.java).value

            val methodTree = trees.getTree(element)
            methodTree.accept()

            if (!shouldRun) {
                continue
            }

//            val code = """
//                Log.i("RunIf", "Method $className.$methodName() is running.")
//            """.trimIndent()
            val code = ""
            val methodSpec =
                FunSpec.builder("run$methodName").addModifiers(KModifier.PUBLIC)
                    .returns(Unit::class).addStatement(code).build()
            val typeSpec = TypeSpec.classBuilder("RunIf_$methodName")
                .addModifiers(KModifier.PUBLIC, KModifier.FINAL).addFunction(methodSpec).build()
            val javaFile =
                FileSpec.builder(packageName, "RunIf_$methodName").addType(typeSpec).build()
            try {
                messager.printMessage(Diagnostic.Kind.NOTE, "absss write $methodName")
                javaFile.writeTo(filer)
            } catch (e: IOException) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write file: ${e.message}")
            }
        }
        return true
    }
}
