import models.*;

import java.math.BigDecimal;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static Blockchain blockchain = Blockchain.getInstance();
    public static int DIFFICULTY = 3;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {

//        blockchain.add(new Block("I am the first block", "0"));
//        System.out.println("Trying to mine block 1... " + blockchain.get(0).getHash());
//        blockchain.get(0).mineBlock(DIFFICULTY);
//
//        blockchain.add(new Block("Second block", blockchain.get(blockchain.size() - 1).getHash()));
//        System.out.println("Trying to mine block 2... " + blockchain.get(1).getHash());
//        blockchain.get(1).mineBlock(DIFFICULTY);
//
//        blockchain.add(new Block("Third block", blockchain.get(blockchain.size() - 1).getHash()));
//        System.out.println("Trying to mine block 3... " + blockchain.get(2).getHash());
//        blockchain.get(2).mineBlock(DIFFICULTY);
//
//        System.out.println("Blockchain is Valid: " + isChainValid());
//
//        String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
//        System.out.println("\nThe block chain: ");
//        System.out.println(blockchainJson);

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Wallet walletA = new Wallet();
        Wallet walletB = new Wallet();
        Wallet walletC = new Wallet();

        genesisTransaction = new Transaction(walletC.getPublicKey(), walletA.getPublicKey(), BigDecimal.valueOf(100), null);
        genesisTransaction.generateSignature(walletC.getPrivateKey());
        genesisTransaction.setTransactionHash("0");
        genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getRecipient(), genesisTransaction.getAmount(), genesisTransaction.getTransactionHash())); //manually add the Transactions Output
        Blockchain.unspentTransactionOutputs.put(genesisTransaction.getOutputs().get(0).getHash(), genesisTransaction.getOutputs().get(0));

        System.out.println("Creating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        Block block1 = new Block(genesis.getHash());
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), BigDecimal.valueOf(40)));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), BigDecimal.valueOf(1000)));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.getPublicKey(), BigDecimal.valueOf(20)));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        System.out.println("Is Chain valid? " + isChainValid());
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(DIFFICULTY);
        blockchain.add(newBlock);
    }

    public static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[DIFFICULTY]).replace('\0', '0');
        Map<String,TransactionOutput> tempUTXOs = new HashMap<>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getHash(), genesisTransaction.getOutputs().get(0));

        for(int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            if(!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash()) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            if(!currentBlock.getHash().substring( 0, DIFFICULTY).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            TransactionOutput tempOutput;
            for(int t = 0; t < currentBlock.getTransactionList().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactionList().get(t);

                if(currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(!currentTransaction.getInputAmount().equals(currentTransaction.getOutputAmount())) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }
                for(TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());
                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }
                    if(!input.getUnsentTransactionOutput().getAmount().equals(tempOutput.getAmount())) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }
                    tempUTXOs.remove(input.getTransactionOutputId());
                }
                for(TransactionOutput output: currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getHash(), output);
                }
                if( currentTransaction.getOutputs().get(0).getRecipient() != currentTransaction.getRecipient()) {
                    System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
                    return false;
                }
                if( currentTransaction.getOutputs().get(1).getRecipient() != currentTransaction.getSender()) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }
        }
        return true;
    }
}
