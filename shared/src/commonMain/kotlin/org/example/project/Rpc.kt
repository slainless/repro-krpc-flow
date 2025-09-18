import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc


@Rpc
interface MyRpc {
    fun myFunction(data: Flow<ByteArray>): Flow<String>
}