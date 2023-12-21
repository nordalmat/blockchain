package models;

import utils.StringUtil;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block {

    private String hash;
    private final String previousHash;
    private String merkleRoot;
    private final Long timeStamp;
    private final List<Transaction> transactionList = new ArrayList<>();
    private int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactionList);
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined " + getHash());
    }


    public String calculateHash() {
        try {
            return StringUtil.applySha256(
                    getPreviousHash() + getTimeStamp() + getNonce() + getMerkleRoot()
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) return;
        try {
            if (!previousHash.equals("0") && !transaction.processTransaction()) {
                System.out.println("Transaction failed to process. Discarded.");
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        transactionList.add(transaction);
        System.out.println("Transaction was added to the block");
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }
}
