// WITH_RUNTIME

interface Container<PARAM: Any>
interface ContainerType<PARAM: Any, CONT: Container<PARAM>>
fun <R: Any> doGet(
    ep: ContainerType<*, *>
): String = TODO()
@JvmName("test")
fun <R: Any, PARAM: Any, CONT: Container<PARAM>> doGet(
    ep: ContainerType<PARAM, CONT>
): String = TODO()
fun main() {

}