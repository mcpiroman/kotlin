/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

object KT43780TestObject {
    const val x = 5
    val y = 6
    const val shared = "shared"
    const val Shared = "Shared"
}

class KT43780TestClassWithCompanion {
    companion object {
        val z = 7
    }
}

object Shared {
    const val x = 8
}

class Companion {
    val t = 10
    companion object {
        const val x = 9
    }
}

enum class KT43780Enum {
    COMPANION;

    companion object {
        const val x = 11
    }
}