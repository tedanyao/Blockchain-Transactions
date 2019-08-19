package 

import transactions.*;
import org.bitcoinj.core.*;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.io.File;
import java.math.BigInteger;
/**
 * Created by bbuenz on 23.09.15.
 */
public class ScriptTests {
    // TODO: Change this to true to use mainnet.
    private boolean useMainNet = true;
    // TODO: Change this to the address of the testnet faucet you use.
    //private static final String faucetAddress = "n2eMqTT929pb1RDNuqEnxdaLau1rxy3efi";
    private static final String faucetAddress = "muUdKQKWSt7AFB5Wgnf875psoieoRonk3B";

    private String wallet_name;
    private NetworkParameters networkParameters;
    private WalletAppKit kit;

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptTests.class);

    public ScriptTests() {
        if (useMainNet) {
            networkParameters = new MainNetParams();
            wallet_name = "main-wallet";
            LOGGER.info("Running on mainnet.");
        } else {
            networkParameters = new TestNet3Params();
            wallet_name = "test-wallet2";
            LOGGER.info("Running on testnet.");
        }
        kit = new WalletAppKit(networkParameters, new File(wallet_name), "password");

    }

    public void downloadBlockchain() {
        LOGGER.info("Starting to sync blockchain. This might take a few minutes");
        kit.setAutoSave(true);
        LOGGER.info("Step 2");
        kit.startAsync();
        LOGGER.info("Step 3");
        kit.awaitRunning();
        LOGGER.info("Step 4");
        kit.wallet().allowSpendingUnconfirmedTransactions();
        LOGGER.info("Step 5");
        LOGGER.info("Synced blockchain.");
        LOGGER.info("You've got " + kit.wallet().getBalance() + " in your pocket");
    }
    //@Test
    public void printMyAddress() {
        String privateKey = "cMiPT4M8vmdYUHmX5iEQHJ9BUyS7Nee1bTTwa29Je2QiXdKxXpWm";
        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(networkParameters,privateKey);
        ECKey k = dumpedPrivateKey.getKey();
        //ECKey k = new ECKey();
        System.out.println("We created key:\n" + k);
        Address addressFromKey = k.toAddress(networkParameters); // address: 1xinx...
        System.out.println("On the  network, we can use this address:\n" + addressFromKey);
        System.out.println(k.getPublicKeyAsHex()); // publicKey 130 char
        System.out.println(k.getPrivateKeyAsHex()); // Private Key Hexadecimal Format, 64
        System.out.println(k.getPrivateKeyAsWiF(networkParameters)); // real privateKey: 5J...

    }
      @Test
      public void importMyPrivateKey() {
        downloadBlockchain();
        String privateKey = "5JbaWCbJ8L9L1obtAw5NCQGgRcZmekD5D4cY9giMQg6TMbLyAzp"; //"cTcEAoZvBSV2naQuFtZJy7aGHvKaiy2ysvktMfQQgnjA97fG5dTw";
        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(networkParameters,privateKey);
        ECKey k = dumpedPrivateKey.getKey();

        kit.wallet().importKey(k);

        kit.stopAsync();
        kit.awaitTerminated();

      }

    @ Test
    public void printMyPrivateKeys() {
        downloadBlockchain();
        LOGGER.info("Your currentReceive address is {}", kit.wallet().currentReceiveAddress());
        LOGGER.info("Your currentReceive address is {}", kit.wallet().getBalance());

        List<ECKey> keys = kit.wallet().getImportedKeys();
        for (ECKey key: keys) {
            System.out.println(key.toAddress(networkParameters) + ": " + key.getPrivateKeyAsWiF(networkParameters));
        }
        kit.stopAsync();
        kit.awaitTerminated();
    }
    @Test
    public void genNewKey() {
        //ECKey k = new ECKey();
        //System.out.println("PrivateKey: " + k.getPrivateKeyAsWiF(networkParameters));
        //System.out.println("Address: " + k.toAddress(networkParameters));
        importMyPrivateKey();
    }

    @Test
    public void printAddress() {
        downloadBlockchain();
        LOGGER.info("Your currentReceive address is {}", kit.wallet().currentReceiveAddress());
        LOGGER.info("Your freshReceive address is {}", kit.wallet().freshReceiveAddress());
        kit.stopAsync();
        kit.awaitTerminated();
    }

    private void testTransaction(ScriptTransaction scriptTransaction) throws InsufficientMoneyException {
        final Script inputScript = scriptTransaction.createInputScript();
        //Transaction transaction = scriptTransaction.createOutgoingTransaction(inputScript, Coin.valueOf(10000)); // give xxx to vanity
        Transaction transaction = scriptTransaction.createOutgoingTransaction(inputScript, Coin.CENT); // give xxx to vanity
        TransactionOutput relevantOutput = transaction.getOutputs().stream().filter(to -> to.getScriptPubKey().equals(inputScript)).findAny().get();
        Transaction redemptionTransaction = scriptTransaction.createUnsignedRedemptionTransaction(relevantOutput, scriptTransaction.getReceiveAddress());
        Script redeemScript = scriptTransaction.createRedemptionScript(redemptionTransaction);
        scriptTransaction.testScript(inputScript, redeemScript, redemptionTransaction);
        redemptionTransaction.getInput(0).setScriptSig(redeemScript);
        scriptTransaction.sendTransaction(transaction);
        scriptTransaction.sendTransaction(redemptionTransaction);
    }

    // TODO: Uncomment this once you have coins on mainnet or testnet to check that transactions are working as expected.
    //@Test
    public void testPayToPubKey() throws InsufficientMoneyException {
        try (ScriptTransaction payToPubKey = new PayToPubKey(networkParameters, new File(wallet_name), "password")) {
            testTransaction(payToPubKey);

       } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    // TODO: Uncomment this when you are ready to test PayToPubKeyHash.
    @Test
    public void testPayToPubKeyHash() throws InsufficientMoneyException {
        try (ScriptTransaction payToPubKeyHash = new PayToPubKeyHash(networkParameters, new File(wallet_name), "password")) {
            testTransaction(payToPubKeyHash);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    // TODO: Uncomment this when you are ready to test LinearEquationTransaction.
    @Test
    public void testLinearEquation() throws InsufficientMoneyException {
        try (LinearEquationTransaction linEq = new LinearEquationTransaction(networkParameters, new File(wallet_name), "password")) {
            testTransaction(linEq);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

     // TODO: Uncomment this when you are ready to test MultiSigTransaction.
     @Test
    public void testMultiSig() throws InsufficientMoneyException {
        try (ScriptTransaction multiSig = new MultiSigTransaction(networkParameters, new File(wallet_name), "password")) {
            testTransaction(multiSig);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

     // TODO: Uncomment this when you are ready to send money back to Faucet on testnet.
//    @Test
//    public void sendMoneyBackToFaucet() throws AddressFormatException, InsufficientMoneyException {
//        if (useMainNet) {
//            return;
//        }
//        downloadBlockchain();
//        Transaction transaction = kit.wallet().createSend(new Address(networkParameters, faucetAddress), Coin.CENT);
//        kit.wallet().commitTx(transaction);
//        kit.peerGroup().broadcastTransaction(transaction);
//        kit.stopAsync();
//        kit.awaitTerminated();
//    }
}
