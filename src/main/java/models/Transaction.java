package models;

import utils.StringUtil;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transaction {
    private String transactionHash;
    private final PublicKey sender;
    private final PublicKey recipient;
    private final BigDecimal amount;
    private byte[] signature;

    private final List<TransactionInput> inputs;
    private final List<TransactionOutput> outputs = new ArrayList<>();

    private static long count = 0L;
    private static final BigDecimal minimumTransaction = BigDecimal.valueOf(0.1);

    public Transaction(PublicKey sender, PublicKey recipient, BigDecimal amount, List<TransactionInput> inputs) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.inputs = inputs;
    }

    public boolean processTransaction() throws NoSuchAlgorithmException {
        if (verifySignature()) {
            System.out.println("Transaction Signature failed to verify");
            return false;
        }
        for (TransactionInput input : inputs) {
            input.setUnsentTransactionOutput(Blockchain.unspentTransactionOutputs.get(input.getTransactionOutputId()));
        }
        if (getInputAmount().compareTo(minimumTransaction) < 0) {
            System.out.println("#Transaction inputs to small: " + getInputAmount());
            return false;
        }

        BigDecimal leftOver = getInputAmount().subtract(amount);
        transactionHash = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, amount, transactionHash));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionHash));

        for (TransactionOutput output : outputs) {
            Blockchain.unspentTransactionOutputs.put(output.getHash(), output);
        }

        for (TransactionInput input : inputs) {
            if (input.getUnsentTransactionOutput() == null)
                continue;
            Blockchain.unspentTransactionOutputs.remove(input.getUnsentTransactionOutput().getHash());
        }
        return true;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getInputAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (TransactionInput input : inputs) {
            if (input.getUnsentTransactionOutput() == null)
                continue;
            total = total.add(input.getUnsentTransactionOutput().getAmount());
        }
        return total;
    }

    public BigDecimal getOutputAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (TransactionOutput output : outputs) {
            total = total.add(output.getAmount());
        }
        return total;
    }

    public String calculateHash() {
        count++;
        try {
            return StringUtil.applySha256(
                    StringUtil.getStringFromKey(sender) +
                            StringUtil.getStringFromKey(recipient) +
                            amount.toString() + Arrays.toString(signature)
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + amount.toString();
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + amount.toString();
        return !StringUtil.verifyECDSASig(sender, data, signature);
    }
}
