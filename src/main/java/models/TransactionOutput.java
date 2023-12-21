package models;

import utils.StringUtil;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class TransactionOutput {

    private final String hash;
    private final PublicKey recipient;
    private final BigDecimal amount;

    public TransactionOutput(PublicKey recipient, BigDecimal amount, String parentTransactionId) {
        this.recipient = recipient;
        this.amount = amount;
        try {
            this.hash = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + amount.toString() + parentTransactionId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isMine(PublicKey publicKey) {
        return (publicKey.equals(recipient));
    }

    public String getHash() {
        return hash;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PublicKey getRecipient() {
        return recipient;
    }
}
