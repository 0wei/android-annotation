package com.xx.ksp_annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KspLog(val value: Boolean)