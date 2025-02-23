package com.xx.ksp_compiler

import com.google.devtools.ksp.processing.Dependencies

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
import com.squareup.kotlinpoet.ksp.writeTo
import com.xx.ksp_annotation.KspLog

internal class KspLogSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("+++++++++KspLogSymbolProcessorProvider+++++++++++++++++")
//        environment.logger.error("+++++++++KspLogSymbolProcessorProvider+++++++++++++++++")
        return KspLogSymbolProcessor(environment)
    }
}


internal class KspLogSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(KspLog::class.java.name)
        val ret = mutableListOf<KSAnnotated>()
        environment.logger.info("+++++++++process+++++++++++++++++")
        val functionsByClass =
            mutableMapOf<KSClassDeclaration, MutableList<KSFunctionDeclaration>>()
        symbols.toList().forEach {
            environment.logger.warn("+++++++++process:$it")
            environment.logger.warn("+++++++++process:${it.validate()}")
            if (!it.validate())
                ret.add(it)
            else {
                if (it is KSFunctionDeclaration) {
                    // 获取函数所在的类
                    val parentClass = it.parentDeclaration as? KSClassDeclaration
                    if (parentClass != null) {
                        // 将函数按类分组
                        functionsByClass.getOrPut(parentClass) { mutableListOf() }.add(it)
                    }
                }
                // it.accept(TestKspVisitor(environment), Unit) //处理符号
            }
        }
        // 为每个类生成代码
        functionsByClass.forEach { (classDeclaration, functions) ->
            generateCodeForFunctions(classDeclaration, functions)
        }        //返回无法处理的符号
        return ret
    }

    private fun generateCodeForFunctions(
        classDeclaration: KSClassDeclaration,
        functions: List<KSFunctionDeclaration>
    ) {
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.simpleName.asString()
        val generatedClassName = "${className}Generated"

        // 创建一个 FileSpec 用于生成代码
        val fileSpec = FileSpec.builder(packageName, generatedClassName)
            .addFileComment("Generated code for class: $className")
            .apply {
                // 为每个带有注解的函数生成代码
                functions.forEach { function ->
                    val functionName = function.simpleName.asString()
                    val annotationNames = function.annotations
                        .map { it.annotationType.resolve().declaration.simpleName.asString() }
                        .joinToString(", ")

                    addFunction(
                        FunSpec.builder("process${functionName.capitalize()}")
                            .addModifiers(KModifier.PUBLIC)
                            .returns(Unit::class)
                            .addStatement(
                                "println(\"Processing function: $functionName with annotations: $annotationNames\")"
                            )
                            .build()
                    )
                }
            }
            .build()

        // 写入文件
        fileSpec.writeTo(
            codeGenerator = environment.codeGenerator,
            dependencies = Dependencies(false, classDeclaration.containingFile!!)
        )
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
                    .addStatement(
                        "println(%S)",
                        "Generated code for method: $methodName in class: $className"
                    )
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
