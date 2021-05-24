import Kt

private func testObject() throws {
    let object = KT43780TestObject.shared
    try assertEquals(actual: object.x, expected: 5)
    try assertEquals(actual: object.y, expected: 6)
    try assertEquals(actual: object.shared, expected: "shared")
    try assertTrue(object === KT43780TestObject())
}

private func testCompanionObject() throws {
    let object = KT43780TestClassWithCompanion.companion
    try assertEquals(actual: object.z, expected: 7)
    try assertTrue(object === KT43780TestClassWithCompanion.Companion())
    try assertTrue(object === KT43780TestClassWithCompanion.Companion.shared)
}


class Kt43780Tests : SimpleTestProvider {
    override init() {
        super.init()

        test("testObject", testObject)
        test("testCompanionObject", testCompanionObject)
    }
}
