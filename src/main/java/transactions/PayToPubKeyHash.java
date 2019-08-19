package transactions;

import org.bitcoinj.core.*;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.File;

import static org.bitcoinj.script.ScriptOpCodes.*;
import static org.bitcoinj.script.ScriptOpCodes.OP_VERIFY;

/**
 * Created by bbuenz on 24.09.15.
 */
public class PayToPubKeyHash extends ScriptTransaction {
    // TODO: Problem 1
    private DeterministicKey key;
    //private ECKey key;

    public PayToPubKeyHash(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        key = getWallet().freshReceiveKey();
        //String privateKey = "5JbaWCbJ8L9L1obtAw5NCQGgRcZmekD5D4cY9giMQg6TMbLyAzp";
        //DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(parameters,privateKey);
        //key = dumpedPrivateKey.getKey();

    }

    @Override
    public Script createInputScript() {
        // TODO: Create a P2PKH script
        // TODO: be sure to test this script on the mainnet using a vanity address
        ScriptBuilder builder = new ScriptBuilder();
        builder.op(OP_DUP);
        builder.op(OP_HASH160);
        builder.data(key.getPubKeyHash());
        builder.op(OP_EQUALVERIFY);
        builder.op(OP_CHECKSIG);
        return builder.build();
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedTransaction) {
        // TODO: Redeem the P2PKH transaction
        TransactionSignature txSig = sign(unsignedTransaction, key);

        ScriptBuilder builder = new ScriptBuilder();
        builder.data(txSig.encodeToBitcoin());
        builder.data(key.getPubKey());
        return builder.build();
    }
}
