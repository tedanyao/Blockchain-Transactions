package transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.File;
import java.math.BigInteger;

import static org.bitcoinj.script.ScriptOpCodes.*;

/**
 * Created by bbuenz on 24.09.15.
 */
public class MultiSigTransaction extends ScriptTransaction {
    // TODO: Problem 3
    private ECKey bankKey;
    private ECKey cus1Key;
    private ECKey cus2Key;
    private ECKey cus3Key;

    public MultiSigTransaction(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        bankKey = new ECKey();
        cus1Key = new ECKey();
        cus2Key = new ECKey();
        cus3Key = new ECKey();
        getWallet().importKey(bankKey);
        getWallet().importKey(cus1Key);
        getWallet().importKey(cus2Key);
        getWallet().importKey(cus3Key);
    }

    @Override
    public Script createInputScript() {
        // TODO: Create a script that can be spend using signatures from the bank and one of the customers
        ScriptBuilder builder = new ScriptBuilder();
        builder.data(bankKey.getPubKey());
        builder.op(OP_CHECKSIGVERIFY);
        builder.op(OP_1);
        builder.data(cus1Key.getPubKey());
        builder.data(cus2Key.getPubKey());
        builder.data(cus3Key.getPubKey());
        builder.op(OP_3);
        builder.op(OP_CHECKMULTISIG);

        return builder.build();
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedTransaction) {
        // Please be aware of the CHECK_MULTISIG bug!
        // TODO: Create a spending script
        TransactionSignature bankSig = sign(unsignedTransaction, bankKey);
        TransactionSignature cus1Sig = sign(unsignedTransaction, cus1Key);

        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(0);
        builder.data(cus1Sig.encodeToBitcoin());
        builder.data(bankSig.encodeToBitcoin());
        return builder.build();
    }
}
