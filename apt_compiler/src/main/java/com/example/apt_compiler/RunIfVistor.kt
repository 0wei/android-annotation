package com.example.apt_compiler

import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeTranslator

class RunIfVistor: TreeTranslator() {
    override fun visitMethodDef(tree: JCTree.JCMethodDecl?) {
        super.visitMethodDef(tree)

    }
}