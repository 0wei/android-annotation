package com.xx.ksp_compiler

import com.google.devtools.ksp.processing.Dependencies
import com.xx.ksp_annotation.RunIf

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.IOException
import javax.tools.Diagnostic

internal class RunIfSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("+++++++++RunIfSymbolProcessorProvider+++++++++++++++++")
//        environment.logger.error("+++++++++RunIfSymbolProcessorProvider+++++++++++++++++")
        return RunIfSymbolProcessor(environment)
    }
}


internal class RunIfSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(RunIf::class.java.name)
        val ret = mutableListOf<KSAnnotated>()
        environment.logger.info("+++++++++process+++++++++++++++++")
        symbols.toList().forEach {
            environment.logger.warn("+++++++++process:$it")
            environment.logger.warn("+++++++++process:${it.validate()}")
            if (!it.validate())
                ret.add(it)
            else
                it.accept(TestKspVisitor(environment), Unit)//处理符号
        }
        //返回无法处理的符号
        return ret
    }
}

internal class TestKspVisitor(private val environment: SymbolProcessorEnvironment) :
    KSVisitorVoid() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val packageName = function.containingFile!!.packageName.asString()
        val className = function.parentDeclaration!!.simpleName.asString() // 获取方法所在的类名
        val methodName = function.simpleName.asString() // 获取方法名
        // 使用 KotlinPoet 生成代码
        val fileSpec = FileSpec.builder(packageName, "${className}Generated")
            .addFunction(
                FunSpec.builder("print${methodName.capitalize()}Info")
                    .addStatement("println(%S)", "Generated code for method: $methodName in class: $className")
                    .build()
            )
            .build()

        // 将生成的代码写入文件
        fileSpec.writeTo(environment.codeGenerator, Dependencies(false, function.containingFile!!))
    }
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
//        val packageName = classDeclaration.containingFile!!.packageName.asString()//获取这个类的包名
//        val originalClassName = classDeclaration.simpleName.asString()//获取类名
//        val className = "My${originalClassName}"
//        val file = environment.codeGenerator.createNewFile(//创建新的文件(默认.kt)
//            Dependencies(
//                true,
//                classDeclaration.containingFile!!
//            ), packageName, className
//        )
//        file.write("package $packageName\n\n".toByteArray())//写入文件
//        file.write("class $className {\n".toByteArray())
//        file.write("    val fields = ".toByteArray())
//        val fields = classDeclaration.getAllProperties().map {//遍历所有的属性
//            val name = it.simpleName.getShortName()//属性名
//            val type = it.type.resolve().toString()//属性类型
//            "$name: $type"
//        }.joinToString()
//        file.write("\"$fields\"\n".toByteArray())
//        file.write("}".toByteArray())
//        file.close()
    }
}
