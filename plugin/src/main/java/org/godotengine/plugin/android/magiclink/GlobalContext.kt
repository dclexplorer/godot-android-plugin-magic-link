import link.magic.android.Magic
import org.web3j.protocol.Web3j
import org.web3j.protocol.geth.Geth

object GlobalContext {
    lateinit var magic: Magic
    lateinit var web3j: Web3j
    lateinit var gethWeb3j: Geth

    var address: String? = null

    lateinit var callbackUrl: String
}